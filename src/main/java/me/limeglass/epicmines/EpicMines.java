package me.limeglass.epicmines;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import me.limeglass.epicmines.command.CommandHandler;
import me.limeglass.epicmines.manager.ExternalManager;
import me.limeglass.epicmines.manager.Manager;
import me.limeglass.epicmines.manager.ManagerHandler;
import me.limeglass.epicmines.manager.managers.MineManager;
import me.limeglass.epicmines.utils.Formatting;

public class EpicMines extends JavaPlugin {

	private final Map<String, FileConfiguration> configurations = new HashMap<>();
	private final String packageName = "me.limeglass.epicmines";
	private CommandHandler commandHandler;
	private ManagerHandler managerHandler;
	private static EpicMines instance;

	@Override
	public void onEnable() {
		instance = this;
		File configFile = new File(getDataFolder(), "config.yml");
		//If newer version was found, update configuration.
		if (!getDescription().getVersion().equals(getConfig().getString("version"))) {
			if (configFile.exists())
				configFile.delete();
		}
		//Create all the default files.
		for (String name : Arrays.asList("config", "messages", "sounds", "inventories")) {
			File file = new File(getDataFolder(), name + ".yml");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				saveResource(file.getName(), false);
				debugMessage("created new default file " + file.getName());
			}
			FileConfiguration configuration = new YamlConfiguration();
			try {
				configuration.load(file);
				configurations.put(name, configuration);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		managerHandler = new ManagerHandler(this);
		commandHandler = new CommandHandler(this);
		EpicMinesAPI.setInstance(this);
		Bukkit.getConsoleSender().sendMessage("[EpicMines] EpicMines has been Enabled!");
	}

	@Override
	public void onDisable() {
		getManager(MineManager.class).save();
	}

	public <T extends ExternalManager> T getExternalManager(Class<T> expected) {
		return managerHandler.getExternalManager(expected);
	}

	/**
	 * Grab a FileConfiguration if found.
	 * Call it without it's file extension, just the simple name of the file.
	 * 
	 * @param configuration The name of the configuration to search for.
	 * @return Optional<FileConfiguration> as the file may or may not exist.
	 */
	public Optional<FileConfiguration> getConfiguration(String configuration) {
		return Optional.ofNullable(configurations.get(configuration));
	}

	/**
	 * Grab a Manager by it's class and create it if not present.
	 * 
	 * @param <T> <T extends Manager>
	 * @param expected The expected Class that extends Manager.
	 * @return The Manager that matches the defined class.
	 */
	public <T extends Manager> T getManager(Class<T> expected) {
		return managerHandler.getManager(expected);
	}

	public static void consoleMessage(String string) {
		Bukkit.getConsoleSender().sendMessage(Formatting.color("&6[EpicMines]&7 " + string));
	}

	public static void debugMessage(String string) {
		if (instance.getConfig().getBoolean("debug"))
			consoleMessage("&b" + string);
	}

	/**
	 * @return The CommandManager allocated to the Kingdoms instance.
	 */
	public CommandHandler getCommandHandler() {
		return commandHandler;
	}

	public ManagerHandler getManagerHandler() {
		return managerHandler;
	}

	public static EpicMines getInstance() {
		return instance;
	}

	public List<Manager> getManagers() {
		return managerHandler.getManagers();
	}

	public String getPackageName() {
		return packageName;
	}

}
