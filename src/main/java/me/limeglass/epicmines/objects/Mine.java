package me.limeglass.epicmines.objects;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;
import com.google.common.collect.Streams;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.flags.DelayFlag;
import me.limeglass.epicmines.flags.MineFlag;
import me.limeglass.epicmines.flags.MineFlag.FlagInfo;
import me.limeglass.epicmines.utils.CuboidRegion;
import me.limeglass.epicmines.utils.MessageBuilder;

public class Mine {

	private final Set<MineFlag> flags = Sets.newHashSet(new DelayFlag());
	private final ResetInfo resetInfo = new ResetInfo(this);
	private long update = System.currentTimeMillis();
	private final Location pos1, pos2, teleport;
	private final CuboidRegion region;
	private final String name;

	public Mine(String name, Location pos1, Location pos2, Location teleport) {
		this.region = new CuboidRegion(pos1, pos2);
		this.teleport = teleport;
		this.name = name;
		this.pos1 = pos1;
		this.pos2 = pos2;
	}

	public void reset() {
		update();
		resetInfo.reset();
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

	public Set<MineFlag> getFlags() {
		return flags;
	}

	public DelayFlag getDelayFlag() {
		return flags.stream()
				.filter(flag -> flag instanceof DelayFlag)
				.map(flag -> (DelayFlag) flag)
				.findFirst()
				.get();
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

	public Optional<MineFlag> getFlag(String name) {
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

	public void addFlag(Player player, FlagInfo<?> info, String... arguments) {
		flags.removeIf(flag -> flag.getName().equalsIgnoreCase(info.getName()));
		Bukkit.getScheduler().runTaskAsynchronously(EpicMines.getInstance(), () -> {
			try {
				MineFlag flag = info.getFlagClass().newInstance();
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

	public Set<Chunk> getChunks() {
		return getCuboidRegion().getChunks();
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
		return getCuboidRegion().isWithin(location.toVector());
	}

	public boolean isWithin(Chunk chunk) {
		return getCuboidRegion().isWithin(chunk);
	}

}
