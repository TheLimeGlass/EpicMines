package me.limeglass.epicmines.database.serializer;

import java.lang.reflect.Type;
import java.util.UUID;

import com.google.common.collect.HashMultiset;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.reflect.TypeToken;

import me.limeglass.epicmines.database.Serializer;
import me.limeglass.epicmines.objects.MineStatistics;
import me.limeglass.epicmines.objects.MineStatistics.Enter;

public class MineStatisticsSerializer implements Serializer<MineStatistics> {

	private final Gson gson = new Gson();

	@Override
	public JsonElement serialize(MineStatistics statistic, Type type, JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.addProperty("mined", gson.toJson(statistic.getBlocksMined()));
		object.addProperty("enters", gson.toJson(statistic.getMineEnters()));
		return object;
	}

	@Override
	public MineStatistics deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		MineStatistics statistics = new MineStatistics();
		JsonObject object = json.getAsJsonObject();
		JsonElement element = object.get("mined");
		if (element == null || element.isJsonNull())
			return null;
		statistics.getBlocksMined().addAll(gson.fromJson(element.getAsString(), new TypeToken<HashMultiset<String>>(){}.getType()));
		element = object.get("enters");
		if (element == null || element.isJsonNull())
			return null;
		statistics.getMineEnters().addAll(gson.fromJson(element.getAsString(), new TypeToken<HashMultiset<Enter>>(){}.getType()));
		return statistics;
	}

	public class EnterSerializer implements Serializer<Enter> {

		private final Gson gson = new Gson();

		@Override
		public JsonElement serialize(Enter enter, Type type, JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			object.addProperty("uuid", gson.toJson(enter.getPlayerUUID()));
			object.addProperty("time", enter.getTimestamp());
			return object;
		}

		@Override
		public Enter deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
			JsonObject object = json.getAsJsonObject();
			JsonElement element = object.get("uuid");
			if (element == null || element.isJsonNull())
				return null;
			UUID uuid = gson.fromJson(element.getAsString(), UUID.class);
			JsonElement time = object.get("time");
			if (time == null || time.isJsonNull())
				return null;
			return new Enter(uuid, time.getAsLong());
		}

	}

}
