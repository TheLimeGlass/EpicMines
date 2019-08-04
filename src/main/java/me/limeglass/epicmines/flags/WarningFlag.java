package me.limeglass.epicmines.flags;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.reflect.TypeToken;

import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.utils.MessageBuilder;

public class WarningFlag extends MineFlag {

	private final Set<Integer> warnings = new HashSet<>();
	private final Gson gson = new Gson();

	public WarningFlag() {
		super("warnings", "<seconds> <...>", "Set reset warnings");
	}

	@Override
	public void tick(Mine mine) {
		long left = mine.getTimeLeft();
		for (int warning : warnings) {
			if (warning != left)
				continue;
			new MessageBuilder("flags.warning")
					.setPlaceholderObject(mine)
					.send(mine.getPlayersAround());
		}
	}

	@Override
	public void onReset(Mine mine) {}

	@Override
	public boolean canReset(Mine mine) {
		return false;
	}

	@Override
	public void onMine(BlockBreakEvent event) {}

	@Override
	public boolean onAttach(Player player, Mine mine, String[] arguments) {
		Set<Integer> values = new HashSet<>();
		for (String warning : arguments) {
			int value = Integer.parseInt(warning);
			if (value <= 0) {
				new MessageBuilder("flags.warnings-error")
						.setPlaceholderObject(player)
						.replace("%input%", value)
						.send(player);
				return false;
			}
			values.add(value);
		}
		warnings.clear();
		warnings.addAll(values);
		new MessageBuilder("flags.warnings-set")
				.replace("%warnings%", warnings, warning -> warning + " seconds")
				.replace("%player%", player.getName())
				.setPlaceholderObject(mine)
				.send(player);
		return true;
	}

	@Override
	public void sendInfo(Mine mine, Player player) {
		new MessageBuilder("flags.warning-info")
				.replace("%warnings%", warnings, warning -> warning + " seconds")
				.replace("%player%", player.getName())
				.setPlaceholderObject(mine)
				.send(player);
	}

	@Override
	protected JsonElement serialize(JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.addProperty("warnings", gson.toJson(warnings));
		return object;
	}

	@Override
	protected void deserialize(JsonElement json, JsonDeserializationContext context) {
		JsonObject object = json.getAsJsonObject();
		JsonElement element = object.get("warnings");
		if (element == null || element.isJsonNull())
			return;
		warnings.addAll(gson.fromJson(element.getAsString(), new TypeToken<HashSet<Integer>>(){}.getType()));
	}

}
