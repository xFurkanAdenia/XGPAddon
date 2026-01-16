package com.xfurkanadenia.xGPAddon.model;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.manager.GUIManager;
import com.xfurkanadenia.xGPAddon.util.Utils;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class GenericMenu extends FastInv {

    public GenericMenu(String fileName, Player player) {
        super(getSize(fileName), getTitle(fileName, player));

        XGPAddon plugin = XGPAddon.getInstance();
        GUIManager guiManager = plugin.getGuiManager();
        FileConfiguration config = guiManager.guis.get(fileName);

        if (config == null) {
            plugin.getLogger().severe(fileName + ".yml bulunamadı!");
            return;
        }

        // items kısmını oku
        if (config.isConfigurationSection("items")) {
            config.getConfigurationSection("items").getKeys(false).forEach(key -> {
                ConfigurationSection itemSec = config.getConfigurationSection("items." + key);
                if (itemSec == null) return;

                Map<String, String> vars = new HashMap<>();
                vars.put("player", player.getName());

                if (itemSec.contains("slot")) {
                    setItem(itemSec.getInt("slot"),
                            guiManager.getItem(itemSec, player, vars),
                            e -> guiManager.executeActions(player, fileName, itemSec.getInt("slot"), Map.of())
                    );
                }
                else if (itemSec.contains("slots")) {
                    itemSec.getIntegerList("slots").forEach(slot ->
                            setItem(slot,
                                    guiManager.getItem(itemSec, player, vars),
                                    e -> guiManager.executeActions(player, fileName, slot, Map.of())
                            )
                    );
                }
                else {
                    plugin.getLogger().warning("Item '" + key + "' için slot veya slots belirtilmemiş!");
                }
            });
        }
    }

    private static String getTitle(String fileName, Player player) {
        FileConfiguration config = XGPAddon.getInstance().getGuiManager().guis.get(fileName);
        return Utils.getFormatted(config.getString("menu-title", "&cMenu"), player, new HashMap<>());
    }

    private static int getSize(String fileName) {
        FileConfiguration config = XGPAddon.getInstance().getGuiManager().guis.get(fileName);
        return config.getInt("menu-size", 54);
    }
}
