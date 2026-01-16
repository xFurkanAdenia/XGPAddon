package com.xfurkanadenia.xGPAddon.integration;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.xfurkanadenia.xGPAddon.XGPAddon;
import nl.marido.deluxecombat.api.DeluxeCombatAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CombatIntegration {
    private DeluxeCombatAPI dcApi;
    private ICombatLogX clApi;
    public CombatIntegration() {
        XGPAddon main = XGPAddon.getInstance();
        Plugin dcPlugin = main.getServer().getPluginManager().getPlugin("DeluxeCombat");
        Plugin clPlugin = main.getServer().getPluginManager().getPlugin("CombatLogX");
        if (dcPlugin != null) {
            dcApi = (DeluxeCombatAPI) clPlugin;
        } else if (clPlugin != null) {
            clApi = (ICombatLogX) clPlugin;
        }
    }

    public boolean isInCombat(Player p) {
        if(dcApi != null) {
            return dcApi.isInCombat(p);
        }
        else if (clApi != null) {
            return clApi.getCombatManager().isInCombat(p);
        }
        return false;
    }
}
