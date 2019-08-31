package me.limeglass.epicmines.flags;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import me.limeglass.epicmines.database.Serializer;
import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.objects.ResetInfo.BlockChance;

public abstract class MineFlag implements Serializer<MineFlag> {

	private final String[] description;
	private final String name, usage;

	public MineFlag(String name, String usage, String... description) {
		this.description = description;
		this.usage = usage;
		this.name = name;
	}

	public String[] getDescription() {
		return description;
	}

	public String getUsage() {
		return usage;
	}

	public String getName() {
		return name;
	}

	public FlagInfo<? extends MineFlag> toInfo() {
		return new FlagInfo<>(getClass(), name, usage, description);
	}

	/**
	 * Message the player info about what the flag state currently is, what values are set, etc.
	 * 
	 * @param player The player requesting info on the flag for the mine.
	 */
	public abstract void sendInfo(Mine mine, Player player);

	/**
	 * Called every second on updates. Used for tickables.
	 * 
	 * @param mine Mine involved.
	 */
	public abstract void tick(Mine mine);

	/**
	 * Called when a mine resets. Override to control what happens during a reset.
	 * @see #modify(Vector,Vector) to modify the iterator.
	 * 
	 * @param mine Mine involved.
	 */
	public Consumer<Iterator<Block>> onReset(List<BlockChance> probability, Mine mine) {
		return null;
	}

	/**
	 * Override to change the block iterator locations.
	 * 
	 * @param world The world of the Mine.
	 * @param min Vector of the lowest point in the cuboid.
	 * @param max Vector of the highest point in the cuboid.
	 */
	public Iterator<Block> modify(World world, Vector min, Vector max) {
		return null;
	}

	/**
	 * Called before a mine resets. Override to be able to change.
	 * 
	 * @param mine Mine involved.
	 * @return boolean if the mine can reset.
	 */
	public boolean canReset(Mine mine) {
		return false;
	}

	/**
	 * Called when a player breaks a block within the mine with this flag.
	 * 
	 * @param event BlockBreakEvent involved.
	 */
	public abstract void onMine(BlockBreakEvent event);

	/**
	 * Called when a player enters the Mine.
	 * Override this, because it is called alot, be sure you use proper management.
	 * 
	 * @param event PlayerMoveEvent involved.
	 */
	public void onEnter(PlayerMoveEvent event) {
		
	}

	/**
	 * Called when a player leaves the Mine.
	 * Override this, because it is called alot, be sure you use proper management.
	 * 
	 * @param event PlayerMoveEvent involved.
	 */
	public void onLeave(PlayerMoveEvent event) {
		
	}

	/**
	 * Called when a flag is added to a mine with the set arguments.
	 * Treat as a command. This is also called on editing.
	 * 
	 * @param player The player that added this flag.
	 * @param mine Mine involved.
	 * @param mine String[] arguments involved.
	 * @return boolean false if the flag arguments were entered wrong.
	 */
	public abstract boolean onAttach(Player player, Mine mine, String[] arguments);

	protected abstract JsonElement serialize(JsonSerializationContext context);

	protected abstract void deserialize(JsonElement json, JsonDeserializationContext context);

	@Override
	public MineFlag deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		return deserialize(json, getClass(), context);
	}

	@Override
	public JsonElement serialize(MineFlag flag, Type typeOfSrc, JsonSerializationContext context) {
		return serialize(context);
	}

	public class FlagInfo<F extends MineFlag> {

		private final String[] description;
		private final String name, usage;
		private final Class<F> clazz;

		public FlagInfo(Class<F> clazz, String name, String usage, String... description) {
			this.description = description;
			this.clazz = clazz;
			this.usage = usage;
			this.name = name;
		}

		public Class<F> getFlagClass() {
			return clazz;
		}

		public String[] getDescription() {
			return description;
		}

		public String getUsage() {
			return usage;
		}

		public String getName() {
			return name;
		}

	}

}
