package com.xfurkanadenia.xGPAddon.file;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.xfurkanadenia.xGPAddon.XGPAddon;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DataFile {


    private File path;
    private String fileName;

    private FileConfiguration config;

    public DataFile(String fileName) {
        this.fileName = fileName;
    }

    public void reloadConfig() {
        if(this.path == null)
            this.path = new File(XGPAddon.getInstance().getDataFolder(), fileName);
        config = YamlConfiguration.loadConfiguration(path);
        InputStream inputStream = XGPAddon.getInstance().getResource(fileName);
        if(inputStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));
            this.config.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig() {
        if(config == null) reloadConfig();
        return config;
    }

    public void saveConfig() {
        if(config == null || path == null) return;

        try {
            config.save(this.path);
        } catch (Exception err) {
            XGPAddon.getInstance().getLogger().severe(String.format("Config saving failed (%s): ", path) + err);
        }
    }

    public void saveDefaultConfig() {
        if(path == null)
            this.path = new File(XGPAddon.getInstance().getDataFolder(), fileName);
        if(!this.path.exists())
            XGPAddon.getInstance().saveResource(fileName, false);
    }

}