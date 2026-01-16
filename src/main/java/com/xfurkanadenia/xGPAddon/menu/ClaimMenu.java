package com.xfurkanadenia.xGPAddon.menu;

import com.xfurkanadenia.xGPAddon.LanguageLoader;
import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.manager.GUIManager;
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

public class ClaimMenu extends FastInv {

    public ClaimMenu(Player p, NClaim nClaim) {
        super(XGPAddon.getInstance().getGuiManager().guis.get("claimMenu").getInt("size", 9), Utils.getFormatted(
                XGPAddon.getInstance().getGuiManager().guis.get("claimMenu")
                        .getString("title", "Claim Menu"),
                p, Utils.getClaimVariables(nClaim))
        );

        XGPAddon main = XGPAddon.getInstance();
        GUIManager guiManager = main.getGuiManager();
        FileConfiguration gui = guiManager.guis.get("claimMenu");
        ConfigurationSection guiItems = gui.getConfigurationSection("items");
        LanguageLoader languageLoader = main.getLanguageLoader();

        if (guiItems == null) return;

        guiItems.getKeys(false).forEach(key -> {
            ConfigurationSection itemCfg = guiItems.getConfigurationSection(key);
            if (itemCfg == null) return;

            // Placeholder’lar
            Map<String, String> placeholders = Utils.getClaimVariables(nClaim);


            // Item oluştur
            ItemStack item = guiManager.getItem(itemCfg, p, placeholders);

            // Slotlar
            List<Integer> slots = itemCfg.contains("slots")
                    ? itemCfg.getIntegerList("slots")
                    : List.of(itemCfg.getInt("slot"));

            // Actionlar
            slots.forEach(slot -> {
                setItem(slot, Utils.getFormattedItem(item, p, placeholders), e -> {
                    if (itemCfg.contains("actions")) {
                        guiManager.executeActions(p, "claimMenu", e.getRawSlot(), placeholders);
                    }

                    // Özel buton davranışları
                    String type = itemCfg.getString("type", "").toLowerCase();
                    switch (type) {
                        case "back" -> {
                            p.closeInventory();
                            guiManager.openGui(p, new MenuMain(p));
                        }
                        case "rename" -> {
                            p.closeInventory();
                            main.getDataManager().getRenameChat().put(p, nClaim);
                            XGPAddon.getInstance().getLanguageLoader().getList("claim-rename-chat").forEach(p::sendMessage);
                        }
                        case "teleport" -> {
                            if(!nClaim.getClaimSettings().getTrustedSettings().containsKey(p.getUniqueId().toString()) && !nClaim.getOwner().equals(p.getUniqueId())) return;
                            p.closeInventory();
                            p.teleport(nClaim.getLocation());
                        }
                        case "extend_time" -> {
                            if(!nClaim.getClaimSettings().getTrustedSettings().keySet().contains(p.getUniqueId().toString()) && nClaim.getOwner().equals(p.getUniqueId())) return;
                            p.closeInventory();
                            main.getDataManager().getPlayerChat().remove(p);
                            main.getDataManager().getPlayerChat().put(p, nClaim);
                            p.sendTitle(languageLoader.get("title-claim-extend-time"),
                                    languageLoader.get("subtitle-claim-extend-time"));
                            languageLoader.getList("claim-extend-time-chat", nClaim).forEach(p::sendMessage);
                        }
                        case "settings" -> {
                            if (!nClaim.getOwner().equals(p.getUniqueId())) return;
                            guiManager.openGui(p, new SettingsCategoryMenu(p, nClaim));
                            guiManager.executeActions(p, "claimMenu", e.getRawSlot(), Map.of());
                        }
                        case "abandon" -> {
                            if (!nClaim.getOwner().equals(p.getUniqueId())) return;
                            guiManager.openGui(p, new AbandonConfirmMenu(p, nClaim));
                            guiManager.executeActions(p, "claimMenu", e.getRawSlot(), placeholders);
                        }
                    }
                });
            });
        });
    }
}
