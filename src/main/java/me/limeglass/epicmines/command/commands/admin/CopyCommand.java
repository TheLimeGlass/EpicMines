package me.limeglass.epicmines.command.commands.admin;

import java.util.Optional;

import org.bukkit.command.CommandSender;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.command.AdminCommand;
import me.limeglass.epicmines.manager.managers.MineManager;
import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.utils.MessageBuilder;

public class CopyCommand extends AdminCommand {

	public CopyCommand() {
		super(true, "copy", "clone");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		if (arguments.length <= 1)
			return ReturnType.SYNTAX_ERROR;
		MineManager manager = EpicMines.getInstance().getManager(MineManager.class);
		Optional<Mine> optional = manager.getMine(arguments[0]);
		if (!optional.isPresent()) {
			new MessageBuilder("mines.no-mine-found")
					.replace("%input%", arguments[0])
					.setPlaceholderObject(sender)
					.send(sender);
			return ReturnType.FAILURE;
		}
		Mine copy = optional.get();
		optional = manager.getMine(arguments[1]);
		if (!optional.isPresent()) {
			new MessageBuilder("mines.no-mine-found")
					.replace("%input%", arguments[1])
					.setPlaceholderObject(sender)
					.send(sender);
			return ReturnType.FAILURE;
		}
		Mine mine = optional.get();
		mine.apply(copy);
		new MessageBuilder("mines.no-mine-found")
				.replace("%copy%", copy.getName())
				.setPlaceholderObject(mine)
				.send(sender);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "copy";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"epicmines.clone", "epicmines.copy", "epicmines.admin"};
	}

}
