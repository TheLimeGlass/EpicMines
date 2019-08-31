package me.limeglass.epicmines.displays;

import me.limeglass.epicmines.manager.managers.MineManager.DisplayCategory;
import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.objects.SignLines;

public abstract class DisplayStatistic {

	private final DisplayCategory category;
	private final String name;

	public DisplayStatistic(DisplayCategory category, String name) {
		this.category = category;
		this.name = name;
	}

	public DisplayCategory getCategory() {
		return category;
	}

	public String getName() {
		return name;
	}

	/**
	 * Grab a formatted string from the given Mine.
	 * 
	 * @param mine The Mine that is requesting the Display.
	 * @return The formatted String.
	 */
	public abstract String grab(Mine mine);

	/**
	 * Grab what this display will show on a sign.
	 * 
	 * @param mine The Mine that is requesting the Display.
	 * @return A custom object to handle the Sign management.
	 */
	public abstract SignLines grabSign(Mine mine);

}
