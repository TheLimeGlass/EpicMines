package me.limeglass.epicmines.database;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.limeglass.epicmines.database.serializer.ItemStackSerializer;
import me.limeglass.epicmines.database.serializer.LocationSerializer;
import me.limeglass.epicmines.database.serializer.MineFlagSerializer;
import me.limeglass.epicmines.database.serializer.MineSerializer;
import me.limeglass.epicmines.database.serializer.MineStatisticsSerializer;
import me.limeglass.epicmines.flags.MineFlag;
import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.objects.MineStatistics;
import me.limeglass.epicmines.objects.MineStatistics.Enter;

public abstract class Database<T> {

	protected final Gson gson;

	public Database(MineFlag... flags) {
		MineStatisticsSerializer mineStatistics = new MineStatisticsSerializer();
		GsonBuilder builder = new GsonBuilder()
				.registerTypeAdapter(Enter.class, mineStatistics.new EnterSerializer())
				.registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
				.registerTypeAdapter(MineFlag.class, new MineFlagSerializer())
				.registerTypeAdapter(Location.class, new LocationSerializer())
				.registerTypeAdapter(Mine.class, new MineSerializer())
				.registerTypeAdapter(MineStatistics.class, mineStatistics)
				.excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
				.enableComplexMapKeySerialization()
				.serializeNulls();
		for (MineFlag flag: flags)
			builder.registerTypeAdapter(flag.getClass(), flag);
		gson = builder.create();
	}

	public abstract void put(String key, T value);

	public abstract T get(String key, T def);

	public abstract boolean has(String key);

	public abstract Set<String> getKeys();

	public T get(String key) {
		return get(key, null);
	}

	public void delete(String key) {
		put(key, null);
	}

	public abstract void clear();

	public String serialize(Object object, Type type) {
		return gson.toJson(object, type);
	}

	public Object deserialize(String json, Type type) {
		return gson.fromJson(json, type);
	}

}
