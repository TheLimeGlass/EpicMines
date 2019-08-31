package me.limeglass.epicmines;

import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;

import me.limeglass.epicmines.displays.DisplayStatistic;
import me.limeglass.epicmines.flags.MineFlag;
import me.limeglass.epicmines.manager.managers.MineManager;
import me.limeglass.epicmines.objects.Mine;

public class EpicMinesAPI {

	private static MineManager manager;
	private static EpicMines instance;

	public static void setInstance(EpicMines instance) {
		Validate.isTrue(EpicMinesAPI.instance == null, "The API instance can not be set twice!");
		EpicMinesAPI.instance = instance;
		manager = instance.getManager(MineManager.class);
	}

	/**
	 * Get all the loaded mines.
	 * 
	 * @return Set<Mine> of all mines.
	 */
	public static Set<Mine> getMines() {
		return manager.getMines();
	}

	/**
	 * Grab a mine by name, also forces the mine to be loaded if it's not already.
	 * 
	 * @param name The name of the Mine to search for.
	 * @return Optional<Mine> the mine with the matching name if found.
	 */
	public static Optional<Mine> getMine(String name) {
		return manager.getMine(name);
	}

	/**
	 * Grab mines by name, also forces the mine to be loaded if it's not already.
	 * 
	 * @param names The names of the Mines to search for.
	 * @return Set<Mine> the mines with the matching names.
	 */
	public static Set<Mine> getMines(String... names) {
		return manager.getMines(names);
	}

	/**
	 * Get the loaded mines at a location. There can possibly be multiple.
	 * 
	 * @param location Location to check at.
	 * @return Set<Mine> of all mines that may be at this location.
	 */
	public static Set<Mine> getMines(Location location) {
		return manager.getMines(location);
	}

	/**
	 * Add a MineFlag to the system to be used in the setup.
	 * MineFlags allow to do special features and things on Mine resets and block breaking.
	 * 
	 * @param <F> extends MineFlag
	 * @param flag The MineFlag class to register, extend in your plugin.
	 */
	public static <F extends MineFlag> void registerFlag(F flag) {
		manager.addFlag(flag);
	}

	/**
	 * Add a DisplayStatistic the statistics can be displayed within placeholders, holograms, signs etc.
	 * 
	 * @param <D> extends DisplayStatistic
	 * @param display The DisplayStatistic class to register, extend in your plugin.
	 */
	public static <D extends DisplayStatistic> void registerDisplay(D display) {
		manager.addDisplay(display);
	}

}
