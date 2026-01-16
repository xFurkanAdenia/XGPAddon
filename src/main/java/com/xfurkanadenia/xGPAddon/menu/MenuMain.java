package com.xfurkanadenia.xGPAddon.menu;

import com.xfurkanadenia.xGPAddon.LanguageLoader;
import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.manager.GUIManager;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import com.xfurkanadenia.xGPAddon.util.Utils;
import fr.mrmicky.fastinv.FastInv;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MenuMain extends FastInv {

    private final int page;
    private int totalPages = 1; 

    public MenuMain(Player p) {
        this(p, 0);
    }

    public MenuMain(Player p, int page) {
        super(XGPAddon.getInstance().getGuiManager().guis.get("mainMenu").getInt("size", 9), Utils.getFormatted(
                XGPAddon.getInstance().getGuiManager().guis.get("mainMenu").getString("title", "Chest"),
                p,
                Map.of("page", String.valueOf(page + 1))
        ));
        this.page = page;

        XGPAddon main = XGPAddon.getInstance();
        GUIManager guiManager = main.getGuiManager();
        FileConfiguration gui = guiManager.guis.get("mainMenu");
        ConfigurationSection guiItems = gui.getConfigurationSection("items");

        if (guiItems == null) return;

        
        List<NClaim> claimsList = new ArrayList<>();
        for (NClaim nClaim : main.getDataManager().getClaims()) {
            if(!nClaim.getOwner().equals(p.getUniqueId()) && !nClaim.getClaim().hasExplicitPermission(p.getUniqueId(), ClaimPermission.Build)) continue;
            claimsList.add(nClaim);
        }

        
        int claimsSlotsCount;
        if (guiItems.contains("claims")) {
            ConfigurationSection claimsItemCfg = guiItems.getConfigurationSection("claims");
            if (claimsItemCfg != null) {
                List<Integer> claimSlots = claimsItemCfg.getIntegerList("slots");
                if (claimSlots.isEmpty() && claimsItemCfg.contains("slot")) {
                    claimSlots = List.of(claimsItemCfg.getInt("slot"));
                }
                claimsSlotsCount = claimSlots.size();
            } else {
                claimsSlotsCount = 0;
            }
        } else {
            claimsSlotsCount = 0;
        }

        if (claimsSlotsCount > 0) {
            totalPages = (int) Math.ceil((double) claimsList.size() / claimsSlotsCount);
        }

        guiItems.getKeys(false).forEach(key -> {
            ConfigurationSection itemCfg = guiItems.getConfigurationSection(key);
            if (itemCfg == null) return;

            String type = itemCfg.getString("type", "").toLowerCase();
            Map<String, String> placeholders = new HashMap<>();
            ItemStack item = guiManager.getItem(itemCfg, p);

            
            List<Integer> slots = itemCfg.contains("slots")
                    ? itemCfg.getIntegerList("slots")
                    : List.of(itemCfg.getInt("slot"));

            switch (type) {
                case "claims" -> {
                    int start = page * claimsSlotsCount;
                    int end = Math.min(start + claimsSlotsCount, claimsList.size());
                    AtomicInteger slotIndex = new AtomicInteger(0);

                    for (int i = start; i < end; i++) {
                        NClaim nClaim = claimsList.get(i);
                        placeholders.put("claim_name", nClaim.getClaimName());
                        placeholders.put("claim_id", nClaim.getClaimId());
                        placeholders.put("claim_owner", nClaim.getClaim().getOwnerName());
                        placeholders.put("claim_owner_uuid", nClaim.getOwner().toString());
                        placeholders.put("claim_time", String.valueOf(nClaim.getTime()));
                        placeholders.put("claim_time_formatted", LanguageLoader.formatTime(nClaim.getTime()));

                        int slot = slots.get(slotIndex.getAndIncrement());
                        setItem(slot, Utils.getFormattedItem(item, p, placeholders), (e) -> {
                            p.closeInventory();
                            if(!nClaim.getClaimSettings().getTrustedSettings().keySet().contains(p.getUniqueId().toString()) && !nClaim.getOwner().equals(p.getUniqueId())) return;
                            new ClaimMenu(p, nClaim).open(p);
                            guiManager.executeActions(p, "mainMenu", e.getRawSlot(), Map.of());
                        });
                    }
                }
                case "previous_page" -> {
                    if (page > 0) {
                        setItem(slots.get(0), Utils.getFormattedItem(item, p, placeholders),
                                e -> {
                                    guiManager.openGui(p, new MenuMain(p, page - 1));
                                    guiManager.executeActions(p, "mainMenu", e.getRawSlot(), Map.of());
                                });
                    }
                }
                case "next_page" -> {
                    if (page < totalPages - 1) {
                        setItem(slots.get(0), Utils.getFormattedItem(item, p, placeholders),
                                e -> {
                                    guiManager.openGui(p, new MenuMain(p, page + 1));
                                    guiManager.executeActions(p, "mainMenu", e.getRawSlot(), Map.of());
                                });
                    }
                }
                default -> {
                    
                    slots.forEach(slot -> setItem(slot, Utils.getFormattedItem(item, p, placeholders), (v) -> {
                        if (itemCfg.contains("actions")) {
                            guiManager.executeActions(p, "mainMenu", v.getRawSlot(), Map.of());
                        }
                    }));
                }
            }
        });
    }
}
