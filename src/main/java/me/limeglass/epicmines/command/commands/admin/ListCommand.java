package me.limeglass.epicmines.command.commands.admin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.limeglass.epicmines.EpicMines;
import me.limeglass.epicmines.command.AdminCommand;
import me.limeglass.epicmines.manager.managers.MineManager;
import me.limeglass.epicmines.objects.Mine;
import me.limeglass.epicmines.utils.MessageBuilder;
import me.limeglass.epicmines.utils.ReflectionUtil;
import me.limeglass.epicmines.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class ListCommand extends AdminCommand {

	public ListCommand() {
		super(false, "list", "mines", "l");
	}

	@Override
	protected ReturnType runCommand(String command, CommandSender sender, String... arguments) {
		Player player = (Player) sender;
		List<Mine> sorted = EpicMines.getInstance().getManager(MineManager.class).getAllMines().stream()
				.sorted(Comparator.comparing(Mine::getName))
				.collect(Collectors.toList());
		if (sorted.isEmpty()) {
			new MessageBuilder("mines.no-mines")
					.setPlaceholderObject(player)
					.send(player);
			return ReturnType.FAILURE;
		}
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		int line = 1;
		List<BaseComponent[]> lines = new ArrayList<>();
		for (Mine mine : sorted) {
			lines.add(new ComponentBuilder(new MessageBuilder(false, "commands.list.title")
					.setPlaceholderObject(mine)
					.replace("%spot%", line)
					.get())
							.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/em teleport " + mine.getName()))
							.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(new MessageBuilder(false, "commands.list.hover")
									.setPlaceholderObject(mine)
									.replace("%spot%", line)
									.get()).create()))
							.create());
			line++;
			if (line >= 14)
				bookMeta.spigot().addPage(getPage(lines));
		}
		if (!lines.isEmpty())
			bookMeta.spigot().addPage(getPage(lines));
		bookMeta.setAuthor("EpicMines");
		bookMeta.setTitle("EpicMines");
		book.setItemMeta(bookMeta);
		if (!Utils.methodExists(Player.class, "openBook", ItemStack.class)) { // Versions under 1.14.3
			try {
				int slot = player.getInventory().getHeldItemSlot();
				ItemStack item = player.getInventory().getItem(slot);
				player.getInventory().setItem(slot, book);
				ByteBuf buf = Unpooled.buffer(256);
				buf.setByte(0, (byte)0);
				buf.writerIndex(1);
				Class<?> packet = ReflectionUtil.getNMSClass("PacketPlayOutCustomPayload");
				Class<?> dataSerializerPacket = ReflectionUtil.getNMSClass("PacketDataSerializer");
				Object dataSerializer = dataSerializerPacket.getConstructor(ByteBuf.class).newInstance(buf);
				Object constructor = packet.getConstructor(String.class, dataSerializerPacket).newInstance("MC|BOpen", dataSerializer);
				ReflectionUtil.sendPacket(constructor, player);
				player.getInventory().setItem(slot, item);
			} catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			return ReturnType.SUCCESS;
		}
		player.openBook(book);
		return ReturnType.SUCCESS;
	}

	private BaseComponent[] getPage(List<BaseComponent[]> lines) {
		List<BaseComponent[]> formatted = new ArrayList<>();
		Iterator<BaseComponent[]> iterator = lines.iterator();
		int size = 0;
		while (iterator.hasNext()) {
			BaseComponent[] components = iterator.next();
			components[components.length - 1].addExtra("\n");
			size += components.length;
			formatted.add(components);
			iterator.remove();
		}
		BaseComponent[] page = new BaseComponent[size];
		int spot = 0;
		for (BaseComponent[] components : formatted) {
			for (BaseComponent component : components) {
				page[spot] = component;
				spot++;
			}
		}
		return page;
	}

	@Override
	public String getConfigurationNode() {
		return "list";
	}

	@Override
	public String[] getPermissionNodes() {
		return new String[] {"epicmines.list", "epicmines.admin"};
	}

}
