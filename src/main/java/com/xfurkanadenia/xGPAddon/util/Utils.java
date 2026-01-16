package com.xfurkanadenia.xGPAddon.util;
import com.xfurkanadenia.xGPAddon.Gradient;
import com.xfurkanadenia.xGPAddon.LanguageLoader;
import com.xfurkanadenia.xGPAddon.integration.DiscordWebhookIntegration;
import com.xfurkanadenia.xGPAddon.integration.DiscordWebhookIntegration.EmbedObject;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import fr.mrmicky.fastinv.FastInv;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.xfurkanadenia.xGPAddon.XGPAddon;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;


import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static String translateColorCodes(String message) {
        if (message == null) return "";

        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String hexCode = matcher.group(); // Örn: &#FFAA00
            String color = hexCode.substring(1); // '#' ile başlasın: #FFAA00
            message = message.replace(hexCode, ChatColor.of(color).toString());
            matcher = pattern.matcher(message);
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String placeholders(String txt, Map<String, String> vars) {
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            txt = txt.replace("%" + entry.getKey() + "%", String.valueOf(entry.getValue()));
        }
        return txt;
    }
    public static String placeholders(String txt, Player player, Map<String, String> vars) {
        if (XGPAddon.getInstance().getServer().getPluginManager().getPlugin("PlaceHolderAPI") != null) {
            boolean parse = true;
            String pMsg = txt;
            while (parse) {
                pMsg = PlaceholderAPI.setPlaceholders(player, pMsg);
                if (txt.equalsIgnoreCase(pMsg)) {
                    parse = false;
                }
                txt = pMsg;
            }
        }



        txt = txt
                .replaceAll("%player%", player.getName());

        for (Map.Entry<String, String> entry : vars.entrySet()) {
            txt = txt.replace("%" + entry.getKey() + "%", String.valueOf(entry.getValue()));
        }
        return txt;
    }

    public static String getFormatted(String text, Player player, Map<String, String> vars) {
        String processed = placeholders(text, player, vars);
        if (Gradient.hasColorFormat(text)) {
            processed = Gradient.processText(processed);
        }

        return translateColorCodes(processed);
    }


    public static ItemStack getFormattedItem(ItemStack i, Player p, Map<String, String> vars) {
        ItemStack item = i.clone();
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta.hasDisplayName()) itemMeta.setDisplayName(getFormatted(itemMeta.getDisplayName(), p, vars));
        if (itemMeta.hasLore()) {
            List<String> lore = itemMeta.getLore();
            lore.forEach(txt ->
                    lore.set(lore.indexOf(txt), getFormatted(txt, p, vars))
            );
            itemMeta.setLore(lore);
        }
        itemMeta.addItemFlags(ItemFlag.values());
        item.setItemMeta(itemMeta);
        return item;
    }

    public static Map<String, String> getClaimVariables(NClaim nClaim) {
        int centerX = (nClaim.getClaim().getLesserBoundaryCorner().getBlockX() + nClaim.getClaim().getGreaterBoundaryCorner().getBlockX()) / 2;
        int centerY = (nClaim.getClaim().getLesserBoundaryCorner().getBlockY() + nClaim.getClaim().getGreaterBoundaryCorner().getBlockY()) / 2;
        int centerZ = (nClaim.getClaim().getLesserBoundaryCorner().getBlockZ() + nClaim.getClaim().getGreaterBoundaryCorner().getBlockZ()) / 2;

        LanguageLoader languageLoader = XGPAddon.getInstance().getLanguageLoader();

        Map<String, String> vars = new HashMap<>();

        // Claim bilgileri
        vars.put("claim_id", String.valueOf(nClaim.getClaim().getID()));
        vars.put("claim_name", nClaim.getClaimName());
        vars.put("claim_owner", nClaim.getClaim().getOwnerName());
        vars.put("claim_owner_uuid", String.valueOf(nClaim.getOwner()));
        vars.put("claim_time", String.valueOf(nClaim.getTime()));
        vars.put("claim_time_formatted", LanguageLoader.formatTime(nClaim.getTime()));
        vars.put("claim_size", nClaim.getClaim().getWidth() + "x" + nClaim.getClaim().getHeight());
        vars.put("claim_height", String.valueOf(nClaim.getClaim().getHeight()));
        vars.put("claim_width", String.valueOf(nClaim.getClaim().getWidth()));
        vars.put("claim_coordinate", centerX + "," + centerY + "," + centerZ);
        vars.put("explosion", languageLoader.getBoolean(nClaim.getClaimSettings().isAllowExplosives()));
        vars.put("pvp", languageLoader.getBoolean(nClaim.getClaimSettings().isAllowPvP()));
        vars.put("entering", languageLoader.getBoolean(nClaim.getClaimSettings().isEntering()));
        vars.put("elytra", languageLoader.getBoolean(nClaim.getClaimSettings().canUseElytra()));
        vars.put("leaf_decay", languageLoader.getBoolean(nClaim.getClaimSettings().isLeafDecay()));
        vars.put("animal_spawning", languageLoader.getBoolean(nClaim.getClaimSettings().isSpawnAnimals()));
        vars.put("monster_spawning", languageLoader.getBoolean(nClaim.getClaimSettings().isSpawnMonsters()));
        vars.put("fire_spread", languageLoader.getBoolean(nClaim.getClaimSettings().isFireSpread()));
        vars.put("price", String.valueOf(XGPAddon.getInstance().getConfig().getInt("settings.claimTimePrice")));
        vars.put("claim_x", String.valueOf(nClaim.getClaim().getLesserBoundaryCorner().getBlockX()));
        vars.put("claim_y", String.valueOf(nClaim.getClaim().getLesserBoundaryCorner().getBlockY()));
        vars.put("claim_z", String.valueOf(nClaim.getClaim().getLesserBoundaryCorner().getBlockZ()));
        vars.put("time", LanguageLoader.formatTime(nClaim.getTime()));

        return vars;
    }

    public static Map<String, String> getClaimVariablesPercent(NClaim nClaim) {
        int centerX = (nClaim.getClaim().getLesserBoundaryCorner().getBlockX() + nClaim.getClaim().getGreaterBoundaryCorner().getBlockX()) / 2;
        int centerY = (nClaim.getClaim().getLesserBoundaryCorner().getBlockY() + nClaim.getClaim().getGreaterBoundaryCorner().getBlockY()) / 2;
        int centerZ = (nClaim.getClaim().getLesserBoundaryCorner().getBlockZ() + nClaim.getClaim().getGreaterBoundaryCorner().getBlockZ()) / 2;

        LanguageLoader languageLoader = XGPAddon.getInstance().getLanguageLoader();

        Map<String, String> vars = new HashMap<>();

        // Claim bilgileri
        vars.put("%claim_id%", String.valueOf(nClaim.getClaim().getID()));
        vars.put("%claim_name%", nClaim.getClaimName());
        vars.put("%claim_owner%", nClaim.getClaim().getOwnerName());
        vars.put("%claim_owner_uuid%", String.valueOf(nClaim.getOwner()));
        vars.put("%claim_time%", String.valueOf(nClaim.getTime()));
        vars.put("%claim_time_formatted%", LanguageLoader.formatTime(nClaim.getTime()));
        vars.put("%claim_size%", nClaim.getClaim().getWidth() + "x" + nClaim.getClaim().getHeight());
        vars.put("%claim_height%", String.valueOf(nClaim.getClaim().getHeight()));
        vars.put("%claim_width%", String.valueOf(nClaim.getClaim().getWidth()));
        vars.put("%claim_coordinate%", centerX + "," + centerY + "," + centerZ);
        vars.put("%explosion%", languageLoader.getBoolean(nClaim.getClaimSettings().isAllowExplosives()));
        vars.put("%pvp%", languageLoader.getBoolean(nClaim.getClaimSettings().isAllowPvP()));
        vars.put("%entering%", languageLoader.getBoolean(nClaim.getClaimSettings().isEntering()));
        vars.put("%elytra%", languageLoader.getBoolean(nClaim.getClaimSettings().canUseElytra()));
        vars.put("%leaf_decay%", languageLoader.getBoolean(nClaim.getClaimSettings().isLeafDecay()));
        vars.put("%animal_spawning%", languageLoader.getBoolean(nClaim.getClaimSettings().isSpawnAnimals()));
        vars.put("%monster_spawning%", languageLoader.getBoolean(nClaim.getClaimSettings().isSpawnMonsters()));
        vars.put("%fire_spread%", languageLoader.getBoolean(nClaim.getClaimSettings().isFireSpread()));
        vars.put("%price%", String.valueOf(XGPAddon.getInstance().getConfig().getInt("settings.claimTimePrice")));
        vars.put("%claim_x%", String.valueOf(nClaim.getClaim().getLesserBoundaryCorner().getBlockX()));
        vars.put("%claim_y%", String.valueOf(nClaim.getClaim().getLesserBoundaryCorner().getBlockY()));
        vars.put("%claim_z%", String.valueOf(nClaim.getClaim().getLesserBoundaryCorner().getBlockZ()));
        vars.put("%time%", LanguageLoader.formatTime(nClaim.getTime()));

        return vars;
    }

    public static boolean isInt(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void pushPlayerBack(Player player, double strength) {
        Vector direction = player.getLocation().getDirection();
        Vector backward = direction.multiply(-1).normalize().multiply(strength);
        backward.setY(0.5);
        player.setVelocity(backward);
    }

    public static boolean isFloat(String str) {
        try {

            Float.parseFloat(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void sendWebhooks(NClaim nClaim) {
        FileConfiguration config = XGPAddon.getInstance().getConfig();
        ConfigurationSection webhookConfig = config.getConfigurationSection("webhook");
        if(!webhookConfig.getBoolean("enabled", false)) return;
        Map<String, String> placeholders = new HashMap<>(getClaimVariables(nClaim));
        placeholders.put("player", Bukkit.getOfflinePlayer(nClaim.getOwner()).getName());
        String url = placeholders(webhookConfig.getString("url", ""), placeholders);
        String username = placeholders(webhookConfig.getString("username", ""), placeholders);
        String avatarUrl = placeholders(webhookConfig.getString("avatar-url", ""), placeholders);
        String content = placeholders(webhookConfig.getString("content", ""), placeholders);
        ConfigurationSection embeds = webhookConfig.getConfigurationSection("embeds");
        DiscordWebhookIntegration webhook = new DiscordWebhookIntegration(url);
        if(!username.isEmpty()) webhook.setUsername(username);
        if(!avatarUrl.isEmpty()) webhook.setAvatarUrl(avatarUrl);
        if(!content.isEmpty()) webhook.setContent(content);
        if(!embeds.getKeys(false).isEmpty())
            for(String key : embeds.getKeys(false)) {
            ConfigurationSection embedCfg = embeds.getConfigurationSection(key);

            String title = placeholders(embedCfg.getString("title", ""), placeholders);
            String description = placeholders(embedCfg.getString("description", ""), placeholders);
            String color = embedCfg.getString("color", "#000000");
            ConfigurationSection fields = embedCfg.getConfigurationSection("fields");

            EmbedObject embed = new EmbedObject();

            if(!title.isEmpty()) embed.setTitle(title);
            if(!description.isEmpty()) embed.setDescription(description);
            if(!color.isEmpty()) embed.setColor(Color.decode(color));
            if(fields != null && !fields.getKeys(false).isEmpty())
                for(String fieldKey : fields.getKeys(false)) {
                ConfigurationSection fieldCfg = fields.getConfigurationSection(fieldKey);
                String name = placeholders(fieldCfg.getString("name", ""), placeholders);
                String value = placeholders(fieldCfg.getString("value", ""), placeholders);
                boolean inline = fieldCfg.getBoolean("inline", false);
                embed.addField(name, value, inline);
            }
            webhook.addEmbed(embed);

        }
        try {
            webhook.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}