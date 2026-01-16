package com.xfurkanadenia.xGPAddon.model;

public class ClaimTrustedSettings {
    private boolean allowBlockBreak;
    private boolean allowBlockPlace;
    private boolean allowSpawnerBreak;
    private boolean allowChestAccess;
    private boolean allowDoorOpen;
    private boolean allowTrapDoorOpen;
    public ClaimTrustedSettings(boolean allowBlockBreak, boolean allowBlockPlace, boolean allowSpawnerBreak, boolean allowDoorOpen, boolean allowTrapDoorOpen, boolean allowChestAccess) {
        this.allowBlockBreak = allowBlockBreak;
        this.allowBlockPlace = allowBlockPlace;
        this.allowSpawnerBreak = allowSpawnerBreak;
        this.allowChestAccess = allowChestAccess;
        this.allowDoorOpen = allowDoorOpen;
        this.allowTrapDoorOpen = allowTrapDoorOpen;
    }

    public boolean getAllowBlockBreak() { return allowBlockBreak; }
    public boolean getAllowBlockPlace() { return allowBlockPlace; }
    public boolean getAllowSpawnerBreak() { return allowSpawnerBreak; }
    public boolean getAllowChestAccess() { return allowChestAccess; }
    public void setAllowBlockBreak(boolean allowBlockBreak) { this.allowBlockBreak = allowBlockBreak; }
    public void setAllowBlockPlace(boolean allowBlockPlace) { this.allowBlockPlace = allowBlockPlace; }
    public void setAllowSpawnerBreak(boolean allowSpawnerBreak) { this.allowSpawnerBreak = allowSpawnerBreak; }
    public void setAllowChestAccess(boolean allowChestAccess) { this.allowChestAccess = allowChestAccess; }

    public boolean getAllowDoorOpen() {
        return allowDoorOpen;
    }

    public boolean getAllowTrapDoorOpen() {
        return allowTrapDoorOpen;
    }

    public void setAllowTrapDoorOpen(boolean allowTrapDoorOpen) {
        this.allowTrapDoorOpen = allowTrapDoorOpen;
    }

    public void setAllowDoorOpen(boolean allowDoorOpen) {
        this.allowDoorOpen = allowDoorOpen;
    }
}
