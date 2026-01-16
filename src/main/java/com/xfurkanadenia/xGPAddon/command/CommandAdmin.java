package com.xfurkanadenia.xGPAddon.command;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class CommandAdmin implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            if(player.hasPermission("ngpaddon.admin") || player.isOp()){
                XGPAddon main = XGPAddon.getInstance();
                main.saveDefaultConfig();
                main.reloadConfig();
                main.getLanguageLoader().loadLangs(main);
                main.getGuiManager().guis.clear();
                main.getGuiManager().loadGuis();
                player.sendMessage(XGPAddon.getInstance().getLanguageLoader().get("config-reloaded"));
            }

        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1){
            return List.of("reload");
        }
        return List.of();
    }
}
