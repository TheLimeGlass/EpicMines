package me.limeglass.epicmines.flags;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.utils.MessageBuilder;

public class AirFlag extends MineFlag {

	public AirFlag() {
		super("air", "", "Set the mine to only reset air blocks");
	}

	@Override
	public void tick(Mine mine) {}

	@Override
	public void onMine(BlockBreakEvent event) {}

	@Override
	public Iterator<Block> modify(World world, Vector min, Vector max) {
		return new Iterator<Block>() {

			int nextX = min.getBlockX();
			int nextY = min.getBlockY();
			int nextZ = min.getBlockZ();

			@Override
			public boolean hasNext() {
				return nextX != Integer.MIN_VALUE;
			}

			@Override
			public Block next() {
				if (!hasNext())
					throw new NoSuchElementException();
				Block result = new Location(world, nextX, nextY, nextZ).getBlock();
				while (result.getType() != Material.AIR && hasNext() && isWithin(new BlockVector(nextX, nextY, nextZ))) {
					forwardOne();
					result = new Location(world, nextX, nextY, nextZ).getBlock();
				}
				forwardOne();
				forward();
				return result;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			public void forwardOne() {
				if (++nextX <= max.getX())
					return;
				nextX = min.getBlockX();
				if (++nextY <= max.getY())
					return;
				nextY = min.getBlockY();

				if (++nextZ <= max.getZ())
					return;
				nextX = Integer.MIN_VALUE;
			}

			public void forward() {
				while (hasNext() && !isWithin(new BlockVector(nextX, nextY, nextZ))) {
					forwardOne();
				}
			}

			public boolean isWithin(BlockVector vector) {
				double x = vector.getX();
				double y = vector.getY();
				double z = vector.getZ();
				return x >= min.getBlockX() && x <= max.getBlockX() && y >= min.getBlockY() && y <= max.getBlockY() && z >= min.getBlockZ() && z <= max.getBlockZ();
			}
		};
	}

	@Override
	public boolean onAttach(Player player, Mine mine, String[] arguments) {
		new MessageBuilder("flags.air-set")
				.replace("%player%", player.getName())
				.setPlaceholderObject(mine)
				.send(player);
		return true;
	}

	@Override
	protected JsonElement serialize(JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		return object;
	}

	@Override
	protected void deserialize(JsonElement json, JsonDeserializationContext context) {}

	@Override
	public void sendInfo(Mine mine, Player player) {
		new MessageBuilder("flags.air-info")
				.replace("%player%", player.getName())
				.setPlaceholderObject(mine)
				.send(player);
	}

}
