package me.limeglass.epicmines.flags;

import java.lang.reflect.Type;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import me.limeglass.epicmines.database.Serializer;
import me.limeglass.epicmines.objects.Mine;

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
	 * Called when a mine resets.
	 * 
	 * @param mine Mine involved.
	 */
	public abstract void onReset(Mine mine);

	/**
	 * Called before a mine resets.
	 * 
	 * @param mine Mine involved.
	 * @return boolean if the mine can reset.
	 */
	public abstract boolean canReset(Mine mine);

	/**
	 * Called when a player breaks a block within the mine with this flag.
	 * 
	 * @param event BlockBreakEvent involved.
	 */
	public abstract void onMine(BlockBreakEvent event);

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

		private final Class<F> clazz;
		private final String[] description;
		private final String name, usage;

		public FlagInfo(Class<F> clazz, String name, String usage, String... description) {
			this.description = description;
			this.clazz = clazz;
			this.usage = usage;
			this.name = name;
		}

		public Class<? extends MineFlag> getFlagClass() {
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
