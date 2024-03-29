package me.limeglass.epicmines.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.PluginManager;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.manager.managers.external.CitizensManager;
import me.limeglass.epicmines.placeholders.DefaultPlaceholders;
import me.limeglass.epicmines.utils.Utils;

public class ManagerHandler {

	private final Set<ExternalManager> externalManagers = new HashSet<>();
	private final List<Manager> managers = new ArrayList<>();

	public ManagerHandler(EpicMines instance) {
		PluginManager pluginManager = instance.getServer().getPluginManager();
		if (pluginManager.isPluginEnabled("Citizens"))
			externalManagers.add(new CitizensManager());
		DefaultPlaceholders.initalize();
		for (Class<Manager> clazz : Utils.getClassesOf(instance, instance.getPackageName() + ".manager", Manager.class)) {
			if (clazz == Manager.class)
				continue;
			try {
				Manager manager = clazz.newInstance();
				managers.add(manager);
			} catch (InstantiationException | IllegalAccessException e) {
				EpicMines.consoleMessage("&dManager " + clazz.getName() + " doesn't have a nullable constructor.");
				e.printStackTrace();
				continue;
			}
		}
		for (Manager manager : managers) {
			if (!manager.hasListener())
				continue;
			try {
				Bukkit.getPluginManager().registerEvents(manager, instance);
			} catch (IllegalPluginAccessException e) {
				EpicMines.consoleMessage("&dFailed to register listener for manager: " + manager.getClass().getName());
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends ExternalManager> T getExternalManager(Class<T> clazz) {
		for (ExternalManager manager : externalManagers) {
			if (manager.getClass().equals(clazz))
				return (T) manager;
		}
		try {
			T manager = clazz.newInstance();
			externalManagers.add(manager);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <M extends Manager> M getManager(Class<M> clazz) {
		for (Manager manager : managers) {
			if (manager.getClass().equals(clazz))
				return (M) manager;
		}
		try {
			M manager = clazz.newInstance();
			managers.add(manager);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void registerManager(Manager manager) {
		if (!managers.contains(manager))
			managers.add(manager);
	}

	public Set<ExternalManager> getExternalManagers() {
		return externalManagers;
	}

	public List<Manager> getManagers() {
		return managers;
	}

}
