package me.limeglass.epicmines.flags;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.utils.MessageBuilder;
import me.limeglass.epicmines.utils.SoundPlayer;

public class PotionFlag extends MineFlag {

	private final Map<PotionEffectType, Integer> potions = new HashMap<>();

	public PotionFlag() {
		super("effect", "add/remove <potion> [<amplification>]", "Add a potion effect when entering mine.");
	}

	@Override
	public void tick(Mine mine) {
		for (Entry<PotionEffectType, Integer> entry : potions.entrySet()) {
			PotionEffect potion = new PotionEffect(entry.getKey(), 20, entry.getValue());
			for (Player player : mine.getPlayersWithin()) {
				PotionEffect existing = player.getPotionEffect(entry.getKey());
				if (existing != null)
					continue;
				player.removePotionEffect(entry.getKey());
				player.addPotionEffect(potion);
			}
		}
	}

	@Override
	public void onMine(BlockBreakEvent event) {}

	@Override
	public boolean onAttach(Player player, Mine mine, String[] arguments) {
		if (arguments.length < 2)
			return false;
		PotionEffectType potion = null;
		try {
			potion = PotionEffectType.getByName(arguments[1]);
		} catch (Exception e) {}
		if (potion == null) {
			new MessageBuilder("flags.effect-error")
					.replace("%input%", arguments[1])
					.setPlaceholderObject(player)
					.send(player);
			new SoundPlayer("error").playTo(player);
			return false;
		}
		int amp = 1;
		try {
			if (arguments.length >= 3)
				amp = Integer.parseInt(arguments[2]);
		} catch (NumberFormatException e) {}
		switch (arguments[0]) {
			case "add":
				potions.put(potion, amp);
				new MessageBuilder("flags.effect-added")
						.replace("%potion%", potion.getName())
						.setPlaceholderObject(player)
						.replace("%amp%", amp)
						.send(player);
			case "remove":
				potions.remove(potion);
				new MessageBuilder("flags.effect-removed")
						.replace("%potion%", potion.getName())
						.setPlaceholderObject(player)
						.replace("%amp%", amp)
						.send(player);
		}
		return true;
	}

	public Map<PotionEffectType, Integer> getPotions() {
		return potions;
	}

	@Override
	protected JsonElement serialize(JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		JsonArray effects = new JsonArray();
		for (Entry<PotionEffectType, Integer> entry : potions.entrySet()) {
			JsonObject effect = new JsonObject();
			effect.addProperty("potion", entry.getKey().getName());
			effect.addProperty("amplification", entry.getValue());
			effects.add(effect);
		}
		object.add("effects", effects);
		return object;
	}

	@Override
	protected void deserialize(JsonElement json, JsonDeserializationContext context) {
		JsonObject object = json.getAsJsonObject();
		JsonElement effects = object.get("effects");
		if (effects != null && !effects.isJsonNull() && effects.isJsonArray()) {
			JsonArray array = effects.getAsJsonArray();
			array.forEach(element -> {
				JsonObject effect = element.getAsJsonObject();
				JsonElement name = effect.get("potion");
				PotionEffectType potion = null;
				try {
					potion = PotionEffectType.getByName(name.getAsString());
				} catch (Exception e) {}
				if (potion == null)
					return;
				JsonElement amplification = effect.get("amplification");
				int amp = 1;
				if (amplification != null && !amplification.isJsonNull())
					amp = amplification.getAsInt();
				potions.put(potion, amp);
			});
		}
	}

	@Override
	public void sendInfo(Mine mine, Player player) {
		new MessageBuilder("flags.effect-info")
				.replace("%player%", player.getName())
				.setPlaceholderObject(mine)
				.send(player);
	}

}
