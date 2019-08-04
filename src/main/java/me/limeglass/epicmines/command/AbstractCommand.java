package me.limeglass.epicmines.command;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.utils.Formatting;

public abstract class AbstractCommand {

	private final String[] commands;
	private final boolean console;

	protected AbstractCommand(boolean console, String... commands) {
		this.console = console;
		this.commands = commands;
	}

	protected enum ReturnType {
		SUCCESS,
		FAILURE,
		SYNTAX_ERROR
	}

	public boolean containsCommand(String input) {
		for (String command : commands) {
			if (command.equalsIgnoreCase(input))
				return true;
		}
		return false;
	}

	protected boolean isConsoleAllowed() {
		return console;
	}

	protected String[] getCommands() {
		return commands;
	}

	protected abstract ReturnType runCommand(String command, CommandSender sender, String... arguments);

	public abstract String getConfigurationNode();

	public abstract String[] getPermissionNodes();

	public String getDescription(CommandSender sender) {
		FileConfiguration messages = EpicMines.getInstance().getConfiguration("messages").get();
		String description = messages.getString("commands." + getConfigurationNode() + ".description");
		return Formatting.color(description);
	}

	public String getSyntax(CommandSender sender) {
		FileConfiguration messages = EpicMines.getInstance().getConfiguration("messages").get();
		String syntax = messages.getString("commands." + getConfigurationNode() + ".syntax");
		return Formatting.color(syntax);
	}

}
