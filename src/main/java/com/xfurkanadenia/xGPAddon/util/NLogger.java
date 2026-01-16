package com.xfurkanadenia.xGPAddon.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class NLogger {

    public static void info(String message) {
        Bukkit.getConsoleSender().sendMessage("§7[§fXGPAddon§7] " + ChatColor.GREEN + message);
    }

    public static void warn(String message) {
        Bukkit.getConsoleSender().sendMessage("§7[§fXGPAddon§7] " + ChatColor.YELLOW + message);
    }

    public static void error(String message) {
        Bukkit.getConsoleSender().sendMessage("§7[§fXGPAddon§7] " + ChatColor.RED + message);
    }

}