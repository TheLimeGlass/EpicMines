package me.limeglass.epicmines.objects;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.displays.DisplayStatistic;
import me.limeglass.epicmines.displays.PlayerDisplayStatistic;
import me.limeglass.epicmines.utils.Utils;

public class MineSign {

	private final DisplayStatistic display;
	private final Location location;

	public MineSign(DisplayStatistic display, Location location) {
		this.display = display;
		this.location = location;
		if (Utils.methodExists(Sign.class, "setEditable", boolean.class))
			getSign().ifPresent(sign -> sign.setEditable(false));
	}

	public DisplayStatistic getStatistic() {
		return display;
	}

	public Location getLocation() {
		return location;
	}

	public Optional<Sign> getSign() {
		BlockState state = location.getBlock().getState();
		if (!(state instanceof Sign))
			return Optional.empty();
		return Optional.of((Sign) state);
	}

	public void update(Mine mine) {
		int radius = EpicMines.getInstance().getConfig().getInt("signs.radius", 30);
		if (display instanceof PlayerDisplayStatistic) {
			PlayerDisplayStatistic playerDisplay = ((PlayerDisplayStatistic) display);
			location.getWorld().getPlayers().parallelStream()
					.filter(player -> player.getLocation().distance(location) <= radius)
					.forEach(player -> player.sendSignChange(location, playerDisplay.grabSign(mine, player).getLines()));
		} else {
			getSign().ifPresent(signState -> display.grabSign(mine).apply(signState));
		}
	}

}
