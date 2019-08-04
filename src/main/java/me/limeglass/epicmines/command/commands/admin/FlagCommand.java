package me.limeglass.epicmines.command.commands.admin;

import java.util.Arrays;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.command.AdminCommand;
import me.limeglass.epicmines.flags.MineFlag;
import me.limeglass.epicmines.flags.MineFlag.FlagInfo;
import me.limeglass.epicmines.manager.managers.MineManager;
import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.objects.StringList;
import me.limeglass.epicmines.utils.ListMessageBuilder;
import me.limeglass.epicmines.utils.MessageBuilder;

public class FlagCommand extends AdminCommand {

	public FlagCommand() {
		super(false, "flags", "flag", "f");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		MineManager manager = EpicMines.getInstance().getManager(MineManager.class);
		if (arguments.length <= 0) {
			new ListMessageBuilder("flags.command")
					.setPlaceholderObject(player)
					.send(player);
			for (FlagInfo<?> flag : manager.getFlagInfos()) {
				new MessageBuilder(false, "flags.flag")
						.replace("%description%", new StringList(flag.getDescription()).merge())
						.replace("%usage%", flag.getUsage())
						.replace("%flag%", flag.getName())
						.setPlaceholderObject(player)
						.send(player);
			}
			return ReturnType.SUCCESS;
		}
		Optional<Mine> optional = manager.getMine(arguments[0]);
		if (!optional.isPresent()) {
			new MessageBuilder("mines.no-mine-found")
					.replace("%input%", arguments[0])
					.setPlaceholderObject(player)
					.send(player);
			return ReturnType.FAILURE;
		}
		Mine mine = optional.get();
		if (arguments.length == 1) {
			new MessageBuilder("flags.on-mine")
					.setPlaceholderObject(mine)
					.send(player);
			return ReturnType.SUCCESS;
		} else if (arguments.length == 2) {
			runCommand(command, sender, new String[]{});
			return ReturnType.SYNTAX_ERROR;
		}
		Optional<FlagInfo<?>> info = manager.getFlagInfo(arguments[2]);
		if (!info.isPresent()) {
			new MessageBuilder("flags.no-flag-found")
					.replace("%input%", arguments[2])
					.setPlaceholderObject(player)
					.send(player);
			return ReturnType.FAILURE;
		}
		Optional<? extends MineFlag> flag = mine.getFlag(info.get());
		String[] rest = Arrays.copyOfRange(arguments, 3, arguments.length);
		if (rest == null)
			rest = new String[]{};
		switch (arguments[1]) {
			case "info":
				if (!flag.isPresent()) {
					new MessageBuilder("mines.no-flag")
							.replace("%input%", arguments[2])
							.setPlaceholderObject(player)
							.send(player);
					return ReturnType.FAILURE;
				}
				flag.get().sendInfo(mine, player);
				break;
			case "set":
			case "add":
				mine.addFlag(player, info.get(), rest);
				break;
			case "remove":
				mine.removeFlag(player, info.get());
				break;
			case "edit":
				if (!flag.isPresent()) {
					new MessageBuilder("mines.no-flag")
							.replace("%input%", arguments[2])
							.setPlaceholderObject(player)
							.send(player);
					return ReturnType.FAILURE;
				}
				mine.editFlag(player, flag.get(), rest);
				break;
		}
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "flags";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"epicmines.flags", "epicmines.admin"};
	}

}
