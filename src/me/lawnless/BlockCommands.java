package me.lawnless;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockCommands extends JavaPlugin implements Listener {
	
	FileConfiguration config = this.getConfig();
	
	@Override
	public void onEnable() {
		getConfig().options().copyDefaults(true);
		saveConfig();
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("Plugin enabled.");
	}
	
	@Override
	public void onDisable() {
		getLogger().info("Plugin disabled.");
	}
	
	@EventHandler
	public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.getPlayer().hasPermission("bclist.bypass")) return;
		List<String> allowedCommands = getConfig().getStringList("allowed-commands");
		List<String> blockedCommands = getConfig().getStringList("blocked-commands");
		if (blockedCommands.contains(event.getMessage().replace("/", ""))) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.commandBlocked")));
		}
		if (!allowedCommands.contains(event.getMessage().replace("/", ""))) {
			event.setCancelled(true);
			StringBuilder sb = new StringBuilder();
			for (String allowedCommand : allowedCommands) {
				if (allowedCommand == allowedCommands.get(allowedCommands.size() - 1)) {
					sb.append("/"+allowedCommand+"");
				} else {
					sb.append("/"+allowedCommand+", ");
				}
			}
			event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.usableCommands").replace("{commands}", sb.toString())));
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player p = (Player) sender;
		if (command.getName().equalsIgnoreCase("bclist")) {
			if (sender instanceof Player) {
				if (!p.hasPermission("bclist.bclist")) { p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.noPermission"))); return true; }
				if (args.length == 0) {
					List<String> usageMessages = getConfig().getStringList("messages.usage");
					for (String msg : usageMessages) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
					}
				} else {
					if (args[0].equalsIgnoreCase("add")) {
						if (args.length == 1) {
							List<String> usageMessages = getConfig().getStringList("messages.usage");
							for (String msg : usageMessages) {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
							}
							return true;
						}
						if (!p.hasPermission("bclist.add")) { p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.noPermission"))); return true; }
						args[1] = args[1].replace("/", "");
						List<String> commandsList = getConfig().getStringList("blocked-commands");
						commandsList.add(args[1]);
						getConfig().set("blocked-commands", commandsList);
						saveConfig();
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.commandAdded").replace("{command}", "/"+args[1])));
					} else if (args[0].equalsIgnoreCase("remove")) {
						if (args.length == 1) {
							List<String> usageMessages = getConfig().getStringList("messages.usage");
							for (String msg : usageMessages) {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
							}
							return true;
						}
						if (!p.hasPermission("bclist.remove")) { p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.noPermission"))); return true; }
						args[1] = args[1].replace("/", "");
						List<String> commandsList = getConfig().getStringList("blocked-commands");
						if (!commandsList.contains(args[1])) { p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.commandNotFound").replace("{command}", "/"+args[1]))); return true; }
						commandsList.remove(args[1]);
						getConfig().set("blocked-commands", commandsList);
						saveConfig();
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.commandRemoved").replace("{command}", "/"+args[1])));
					} else if (args[0].equalsIgnoreCase("see")) {
						if (!p.hasPermission("bclist.see")) { p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.noPermission"))); return true; }
						List<String> commandsList = getConfig().getStringList("blocked-commands");
						StringBuilder sb = new StringBuilder();
						for (String blockedCommand : commandsList) {
							if (blockedCommand == commandsList.get(commandsList.size() - 1)) {
								sb.append("/"+blockedCommand);
							} else {
								sb.append("/"+blockedCommand+", ");
							}
						}
						p.sendMessage("§8[§6"+sb.toString()+"§8]");
					} else if (args[0].equalsIgnoreCase("reload")) {
						if (!p.hasPermission("bclist.reload")) { p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.noPermission"))); return true; }
						reloadConfig();
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.configReloaded")));
					} else {
						List<String> usageMessages = getConfig().getStringList("messages.usage");
						for (String msg : usageMessages) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
						}
					}
				}
			} else {
				getLogger().info(getConfig().getString("messages.noConsoleAllowed"));
				// sender.sendMessage();
			}
		}
		return true;
	}
	
	@Override
  	public void reloadConfig() {
		super.reloadConfig();

		saveDefaultConfig();
		config = getConfig();
		config.options().copyDefaults(true);
		saveConfig();
	}

}
