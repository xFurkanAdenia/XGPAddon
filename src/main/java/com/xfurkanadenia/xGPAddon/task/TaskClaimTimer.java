package com.xfurkanadenia.xGPAddon.task;

import com.xfurkanadenia.xGPAddon.LanguageLoader;
import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import com.xfurkanadenia.xGPAddon.util.Utils;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TaskClaimTimer extends BukkitRunnable {

    private final PriorityQueue<NClaim> expiringClaims = new PriorityQueue<>(Comparator.comparingLong(NClaim::getTime));

    @Override
    public void run() {
        XGPAddon main = XGPAddon.getInstance();
        LanguageLoader languageLoader = main.getLanguageLoader();
        Iterator<NClaim> iterator = expiringClaims.iterator();

        while (iterator.hasNext()) {
            NClaim claim = iterator.next();

            long time = claim.getTime();

            if (time >= 0) {
                claim.setTime(time - (XGPAddon.getInstance().getClaimCheckTime() / 20));
            } else {
                iterator.remove();
                Bukkit.getScheduler().runTask(XGPAddon.getInstance(), () -> {

                    Utils.sendWebhooks(claim);
                    if(main.getConfig().getBoolean("settings.broadcastClaimExplosion"))
                        for(Player player : Bukkit.getOnlinePlayers()) {
                        Map<String, String> placeholders = new HashMap<>(Utils.getClaimVariablesPercent(claim));
                        placeholders.put("%player%", Bukkit.getOfflinePlayer(claim.getOwner()).getName());
                        player.sendMessage(languageLoader.getWithPlaceholders("claim-explosion-announce" , placeholders));
                    }
                    GriefPrevention.instance.dataStore.deleteClaim(claim.getClaim());
                    XGPAddon.getInstance().getDataManager().getCachedClaims().remove(claim.getClaim());
                    XGPAddon.getInstance().getDataManager().getClaims().remove(claim);

                });
            }
        }
    }

    public PriorityQueue<NClaim> getExpiringClaims() {
        return expiringClaims;
    }
}
