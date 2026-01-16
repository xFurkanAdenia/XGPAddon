package com.xfurkanadenia.xGPAddon.manager;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.model.ClaimSettings;
import com.xfurkanadenia.xGPAddon.model.ClaimTrustedSettings;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import com.xfurkanadenia.xGPAddon.util.ClaimSettingsUtil;
import com.xfurkanadenia.xGPAddon.util.NLogger;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.N;

import java.util.*;

public class DataManager {

    private final XGPAddon main;

    private final HashSet<NClaim> claims = new HashSet<>();
    private final HashSet<Claim> cachedClaims = new HashSet<>();

    private final HashMap<Player, NClaim> playerChat = new HashMap<>();
    private final HashMap<Player, NClaim> banChat = new HashMap<>();
    private final HashMap<Player, NClaim> renameChat = new HashMap<>();
    private final HashMap<Player, NClaim> claimEntries = new HashMap<>();
    public DataManager(XGPAddon main) {
        this.main = main;
    }

    public void saveClaims() {
        FileConfiguration config = main.getConfigurationManager().getClaimData().getConfiguration();

        if (!config.contains("claims")) {
            config.createSection("claims");
        }

        for (NClaim claim : claims) {
            String claimId = claim.getClaim().getID().toString();
            String basePath = "claims." + claimId;

            if (claim.getTime() < 0) {
                config.set(basePath, null);
                continue;
            }

            config.set(basePath + ".time", claim.getTime());
            config.set(basePath + ".owner", claim.getOwner().toString());
            config.set(basePath + ".claimName", claim.getClaimName());


            config.set(basePath + ".settings.explosion", claim.getClaimSettings().isAllowExplosives());
            config.set(basePath + ".settings.pvp", claim.getClaimSettings().isAllowPvP());
            config.set(basePath + ".settings.entering", claim.getClaimSettings().isEntering());
            config.set(basePath + ".settings.useElytra", claim.getClaimSettings().canUseElytra());
            config.set(basePath + ".settings.leafDecay", claim.getClaimSettings().isLeafDecay());
            config.set(basePath + ".settings.allowMobs", claim.getClaimSettings().isSpawnAnimals());
            config.set(basePath + ".settings.allowMonsters", claim.getClaimSettings().isSpawnMonsters());
            config.set(basePath + ".settings.fireSpread", claim.getClaimSettings().isFireSpread());
            config.set(basePath + ".settings.bannedPlayers",
                    claim.getClaimSettings().getBannedPlayers().stream()
                            .map(UUID::toString)
                            .toList()
            );

            Map<String, ClaimTrustedSettings> trustedSettings = claim.getClaimSettings().getTrustedSettings();
            ConfigurationSection tsSection = config.createSection(basePath + ".settings.trustedSettings");
            trustedSettings.forEach((uuid, settings) -> {
                ConfigurationSection sub = tsSection.createSection(uuid.toString());
                sub.set("allowBlockBreak", settings.getAllowBlockBreak());
                sub.set("allowBlockPlace", settings.getAllowBlockPlace());
                sub.set("allowSpawnerBreak", settings.getAllowSpawnerBreak());
                sub.set("allowChestAccess", settings.getAllowChestAccess());
                sub.set("allowDoorOpen", settings.getAllowDoorOpen());
                sub.set("allowTrapDoorOpen", settings.getAllowTrapDoorOpen());
            });
        }
    }


    public void loadClaims() {
        FileConfiguration config = main.getConfigurationManager().getClaimData().getConfiguration();

        if (!config.contains("claims")) {
            config.createSection("claims");
            main.getConfigurationManager().getClaimData().saveConfigurationSilent();
            if (main.getConfig().getBoolean("settings.debug"))
                NLogger.info("Created empty 'claims' section in claimdata.yml");
        }

        for (String claimID : config.getConfigurationSection("claims").getKeys(false)) {
            Claim claim = GriefPrevention.instance.dataStore.getClaim(Long.parseLong(claimID));
            if (claim == null) {
                config.set("claims." + claimID, null);
                continue;
            }

            if (getClaim(claim) != null) {
                continue;
            }

            long time = config.getLong("claims." + claimID + ".time");

            UUID owner = UUID.fromString(config.getString("claims." + claimID + ".owner"));
            String claimName = config.getString("claims." + claimID + ".claimName");
            ClaimSettings claimSettings = new ClaimSettings(
                    config.getBoolean("claims." + claimID + ".settings.explosion"),
                    config.getBoolean("claims." + claimID + ".settings.pvp"),
                    config.getBoolean("claims." + claimID + ".settings.entering"),
                    config.getBoolean("claims." + claimID + ".settings.useElytra"),
                    config.getBoolean("claims." + claimID + ".settings.leafDecay"),
                    config.getBoolean("claims." + claimID + ".settings.allowMobs"),
                    config.getBoolean("claims." + claimID + ".settings.allowMonsters"),
                    config.getBoolean("claims." + claimID + ".settings.fireSpread"),
                    ClaimSettingsUtil.getClaimTrustedSettings(config.getConfigurationSection("claims." + claimID + ".settings.trustedSettings")),
                    config.getStringList("claims." + claimID + ".settings.bannedPlayers").stream().map(UUID::fromString).toList()
            );

            NClaim nClaim = new NClaim(claim, time, claimName, claimID, owner, claimSettings);
            claims.add(nClaim);
        }

        main.getConfigurationManager().getClaimData().saveConfigurationSilent();
    }



    public NClaim getClaim(Claim claim){
        for (NClaim nClaim : claims) {
            if (nClaim.getClaim().equals(claim)) {
                return nClaim;
            }
        }
        return null;
    }

    public NClaim getClaim(String claimID){
        for (NClaim nClaim : claims) {
            if (nClaim.getClaimId().equals(claimID)) {
                return nClaim;
            }
        }
        return null;
    }

    public int getClaimCount(UUID uuid){
        return claims.stream().filter(nClaim -> nClaim.getOwner().equals(uuid)).mapToInt(nClaim -> 1).sum();
    }


    public int getClaimLimit(UUID uuid){
        Player player = Bukkit.getPlayer(uuid);
        if(player == null) return -1;
        int limit = XGPAddon.getInstance().getConfig().getInt("limits.default");
        for(String key : XGPAddon.getInstance().getConfig()
                .getConfigurationSection("limits")
                .getKeys(false)
                .stream()
                .sorted(Comparator.comparingInt(
                        key -> XGPAddon.getInstance().getConfig().getInt("limits." + key)
                )).toList())
                {
                    int value = XGPAddon.getInstance().getConfig().getInt("limits." + key);
                    if(player.hasPermission("xgpaddon.limit." + key)) limit = value;
                }
        return limit;
    }

    public boolean hasClaimLimit(UUID uuid){
        int pClaimCount = getClaimCount(uuid);
        int pClaimLimit = getClaimLimit(uuid);
        if(pClaimCount >= pClaimLimit) return false;
        return true;
    }

    public HashSet<NClaim> getClaims() {
        return claims;
    }

    public List<Player> getPlayersInClaim(NClaim claim) {
        List<Player> players = new ArrayList<>();
        DataManager dataManager = XGPAddon.getInstance().getDataManager();
        DataStore dataStore = GriefPrevention.instance.dataStore;
        Bukkit.getOnlinePlayers().forEach(p -> {
            Claim pGClaim = dataStore.getClaimAt(p.getLocation(), false, null);
            if(pGClaim == null) return;
            NClaim pClaim = dataManager.getClaim(pGClaim);
            if(pClaim.equals(claim)) players.add(p);
        });

        return players;
    }

    public HashSet<Claim> getCachedClaims() {
        return cachedClaims;
    }

    public void addClaimsToTimer(){
        for(NClaim nClaim : claims){
            if(!main.getTaskClaimTimer().getExpiringClaims().contains(nClaim))
                main.getTaskClaimTimer().getExpiringClaims().add(nClaim);
        }
    }

    public HashMap<Player, NClaim> getPlayerChat() {
        return playerChat;
    }

    public HashMap<Player, NClaim> getBanChat() {
        return banChat;
    }

    public HashMap<Player, NClaim> getRenameChat() {
        return renameChat;
    }

    public HashMap<Player, NClaim> getClaimEntries() {
        return claimEntries;
    }
}
