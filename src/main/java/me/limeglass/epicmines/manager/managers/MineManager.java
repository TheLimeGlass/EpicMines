package me.limeglass.epicmines.manager.managers;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitScheduler;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.database.Database;
import me.limeglass.epicmines.flags.MineFlag;
import me.limeglass.epicmines.flags.MineFlag.FlagInfo;
import me.limeglass.epicmines.manager.Manager;
import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.utils.IntervalUtils;
import me.limeglass.epicmines.utils.Utils;

public class MineManager extends Manager {

	private final Set<FlagInfo<?>> flags = new HashSet<>();
	private final Set<Mine> mines = new HashSet<>();
	private Database<Mine> database;

	public MineManager() {
		super(true);
		EpicMines instance = EpicMines.getInstance();
		Set<MineFlag> flags = new HashSet<>();
		for (Class<MineFlag> clazz : Utils.getClassesOf(instance, instance.getPackageName() + ".flags", MineFlag.class)) {
			if (clazz == MineFlag.class)
				continue;
			try {
				MineFlag flag = clazz.newInstance();
				addFlag(flag);
				flags.add(flag);
			} catch (InstantiationException | IllegalAccessException e) {
				EpicMines.consoleMessage("&dFlag " + clazz.getName() + " doesn't have a nullable constructor.");
				e.printStackTrace();
				continue;
			}
		}
		FileConfiguration configuration = instance.getConfig();
		String table = configuration.getString("database.player-table", "Players");
		if (configuration.getBoolean("database.mysql.enabled", false))
			database = getMySQLDatabase(table, Mine.class, flags.toArray(new MineFlag[flags.size()]));
		else
			database = getFileDatabase(table, Mine.class, flags.toArray(new MineFlag[flags.size()]));
		String interval = configuration.getString("database.autosave", "5 miniutes");
		BukkitScheduler scheduler = Bukkit.getScheduler();
		scheduler.runTaskTimerAsynchronously(instance, () -> mines.forEach(mine -> database.put(mine.getName(), mine)), 0, IntervalUtils.getInterval(interval) * 20);
		scheduler.runTaskTimer(instance, () -> {
			for (Mine mine : mines) {
				boolean reset = false;
				for (MineFlag flag : mine.getFlags()) {
					flag.tick(mine);
					if (flag.canReset(mine))
						reset = true;
				}
				if (reset)
					resetMine(mine);
			}
		}, 0, 20);
	}

	public void save() {
		mines.forEach(mine -> database.put(mine.getName(), mine));
	}

	@SuppressWarnings("unchecked")
	public <F extends MineFlag> Optional<FlagInfo<F>> getFlagInfo(Class<F> flag) {
		return flags.stream()
				.filter(info -> info.getClass().equals(flag))
				.map(info -> (FlagInfo<F>) info)
				.findFirst();
	}

	public Optional<FlagInfo<?>> getFlagInfo(String name) {
		return flags.stream().filter(info -> info.getName().equalsIgnoreCase(name)).findFirst();
	}

	public Set<FlagInfo<?>> getFlagInfos() {
		return flags;
	}

	public Set<Mine> getMines() {
		return mines;
	}

	public Optional<Mine> getMine(String name) {
		Optional<Mine> optional = mines.stream()
				.filter(mine -> mine.getName().equalsIgnoreCase(name))
				.findFirst();
		if (optional.isPresent())
			return optional;
		return Optional.ofNullable(database.get(name));
	}

	public Set<Mine> getMines(String... names) {
		Set<Mine> set = new HashSet<>();
		for (String name : names)
			getMine(name).ifPresent(mine -> set.add(mine));
		return set;
	}

	public <F extends MineFlag> void addFlag(F flag) {
		flags.add(flag.toInfo());
	}

	public Mine getClosest(Location location) {
		return mines.stream()
				.sorted((mine1, mine2) -> Double.compare(mine1.getClosestCorner(location).distance(location), mine2.getClosestCorner(location).distance(location)))
				.findFirst()
				.get();
	}

	public Set<Mine> getMines(Chunk chunk) {
		return mines.stream()
				.filter(mine -> mine.isWithin(chunk))
				.collect(Collectors.toSet());
	}

	public Set<Mine> getMines(Location location) {
		return mines.stream()
				.filter(mine -> mine.isWithin(location))
				.collect(Collectors.toSet());
	}

	public void resetMine(Mine mine) {
		mine.update();
		//TODO
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();
		database.getKeys().parallelStream()
				.map(string -> database.get(string))
				.filter(mine -> mine.isWithin(chunk))
				.forEach(mine -> mines.add(mine));
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		getMines(event.getChunk()).stream()
				.filter(mine -> mine.getChunks().stream().anyMatch(chunk -> chunk.isLoaded()))
				.forEach(mine -> {
					database.put(mine.getName(), mine);
					Bukkit.getScheduler().runTaskLaterAsynchronously(EpicMines.getInstance(), () -> {
						if (mine.getChunks().stream().anyMatch(chunk -> chunk.isLoaded()))
							return;
						mines.remove(mine);
					}, 20 * 10); //Remove from cache in 10 seconds.
				});
	}

}
