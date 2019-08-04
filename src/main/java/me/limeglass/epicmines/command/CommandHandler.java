package me.limeglass.epicmines.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.command.AbstractCommand.ReturnType;
import me.limeglass.epicmines.command.commands.EpicMinesCommand;
import me.limeglass.epicmines.utils.MessageBuilder;
import me.limeglass.epicmines.utils.Utils;

public class CommandHandler implements CommandExecutor {

	private final List<AbstractCommand> commands = new ArrayList<>();

	public CommandHandler(EpicMines instance) {
		instance.getCommand("epicmines").setExecutor(this);
		Utils.getClassesOf(instance, instance.getPackageName() + ".command.commands", AbstractCommand.class).forEach(clazz -> {
			try {
				AbstractCommand command = clazz.newInstance();
				commands.add(command);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
		for (AbstractCommand abstractCommand : commands) {
			// It's the main command
			if (arguments.length <= 0 && abstractCommand instanceof EpicMinesCommand) {
				processRequirements(abstractCommand, sender, arguments);
				return true;
			} else if (arguments.length > 0 && abstractCommand.containsCommand(arguments[0])) {
				processRequirements(abstractCommand, sender, arguments);
				return true;
			}
		}
		new MessageBuilder("messages.command-doesnt-exist").send(sender);
		return true;
	}

	private void processRequirements(AbstractCommand command, CommandSender sender, String[] arguments) {
		if (!(sender instanceof Player) && !command.isConsoleAllowed()) {
			 new MessageBuilder("messages.must-be-player")
			 		.replace("%command%", command.getSyntax(sender))
			 		.setPlaceholderObject(sender)
			 		.send(sender);
			return;
		}
		if (command.getPermissionNodes() == null || Arrays.stream(command.getPermissionNodes()).parallel().anyMatch(permission -> sender.hasPermission(permission))) {
			if (command instanceof AdminCommand) {
				if (sender instanceof Player && !sender.hasPermission("epicmines.admin")) {
					new MessageBuilder("messages.no-permission").send(sender);
					return;
				}
			}
			String[] array = arguments;
			String entered = "epicmines";
			if (arguments.length > 0) {
				entered = array[0];
				array = Arrays.copyOfRange(arguments, 1, arguments.length);
			}
			ReturnType returnType = command.runCommand(entered, sender, array);
			if (returnType == ReturnType.SYNTAX_ERROR) {
				 new MessageBuilder("messages.invalid-command", "messages.invalid-command-correction")
				 		.replace("%command%", command.getSyntax(sender))
				 		.setPlaceholderObject(sender)
				 		.send(sender);
			}
			return;
		}
		new MessageBuilder("messages.no-permission").send(sender);
	}

	public List<AbstractCommand> getCommands() {
		return Collections.unmodifiableList(commands);
	}

}
