package com.xfurkanadenia.xGPAddon.task;

import com.xfurkanadenia.xGPAddon.LanguageLoader;
import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.manager.DataManager;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import com.xfurkanadenia.xGPAddon.util.Utils;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class TaskClaimEnter extends BukkitRunnable {
    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            XGPAddon main = XGPAddon.getInstance();
            LanguageLoader languageLoader = main.getLanguageLoader();
            DataManager dataManager = main.getDataManager();

            Claim currentClaim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), false, null);
            NClaim previousNClaim = dataManager.getClaimEntries().get(player);

            if (currentClaim == null && previousNClaim != null) {
                // Claim'den çıktı - Unowned area
                Map<String, String> map = Map.of(
                        "%pvp_enabled%", languageLoader.getBoolean(true),
                        "%claim_name%", languageLoader.get("unowned-claim")
                );
                sendTitleMessages(player, languageLoader, "claim-enter-pvp-enabled", map);
                dataManager.getClaimEntries().remove(player);
            }
            else if (currentClaim != null && (previousNClaim == null || !currentClaim.equals(previousNClaim.getClaim()))) {
                // Yeni claim'e girdi
                NClaim currentNClaim = dataManager.getClaim(currentClaim);
                if (currentNClaim != null) {
                    boolean isPvPEnabled = currentNClaim.getClaimSettings().isAllowPvP();
                    String pvpType = isPvPEnabled ? "claim-enter-pvp-enabled" : "claim-enter-pvp-disabled";
                    Map<String, String> placeholders = getPlaceholders(
                            Map.of("%pvp_enabled%", languageLoader.getBoolean(isPvPEnabled)), currentNClaim);

                    sendTitleMessages(player, languageLoader, pvpType, placeholders);
                    dataManager.getClaimEntries().put(player, currentNClaim);
                }
            }
        });
    }

    private void sendTitleMessages(org.bukkit.entity.Player player, LanguageLoader languageLoader, String type, Map<String, String> placeholders) {
        String title = languageLoader.getWithPlaceholders(type + "-title", placeholders);
        String subtitle = languageLoader.getWithPlaceholders(type + "-subtitle", placeholders);
        player.sendTitle(title, subtitle, 10, 30, 10);
    }

    public static Map<String, String> getPlaceholders(Map<String, String> placeholders, NClaim claim) {
        HashMap<String, String> placeholderMap = new HashMap<>(placeholders);
        placeholderMap.putAll(Utils.getClaimVariablesPercent(claim));
        return placeholderMap;
    }
}