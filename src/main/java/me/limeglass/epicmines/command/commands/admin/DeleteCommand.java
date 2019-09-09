package me.limeglass.epicmines.command.commands.admin;

import java.util.Optional;

import org.bukkit.command.CommandSender;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.command.AdminCommand;
import me.limeglass.epicmines.manager.managers.MineManager;
import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.utils.MessageBuilder;

public class DeleteCommand extends AdminCommand {

	public DeleteCommand() {
		super(true, "delete", "d");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		MineManager manager = EpicMines.getInstance().getManager(MineManager.class);
		if (arguments.length <= 0)
			return ReturnType.SYNTAX_ERROR;
		Optional<Mine> optional = manager.getMine(arguments[0]);
		if (!optional.isPresent()) {
			new MessageBuilder("mines.no-mine-found")
					.replace("%input%", arguments[0])
					.setPlaceholderObject(sender)
					.send(sender);
			return ReturnType.FAILURE;
		}
		Mine mine = optional.get();
		manager.deleteMine(mine);
		new MessageBuilder("commands.delete.deleted")
				.replace("%input%", arguments[0])
				.setPlaceholderObject(sender)
				.send(sender);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "delete";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"epicmines.delete", "epicmines.admin"};
	}

}
