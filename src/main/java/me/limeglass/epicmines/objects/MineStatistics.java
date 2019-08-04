package me.limeglass.epicmines.objects;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class MineStatistics {

	private final Multiset<String> mined = HashMultiset.create();
	private final Multiset<Enter> enters = HashMultiset.create();

	public MineStatistics() {}

	/**
	 * @return Multiset<String> of all materials mined. The String is the Material name at version's time.
	 */
	public Multiset<String> getBlocksMined() {
		return mined;
	}

	/**
	 * @return Multiset<Enter> of all times the player UUID has entered the mine.
	 */
	public Multiset<Enter> getMineEnters() {
		return enters;
	}

	public int getBlocksMined(Material material) {
		return mined.count(material.name());
	}

	public void blockMined(Material material) {
		mined.add(material.name());
	}

	public void onEnter(Player player) {
		enters.add(new Enter(player.getUniqueId()));
	}

	public static class Enter {

		private final long time;
		private final UUID uuid;

		public Enter(UUID uuid) {
			this.time = System.currentTimeMillis();
			this.uuid = uuid;
		}

		public Enter(UUID uuid, long time) {
			this.time = time;
			this.uuid = uuid;
		}

		public UUID getPlayerUUID() {
			return uuid;
		}

		public long getTimestamp() {
			return time;
		}

	}

}
