package me.limeglass.epicmines.objects;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;
import com.google.common.collect.Streams;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.flags.DelayFlag;
import me.limeglass.epicmines.flags.MineFlag;
import me.limeglass.epicmines.flags.MineFlag.FlagInfo;
import me.limeglass.epicmines.manager.managers.MineManager;
import me.limeglass.epicmines.utils.CuboidRegion;
import me.limeglass.epicmines.utils.MessageBuilder;

public class Mine {

	private final Set<MineFlag> flags = Sets.newHashSet(new DelayFlag());
	private final MineStatistics statistics = new MineStatistics();
	private final ResetInfo resetInfo = new ResetInfo(this);
	private final Set<String> childern = new HashSet<>();
	private final Set<MineSign> signs = new HashSet<>();
	private final Set<Chunk> chunks = new HashSet<>();
	private long update = System.currentTimeMillis();
	private final Location pos1, pos2;
	private final CuboidRegion region;
	private final String name;
	private final World world;
	private Location teleport;

	public Mine(String name, Location pos1, Location pos2, Location teleport) {
		this.region = new CuboidRegion(pos1, pos2);
		this.teleport = teleport;
		this.name = name;
		this.pos1 = pos1;
		this.pos2 = pos2;
		chunks.addAll(region.getChunks());
		world = pos1.getWorld();
	}

	public void reset() {
		update();
		resetInfo.reset();
	}

	public void addChild(Mine child) {
		childern.add(child.getName());
	}

	public void removeChild(Mine child) {
		childern.removeIf(string -> string.equalsIgnoreCase(child.getName()));
	}

	public Set<Mine> getChildern() {
		return EpicMines.getInstance().getManager(MineManager.class).getMines(childern);
	}

	public Set<Mine> getParents() {
		return EpicMines.getInstance().getManager(MineManager.class).getMines().parallelStream()
				.filter(mine -> !mine.getName().equalsIgnoreCase(name))
				.filter(mine -> mine.getChildern().stream()
						.anyMatch(child -> child.getName().equalsIgnoreCase(name)))
				.collect(Collectors.toSet());
	}

	public World getWorld() {
		return world;
	}

	public MineStatistics getStatistics() {
		return statistics;
	}

	public void unload() {
		EpicMines.getInstance().getManager(MineManager.class).getMines().remove(this);
	}

	public void update() {
		update = System.currentTimeMillis();
	}

	public CuboidRegion getCuboidRegion() {
		return region.clone();
	}

	public Iterator<Block> iterator() {
		return getCuboidRegion().iterator();
	}

	public ResetInfo getResetInfo() {
		return resetInfo;
	}

	public void addSign(MineSign sign) {
		signs.add(sign);
	}

	public Set<MineSign> getSigns() {
		return signs;
	}

	public Set<? extends MineFlag> getFlags() {
		return flags;
	}

	public DelayFlag getDelayFlag() {
		return flags.stream()
				.filter(flag -> flag instanceof DelayFlag)
				.map(flag -> (DelayFlag) flag)
				.findFirst()
				.get();
	}

	public void setTeleport(Location teleport) {
		this.teleport = teleport;
	}

	public Location getTeleport() {
		return teleport;
	}

	public long getTimeLeft() {
		return getDelayFlag().getSeconds() - ((System.currentTimeMillis() - update) / 1000);
	}

	public boolean hasFlag(FlagInfo<?> info) {
		return getFlag(info).isPresent();
	}

	public Optional<? extends MineFlag> getFlag(String name) {
		return flags.stream()
				.filter(flag -> flag.getName().equalsIgnoreCase(name))
				.findFirst();
	}

	@SuppressWarnings("unchecked")
	public <F extends MineFlag> Optional<F> getFlag(FlagInfo<F> info) {
		return flags.stream()
				.filter(flag -> flag.getName().equalsIgnoreCase(name))
				.filter(flag -> flag.getClass().equals(info.getClass()))
				.map(flag -> (F) flag)
				.findFirst();
	}

	public <F extends MineFlag> void addFlag(Player player, FlagInfo<F> info, String... arguments) {
		flags.removeIf(flag -> flag.getName().equalsIgnoreCase(info.getName()));
		Bukkit.getScheduler().runTaskAsynchronously(EpicMines.getInstance(), () -> {
			try {
				F flag = info.getFlagClass().newInstance();
				if (!flag.onAttach(player, this, arguments))
					return;
				flags.add(flag);
			} catch (InstantiationException | IllegalAccessException e) {
				//Already warned before on initialize if extended Flag is invalid.
			}
		});
	}

	public void editFlag(Player player, MineFlag flag, String... arguments) {
		flag.onAttach(player, this, arguments);
	}

	public void removeFlag(Player player, FlagInfo<?> info) {
		if (info.getClass().equals(DelayFlag.class)) {
			return;
		}
		flags.removeIf(flag -> flag.getClass().equals(info.getClass()));
		new MessageBuilder("flag.remove")
				.replace("%flag%", info.getName())
				.setPlaceholderObject(this)
				.send(player);
	}

	public String getName() {
		return name;
	}

	public Location getPosition1() {
		return pos1;
	}

	public Location getPosition2() {
		return pos2;
	}

	/**
	 * @return The last known long millisecond time of reset.
	 */
	public long getLastUpdate() {
		return update;
	}

	public Location getClosestCorner(Location location) {
		double distance = location.distance(pos1);
		if (distance < location.distance(pos2))
			return pos1;
		return pos2;
	}

	public boolean isEmpty() {
		return Streams.stream(getCuboidRegion().iterator()).parallel().allMatch(block -> block.getType() == Material.AIR);
	}

	public Set<Player> getPlayersAround() {
		Set<Player> players = Sets.newHashSet(getPlayersWithin());
		for (Player player : Bukkit.getOnlinePlayers()) {
			Location location = player.getLocation();
			int radius = 15;
			if (location.distance(pos1) <= radius)
				players.add(player);
			else if (location.distance(pos2) <= radius)
				players.add(player);
		}
		return players;
	}

	public Set<Player> getPlayersWithin() {
		return Bukkit.getOnlinePlayers().stream()
				.filter(player -> isWithin(player.getLocation()))
				.collect(Collectors.toSet());
	}

	public boolean isWithin(Location location) {
		return region.isWithin(location.toVector());
	}

	public Set<Chunk> getChunks() {
		return Collections.unmodifiableSet(chunks);
	}

	/**
	 * @param chunk The Chunk to check within.
	 * @return boolean if it's within.
	 */
	public boolean isWithin(Chunk chunk) {
		return getChunks().parallelStream().anyMatch(check -> check.getX() == chunk.getX() && check.getZ() == chunk.getZ());
	}

}
