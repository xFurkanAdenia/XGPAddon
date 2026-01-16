package com.xfurkanadenia.xGPAddon.menu;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.manager.GUIManager;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import com.xfurkanadenia.xGPAddon.util.Utils;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BanMenu extends FastInv {

    private final int page;
    private int totalPages = 1; // banned yoksa en az 1 sayfa

    public BanMenu(Player p, NClaim claim) {
        this(p, claim, 0);
    }

    public BanMenu(Player p, NClaim claim, int page) {
        super(XGPAddon.getInstance().getGuiManager().guis.get("banMenu").getInt("size", 9), Utils.getFormatted(
                XGPAddon.getInstance().getGuiManager().guis.get("banMenu").getString("title", "Chest"),
                p,
                getPlaceholders(Map.of("page", String.valueOf(page + 1)), claim)
        ));
        this.page = page;

        XGPAddon main = XGPAddon.getInstance();
        GUIManager guiManager = main.getGuiManager();
        FileConfiguration gui = guiManager.guis.get("banMenu");
        ConfigurationSection guiItems = gui.getConfigurationSection("items");

        if (guiItems == null) return;

        // Banlı oyuncular listesi
        List<OfflinePlayer> bannedPlayers = claim.getClaimSettings().getBannedPlayers()
                .stream()
                .map(uuid -> p.getServer().getOfflinePlayer(uuid))
                .toList();

        // Önce claims (burada banned listesi) slot sayısını bulalım
        int bannedSlotsCount;
        if (guiItems.contains("bannedPlayers")) {
            ConfigurationSection bannedCfg = guiItems.getConfigurationSection("bannedPlayers");
            if (bannedCfg != null) {
                List<Integer> slots = bannedCfg.getIntegerList("slots");
                if (slots.isEmpty() && bannedCfg.contains("slot")) {
                    slots = List.of(bannedCfg.getInt("slot"));
                }
                bannedSlotsCount = slots.size();
            } else {
                bannedSlotsCount = 0;
            }
        } else {
            bannedSlotsCount = 0;
        }

        if (bannedSlotsCount > 0) {
            totalPages = (int) Math.ceil((double) bannedPlayers.size() / bannedSlotsCount);
            if (totalPages == 0) totalPages = 1;
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
                case "bannedplayers" -> {
                    int start = page * bannedSlotsCount;
                    int end = Math.min(start + bannedSlotsCount, bannedPlayers.size());
                    AtomicInteger slotIndex = new AtomicInteger(0);

                    for (int i = start; i < end; i++) {
                        OfflinePlayer banned = bannedPlayers.get(i);
                        placeholders.put("player_name", banned.getName());
                        placeholders.put("player_uuid", banned.getUniqueId().toString());

                        int slot = slots.get(slotIndex.getAndIncrement());
                        setItem(slot, Utils.getFormattedItem(item, p, placeholders), (e) -> {
                            p.closeInventory();
                            claim.getClaimSettings().getBannedPlayers().remove(banned.getUniqueId());
                            guiManager.openGui(p, new BanMenu(p, claim));
                            guiManager.executeActions(p, "banMenu", e.getRawSlot(), Map.of());
                        });
                    }
                }
                case "previous_page" -> {
                    if (page > 0) {
                        setItem(slots.get(0), Utils.getFormattedItem(item, p, placeholders),
                                e -> {
                                    p.closeInventory();
                                    guiManager.openGui(p, new BanMenu(p, claim, page - 1));
                                    guiManager.executeActions(p, "banMenu", e.getRawSlot(), Map.of());
                                });
                    }
                }
                case "next_page" -> {
                    if (page < totalPages - 1) {
                        setItem(slots.get(0), Utils.getFormattedItem(item, p, placeholders),
                                e -> {
                                    guiManager.openGui(p, new BanMenu(p, claim, page + 1));
                                    guiManager.executeActions(p, "banMenu", e.getRawSlot(), Map.of());
                                });
                    }
                }

                case "back" -> {
                    slots.forEach(slot -> setItem(slot, Utils.getFormattedItem(item, p, placeholders), (v) -> {
                        p.closeInventory();
                        guiManager.openGui(p, new PlayerSettingsMenu(p, claim));
                        guiManager.executeActions(p, "banMenu", v.getRawSlot(), Map.of());

                    }));
                }
                case "ban" -> {
                    slots.forEach(slot -> setItem(slot, Utils.getFormattedItem(item, p, placeholders), (v) -> {
                        p.closeInventory();
                        main.getDataManager().getBanChat().put(p, claim);
                        XGPAddon.getInstance().getLanguageLoader().getList("ban-menu-ban-chat").forEach(p::sendMessage);
                    }));
                }
                default -> {
                    slots.forEach(slot -> setItem(slot, Utils.getFormattedItem(item, p, placeholders), (v) -> {
                        if (itemCfg.contains("actions")) {
                            guiManager.executeActions(p, "banMenu", v.getRawSlot(), Map.of());
                        }
                    }));
                }
            }
        });
    }

    public static Map<String, String> getPlaceholders(Map<String, String> placeholders, NClaim claim) {
        HashMap<String, String> placeholderMap = new HashMap<>(placeholders);
        placeholderMap.putAll(Utils.getClaimVariables(claim));
        return placeholderMap;
    }
}
