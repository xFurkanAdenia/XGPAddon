package com.xfurkanadenia.xGPAddon.events;

import com.xfurkanadenia.xGPAddon.model.NClaim;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ClaimTrustEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private NClaim claim;
    private Player player;
    public ClaimTrustEvent(NClaim claim, Player player) {
        this.claim = claim;
        this.player = player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public NClaim getClaim() {
        return claim;
    }

    public Player getPlayer() {
        return player;
    }
}
