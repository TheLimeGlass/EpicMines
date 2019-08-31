package me.limeglass.epicmines.displays;

import org.bukkit.entity.Player;

import me.limeglass.epicmines.manager.managers.MineManager.DisplayCategory;
import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.objects.SignLines;

public abstract class PlayerDisplayStatistic extends DisplayStatistic {

	public PlayerDisplayStatistic(DisplayCategory category, String name) {
		super(category, name);
	}

	/**
	 * Grab a formatted string from the given Mine.
	 * 
	 * @param mine The Mine that is requesting the Display.
	 * @param player The player receiving the display.
	 * @return The formatted String.
	 */
	public abstract String grab(Mine mine, Player player);

	/**
	 * Grab what this display will show on a sign.
	 * 
	 * @param mine The Mine that is requesting the Display.
	 * @param player The player receiving the display.
	 * @return A custom object to handle the Sign management.
	 */
	public abstract SignLines grabSign(Mine mine, Player player);

	public SignLines grabSign(Mine mine) {
		return null;
	}

	public String grab(Mine mine) {
		return null;
	}

}
