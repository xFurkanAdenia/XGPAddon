package com.xfurkanadenia.xGPAddon.menu;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.manager.GUIManager;
import com.xfurkanadenia.xGPAddon.model.ClaimSettings;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import com.xfurkanadenia.xGPAddon.util.Utils;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneralSettingsMenu extends FastInv {

    public GeneralSettingsMenu(Player p, NClaim nClaim) {
        super(
                XGPAddon.getInstance().getGuiManager().guis.get("generalSettingsMenu").getInt("size", 9),
                Utils.getFormatted(
                        XGPAddon.getInstance().getGuiManager().guis.get("generalSettingsMenu").getString("title", "Chest"),
                        p,
                        getPlaceholders(Map.of("%claim%", nClaim.getClaimName()), nClaim)
                )
        );

        XGPAddon main = XGPAddon.getInstance();
        GUIManager guiManager = main.getGuiManager();
        FileConfiguration gui = guiManager.guis.get("generalSettingsMenu");
        ConfigurationSection guiItems = gui.getConfigurationSection("items");

        if (guiItems == null) return;

        ClaimSettings claimSettings = nClaim.getClaimSettings();

        guiItems.getKeys(false).forEach(key -> {
            ConfigurationSection itemCfg = guiItems.getConfigurationSection(key);
            if (itemCfg == null) return;

            String type = itemCfg.getString("type", "").toLowerCase();
            Map<String, String> placeholders = new HashMap<>(Utils.getClaimVariables(nClaim));
            ItemStack item = guiManager.getItem(itemCfg, p);

            // slot listesi
            List<Integer> slots = itemCfg.contains("slots")
                    ? itemCfg.getIntegerList("slots")
                    : List.of(itemCfg.getInt("slot"));

            slots.forEach(slot -> setItem(slot, Utils.getFormattedItem(item, p, placeholders), e -> {
                if (itemCfg.getBoolean("block-when-occupied", false) && !main.getDataManager().getPlayersInClaim(nClaim).stream().filter(v -> !nClaim.getOwner().equals(v.getUniqueId())).toList().isEmpty()) {
                    p.sendMessage(main.getLanguageLoader().getWithPlaceholders("cant-use-setting-when-occupied", Utils.getClaimVariablesPercent(nClaim)));
                    return;
                }
                switch (type) {
                    case "animal_spawning" -> {
                        if (!main.getConfig().getStringList("settings.disabled-settings").contains("animalSpawning")) {
                            claimSettings.setSpawnAnimals(!claimSettings.isSpawnAnimals());
                            guiManager.openGui(p, new GeneralSettingsMenu(p, nClaim));
                            guiManager.executeActions(p, "generalSettingsMenu", e.getRawSlot(), Map.of());
                        }
                    }
                    case "monster_spawning" -> {
                        if (!main.getConfig().getStringList("settings.disabled-settings").contains("monsterSpawning")) {
                            claimSettings.setSpawnMonsters(!claimSettings.isSpawnMonsters());
                            guiManager.openGui(p, new GeneralSettingsMenu(p, nClaim));
                        }
                    }
                    case "fire_spread" -> {
                        if (!main.getConfig().getStringList("settings.disabled-settings").contains("fire")) {
                            claimSettings.setFireSpread(!claimSettings.isFireSpread());
                            guiManager.openGui(p, new GeneralSettingsMenu(p, nClaim));
                            guiManager.executeActions(p, "generalSettingsMenu", e.getRawSlot(), Map.of());
                        }
                    }
                    case "allow_pvp" -> {
                        if (!main.getConfig().getStringList("settings.disabled-settings").contains("allowPvp")) {
                            claimSettings.setAllowPvP(!claimSettings.isAllowPvP());
                            guiManager.openGui(p, new GeneralSettingsMenu(p, nClaim));
                            guiManager.executeActions(p, "generalSettingsMenu", e.getRawSlot(), Map.of());
                        }
                    }
                    case "back" -> {
                        p.closeInventory();
                        guiManager.openGui(p, new SettingsCategoryMenu(p, nClaim));
                        guiManager.executeActions(p, "generalSettingsMenu", e.getRawSlot(), Map.of());

                    }


                }
            }));
        });
    }

    public static Map<String, String> getPlaceholders(Map<String, String> placeholders, NClaim claim) {
        HashMap<String, String> placeholderMap = new HashMap<>(placeholders);
        placeholderMap.putAll(Utils.getClaimVariables(claim));
        return placeholderMap;
    }
}
