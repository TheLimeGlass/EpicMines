package me.limeglass.epicmines.displays;

import me.limeglass.epicmines.manager.managers.MineManager.DisplayCategory;
import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.objects.SignLines;
import me.limeglass.epicmines.utils.ListMessageBuilder;
import me.limeglass.epicmines.utils.MessageBuilder;

public class ResetDisplay extends DisplayStatistic {

	public ResetDisplay() {
		super(DisplayCategory.MINE, "reset");
	}

	@Override
	public String grab(Mine mine) {
		return new MessageBuilder(false, "signs.displays.reset.text")
				.setPlaceholderObject(mine)
				.get();
	}

	@Override
	public SignLines grabSign(Mine mine) {
		return new SignLines(new ListMessageBuilder(false, "signs.displays.reset.sign")
				.setPlaceholderObject(mine)
				.get());
	}

}
