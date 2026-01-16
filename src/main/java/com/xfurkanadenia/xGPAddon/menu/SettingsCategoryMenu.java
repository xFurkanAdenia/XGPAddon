package com.xfurkanadenia.xGPAddon.menu;

import com.xfurkanadenia.xGPAddon.LanguageLoader;
import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.manager.GUIManager;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import com.xfurkanadenia.xGPAddon.util.Utils;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsCategoryMenu extends FastInv {

    public SettingsCategoryMenu(Player p, NClaim nClaim) {
        super(XGPAddon.getInstance().getGuiManager().guis.get("settingsCategory").getInt("size", 9), Utils.getFormatted(XGPAddon.getInstance().getGuiManager().guis.get("settingsCategory").getString("title", "Settings Category"), p, Utils.getClaimVariables(nClaim)));

        XGPAddon main = XGPAddon.getInstance();
        GUIManager guiManager = main.getGuiManager();
        FileConfiguration gui = guiManager.guis.get("settingsCategory");
        ConfigurationSection itemsSection = gui.getConfigurationSection("items");
        if (itemsSection == null) return;

        itemsSection.getKeys(false).forEach(key -> {
            ConfigurationSection itemCfg = itemsSection.getConfigurationSection(key);
            if (itemCfg == null) return;

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("claim_name", nClaim.getClaimName());
            placeholders.put("claim_owner", nClaim.getClaim().getOwnerName());

            ItemStack item = guiManager.getItem(itemCfg, p, placeholders);

            List<Integer> slots = itemCfg.contains("slots")
                    ? itemCfg.getIntegerList("slots")
                    : List.of(itemCfg.getInt("slot"));

            String type = itemCfg.getString("type", "").toLowerCase();

            slots.forEach(slot -> {
                setItem(slot, item, e -> {
                    switch (type) {
                        case "general_settings" -> {
                            p.closeInventory();
                            guiManager.openGui(p, new GeneralSettingsMenu(p, nClaim));
                            guiManager.executeActions(p, "settingsCategory", e.getRawSlot(), Map.of());
                        }
                        case "player_settings" -> {
                            p.closeInventory();
                            guiManager.openGui(p, new PlayerSettingsMenu(p, nClaim));
                            guiManager.executeActions(p, "settingsCategory", e.getRawSlot(), Map.of());
                        }
                        case "back" -> {
                            p.closeInventory();
                            guiManager.openGui(p, new ClaimMenu(p, nClaim));
                            guiManager.executeActions(p, "settingsCategory", e.getRawSlot(), Map.of());
                        }
                    }

                    // action çalıştırma
                    if (itemCfg.contains("actions")) {
                        guiManager.executeActions(p, "settingsCategory", e.getRawSlot(), placeholders);
                    }
                });
            });
        });
    }
}
