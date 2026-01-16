package com.xfurkanadenia.xGPAddon;

import com.xfurkanadenia.xGPAddon.model.NClaim;
import com.xfurkanadenia.xGPAddon.util.NLogger;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.xfurkanadenia.xGPAddon.Gradient.processText;

public class LanguageLoader {

    private static final ConcurrentHashMap<String, Object> translationMap = new ConcurrentHashMap<>();
    private final XGPAddon plugin;

    public LanguageLoader(XGPAddon plugin) {
        this.plugin = plugin;
        loadLanguages();
    }

    private void loadLanguages() {
        File languageDirectory = new File(plugin.getDataFolder(), "languages/");

        File defaultLanguageFile = new File(languageDirectory, "en_US.yml");
        File trLanguageFile = new File(languageDirectory, "tr_TR.yml");

        if (!languageDirectory.exists()) {
            if (!languageDirectory.mkdirs()) {
                plugin.getLogger().severe("Failed to create language directory: " + languageDirectory.getAbsolutePath());
                return;
            }
        }

        if (!defaultLanguageFile.exists()) {
            plugin.saveResource("languages/en_US.yml", false);
        }

        if (!trLanguageFile.exists()) {
            plugin.saveResource("languages/tr_TR.yml", false);
        }

        String locale = plugin.getConfig().getString("lang", "en_US");
        File selectedLanguageFile = new File(languageDirectory, locale + ".yml");

        if (!selectedLanguageFile.exists()) {
            plugin.saveResource("languages/" + locale + ".yml", false);
        }

        try {
            FileConfiguration translations = new YamlConfiguration();
            translations.load(selectedLanguageFile);
            for (String key : translations.getKeys(true)) {
                Object value = translations.get(key);
                if (value != null) {
                    translationMap.put(key, value);
                } else {
                    plugin.getLogger().warning("Null value found for key: " + key + " in language file.");
                }
            }

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load language file: " + selectedLanguageFile.getName());
            plugin.getLogger().severe("Error: " + e.getMessage());
        } catch (InvalidConfigurationException e) {
            plugin.getLogger().severe("Invalid YAML format in language file: " + selectedLanguageFile.getName());
            plugin.getLogger().severe("Error: " + e.getMessage());
        }
    }
    @SuppressWarnings("unused")
    public String get(String key) {
        Object value = translationMap.get(key);
        if (value instanceof String) {
            return processText((String) value);
        }
        return processText("Translation not found for key: " + key);
    }

    @SuppressWarnings("unused")
    public String get(String key, Player player) {
        Object value = translationMap.get(key);
        if (value instanceof String processedValue) {
            return processText(processedValue);
        }
        return processText("Translation not found for key: " + key);
    }
    @SuppressWarnings("unused")
    public List<String> getList(String key) {
        Object value = translationMap.get(key);

        if (value instanceof List<?> list) {
            List<String> processedList = new ArrayList<>();
            for (Object obj : list) {
                if (obj instanceof String line) {
                    processedList.add(processText(line));
                }
            }
            return processedList;
        }
        return List.of("Translation list not found for key: " + key);
    }

    @SuppressWarnings("unused")
    public List<String> getList(String key, Player player) {
        Object value = translationMap.get(key);

        if (value instanceof List<?> list) {
            List<String> processedList = new ArrayList<>();
            for (Object obj : list) {
                if (obj instanceof String line) {
                    processedList.add(processText(replacePlaceholders(line, player)));
                }
            }
            return processedList;
        }
        return List.of("Translation list not found for key: " + key);
    }

    public String getBoolean(boolean bool) {
        return bool ? get("true") : get("false");
    }

    @SuppressWarnings("unused")
    public List<String> getList(String key, NClaim nClaim) {
        Object value = translationMap.get(key);

        if (value instanceof List<?> list) {
            List<String> processedList = new ArrayList<>();
            for (Object obj : list) {
                if (obj instanceof String line) {
                    String placeholder = replacePlaceholders(line, nClaim);
                    processedList.add(processText(placeholder));
                }
            }
            return processedList;
        }
        return List.of("Translation list not found for key: " + key);
    }

    public String replacePlaceholders(String text, Player player) {
        return text.replace("%player_name%", player.getName())
                .replace("%player_health%", String.valueOf(player.getHealth() / 2.0))
                .replace("%player_level%", String.valueOf(player.getLevel()));
    }

    public String replacePlaceholders(String text, NClaim nClaim){
        Location lesser = nClaim.getClaim().getLesserBoundaryCorner();
        Location greater = nClaim.getClaim().getGreaterBoundaryCorner();
        double centerX = (lesser.getX() + greater.getX()) / 2;
        double centerY = (lesser.getY() + greater.getY()) / 2;
        double centerZ = (lesser.getZ() + greater.getZ()) / 2;

        return text.replace("%claim_id%", String.valueOf(nClaim.getClaim().getID()))
                .replace("%claim_name%", nClaim.getClaimName())
                .replace("%claim_owner%", nClaim.getClaim().getOwnerName())
                .replace("%claim_time%", formatTime(nClaim.getTime()))
                .replace("%claim_size%", nClaim.getClaim().getWidth() + "x" + nClaim.getClaim().getHeight())
                .replace("%claim_height%", String.valueOf(nClaim.getClaim().getHeight()))
                .replace("%claim_width%", String.valueOf(nClaim.getClaim().getWidth()))
                .replace("%claim_coordinate%", centerX + "," + centerY + "," + centerZ)
                .replace("%explosion%", getBoolean(nClaim.getClaimSettings().isAllowExplosives()))
                .replace("%pvp%", getBoolean(nClaim.getClaimSettings().isAllowPvP()))
                .replace("%entering%", getBoolean(nClaim.getClaimSettings().isEntering()))
                .replace("%elytra%", getBoolean(nClaim.getClaimSettings().canUseElytra()))
                .replace("%leaf_decay%", getBoolean(nClaim.getClaimSettings().isLeafDecay()))
                .replace("%animal_spawning%", getBoolean(nClaim.getClaimSettings().isSpawnAnimals()))
                .replace("%monster_spawning%", getBoolean(nClaim.getClaimSettings().isSpawnMonsters()))
                .replace("%fire_spread%", getBoolean(nClaim.getClaimSettings().isFireSpread()))
                .replace("%price%", String.valueOf(XGPAddon.getInstance().getConfig().getInt("settings.claimTimePrice")))
                .replace("%x%", String.valueOf(nClaim.getClaim().getLesserBoundaryCorner().getBlockX()))
                .replace("%y%", String.valueOf(nClaim.getClaim().getLesserBoundaryCorner().getBlockY()))
                .replace("%z%", String.valueOf(nClaim.getClaim().getLesserBoundaryCorner().getBlockZ()))
                .replace("%time%", formatTime(nClaim.getTime()));

    }

    @SuppressWarnings("unused")
    public List<String> getWithPlaceholdersList(String key, Map<String, String> placeholders) {
        Object value = translationMap.get(key);

        if (value instanceof List<?> list) {
            List<String> translatedList = new ArrayList<>();

            for (Object obj : list) {
                if (obj instanceof String translation) {
                    if (placeholders != null) {
                        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                            translation = translation.replace(entry.getKey(), entry.getValue());
                        }
                    }
                    translatedList.add(processText(translation));
                }
            }
            return translatedList;
        }

        return List.of(processText("Translation not found for key: " + key));
    }


    @SuppressWarnings("unused")
    public String getWithPlaceholders(String key, Map<String, String> placeholders) {
        Object value = translationMap.get(key);

        if (value instanceof String translation) {
            if (placeholders != null) {
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    translation = translation.replace(entry.getKey(), entry.getValue());
                }
            }
            return processText(translation);
        }

        return processText("Translation not found for key: " + key);
    }

    @SuppressWarnings("unused")
    public void reload() {
        translationMap.clear();
        loadLanguages();
    }

    public void loadLangs(XGPAddon plugin) {
        File languageFile = new File(plugin.getDataFolder(), "languages/" + plugin.getConfig().getString("lang", "en_US") + ".yml");

        FileConfiguration translations = YamlConfiguration.loadConfiguration(languageFile);
        translationMap.clear();
        for (String key : translations.getKeys(true)) {
            Object value = translations.get(key);
            if (value != null) {
                translationMap.put(key, value);
            } else {
                NLogger.error("Null value found for key: " + key + " in language file.");
            }
        }
    }

    public static String formatTime(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;

        StringBuilder formatted = new StringBuilder();

        if (days > 0) formatted.append(days).append(" " + XGPAddon.getInstance().getLanguageLoader().get("days") + " ");
        if (hours > 0) formatted.append(hours).append(" " + XGPAddon.getInstance().getLanguageLoader().get("hours") + " ");
        if (minutes > 0) formatted.append(minutes).append(" " + XGPAddon.getInstance().getLanguageLoader().get("minutes") + " ");

        return formatted.toString().trim();
    }
}