package com.xfurkanadenia.xGPAddon.menu;

import com.xfurkanadenia.xGPAddon.LanguageLoader;
import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.manager.GUIManager;
import com.xfurkanadenia.xGPAddon.model.ClaimTrustedSettings;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import com.xfurkanadenia.xGPAddon.util.Utils;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TrustedSettingsMenu extends FastInv {

    public TrustedSettingsMenu(UUID target, Player p, NClaim nClaim) {
        super(XGPAddon.getInstance().getGuiManager().guis.get("trustedSettingsMenu").getInt("size", 9), Utils.getFormatted(
                XGPAddon.getInstance().getGuiManager().guis.get("trustedSettingsMenu")
                        .getString("title", "Trusted Settings"),
                p, getPlaceholders(target, nClaim))
        );

        XGPAddon main = XGPAddon.getInstance();
        GUIManager guiManager = main.getGuiManager();
        FileConfiguration gui = guiManager.guis.get("trustedSettingsMenu");
        ConfigurationSection guiItems = gui.getConfigurationSection("items");
        LanguageLoader languageLoader = main.getLanguageLoader();

        if (guiItems == null) return;

        ClaimTrustedSettings settings = nClaim.getClaimSettings().getTrustedSettings(target);
        if (settings == null) return;

        // Placeholderlar
        Map<String, String> placeholders = getPlaceholders(target, nClaim);
        placeholders.put("player_allowBlockBreak", languageLoader.getBoolean(settings.getAllowBlockBreak()));
        placeholders.put("player_allowBlockPlace", languageLoader.getBoolean(settings.getAllowBlockPlace()));
        placeholders.put("player_allowSpawnerBreak", languageLoader.getBoolean(settings.getAllowSpawnerBreak()));
        placeholders.put("player_allowChestAccess", languageLoader.getBoolean(settings.getAllowChestAccess()));
        placeholders.put("player_allowDoorOpen", languageLoader.getBoolean(settings.getAllowDoorOpen()));
        placeholders.put("player_allowTrapDoorOpen", languageLoader.getBoolean(settings.getAllowTrapDoorOpen()));

        guiItems.getKeys(false).forEach(key -> {
            ConfigurationSection itemCfg = guiItems.getConfigurationSection(key);
            if (itemCfg == null) return;

            // Item oluştur
            ItemStack item = guiManager.getItem(itemCfg, p, placeholders);

            // Slotlar
            List<Integer> slots = itemCfg.contains("slots")
                    ? itemCfg.getIntegerList("slots")
                    : List.of(itemCfg.getInt("slot"));

            slots.forEach(slot -> {
                setItem(slot, Utils.getFormattedItem(item, p, placeholders), e -> {
                    String type = itemCfg.getString("type", "").toLowerCase();
                    switch (type) {
                        case "back" -> {
                            p.closeInventory();
                            new PlayerSettingsMenu(p, nClaim).open(p);
                        }
                        case "toggle_block_break" -> {
                            settings.setAllowBlockBreak(!settings.getAllowBlockBreak());
                            guiManager.openGui(p, new TrustedSettingsMenu(target, p, nClaim));
                            guiManager.executeActions(p, "settingsCategory", e.getRawSlot(), Map.of());
                        }
                        case "toggle_block_place" -> {
                            settings.setAllowBlockPlace(!settings.getAllowBlockPlace());
                            guiManager.openGui(p, new TrustedSettingsMenu(target, p, nClaim));
                            guiManager.executeActions(p, "settingsCategory", e.getRawSlot(), Map.of());
                        }
                        case "toggle_spawner_break" -> {
                            settings.setAllowSpawnerBreak(!settings.getAllowSpawnerBreak());
                            guiManager.openGui(p, new TrustedSettingsMenu(target, p, nClaim));
                            guiManager.executeActions(p, "settingsCategory", e.getRawSlot(), Map.of());
                        }
                        case "toggle_door_open" -> {
                            settings.setAllowDoorOpen(!settings.getAllowDoorOpen());
                            guiManager.openGui(p, new TrustedSettingsMenu(target, p, nClaim));
                            guiManager.executeActions(p, "settingsCategory", e.getRawSlot(), Map.of());
                        }
                        case "toggle_trap_door_open" -> {
                            settings.setAllowTrapDoorOpen(!settings.getAllowTrapDoorOpen());
                            guiManager.openGui(p, new TrustedSettingsMenu(target, p, nClaim));
                            guiManager.executeActions(p, "settingsCategory", e.getRawSlot(), Map.of());
                        }
                        case "toggle_chest_access" -> {
                            settings.setAllowChestAccess(!settings.getAllowChestAccess());
                            guiManager.openGui(p, new TrustedSettingsMenu(target, p, nClaim));
                            guiManager.executeActions(p, "settingsCategory", e.getRawSlot(), Map.of());
                        }
                    }
                });
            });
        });
    }

    private static Map<String, String> getPlaceholders(UUID target, NClaim nClaim) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("claim_name", nClaim.getClaimName());
        placeholders.put("claim_id", nClaim.getClaimId());
        placeholders.put("claim_owner", nClaim.getClaim().getOwnerName());
        placeholders.put("claim_owner_uuid", nClaim.getOwner().toString());
        placeholders.put("player", Bukkit.getOfflinePlayer(target).getName());
        placeholders.put("trusted", Bukkit.getOfflinePlayer(target).getName());
        return placeholders;
    }
}
