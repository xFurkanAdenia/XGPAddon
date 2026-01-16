package com.xfurkanadenia.xGPAddon.listener;

import com.xfurkanadenia.xGPAddon.LanguageLoader;
import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.manager.DataManager;
import com.xfurkanadenia.xGPAddon.model.ClaimSettings;
import com.xfurkanadenia.xGPAddon.model.ClaimTrustedSettings;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import com.xfurkanadenia.xGPAddon.util.Utils;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.events.ClaimInspectionEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

public class ClaimSettingsListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPVP(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof Player)) return;

        if (e.getDamager().isOp()) return;



        Claim victimClaim = GriefPrevention.instance.dataStore.getClaimAt(e.getEntity().getLocation(), true, GriefPrevention.instance.dataStore.getPlayerData(e.getEntity().getUniqueId()).lastClaim);
        Claim attackerClaim = GriefPrevention.instance.dataStore.getClaimAt(e.getDamager().getLocation(), true, GriefPrevention.instance.dataStore.getPlayerData(e.getDamager().getUniqueId()).lastClaim);

        XGPAddon main = XGPAddon.getInstance();
        DataManager dataManager = main.getDataManager();
        LanguageLoader languageLoader = main.getLanguageLoader();


        Player attacker = (Player) e.getDamager();

        if(attackerClaim != null && victimClaim == null) {
            NClaim attackerNClaim = main.getDataManager().getClaim(attackerClaim);
            if (attackerClaim.equals(victimClaim)) return;
            if(!attackerNClaim.getClaimSettings().isAllowPvP()) {
                e.getDamager().sendMessage(XGPAddon.getInstance().getLanguageLoader().getWithPlaceholders("cant-damage-in-pvp-disabled-claim", Utils.getClaimVariablesPercent(attackerNClaim)));
                e.setCancelled(true);
                return;
            }
        }

        if (victimClaim != null) {
            NClaim nVictimClaim = dataManager.getClaim(victimClaim);
            if (nVictimClaim != null && !nVictimClaim.getClaimSettings().isAllowPvP()) {
                attacker.sendMessage(languageLoader.get("pvp-disabled"));
                e.setCancelled(true);
                return;
            }
        }

        if (attackerClaim != null) {
            NClaim nAttackerClaim = dataManager.getClaim(attackerClaim);
            if (nAttackerClaim != null && !nAttackerClaim.getClaimSettings().isAllowPvP()) {
                e.setCancelled(true);
                attacker.sendMessage(languageLoader.get("pvp-disabled"));
            }
        }

    }

    @EventHandler
    public void onExplosive(EntityExplodeEvent e) {
        Location location = e.getEntity().getLocation();
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
        if (XGPAddon.getInstance().getDataManager().getClaim(claim) == null) return;

        NClaim nClaim = XGPAddon.getInstance().getDataManager().getClaim(claim);
        ClaimSettings claimSettings = nClaim.getClaimSettings();
        if (!claimSettings.isAllowExplosives()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDecay(LeavesDecayEvent e) {
        Location location = e.getBlock().getLocation();
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
        if (XGPAddon.getInstance().getDataManager().getClaim(claim) == null) return;

        NClaim nClaim = XGPAddon.getInstance().getDataManager().getClaim(claim);
        ClaimSettings claimSettings = nClaim.getClaimSettings();
        if (!claimSettings.isLeafDecay()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntering(PlayerMoveEvent e) {
        if (e.getPlayer().isOp()) return;
        Location to = e.getTo();
        Location from = e.getFrom();
        if (to.getBlockX() == from.getBlockX() && to.getBlockZ() == from.getBlockZ()) return;
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(to, false, null);
        if (XGPAddon.getInstance().getDataManager().getClaim(claim) == null) return;
        NClaim nClaim = XGPAddon.getInstance().getDataManager().getClaim(claim);

        ClaimSettings claimSettings = nClaim.getClaimSettings();

        boolean perm = claim.hasExplicitPermission(e.getPlayer(), ClaimPermission.Access);
        if (perm) return;

        if (!claimSettings.isEntering()) {
            e.setCancelled(true);
        }

        if (claimSettings.getBannedPlayers().contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(XGPAddon.getInstance().getLanguageLoader().get("banned-from-claim"));

        }

    }

    @EventHandler
    public void onEnterClaim(PlayerMoveEvent e) {
        if(!XGPAddon.getInstance().getDeluxeCombatIntegration().isInCombat(e.getPlayer())) return;
        Location to = e.getTo();
        Location from = e.getFrom();
        Claim fromClaim = GriefPrevention.instance.dataStore.getClaimAt(from, false, null);
        Claim toClaim = GriefPrevention.instance.dataStore.getClaimAt(to, false, null);
        if ((fromClaim != null && fromClaim.equals(toClaim)) || (fromClaim == null && toClaim == null)) return;
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(to, false, null);
        if(claim == null) return;
        NClaim nClaim = XGPAddon.getInstance().getDataManager().getClaim(claim);
        ClaimSettings claimSettings = nClaim.getClaimSettings();
        if(claimSettings.isAllowPvP()) return;
        Utils.pushPlayerBack(e.getPlayer(), 5);
        e.getPlayer().sendMessage(XGPAddon.getInstance().getLanguageLoader().getWithPlaceholders("cant-enter-while-pvp", Utils.getClaimVariablesPercent(XGPAddon.getInstance().getDataManager().getClaim(claim))));
        e.setCancelled(true);
    }
//    @EventHandler
//    public void onHitInClaim(EntityDamageByEntityEvent e) {
//        XGPAddon main = XGPAddon.getInstance();
//        if(!(e.getEntity() instanceof Player)) return;
//        if(!(e.getDamager() instanceof Player)) return;
//        Claim attackerClaim = GriefPrevention.instance.dataStore.getClaimAt(e.getDamager().getLocation(), true, null);
//        Claim victimClaim = GriefPrevention.instance.dataStore.getClaimAt(e.getEntity().getLocation(), true, null);
//        if(attackerClaim != null) {
//            NClaim attackerNClaim = main.getDataManager().getClaim(attackerClaim);
//            if (attackerClaim.equals(victimClaim)) return;
//            if(!attackerNClaim.getClaimSettings().isAllowPvP()) {
//                e.getDamager().sendMessage(XGPAddon.getInstance().getLanguageLoader().getWithPlaceholders("cant-damage-in-pvp-disabled-claim", Utils.getClaimVariablesPercent(attackerNClaim)));
//                e.setCancelled(true);
//            }
//        }
//    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        if (e.getEntity() instanceof Player) return;
        if (!(e.getEntity() instanceof Monster || e.getEntity() instanceof Animals)) return;

        Location location = e.getLocation();
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
        if (XGPAddon.getInstance().getDataManager().getClaim(claim) == null) return;

        NClaim nClaim = XGPAddon.getInstance().getDataManager().getClaim(claim);
        ClaimSettings claimSettings = nClaim.getClaimSettings();
        if (e.getEntity() instanceof Monster && !claimSettings.isSpawnMonsters()) {
            e.setCancelled(true);
        }

        if (e.getEntity() instanceof Animals && !claimSettings.isSpawnAnimals()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onGlide(EntityToggleGlideEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        if (e.getEntity().isOp()) return;

        Location location = e.getEntity().getLocation();
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
        if (XGPAddon.getInstance().getDataManager().getClaim(claim) == null) return;

        NClaim nClaim = XGPAddon.getInstance().getDataManager().getClaim(claim);
        ClaimSettings claimSettings = nClaim.getClaimSettings();
        if (!claimSettings.canUseElytra()) {
            ((Player) e.getEntity()).setGliding(false);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireSpread(BlockSpreadEvent e) {
        if (e.getSource().getType() != Material.FIRE) return;
        Location location = e.getBlock().getLocation();

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
        if (XGPAddon.getInstance().getDataManager().getClaim(claim) == null) return;

        NClaim nClaim = XGPAddon.getInstance().getDataManager().getClaim(claim);
        ClaimSettings claimSettings = nClaim.getClaimSettings();
        if (!claimSettings.isFireSpread()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireIgnite(BlockIgniteEvent e) {
        Location location = e.getBlock().getLocation();
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
        if (XGPAddon.getInstance().getDataManager().getClaim(claim) == null) return;

        NClaim nClaim = XGPAddon.getInstance().getDataManager().getClaim(claim);
        ClaimSettings claimSettings = nClaim.getClaimSettings();
        if (!claimSettings.isFireSpread()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
        Location location = e.getBlock().getLocation();
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
        if (XGPAddon.getInstance().getDataManager().getClaim(claim) == null) return;

        NClaim nClaim = XGPAddon.getInstance().getDataManager().getClaim(claim);
        ClaimSettings claimSettings = nClaim.getClaimSettings();
        if (!claimSettings.isFireSpread()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInspect(ClaimInspectionEvent e) {
        Collection<Claim> claims = e.getClaims();
        if (claims.isEmpty()) return;
        for (Claim claim : claims) {
            NClaim nClaim = XGPAddon.getInstance().getDataManager().getClaim(claim);
            if (nClaim == null) continue;
            for (String s : XGPAddon.getInstance().getLanguageLoader().getList("claim-inspect-chat", nClaim)) {
                e.getPlayer().sendMessage(s);
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Claim locationClaim = GriefPrevention.instance.dataStore.getClaimAt(e.getTo(), true, null);
        if (locationClaim == null) return;
        NClaim nClaim = XGPAddon.getInstance().getDataManager().getClaim(locationClaim);
        if(nClaim == null) return;
        if (nClaim.getClaimSettings().getBannedPlayers().contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(XGPAddon.getInstance().getLanguageLoader().get("banned-from-claim"));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getPlayer().isOp()) return;

        XGPAddon main = XGPAddon.getInstance();
        Player player = e.getPlayer();
        NClaim claim = main.getDataManager().getClaim(
                GriefPrevention.instance.dataStore.getClaimAt(e.getBlock().getLocation(), false, null)
        );
        if (claim == null) return;

        ClaimTrustedSettings settings = claim.getClaimSettings().getTrustedSettings(player);
        if (settings == null) return;

        Material blockType = e.getBlock().getType();


        if (blockType != Material.SPAWNER) {
            if (settings.getAllowBlockBreak()) return; 
            player.sendMessage(main.getLanguageLoader().get("permission-block-break"));
            e.setCancelled(true);
            return;
        }


        if (settings.getAllowBlockBreak() && settings.getAllowSpawnerBreak()) return; 

        if (!settings.getAllowBlockBreak()) {
            player.sendMessage(main.getLanguageLoader().get("permission-block-break"));
        } else if (!settings.getAllowSpawnerBreak()) {
            player.sendMessage(main.getLanguageLoader().get("permission-spawner-break"));
        }
        e.setCancelled(true);

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if(e.getPlayer().isOp()) return;
        XGPAddon main = XGPAddon.getInstance();
        Player player = e.getPlayer();
        NClaim claim = main.getDataManager().getClaim(GriefPrevention.instance.dataStore.getClaimAt(e.getBlock().getLocation(), false, null));
        if (claim == null) return;
        ClaimTrustedSettings settings = claim.getClaimSettings().getTrustedSettings(player);
        if(settings == null) return;
        if(settings.getAllowBlockPlace()) return;
        player.sendMessage(XGPAddon.getInstance().getLanguageLoader().get("permission-block-place"));
        e.setCancelled(true);
    }

    @EventHandler
    public void onChestOpen(PlayerInteractEvent e) {
        if(e.getPlayer().isOp()) return;
        List<Material> containers = List.of(Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL, Material.HOPPER, Material.HOPPER_MINECART, Material.CHEST_MINECART);
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null) return;
        Block block = e.getClickedBlock();

        XGPAddon main = XGPAddon.getInstance();
        Player player = e.getPlayer();
        NClaim claim = main.getDataManager().getClaim(GriefPrevention.instance.dataStore.getClaimAt(e.getClickedBlock().getLocation(), false, null));
        if(claim == null) return;
        ClaimTrustedSettings settings = claim.getClaimSettings().getTrustedSettings(player);
        if(settings == null) return;
        if(containers.contains(block.getType()) && !settings.getAllowChestAccess()) {
            player.sendMessage(XGPAddon.getInstance().getLanguageLoader().get("permission-open-chest"));
            e.setCancelled(true);
        }
        if(block.getType().name().endsWith("_TRAPDOOR") && !settings.getAllowTrapDoorOpen()) {
            player.sendMessage(XGPAddon.getInstance().getLanguageLoader().get("permission-open-trap-door"));
            e.setCancelled(true);
        } else if(block.getType().name().endsWith("_DOOR") && !settings.getAllowDoorOpen()) {
            player.sendMessage(XGPAddon.getInstance().getLanguageLoader().get("permission-open-door"));
            e.setCancelled(true);
        }
    }



}
