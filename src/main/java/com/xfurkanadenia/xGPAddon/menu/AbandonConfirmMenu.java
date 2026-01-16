package com.xfurkanadenia.xGPAddon.menu;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.manager.GUIManager;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import com.xfurkanadenia.xGPAddon.util.Utils;
import fr.mrmicky.fastinv.FastInv;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbandonConfirmMenu extends FastInv {

    private final NClaim claim;

    public AbandonConfirmMenu(Player p, NClaim claim) {
        super(XGPAddon.getInstance().getGuiManager().guis.get("abandonConfirmMenu").getInt("size", 9), Utils.getFormatted(XGPAddon.getInstance().getGuiManager().guis.get("abandonConfirmMenu").getString("title", "Abandon Confirm Menu"), p, Utils.getClaimVariables(claim)));
        this.claim = claim;

        XGPAddon main = XGPAddon.getInstance();
        GUIManager guiManager = main.getGuiManager();

        FileConfiguration gui = guiManager.guis.get("abandonConfirmMenu");
        ConfigurationSection itemsSection = gui.getConfigurationSection("items");
        if (itemsSection == null) return;

        itemsSection.getKeys(false).forEach(key -> {
            ConfigurationSection itemCfg = itemsSection.getConfigurationSection(key);
            if (itemCfg == null) return;

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("claim_name", claim.getClaimName());
            placeholders.put("claim_owner", claim.getClaim().getOwnerName());

            ItemStack item = guiManager.getItem(itemCfg, p, placeholders);

            List<Integer> slots = itemCfg.contains("slots")
                    ? itemCfg.getIntegerList("slots")
                    : List.of(itemCfg.getInt("slot"));

            String type = itemCfg.getString("type", "").toLowerCase();

            slots.forEach(slot -> setItem(slot, item, e -> {
                switch (type) {
                    case "confirm" -> {
                        GriefPrevention.instance.dataStore.deleteClaim(claim.getClaim());
                        p.closeInventory();
                        guiManager.openGui(p, new MenuMain(p));
                    }
                    case "cancel" -> {
                        p.closeInventory();
                        guiManager.openGui(p, new ClaimMenu(p, claim));
                        guiManager.executeActions(p, "abandonConfirmMenu", e.getRawSlot(), Map.of());
                    }
                }

                if (itemCfg.contains("actions")) {
                    guiManager.executeActions(p, "abandonConfirmMenu", e.getRawSlot(), placeholders);
                }
            }));
        });
    }
}
