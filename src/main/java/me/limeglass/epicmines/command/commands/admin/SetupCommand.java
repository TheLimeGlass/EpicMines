package me.limeglass.epicmines.command.commands.admin;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.command.AdminCommand;
import me.limeglass.epicmines.manager.managers.SetupManager;
import me.limeglass.epicmines.manager.managers.SetupManager.Setup;
import me.limeglass.epicmines.utils.ListMessageBuilder;
import me.limeglass.epicmines.utils.MessageBuilder;

public class SetupCommand extends AdminCommand {

	public SetupCommand() {
		super(false, "setup", "new");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		SetupManager manager = EpicMines.getInstance().getManager(SetupManager.class);
		Optional<Setup> optional = manager.getSetup(player);
		if (!optional.isPresent()) {
			if (arguments.length != 0) {
				new MessageBuilder("setup.not-in-setup")
						.setPlaceholderObject(player)
						.send(player);
				return ReturnType.FAILURE;
			}
			manager.enterSetup(player);
			return ReturnType.SUCCESS;
		}
		Setup setup = optional.get();
		switch (arguments[0]) {
			case "pos1":
				setup.setPos1(player.getTargetBlockExact(30).getLocation());
				new MessageBuilder("setup.pos1")
						.setPlaceholderObject(setup)
						.send(player);
				new ListMessageBuilder("setup.3")
						.setPlaceholderObject(setup)
						.send(player);
				break;
			case "pos2":
				setup.setPos2(player.getTargetBlockExact(30).getLocation());
				new MessageBuilder("setup.pos2")
						.setPlaceholderObject(setup)
						.send(player);
				new ListMessageBuilder("setup.4")
						.setPlaceholderObject(setup)
						.send(player);
				break;
			case "teleport":
				setup.setTeleport(player.getLocation().add(0, 0.2, 0));
				new MessageBuilder("setup.teleport")
						.setPlaceholderObject(setup)
						.send(player);
				new ListMessageBuilder("setup.5")
						.setPlaceholderObject(setup)
						.send(player);
				manager.finish(setup);
				break;
			case "quit":
				new MessageBuilder("setup.quit")
						.setPlaceholderObject(setup)
						.send(player);
				manager.quit(player);
				break;
			default:
				return ReturnType.SYNTAX_ERROR;
		}
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "setup";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"epicmines.setup", "epicmines.admin"};
	}

}
