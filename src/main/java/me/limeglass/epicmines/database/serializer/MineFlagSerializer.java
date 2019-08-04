package me.limeglass.epicmines.database.serializer;

import java.lang.reflect.Type;
import java.util.Optional;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.database.Serializer;
import me.limeglass.epicmines.flags.MineFlag;
import me.limeglass.epicmines.flags.MineFlag.FlagInfo;
import me.limeglass.epicmines.manager.managers.MineManager;

public class MineFlagSerializer implements Serializer<MineFlag> {

	@Override
	public JsonElement serialize(MineFlag flag, Type type, JsonSerializationContext context) {
		JsonObject object = flag.serialize(flag, type, context).getAsJsonObject();
		object.addProperty("name", flag.getName());
		return object;
	}

	@Override
	public MineFlag deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement name = object.get("name");
		if (name == null || name.isJsonNull())
			return null; // Not a valid Json.
		MineManager manager = EpicMines.getInstance().getManager(MineManager.class);
		Optional<FlagInfo<?>> info = manager.getFlagInfo(name.getAsString());
		if (!info.isPresent())
			return null; // Doesn't exist anymore.
		MineFlag flag;
		try {
			flag = info.get().getFlagClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return null; // User extended the API wrong.
		}
		return flag.deserialize(json, type, context); // Finish deserialize on the API extended MineFlag.
	}

}
