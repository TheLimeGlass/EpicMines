package me.limeglass.epicmines.flags;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.utils.MessageBuilder;

public class DelayFlag extends MineFlag {

	private int seconds = 300;

	public DelayFlag() {
		super("delay", "<seconds>", "Set the reset delay in seconds");
	}

	@Override
	public void tick(Mine mine) {}

	@Override
	public void onReset(Mine mine) {}

	// Already calculated within the getTimeLeft() method.
	@Override
	public boolean canReset(Mine mine) {
		return mine.getTimeLeft() <= 0;
	}

	@Override
	public void onMine(BlockBreakEvent event) {}

	@Override
	public boolean onAttach(Player player, Mine mine, String[] arguments) {
		int value = Integer.parseInt(arguments[0]);
		if (value <= 0) {
			new MessageBuilder("flags.delay-error")
					.setPlaceholderObject(player)
					.replace("%input%", value)
					.send(player);
			return false;
		}
		this.seconds = value;
		new MessageBuilder("flags.delay-set")
				.setPlaceholderObject(player)
				.replace("%input%", value)
				.send(player);
		return true;
	}

	public int getSeconds() {
		return seconds;
	}

	@Override
	protected JsonElement serialize(JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.addProperty("seconds", seconds);
		return object;
	}

	@Override
	protected void deserialize(JsonElement json, JsonDeserializationContext context) {
		JsonObject object = json.getAsJsonObject();
		JsonElement element = object.get("seconds");
		if (element == null || element.isJsonNull())
			return;
		seconds = element.getAsInt();
	}

	@Override
	public void sendInfo(Mine mine, Player player) {
		new MessageBuilder("flags.delay-info")
				.replace("%player%", player.getName())
				.setPlaceholderObject(mine)
				.send(player);
	}

}
