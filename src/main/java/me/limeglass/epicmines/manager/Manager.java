package me.limeglass.epicmines.manager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.database.Database;
import me.limeglass.epicmines.database.H2Database;
import me.limeglass.epicmines.database.MySQLDatabase;
import me.limeglass.epicmines.flags.MineFlag;

public abstract class Manager implements Listener {

	private final Map<Class<?>, Database<?>> databases = new HashMap<>();
	private final boolean listener;

	protected Manager(boolean listener) {
		this.listener = listener;
	}

	@SuppressWarnings("unchecked")
	protected <T> Database<T> getMySQLDatabase(String table, Class<T> type, MineFlag... flags) {
		if (databases.containsKey(type))
			return (MySQLDatabase<T>) databases.get(type);
		ConfigurationSection section = EpicMines.getInstance().getConfig().getConfigurationSection("database");
		String address = section.getString("mysql.address", "localhost");
		String password = section.getString("mysql.password", "1234");
		String name = section.getString("mysql.name", "kingdoms");
		String user = section.getString("mysql.user", "root");
		Database<T> database = null;
		try {
			database = new MySQLDatabase<>(address, name, table, user, password, type, flags);
			EpicMines.debugMessage("MySQL connection " + address + " was a success!");
			databases.put(type, (MySQLDatabase<?>) database);
			return database;
		} catch (SQLException exception) {
			EpicMines.consoleMessage("&cMySQL connection failed!");
			EpicMines.consoleMessage("Address: " + address + " with user: " + user);
			EpicMines.consoleMessage("Reason: " + exception.getMessage());
		} finally {
			if (database == null) {
				EpicMines.consoleMessage("Attempting to use SQLite instead...");
				database = getFileDatabase(table, type);
			}
		}
		return database;
	}

	@SuppressWarnings("unchecked")
	protected <T> Database<T> getFileDatabase(String table, Class<T> type, MineFlag... flags) {
		if (databases.containsKey(type))
			return (H2Database<T>) databases.get(type);
		Database<T> database = null;
		try {
			database = new H2Database<>(table, type, flags);
			EpicMines.debugMessage("Using H2 database for " + type.getSimpleName() + " data");
			databases.put(type, database);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return database;
	}

	public boolean hasListener() {
		return listener;
	}

}
