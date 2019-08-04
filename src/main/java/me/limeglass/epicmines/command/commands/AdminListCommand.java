package me.limeglass.epicmines.command.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.command.AbstractCommand;
import me.limeglass.epicmines.command.AdminCommand;
import me.limeglass.epicmines.utils.Formatting;
import me.limeglass.epicmines.utils.MessageBuilder;

public class AdminListCommand extends AbstractCommand {

	public AdminListCommand() {
		super(true, "admin", "a");
	}

	@Override
	protected ReturnType runCommand(String input, CommandSender sender, String... args) {
		EpicMines instance = EpicMines.getInstance();
		sender.sendMessage("");
		new MessageBuilder("messages.version")
				.replace("%version%", instance.getDescription().getVersion())
				.send(sender);
		for (AbstractCommand command : instance.getCommandHandler().getCommands()) {
			if (!(command instanceof AdminCommand))
				continue;
			if (command.getPermissionNodes() == null || Arrays.stream(command.getPermissionNodes()).parallel().anyMatch(permission -> sender.hasPermission(permission))) {
				sender.sendMessage(Formatting.color("&8 - &c" + command.getSyntax(sender) + "&7 - " + command.getDescription(sender)));
			}
		}
		sender.sendMessage("");
		return ReturnType.SUCCESS;
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"epicmines.admin"};
	}

	@Override
	public String getConfigurationNode() {
		return "admin";
	}

}
