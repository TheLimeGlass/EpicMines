package me.limeglass.epicmines.manager.managers;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitScheduler;

import com.google.common.collect.Sets;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.database.Database;
import me.limeglass.epicmines.displays.DisplayStatistic;
import me.limeglass.epicmines.displays.PlayerDisplayStatistic;
import me.limeglass.epicmines.flags.MineFlag;
import me.limeglass.epicmines.flags.MineFlag.FlagInfo;
import me.limeglass.epicmines.manager.Manager;
import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.objects.MineSign;
import me.limeglass.epicmines.utils.IntervalUtils;
import me.limeglass.epicmines.utils.MessageBuilder;
import me.limeglass.epicmines.utils.Utils;

public class MineManager extends Manager {

	private final static Set<DisplayCategory> categories = Sets.newHashSet(new DisplayCategory("mine"), new DisplayCategory("player"));
	private final Set<DisplayStatistic> displays = new HashSet<>();
	private final Set<FlagInfo<?>> flags = new HashSet<>();
	private final Set<Mine> mines = new HashSet<>();
	private Database<Mine> database;

	public MineManager() {
		super(true);
		EpicMines instance = EpicMines.getInstance();
		Set<MineFlag> flags = new HashSet<>();
		for (Class<? extends MineFlag> clazz : Utils.getClassesOf(instance, instance.getPackageName() + ".flags", MineFlag.class)) {
			if (clazz == MineFlag.class)
				continue;
			try {
				MineFlag flag = clazz.newInstance();
				addFlag(flag);
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
		mines.addAll(database.getKeys().stream().map(name -> database.get(name)).collect(Collectors.toSet()));
		for (Class<? extends DisplayStatistic> clazz : Utils.getClassesOf(instance, instance.getPackageName() + ".displays", DisplayStatistic.class)) {
			if (clazz == DisplayStatistic.class || clazz == PlayerDisplayStatistic.class)
				continue;
			try {
				DisplayStatistic display = clazz.newInstance();
				addDisplay(display);
			} catch (InstantiationException | IllegalAccessException e) {
				EpicMines.consoleMessage("&dDisplay " + clazz.getName() + " doesn't have a nullable constructor.");
				e.printStackTrace();
				continue;
			}
		}
		String interval = configuration.getString("database.autosave", "5 miniutes");
		BukkitScheduler scheduler = Bukkit.getScheduler();
		scheduler.runTaskTimerAsynchronously(instance, () -> save(), 0, IntervalUtils.getInterval(interval) * 20);
		scheduler.runTaskTimer(instance, () -> {
			for (Mine mine : mines) {
				mine.getSigns().forEach(sign -> sign.update(mine));
				boolean reset = false;
				for (MineFlag flag : mine.getFlags()) {
					flag.tick(mine);
					if (flag.canReset(mine))
						reset = true;
				}
				if (reset)
					mine.reset();
			}
		}, 0, 20);
	}

	public void save() {
		if (mines.isEmpty())
			return;
		EpicMines.debugMessage("Saving " + mines.size() + " mines");
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

	public Set<Mine> getAllMines() {
		Set<Mine> all = Sets.newHashSet(mines);
		all.addAll(database.getKeys().stream()
				.map(name -> getMine(name))
				.filter(optional -> optional.isPresent())
				.map(optional -> optional.get())
				.collect(Collectors.toSet()));
		return all;
	}

	public Optional<Mine> getMine(String name) {
		Optional<Mine> optional = mines.stream()
				.filter(mine -> mine.getName().equalsIgnoreCase(name))
				.findFirst();
		if (optional.isPresent())
			return optional;
		return Optional.ofNullable(database.get(name));
	}

	public Set<Mine> getMines(Collection<String> names) {
		return getMines(names.toArray(new String[names.size()]));
	}

	public Set<Mine> getMines(String... names) {
		if (names == null || names.length <= 0)
			return getAllMines();
		Set<Mine> set = new HashSet<>();
		for (String name : names)
			getMine(name).ifPresent(mine -> set.add(mine));
		return set;
	}

	public <F extends MineFlag> void addFlag(F flag) {
		flags.add(flag.toInfo());
	}

	public void addDisplayCategory(DisplayCategory category) {
		Validate.isTrue(!categories.stream().anyMatch(c -> c.getName().equalsIgnoreCase(category.getName())), "There cannot be multiple DisplayCategory's with the same name!");
		categories.add(category);
	}

	public <D extends DisplayStatistic> void addDisplay(D display) {
		Validate.isTrue(!displays.stream().anyMatch(d -> d.getName().equalsIgnoreCase(display.getName())), "There cannot be multiple DisplayStatistic's with the same name!");
		displays.add(display);
	}

	public static Optional<DisplayCategory> getCategory(String name) {
		return categories.stream()
				.filter(category -> category.getName().equalsIgnoreCase(name))
				.findFirst();
	}

	public Optional<DisplayStatistic> getDisplayStatistic(String name) {
		return displays.stream()
				.filter(display -> display.getName().equalsIgnoreCase(name))
				.findFirst();
	}

	public Set<DisplayStatistic> getDisplaysFor(DisplayCategory category) {
		return displays.stream()
				.filter(display -> display.getCategory().getName().equalsIgnoreCase(category.getName()))
				.collect(Collectors.toSet());
	}

	public Mine getClosest(Location location) {
		return mines.stream()
				.sorted((mine1, mine2) -> Double.compare(mine1.getClosestCorner(location).distance(location), mine2.getClosestCorner(location).distance(location)))
				.findFirst()
				.get();
	}

	public Set<Mine> getMines(Chunk... chunks) {
		return mines.stream()
				.filter(mine -> Arrays.stream(chunks)
						.anyMatch(chunk -> mine.isWithin(chunk)))
				.collect(Collectors.toSet());
	}

	public Set<Mine> getMines(Location location) {
		return mines.stream()
				.filter(mine -> mine.isWithin(location))
				.collect(Collectors.toSet());
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onMine(BlockBreakEvent event) {
		Block block = event.getBlock();
		for (Mine mine : mines) {
			if (mine.isWithin(block.getLocation())) {
				mine.getFlags().forEach(flag -> flag.onMine(event));
				mine.getStatistics().blockMined(block.getType());
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onMove(PlayerMoveEvent event) {
		for (Mine mine : mines) {
			if (mine.isWithin(event.getFrom())) {
				if (!mine.isWithin(event.getTo()))
					mine.getFlags().forEach(flag -> flag.onLeave(event));
			} else if (mine.isWithin(event.getTo())) {
				mine.getFlags().forEach(flag -> flag.onEnter(event));
				mine.getStatistics().onEnter(event.getPlayer());
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onSignPlace(SignChangeEvent event) {
		Player player = event.getPlayer();
		if (!player.hasPermission("epicmines.sign") && !player.hasPermission("epicmines.admin"))
			return;
		String[] lines = event.getLines();
		if (!lines[0].equalsIgnoreCase("[epicmines]"))
			return;
		Optional<Mine> optional = getMine(lines[1]);
		if (!optional.isPresent()) {
			new MessageBuilder("mines.no-mine-found")
					.replace("%input%", lines[1])
					.setPlaceholderObject(player)
					.send(player);
			return;
		}
		Mine mine = optional.get();
		getCategory(lines[2]).ifPresent(category -> {
				getDisplaysFor(category).stream()
						.filter(display -> display.getName().equalsIgnoreCase(lines[3]))
						.findFirst().ifPresent(display -> {
							MineSign sign = new MineSign(display, event.getBlock().getLocation());
							mine.addSign(sign);
							sign.update(mine);
							new MessageBuilder("signs.added-sign")
									.replace("%category%", category.getName())
									.replace("%display%", display.getName())
									.setPlaceholderObject(mine)
									.send(player);
						});
		});
	}

	public static class DisplayCategory {

		public static final DisplayCategory PLAYER = getOrCreate("player");
		public static final DisplayCategory MINE = getOrCreate("mine");
		private final String name;

		public DisplayCategory(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static Optional<DisplayCategory> get(String category) {
			return getCategory(category);
		}

		public static DisplayCategory getOrCreate(String name) {
			return getCategory(name).orElseGet(() -> {
				DisplayCategory category = new DisplayCategory(name);
				categories.add(category);
				return category;
			});
		}

	}

}
