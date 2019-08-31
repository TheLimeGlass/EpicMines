package me.limeglass.epicmines.command.commands.admin;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.command.AdminCommand;
import me.limeglass.epicmines.manager.managers.MineManager;
import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.utils.MessageBuilder;

public class SetTeleportCommand extends AdminCommand {

	public SetTeleportCommand() {
		super(false, "setteleport", "setspawn");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		if (arguments.length <= 0)
			return ReturnType.SYNTAX_ERROR;
		Player player = (Player) sender;
		Optional<Mine> mine = EpicMines.getInstance().getManager(MineManager.class).getMine(arguments[0]);
		if (!mine.isPresent()) {
			new MessageBuilder("mines.no-mine-found")
					.replace("%input%", arguments[0])
					.setPlaceholderObject(player)
					.send(player);
			return ReturnType.FAILURE;
		}
		mine.get().setTeleport(player.getLocation().add(0, 0.3, 0));
		new MessageBuilder("commands.setteleport.set")
				.replace("%input%", arguments[0])
				.setPlaceholderObject(player)
				.send(player);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "setteleport";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"epicmines.setteleport", "epicmines.admin"};
	}

}
