package com.xfurkanadenia.xGPAddon.menu;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.manager.GUIManager;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import com.xfurkanadenia.xGPAddon.util.Utils;
import fr.mrmicky.fastinv.FastInv;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlayerSettingsMenu extends FastInv {

    private static final String GUI_NAME = "playerSettingsMenu";
    private final XGPAddon main = XGPAddon.getInstance();
    private final GUIManager guiManager = main.getGuiManager();

    private final List<String> trustedPlayers;
    private final int page;
    private final NClaim nClaim;
    private final Player p;
    private int maxPage = 1;

    public PlayerSettingsMenu(Player p, NClaim nClaim) {
        this(p, nClaim, 0);
    }

    public PlayerSettingsMenu(Player p, NClaim nClaim, int page) {
        super(XGPAddon.getInstance().getGuiManager().guis.get(GUI_NAME).getInt("size", 9),
                Utils.getFormatted(XGPAddon.getInstance().getGuiManager().guis.get(GUI_NAME).getString("title", "Player Settings"),  p, Utils.getClaimVariables(nClaim)));
        this.p = p;
        this.nClaim = nClaim;
        this.page = page;

        trustedPlayers = new ArrayList<>();
        for (OfflinePlayer players : Bukkit.getOfflinePlayers()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(players.getUniqueId());
            if (nClaim.getClaim().hasExplicitPermission(player.getUniqueId(), ClaimPermission.Build)) {
                if (nClaim.getClaim().getOwnerName().equalsIgnoreCase(player.getName())) continue;
                trustedPlayers.add(player.getName());
            }
        }

        setupItems();
    }

    private void setupItems() {
        var gui = guiManager.guis.get(GUI_NAME);
        var itemsSection = gui.getConfigurationSection("items");
        if (itemsSection == null) return;

        // item type’larına göre işlem yap
        itemsSection.getKeys(false).forEach(key -> {
            var itemCfg = itemsSection.getConfigurationSection(key);
            if (itemCfg == null) return;

            String type = itemCfg.getString("type", "").toLowerCase();

            switch (type) {
                case "trusted-template" -> {
                    List<Integer> slots = itemCfg.getIntegerList("slots");
                    if (slots.isEmpty()) return;

                    // max sayfa hesapla (0 tabanlı index)
                    maxPage = Math.max(0, (int) Math.ceil((double) trustedPlayers.size() / slots.size()) - 1);

                    int startIndex = page * slots.size();
                    if (startIndex >= trustedPlayers.size()) return;

                    int endIndex = Math.min(startIndex + slots.size(), trustedPlayers.size());
                    List<String> pagePlayers = trustedPlayers.subList(startIndex, endIndex);

                    for (int i = 0; i < pagePlayers.size(); i++) {
                        String trustedPlayer = pagePlayers.get(i);
                        OfflinePlayer op = Bukkit.getOfflinePlayer(trustedPlayer);

                        Map<String, String> placeholders = Utils.getClaimVariables(nClaim);
                        placeholders.put("player", op.getName());
                        placeholders.put("trusted", trustedPlayer);

                        ItemStack item = guiManager.getItem(itemCfg, p, placeholders);
                        int slot = slots.get(i);

                        setItem(slot, item, e -> {
                            p.closeInventory();
                            guiManager.openGui(p, new TrustedSettingsMenu(op.getUniqueId(), p, nClaim));
                        });
                    }
                }
                case "previouspage" -> {
                    if (page > 0) {
                        ItemStack prevItem = guiManager.getItem(itemCfg, p);
                        setItem(itemCfg.getInt("slot"), prevItem,
                                e -> {
                                    p.closeInventory();
                                    guiManager.openGui(p, new PlayerSettingsMenu(p, nClaim, page - 1));
                                    guiManager.executeActions(p, "generalSettingsMenu", e.getRawSlot(), Map.of());
                                });
                    }
                }
                case "nextpage" -> {
                    if (page < maxPage) {
                        ItemStack nextItem = guiManager.getItem(itemCfg, p);
                        setItem(itemCfg.getInt("slot"), nextItem,
                                e -> guiManager.openGui(p, new PlayerSettingsMenu(p, nClaim, page + 1)));
                    }
                }
                case "ban" -> {
                    p.closeInventory();
                    ItemStack item = guiManager.getItem(itemCfg, p, Map.of("%claim%", nClaim.getClaimName()));
                    List<Integer> slots = itemCfg.contains("slots")
                            ? itemCfg.getIntegerList("slots")
                            : List.of(itemCfg.getInt("slot"));
                    slots.forEach(slot ->
                            setItem(slot, item, e -> {
                                p.closeInventory();
                                guiManager.openGui(p, new BanMenu(p, nClaim));
                            })
                    );
                }
                case "back" -> {
                    p.closeInventory();
                    ItemStack item = guiManager.getItem(itemCfg, p, Map.of("%claim%", nClaim.getClaimName()));
                    List<Integer> slots = itemCfg.contains("slots")
                            ? itemCfg.getIntegerList("slots")
                            : List.of(itemCfg.getInt("slot"));
                    slots.forEach(slot ->
                            setItem(slot, item, e -> {
                                p.closeInventory();
                                guiManager.openGui(p, new SettingsCategoryMenu(p, nClaim));
                            })
                    );
                }
                default -> {
                    // Normal statik item
                    ItemStack item = guiManager.getItem(itemCfg, p, Map.of("%claim%", nClaim.getClaimName()));
                    List<Integer> slots = itemCfg.contains("slots")
                            ? itemCfg.getIntegerList("slots")
                            : List.of(itemCfg.getInt("slot"));
                    slots.forEach(slot ->
                            setItem(slot, item, e -> {
                                if (itemCfg.contains("actions")) {
                                    guiManager.executeActions(p, GUI_NAME, e.getRawSlot(),
                                            Map.of("%claim%", nClaim.getClaimName()));
                                }
                            })
                    );
                }
            }
        });
    }
}
