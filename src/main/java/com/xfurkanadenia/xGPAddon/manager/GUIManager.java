package com.xfurkanadenia.xGPAddon.manager;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.util.Utils;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GUIManager {
    private final XGPAddon plugin;
    public final Map<String, FileConfiguration> guis = new HashMap<>();
    private final Pattern actionPattern = Pattern.compile("\\[(.*?)]\\s*(.*)");
    private static final Map<Player, FastInv> openGuis = new HashMap<>();

    public GUIManager(XGPAddon plugin) {
        this.plugin = plugin;
    }

    public void loadGuis() {
        String[] menus = { "mainMenu.yml", "claimMenu.yml", "settingsCategory.yml", "abandonConfirmMenu.yml", "playerSettingsMenu.yml", "generalSettingsMenu.yml", "trustedSettingsMenu.yml", "banMenu.yml" };

        guis.clear();

        File guisFolder = new File(plugin.getDataFolder(), "guis");
        if (!guisFolder.exists()) {
            guisFolder.mkdirs();
        }

        // Varsayılan dosyaları çıkar
        for (String menuFile : menus) {
            File file = new File(guisFolder, menuFile);
            if (!file.exists()) {
                plugin.saveResource("guis/" + menuFile, false);
                plugin.getLogger().info(menuFile + " varsayılan olarak çıkarıldı.");
            }
        }

        // Klasördeki tüm .yml dosyalarını yükle
        File[] files = guisFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String nameWithoutExt = file.getName().replaceFirst("[.][^.]+$", "");
                guis.put(nameWithoutExt, YamlConfiguration.loadConfiguration(file));
                plugin.getLogger().info(nameWithoutExt + " menüsü yüklendi.");
            }
        }
    }

    public ItemStack getItem(ConfigurationSection itemCfg, Player player) {
        return getItem(itemCfg, player, new HashMap<>());
    }

    public void openGui(Player player, FastInv gui) {
        gui.addCloseHandler(e -> openGuis.remove(player));
        gui.addOpenHandler(e -> openGuis.put(player, gui));
        gui.open(player);
    }

    public ItemStack getItem(ConfigurationSection itemCfg, Player player, Map<String, String> vars) {
        Material material = Material.getMaterial(Objects.requireNonNull(itemCfg.getString("material")));
        if (material == null) {
            plugin.getLogger().severe("Material \"" + itemCfg.getString("material") + "\" not found in " + itemCfg.getName());
            return null;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (itemCfg.contains("displayName") && meta != null) {
            meta.setDisplayName(Utils.getFormatted(itemCfg.getString("displayName"), player, vars));
        }

        if (itemCfg.contains("lore") && meta != null) {
            List<String> formattedLore = new ArrayList<>();
            for (String line : itemCfg.getStringList("lore")) {
                formattedLore.add(Utils.getFormatted(line, player, vars));
            }
            meta.setLore(formattedLore);
        }

        if (itemCfg.contains("customModelData") && meta != null) {
            meta.setCustomModelData(itemCfg.getInt("customModelData"));
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_DYE);
        item.setItemMeta(meta);
        return item;
    }

    public Map<String, String> getActions(String guiName, Integer slot) {
        Map<String, String> actions = new HashMap<>();
        FileConfiguration config = guis.get(guiName);

        if (config == null || !config.contains("items")) {
            return actions;
        }

        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) {
            return actions;
        }

        // Önce actions'ı olan item'ları kontrol et, sonra filler'ları
        List<String> sortedKeys = new ArrayList<>(itemsSection.getKeys(false));
        sortedKeys.sort((a, b) -> {
            boolean aHasActions = itemsSection.getConfigurationSection(a).contains("actions");
            boolean bHasActions = itemsSection.getConfigurationSection(b).contains("actions");
            if (aHasActions && !bHasActions) return -1;
            if (!aHasActions && bHasActions) return 1;
            return 0;
        });

        for (String key : sortedKeys) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection == null) continue;

            // Slot kontrolü
            List<Integer> itemSlots;
            if (itemSection.contains("slots")) {
                itemSlots = itemSection.getIntegerList("slots");
            } else if (itemSection.contains("slot")) {
                itemSlots = Arrays.asList(itemSection.getInt("slot"));
            } else {
                continue;
            }

            if (itemSlots.contains(slot)) {
                if (itemSection.contains("actions")) {
                    List<String> rawActions = itemSection.getStringList("actions");

                    for (String actionLine : rawActions) {
                        if (actionLine == null || actionLine.trim().isEmpty()) continue;

                        Matcher matcher = actionPattern.matcher(actionLine.trim());
                        if (matcher.matches()) {
                            String type = matcher.group(1).trim();
                            String value = matcher.group(2) != null ? matcher.group(2).trim() : "";
                            actions.put(type, value);
                        }
                    }
                    break;
                }
            }
        }

        return actions;
    }

    public void executeActions(Player player, String guiName, int slot, Map<String, String> vars) {
        Map<String, String> actions = getActions(guiName, slot);

        actions.forEach((actionType, value) -> {
            switch (actionType.toLowerCase()) {
                case "player":
                    Bukkit.dispatchCommand(player, Utils.placeholders(value, player, vars));
                    break;
                case "console":
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.placeholders(value, player, vars));
                    break;
                case "message":
                    player.sendMessage(Utils.placeholders(value, player, vars));
                    break;
                case "msg":
                    player.sendMessage(Utils.translateColorCodes(Utils.placeholders(value, player, vars)));
                    break;
                case "close":
                    player.closeInventory();
                    break;
                case "broadcast":
                    Bukkit.broadcastMessage(Utils.translateColorCodes(Utils.placeholders(value, player, vars)));
                    break;
            }
        });
    }

}