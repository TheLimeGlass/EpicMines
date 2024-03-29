package me.limeglass.epicmines.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.limeglass.epicmines.EpicMines;

public class SoundPlayer {

	private final Set<EpicSound> sounds = new HashSet<>();

	public SoundPlayer(String node) {
		this(EpicMines.getInstance().getConfiguration("sounds").get().getConfigurationSection(node));
	}

	public SoundPlayer(ConfigurationSection section) {
		if (!section.getBoolean("enabled", true))
			return;
		section = section.getConfigurationSection("sounds");
		for (String node : section.getKeys(false)) {
			this.sounds.add(new EpicSound(section.getConfigurationSection(node), "CLICK"));
		}
	}

	public SoundPlayer(Collection<EpicSound> sounds) {
		this.sounds.addAll(sounds);
	}

	private List<EpicSound> getSorted() {
		return sounds.parallelStream()
				.sorted(Comparator.comparing(EpicSound::getDelay))
				.collect(Collectors.toList());
	}

	public void playAt(Location... locations) {
		if (sounds.isEmpty())
			return;
		EpicMines instance = EpicMines.getInstance();
		for (EpicSound sound : getSorted()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
				@Override
				public void run() {
					sound.playAt(locations);
				}
			}, sound.getDelay());
		}
	}

	public void playTo(Player... player) {
		if (sounds.isEmpty())
			return;
		EpicMines instance = EpicMines.getInstance();
		for (EpicSound sound : getSorted()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
				@Override
				public void run() {
					sound.playTo(player);
				}
			}, sound.getDelay());
		}
	}

}
