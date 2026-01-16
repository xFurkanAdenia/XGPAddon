package com.xfurkanadenia.xGPAddon.model;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;

import java.util.Locale;
import java.util.UUID;

public class NClaim {

    private Claim claim;
    private String claimName;
    private UUID owner;
    private long time;
    private ClaimSettings claimSettings;
    private final String claimId;

    public NClaim(Claim claim, long time, String claimName, String claimId, UUID owner, ClaimSettings claimSettings) {
        this.claim = claim;
        this.claimName = claimName;
        this.owner = owner;
        this.time = time;
        this.claimSettings = claimSettings;
        this.claimId = claimId;

    }

    public Claim getClaim() {
        return claim;
    }

    public void setClaim(Claim claim) {
        this.claim = claim;
    }

    public String getClaimName() {
        return claimName;
    }

    public void setClaimName(String claimName) {
        this.claimName = claimName;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public ClaimSettings getClaimSettings() {
        return claimSettings;
    }

    public Location getLocation(){
        Location lesser = getClaim().getLesserBoundaryCorner();
        Location greater = getClaim().getGreaterBoundaryCorner();
        int centerX = (int) (lesser.getX() + greater.getX()) / 2;
        int centerZ = (int) (lesser.getZ() + greater.getZ()) / 2;
        int centerY = lesser.getWorld().getHighestBlockYAt(centerX, centerZ) + 1;
        return new Location(lesser.getWorld(), centerX, centerY, centerZ);

    }

    public void setClaimSettings(ClaimSettings claimSettings) {
        this.claimSettings = claimSettings;
    }

    public String getClaimId() {
        return claimId;
    }

}
