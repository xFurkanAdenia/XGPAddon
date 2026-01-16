package com.xfurkanadenia.xGPAddon.model;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ClaimSettings {

    private boolean allowExplosives;
    private boolean allowPvP;
    private boolean entering;
    private boolean useElytra;
    private boolean leafDecay;
    private boolean spawnAnimals;
    private boolean spawnMonsters;
    private boolean fireSpread;
    private Map<String, ClaimTrustedSettings> trustedSettings;
    private List<UUID> bannedPlayers;

    public ClaimSettings(boolean allowExplosives, boolean allowPvP, boolean entering, boolean useElytra, boolean leafDecay, boolean spawnAnimals, boolean spawnMonsters, boolean fireSpread, Map<String, ClaimTrustedSettings> trustedSettings, List<UUID> bannedPlayers) {
        this.allowExplosives = allowExplosives;
        this.allowPvP = allowPvP;
        this.entering = entering;
        this.useElytra = useElytra;
        this.leafDecay = leafDecay;
        this.spawnAnimals = spawnAnimals;
        this.spawnMonsters = spawnMonsters;
        this.fireSpread = fireSpread;
        this.trustedSettings = trustedSettings;
        this.bannedPlayers = bannedPlayers != null ? new ArrayList<>(bannedPlayers) : new ArrayList<>();
    }

    public boolean isAllowExplosives() {
        return allowExplosives;
    }

    public void setAllowExplosives(boolean allowExplosives) {
        this.allowExplosives = allowExplosives;
    }

    public boolean isAllowPvP() {
        return allowPvP;
    }

    public void setAllowPvP(boolean allowPvP) {
        this.allowPvP = allowPvP;
    }

    public boolean isEntering() {
        return entering;
    }

    public void setEntering(boolean entering) {
        this.entering = entering;
    }

    public boolean canUseElytra() {
        return useElytra;
    }

    public void setUseElytra(boolean useElytra) {
        this.useElytra = useElytra;
    }

    public boolean isLeafDecay() {
        return leafDecay;
    }

    public void setLeafDecay(boolean leafDecay) {
        this.leafDecay = leafDecay;
    }

    public boolean isSpawnAnimals() {
        return spawnAnimals;
    }
    public boolean isSpawnMonsters() {
        return spawnMonsters;
    }

    public void setSpawnAnimals(boolean spawnAnimals) {
        this.spawnAnimals = spawnAnimals;
    }
    public void setSpawnMonsters(boolean spawnMonsters) {
        this.spawnMonsters = spawnMonsters;
    }

    public boolean isUseElytra() {
        return useElytra;
    }

    public boolean isFireSpread() {
        return fireSpread;
    }

    public void setFireSpread(boolean fireSpread) {
        this.fireSpread = fireSpread;
    }

    public List<UUID> getBannedPlayers() {
        return bannedPlayers;
    }

    public Map<String, ClaimTrustedSettings> getTrustedSettings() {
        return trustedSettings;
    }

    public ClaimTrustedSettings getTrustedSettings(UUID uuid) {
        if(!this.trustedSettings.containsKey(uuid.toString())) return null;
        return this.trustedSettings.get(uuid.toString());
    }
    public ClaimTrustedSettings getTrustedSettings(Player p) {
        if(!this.trustedSettings.containsKey(p.getUniqueId().toString())) return null;
        return this.trustedSettings.get(p.getUniqueId().toString());
    }



    public void setBannedPlayers(List<UUID> bannedPlayers) {
        this.bannedPlayers = bannedPlayers;
    }
}
