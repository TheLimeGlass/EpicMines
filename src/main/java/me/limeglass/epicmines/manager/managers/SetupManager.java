package me.limeglass.epicmines.manager.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.inventories.AnvilMenu;
import me.limeglass.epicmines.manager.Manager;
import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.utils.ItemStackBuilder;
import me.limeglass.epicmines.utils.ListMessageBuilder;
import me.limeglass.epicmines.utils.MessageBuilder;
import me.limeglass.epicmines.utils.SoundPlayer;

public class SetupManager extends Manager {

	private final Set<Setup> setups = new HashSet<>();

	public SetupManager() {
		super(true);
	}

	public Optional<Setup> getSetup(Player player) {
		return setups.parallelStream()
				.filter(setup -> setup.player.equals(player))
				.findFirst();
	}

	public void finish(Setup setup) {
		Mine mine = new Mine(setup.getName(), setup.getLocation("pos1").get(), setup.getLocation("pos2").get(), setup.getLocation("teleport").get());
		EpicMines.getInstance().getManager(MineManager.class).getMines().add(mine);
		setups.remove(setup);
	}

	public Setup enterSetup(Player player) {
		new ListMessageBuilder("setup.1")
				.setPlaceholderObject(player)
				.send(player);
		new SoundPlayer("setup.enter").playTo(player);
		Setup setup = new Setup(player);
		FileConfiguration inventories = EpicMines.getInstance().getConfiguration("inventories").get();
		ItemStack search = new ItemStackBuilder(inventories.getConfigurationSection("inventories.setup.name-anvil"))
				.setPlaceholderObject(player)
				.build();
		new AnvilMenu(search, player, name -> {
			if (name.contains(" ")) {
				new ListMessageBuilder("setup.no-spaces")
						.setPlaceholderObject(player)
						.replace("%name%", name)
						.send(player);
				return;
			}
			setups.add(setup);
			setup.setName(name);
			new ListMessageBuilder("setup.2")
					.setPlaceholderObject(player)
					.replace("%name%", name)
					.send(player);
		});
		return setup;
	}

	public void quit(Player player) {
		getSetup(player).ifPresent(setup -> setups.remove(setup));
		new MessageBuilder("setup.quit").send(player);
		player.closeInventory();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		quit(event.getPlayer());
	}

	public class Setup {

		private final Map<String, Location> singles = new HashMap<>();
		private final Set<Material> blocks = new HashSet<>();
		private final Player player;
		private String name;

		public Setup(Player player) {
			this.player = player;
		}

		public String getName() {
			return name;
		}

		public Player getPlayer() {
			return player;
		}

		public boolean hasBlocks() {
			return !blocks.isEmpty();
		}

		public void addBlock(Material material) {
			blocks.add(material);
		}

		public void setName(String name) {
			this.name = name;
		}

		public Set<Material> getBlocks() {
			return blocks;
		}

		public void removeBlock(Material material) {
			blocks.remove(material);
		}

		public Optional<Location> getLocation(String key) {
			return Optional.ofNullable(singles.get(key));
		}

		public void setPos1(Location pos1) {
			singles.put("pos1", pos1);
		}

		public void setPos2(Location pos2) {
			singles.put("pos2", pos2);
		}

		public void setTeleport(Location teleport) {
			singles.put("teleport", teleport);
		}

	}

}
