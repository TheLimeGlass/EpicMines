package me.limeglass.epicmines.command.commands.admin;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.command.AdminCommand;
import me.limeglass.epicmines.manager.managers.MineManager;
import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.utils.MessageBuilder;

public class ResetCommand extends AdminCommand {

	public ResetCommand() {
		super(false, "reset", "r");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		MineManager manager = EpicMines.getInstance().getManager(MineManager.class);
		if (arguments.length <= 0)
			return ReturnType.SYNTAX_ERROR;
		Optional<Mine> optional = manager.getMine(arguments[0]);
		if (!optional.isPresent()) {
			new MessageBuilder("mines.no-mine-found")
					.replace("%input%", arguments[0])
					.setPlaceholderObject(player)
					.send(player);
			return ReturnType.FAILURE;
		}
		Mine mine = optional.get();
		mine.reset();
		new MessageBuilder("commands.reset.reset")
				.replace("%input%", arguments[0])
				.setPlaceholderObject(player)
				.send(player);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "reset";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"epicmines.reset", "epicmines.admin"};
	}

}
