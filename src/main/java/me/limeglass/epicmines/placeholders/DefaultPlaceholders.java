package me.limeglass.epicmines.placeholders;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.limeglass.epicmines.manager.managers.SetupManager.Setup;
import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.objects.StringList;

public class DefaultPlaceholders {

	public static void initalize() {
		Placeholders.registerPlaceholder(new Placeholder<CommandSender>("%sender%") {
			@Override
			public String replace(CommandSender sender) {
				return sender.getName();
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Setup>("%position1%") {
			@Override
			public String replace(Setup setup) {
				Optional<Location> pos1 = setup.getLocation("pos1");
				if (!pos1.isPresent())
					return null;
				Location location = pos1.get();
				return Math.round(location.getX()) + ", " + Math.round(location.getY()) + ", " + Math.round(location.getZ());
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Setup>("%position2%") {
			@Override
			public String replace(Setup setup) {
				Optional<Location> pos2 = setup.getLocation("pos2");
				if (!pos2.isPresent())
					return null;
				Location location = pos2.get();
				return Math.round(location.getX()) + ", " + Math.round(location.getY()) + ", " + Math.round(location.getZ());
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Setup>("%player%") {
			@Override
			public String replace(Setup setup) {
				Player player = setup.getPlayer();
				if (player == null)
					return null;
				return player.getName();
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Setup>("%blocks%") {
			@Override
			public String replace(Setup setup) {
				Set<Material> blocks = setup.getBlocks();
				return blocks.size() + "";
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Setup>("%name%") {
			@Override
			public String replace(Setup setup) {
				String name = setup.getName();
				if (name == null)
					return null;
				return name;
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Mine>("%name%") {
			@Override
			public String replace(Mine mine) {
				String name = mine.getName();
				if (name == null)
					return null;
				return name;
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Mine>("%flags%") {
			@Override
			public String replace(Mine mine) {
				return new StringList(mine.getFlags().stream().map(flag -> flag.getName()).collect(Collectors.toSet())).toString();
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Mine>("%time%", "%delay%") {
			@Override
			public String replace(Mine mine) {
				return mine.getDelayFlag().getSeconds() + "";
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Mine>("%seconds%", "%timeleft%", "%reset%") {
			@Override
			public String replace(Mine mine) {
				return mine.getTimeLeft() + "";
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Mine>("%players%") {
			@Override
			public String replace(Mine mine) {
				return new StringList(mine.getPlayersAround().stream().map(player -> player.getName()).collect(Collectors.toSet())).toString();
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Mine>("%position1%") {
			@Override
			public String replace(Mine mine) {
				Location location = mine.getPosition1();
				if (location == null)
					return null;
				return Math.round(location.getX()) + ", " + Math.round(location.getY()) + ", " + Math.round(location.getZ());
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Mine>("%position2%") {
			@Override
			public String replace(Mine mine) {
				Location location = mine.getPosition2();
				if (location == null)
					return null;
				return Math.round(location.getX()) + ", " + Math.round(location.getY()) + ", " + Math.round(location.getZ());
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<Player>("%player%") {
			@Override
			public String replace(Player player) {
				return player.getName();
			}
		});
		Placeholders.registerPlaceholder(new Placeholder<String>("%string%") {
			@Override
			public String replace(String string) {
				return string;
			}
		});
	}

}
