package me.limeglass.epicmines.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.utils.CuboidRegion;

public class ResetInfo {

	private final Map<Material, Double> composition = new HashMap<>();
	private final Mine mine;

	public ResetInfo(Mine mine) {
		this.mine = mine;
	}

	public ResetInfo(Mine mine, Map<Material, Double> composition) {
		this.composition.putAll(composition);
		this.mine = mine;
	}

	public Map<Material, Double> getRawValues() {
		return composition;
	}

	public List<BlockChance> getProbabilityMap() {
		List<BlockChance> probabilityList = new ArrayList<>();
		Map<Material, Double> probability = Maps.newHashMap(composition);
		double max = 0;
		for (Entry<Material, Double> entry : probability.entrySet())
			max += entry.getValue();
		if (max < 1) {
			probability.put(Material.AIR, 1 - max);
			max = 1;
		}
		double i = 0;
		for (Entry<Material, Double> entry : probability.entrySet()) {
			double value = entry.getValue() / max;
			i += value;
			probabilityList.add(new BlockChance(entry.getKey(), i));
		}
		return probabilityList;
	}

	private final Consumer<Iterator<Block>> getDefaultConsumer(List<BlockChance> probability) {
		return iterator -> {
			Random random = new Random();
			while (iterator.hasNext()) {
				Block block = iterator.next();
				double rand = random.nextDouble();
				for (BlockChance entry : probability) {
					if (rand > entry.getChance())
						continue;
					block.setType(entry.getMaterial());
					//TODO block data.
					break;
				}
			}
		};
	}

	public void reset() {
		if (EpicMines.getInstance().getConfig().getBoolean("general.cancel-unloaded-chunks-reset", true)) {
			if (mine.getChunks().stream().allMatch(chunk -> !chunk.isLoaded()))
				return;
		}
		Set<Mine> mines = Sets.newHashSet(mine.getChildern());
		mines.add(mine);
		mines.forEach(mine -> {
			CuboidRegion region = mine.getCuboidRegion();
			mine.getPlayersWithin().forEach(player -> player.teleport(mine.getTeleport()));
			List<BlockChance> probability = getProbabilityMap();
			Iterator<Block> iterator = mine.getFlags().stream()
					.map(reset -> reset.modify(mine.getWorld(), region.min(), region.max()))
					.filter(element -> element != null)
					.findFirst()
					.orElse(mine.iterator());
			Consumer<Iterator<Block>> consumer = mine.getFlags().stream()
					.map(reset -> reset.onReset(probability, mine))
					.filter(element -> element != null)
					.findFirst()
					.orElse(getDefaultConsumer(probability));
			consumer.accept(iterator);
		});
	}

	public static class BlockChance {

		private final Material material;
		private final double chance;

		public BlockChance(Material material, double chance) {
			this.material = material;
			this.chance = chance;
		}

		public Material getMaterial() {
			return material;
		}

		public double getChance() {
			return chance;
		}

	}

}
