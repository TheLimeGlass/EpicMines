package me.limeglass.epicmines.database.serializer;

import java.lang.reflect.Type;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.block.Sign;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.database.Serializer;
import me.limeglass.epicmines.displays.DisplayStatistic;
import me.limeglass.epicmines.manager.managers.MineManager;
import me.limeglass.epicmines.objects.MineSign;

public class MineSignSerializer implements Serializer<MineSign> {

	@Override
	public JsonElement serialize(MineSign sign, Type type, JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.add("location", context.serialize(sign.getLocation(), Location.class));
		object.addProperty("display", sign.getStatistic().getName());
		return object;
	}

	@Override
	public MineSign deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		JsonElement locationElement = object.get("location");
		if (locationElement == null || locationElement.isJsonNull())
			return null;
		Location location = context.deserialize(locationElement, Location.class);
		if (!(location.getBlock().getState() instanceof Sign))
			return null; // Not a sign anymore.
		JsonElement displayElement = object.get("display");
		if (displayElement == null || displayElement.isJsonNull())
			return null; // Doesn't exist anymore.
		Optional<DisplayStatistic> display = EpicMines.getInstance().getManager(MineManager.class).getDisplayStatistic(displayElement.getAsString());
		if (!display.isPresent())
			return null;
		return new MineSign(display.get(), location);
	}

}
