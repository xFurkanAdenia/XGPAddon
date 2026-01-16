package com.xfurkanadenia.xGPAddon.util;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.model.ClaimSettings;
import com.xfurkanadenia.xGPAddon.model.ClaimTrustedSettings;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;

public class ClaimSettingsUtil {

    public static Map<UUID, ClaimTrustedSettings> readTrustedSettings(ConfigurationSection tsSection) {
        Map<UUID, ClaimTrustedSettings> result = new HashMap<>();
        if (tsSection == null) return result;

        for (String uuidKey : tsSection.getKeys(false)) {
            ConfigurationSection sub = tsSection.getConfigurationSection(uuidKey);
            if (sub == null) continue;

            boolean bb = sub.getBoolean("allowBlockBreak");
            boolean bp = sub.getBoolean("allowBlockPlace");
            boolean sb = sub.getBoolean("allowSpawnerBreak");
            boolean ca = sub.getBoolean("allowChestAccess");
            boolean doo = sub.getBoolean("allowDoorOpen");
            boolean tdo = sub.getBoolean("allowTrapDoorOpen");

            ClaimTrustedSettings settings = new ClaimTrustedSettings(bb, bp, sb, doo, tdo, ca);
            try {
                result.put(UUID.fromString(uuidKey), settings);
            } catch (IllegalArgumentException ignored) {}
        }
        return result;
    }

    public static Map<String, ClaimTrustedSettings> getClaimTrustedSettings(ConfigurationSection section) {
        Map<String, ClaimTrustedSettings> settings = new HashMap<>();
        if(section.getKeys(false).isEmpty()) return settings;
        section.getKeys(false).forEach(v -> {
            boolean allowBlockBreak = section.getConfigurationSection(v).getBoolean("allowBlockBreak");
            boolean allowBlockPlace = section.getConfigurationSection(v).getBoolean("allowBlockPlace");
            boolean allowSpawnerBreak = section.getConfigurationSection(v).getBoolean("allowSpawnerBreak");
            boolean allowChestAccess = section.getConfigurationSection(v).getBoolean("allowChestAccess");
            boolean allowDoorOpen = section.getConfigurationSection(v).getBoolean("allowDoorOpen");
            boolean allowTrapDoorOpen = section.getConfigurationSection(v).getBoolean("allowTrapDoorOpen");

            settings.put(v, new ClaimTrustedSettings(allowBlockBreak, allowBlockPlace, allowSpawnerBreak, allowDoorOpen, allowTrapDoorOpen, allowChestAccess));
        });
        return settings;
    }

    public static ClaimTrustedSettings getDefaultClaimTrustedSettings() {
        Map<String, Boolean> claimSettings = new HashMap<>();
        claimSettings.put("allowBlockBreak", true);
        claimSettings.put("allowBlockPlace", true);
        claimSettings.put("allowSpawnerBreak", true);
        claimSettings.put("allowChestAccess", true);
        claimSettings.put("allowDoorOpen", true);
        claimSettings.put("allowTrapDoorOpen", true);

        return new ClaimTrustedSettings(claimSettings.get("allowBlockBreak"), claimSettings.get("allowBlockPlace"), claimSettings.get("allowSpawnerBreak"), claimSettings.get("allowDoorOpen"), claimSettings.get("allowTrapDoorOpen"), claimSettings.get("allowChestAccess"));
    }

    public static ClaimSettings getDefaultClaimSettings() {
        return new ClaimSettings(true, false, true, false, true, true, false, true, new HashMap<>(), List.of());
    }

}