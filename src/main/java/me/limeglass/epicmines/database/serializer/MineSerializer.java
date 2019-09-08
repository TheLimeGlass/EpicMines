package me.limeglass.epicmines.database.serializer;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import me.limeglass.epicmines.database.Serializer;
import me.limeglass.epicmines.flags.MineFlag;
import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.objects.MineSign;
import me.limeglass.epicmines.objects.MineStatistics;

public class MineSerializer implements Serializer<Mine> {

	@Override
	public JsonElement serialize(Mine mine, Type type, JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.addProperty("name", mine.getName());
		object.add("pos1", context.serialize(mine.getPosition1(), Location.class));
		object.add("pos2", context.serialize(mine.getPosition2(), Location.class));
		object.add("teleport", context.serialize(mine.getTeleport(), Location.class));
		JsonArray blocks = new JsonArray();
		for (Entry<Material, Double> entry : mine.getResetInfo().getRawValues().entrySet()) {
			JsonObject chance = new JsonObject();
			chance.addProperty("material", entry.getKey().name());
			chance.addProperty("chance", entry.getValue());
			blocks.add(chance);
		}
		object.add("blocks", blocks);
		JsonArray flags = new JsonArray();
		for (MineFlag flag : mine.getFlags())
			flags.add(context.serialize(flag, flag.getClass()));
		object.add("flags", flags);
		JsonArray signs = new JsonArray();
		for (MineSign sign : mine.getSigns())
			signs.add(context.serialize(sign, MineSign.class));
		object.add("signs", signs);
		object.add("statistics", context.serialize(mine.getStatistics(), MineStatistics.class));
		return object;
	}

	@Override
	public Mine deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement name = object.get("name");
		if (name == null || name.isJsonNull())
			return null;
		JsonElement pos1Element = object.get("pos1");
		if (pos1Element == null || pos1Element.isJsonNull())
			return null;
		Location pos1 = context.deserialize(pos1Element, Location.class);
		JsonElement pos2Element = object.get("pos2");
		if (pos2Element == null || pos2Element.isJsonNull())
			return null;
		Location pos2 = context.deserialize(pos2Element, Location.class);
		JsonElement teleportElement = object.get("teleport");
		if (teleportElement == null || teleportElement.isJsonNull())
			return null;
		Location teleport = context.deserialize(teleportElement, Location.class);
		Mine mine = new Mine(name.getAsString(), pos1, pos2, teleport);
		JsonElement blocksElement = object.get("blocks");
		if (blocksElement != null && !blocksElement.isJsonNull() && blocksElement.isJsonArray()) {
			Map<Material, Double> map = mine.getResetInfo().getRawValues();
			JsonArray array = blocksElement.getAsJsonArray();
			array.forEach(element -> {
				JsonObject chanceElement = element.getAsJsonObject();
				JsonElement material = chanceElement.get("material");
				if (material == null || material.isJsonNull())
					return;
				JsonElement chance = chanceElement.get("chance");
				if (chance == null || chance.isJsonNull())
					return;
				map.put(Material.getMaterial(material.getAsString()), chance.getAsDouble());
			});
		}
		JsonElement flagsElement = object.get("flags");
		if (flagsElement != null && !flagsElement.isJsonNull() && flagsElement.isJsonArray()) {
			JsonArray array = flagsElement.getAsJsonArray();
			array.forEach(element -> mine.getFlags().add(context.deserialize(element, MineFlag.class)));
		}
		JsonElement signsElement = object.get("signs");
		if (signsElement != null && !signsElement.isJsonNull() && signsElement.isJsonArray()) {
			JsonArray array = signsElement.getAsJsonArray();
			array.forEach(element -> mine.addSign(context.deserialize(element, MineSign.class)));
		}
		return mine;
	}

}
