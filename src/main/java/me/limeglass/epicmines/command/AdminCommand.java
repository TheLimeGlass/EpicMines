package me.limeglass.epicmines.command;

public abstract class AdminCommand extends AbstractCommand {

	protected AdminCommand(boolean console, String... commands) {
		super(console, commands);
	}

}
