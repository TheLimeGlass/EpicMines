package me.limeglass.epicmines.command.commands.admin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.command.AdminCommand;
import me.limeglass.epicmines.manager.managers.MineManager;
import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.objects.ResetInfo;
import me.limeglass.epicmines.utils.MessageBuilder;
import me.limeglass.epicmines.utils.SetParser;
import me.limeglass.epicmines.utils.SetParser.ParseResult;
import me.limeglass.epicmines.utils.SetParser.Type;

public class SetCommand extends AdminCommand {

	public SetCommand() {
		super(false, "set", "modify", "block", "blocks", "s", "view");
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
		ResetInfo resetInfo = mine.getResetInfo();
		Map<Material, Double> rawMap = resetInfo.getRawValues();
		if (arguments.length == 1) {
			if (rawMap.isEmpty()) {
				new MessageBuilder("mines.not-set")
						.setPlaceholderObject(mine)
						.send(player);
				return ReturnType.FAILURE;
			}
			new MessageBuilder("mines.set-info")
					.replace("%blocks%", rawMap.entrySet(), entry -> entry.getKey() + "-" + entry.getValue() + "%")
					.setPlaceholderObject(mine)
					.send(player);
			return ReturnType.SUCCESS;
		}
		String[] parse = Arrays.copyOfRange(arguments, 1, arguments.length);
		Map<String, ParseResult> results = new SetParser(parse).parse();
		List<Entry<String, ParseResult>> errors = results.entrySet().stream()
				.filter(entry -> entry.getValue().getType() != Type.SUCCESS)
				.collect(Collectors.toList());
		boolean successful = true;
		for (Entry<String, ParseResult> entry : errors) {
			ParseResult result = entry.getValue();
			switch (result.getType()) {
				case MATERIAL:
					new MessageBuilder("mines.errors.material")
							.replace("%material%", result.getInput())
							.replace("%input%", entry.getKey())
							.setPlaceholderObject(mine)
							.send(player);
					successful = false;
					break;
				case NUMBER:
					new MessageBuilder("mines.errors.chance")
							.replace("%chance%", result.getInput())
							.replace("%input%", entry.getKey())
							.setPlaceholderObject(mine)
							.send(player);
					successful = false;
					break;
				case SYNTAX_ERROR:
					new MessageBuilder("mines.errors.syntax")
							.replace("%input%", entry.getKey())
							.setPlaceholderObject(mine)
							.send(player);
					successful = false;
					break;
				case SUCCESS:
					break;
			}
		}
		if (!successful)
			return ReturnType.FAILURE;
		rawMap.clear();
		for (ParseResult result : results.values())
			rawMap.put(result.getMaterial(), result.getChance());
		new MessageBuilder("mines.set")
				.replace("%blocks%", rawMap.entrySet(), entry -> entry.getKey() + "-" + entry.getValue() + "%")
				.setPlaceholderObject(mine)
				.send(player);
		return ReturnType.SUCCESS;
	}

	@Override
	public String getConfigurationNode() {
		return "set";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"epicmines.set", "epicmines.admin"};
	}

}
