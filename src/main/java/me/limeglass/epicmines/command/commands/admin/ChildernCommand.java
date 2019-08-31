package me.limeglass.epicmines.command.commands.admin;

import java.util.Optional;

import org.bukkit.command.CommandSender;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.command.AdminCommand;
import me.limeglass.epicmines.manager.managers.MineManager;
import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.utils.ListMessageBuilder;
import me.limeglass.epicmines.utils.MessageBuilder;

public class ChildernCommand extends AdminCommand {

	public ChildernCommand() {
		super(true, "child", "childern", "c");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		if (arguments.length <= 0) {
			new ListMessageBuilder("command.childern.command").send(sender);
			return ReturnType.SUCCESS;
		}
		MineManager manager = EpicMines.getInstance().getManager(MineManager.class);
		Optional<Mine> optional = manager.getMine(arguments[0]);
		if (!optional.isPresent()) {
			new MessageBuilder("mines.no-mine-found")
					.replace("%input%", arguments[0])
					.setPlaceholderObject(sender)
					.send(sender);
			return ReturnType.FAILURE;
		}
		Mine mine = optional.get();
		if (arguments.length == 1) {
			new MessageBuilder("command.childern.on-mine")
					.setPlaceholderObject(mine)
					.send(sender);
			return ReturnType.SUCCESS;
		} else if (arguments.length == 2) {
			runCommand(command, sender, new String[]{});
			return ReturnType.SYNTAX_ERROR;
		}
		Optional<Mine> child = manager.getMine(arguments[2]);
		if (!child.isPresent()) {
			new MessageBuilder("mines.no-mine-found")
					.replace("%input%", arguments[2])
					.setPlaceholderObject(sender)
					.send(sender);
			return ReturnType.FAILURE;
		}
		switch (arguments[1]) {
			case "add":
				mine.addChild(child.get());
				new MessageBuilder("commands.childern.added")
						.replace("%child%", arguments[2])
						.replace("%mind%", arguments[0])
						.setPlaceholderObject(sender)
						.send(sender);
				break;
			case "remove":
				mine.removeChild(child.get());
				new MessageBuilder("commands.childern.removed")
						.replace("%child%", arguments[2])
						.replace("%mind%", arguments[0])
						.setPlaceholderObject(sender)
						.send(sender);
				break;
		}
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "childern";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"epicmines.childern", "epicmines.admin"};
	}

}
