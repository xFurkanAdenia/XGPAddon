package com.xfurkanadenia.xGPAddon.listener;

import com.xfurkanadenia.xGPAddon.LanguageLoader;
import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.integration.VaultIntegration;
import com.xfurkanadenia.xGPAddon.manager.DataManager;
import com.xfurkanadenia.xGPAddon.menu.BanMenu;
import com.xfurkanadenia.xGPAddon.menu.ClaimMenu;
import com.xfurkanadenia.xGPAddon.model.ClaimSettings;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import com.xfurkanadenia.xGPAddon.util.Utils;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.checkerframework.checker.units.qual.C;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        String message = e.getMessage();
        DataManager dataManager = XGPAddon.getInstance().getDataManager();

        // Claim süre uzatma chat
        if (dataManager.getPlayerChat().containsKey(p)) {
            e.setCancelled(true);

            if (message.equalsIgnoreCase("cancel")) {
                NClaim claim = dataManager.getClaim(dataManager.getPlayerChat().get(p).getClaimId());
                Bukkit.getScheduler().runTask(XGPAddon.getInstance(), () -> new ClaimMenu(p, claim).open(p));
                dataManager.getPlayerChat().remove(p);
                return;
            }

            try {
                int time = Integer.parseInt(message);
                int price = XGPAddon.getInstance().getConfig().getInt("settings.claimTimePrice");

                if (VaultIntegration.getEcon().getBalance(p) < price * time) {
                    p.sendMessage(XGPAddon.getInstance().getLanguageLoader().get("claim-extend-time-fail"));
                    return;
                }

                if (time < 0) {
                    p.sendMessage(XGPAddon.getInstance().getLanguageLoader().get("invalid-number"));
                    return;
                }

                VaultIntegration.getEcon().withdrawPlayer(p, price * time);
                dataManager.getPlayerChat().get(p).setTime(
                        dataManager.getPlayerChat().get(p).getTime() + (time * 60L)
                );
                p.sendMessage(XGPAddon.getInstance().getLanguageLoader().getWithPlaceholders(
                        "claim-extend-time-success",
                        Map.of("%time%", String.valueOf(time),
                                "%claim_time%", LanguageLoader.formatTime(dataManager.getPlayerChat().get(p).getTime()))
                ));

                NClaim claim = dataManager.getClaim(dataManager.getPlayerChat().get(p).getClaimId());
                Bukkit.getScheduler().runTask(XGPAddon.getInstance(), () -> new ClaimMenu(p, claim).open(p));
                dataManager.getPlayerChat().remove(p);

            } catch (NumberFormatException ex) {
                p.sendMessage(XGPAddon.getInstance().getLanguageLoader().get("invalid-number"));
                NClaim claim = dataManager.getClaim(dataManager.getPlayerChat().get(p).getClaimId());
                Bukkit.getScheduler().runTask(XGPAddon.getInstance(), () -> new ClaimMenu(p, claim).open(p));
                dataManager.getPlayerChat().remove(p);
            }
        }

        // Ban chat
        if (dataManager.getBanChat().containsKey(p)) {
            e.setCancelled(true);

            if (message.equalsIgnoreCase("cancel")) {
                NClaim claim = dataManager.getClaim(dataManager.getBanChat().get(p).getClaimId());
                Bukkit.getScheduler().runTask(XGPAddon.getInstance(), () -> new BanMenu(p, claim).open(p));
                dataManager.getBanChat().remove(p);
                return;
            }

            Player target = XGPAddon.getInstance().getServer().getPlayer(message);
            if (target == null) {
                p.sendMessage(XGPAddon.getInstance().getLanguageLoader().get("player-not-found"));
                return;
            }

            UUID uuid = target.getUniqueId();
            if (uuid.equals(p.getUniqueId())) {
                p.sendMessage(XGPAddon.getInstance().getLanguageLoader().get("player-is-owner"));
                return;
            }

            if (dataManager.getBanChat().get(p).getClaimSettings().getBannedPlayers().contains(uuid)) {
                p.sendMessage(XGPAddon.getInstance().getLanguageLoader().get("already-banned"));
                return;
            }

            ClaimSettings settings = dataManager.getBanChat().get(p).getClaimSettings();
            List<UUID> banned = settings.getBannedPlayers();
            if (banned != null && !banned.contains(uuid)) {
                banned.add(uuid);

                Bukkit.getScheduler().runTask(XGPAddon.getInstance(), () -> {
                    XGPAddon.getInstance().getConfig().getStringList("onPlayerBan").forEach(command -> {
                        Bukkit.dispatchCommand(
                                Bukkit.getConsoleSender(),
                                Utils.getFormatted(command, p, Map.of("banned", target.getName()))
                        );
                    });
                });
            }

            NClaim claim = dataManager.getClaim(dataManager.getBanChat().get(p).getClaimId());
            Bukkit.getScheduler().runTask(XGPAddon.getInstance(), () -> new BanMenu(p, claim).open(p));
            dataManager.getBanChat().remove(p);
            p.sendMessage(XGPAddon.getInstance().getLanguageLoader().getWithPlaceholders(
                    "player-ban", Map.of("%player%", message)
            ));
        }

        if(dataManager.getRenameChat().containsKey(p)) {
            e.setCancelled(true);
            NClaim claim = dataManager.getRenameChat().get(p);
            String msg = e.getMessage();
            int maxLength = XGPAddon.getInstance().getConfig().getInt("settings.claim-name-length-limit", 15);
            if(msg.equalsIgnoreCase("cancel")) {
                Bukkit.getScheduler().runTask(XGPAddon.getInstance(), () -> new ClaimMenu(p, claim).open(p));
                dataManager.getRenameChat().remove(p);
                return;
            }
            Map<String, String> vars = Utils.getClaimVariablesPercent(claim);
            vars.put("%maxlength%", String.valueOf(maxLength));
            if(msg.length() > maxLength) {
                e.getPlayer().sendMessage(XGPAddon.getInstance().getLanguageLoader().getWithPlaceholders("length-limit-exceeded", vars));
                return;
            }
            claim.setClaimName(e.getMessage());
            Bukkit.getScheduler().runTask(XGPAddon.getInstance(), () -> new ClaimMenu(p, claim).open(p));
            dataManager.getRenameChat().remove(p);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClaimCommand(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage().toLowerCase();
        if (msg.split(" ")[0].equalsIgnoreCase("/claim")) {
            Location loc = e.getPlayer().getLocation();
            Claim gClaim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);
            if (gClaim == null) return;
            NClaim claim = XGPAddon.getInstance().getDataManager().getClaim(gClaim);


            if(!claim.getClaim().hasExplicitPermission(e.getPlayer(), ClaimPermission.Build) && !claim.getOwner().equals(e.getPlayer().getUniqueId())) return;
            e.setCancelled(true);
            new ClaimMenu(e.getPlayer(), claim).open(e.getPlayer());
        }
    }
}
