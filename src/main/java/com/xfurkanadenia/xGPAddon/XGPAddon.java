package com.xfurkanadenia.xGPAddon;

import com.xfurkanadenia.xGPAddon.command.CommandAdmin;
import com.xfurkanadenia.xGPAddon.command.CommandPlayer;
import com.xfurkanadenia.xGPAddon.integration.CombatIntegration;
import com.xfurkanadenia.xGPAddon.integration.GPIntegration;
import com.xfurkanadenia.xGPAddon.integration.PlaceholderIntegration;
import com.xfurkanadenia.xGPAddon.integration.VaultIntegration;
import com.xfurkanadenia.xGPAddon.listener.*;
import com.xfurkanadenia.xGPAddon.manager.ClaimManager;
import com.xfurkanadenia.xGPAddon.manager.ConfigurationManager;
import com.xfurkanadenia.xGPAddon.manager.DataManager;
import com.xfurkanadenia.xGPAddon.manager.GUIManager;
import com.xfurkanadenia.xGPAddon.task.TaskAutoSave;
import com.xfurkanadenia.xGPAddon.task.TaskClaimEnter;
import com.xfurkanadenia.xGPAddon.task.TaskClaimTimer;
import com.xfurkanadenia.xGPAddon.task.TaskClaimTrustEvent;
import com.xfurkanadenia.xGPAddon.util.NLogger;
import fr.mrmicky.fastinv.FastInvManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class XGPAddon extends JavaPlugin {

    private static XGPAddon instance;

    private ConfigurationManager configurationManager;
    private DataManager dataManager;

    private LanguageLoader languageLoader;

    private VaultIntegration vaultIntegration;
    private GPIntegration gpIntegration;
    CombatIntegration deluxeCombatIntegration;

    private GUIManager guiManager;

    private TaskClaimTimer taskClaimTimer;
    private TaskClaimTrustEvent taskClaimTrustTimer;

    private TaskClaimEnter taskClaimEnter;

    private long claimCheckTime;

    //Guis
    public HashMap<String, HashMap<String, Object>> MainMenu = new HashMap<>();
    public HashMap<String, HashMap<String, Object>> ClaimMenu = new HashMap<>();
    public HashMap<String, HashMap<String, Object>> GeneralSettingsMenu = new HashMap<>();
    public HashMap<String, HashMap<String, Object>> PlayerSettingsMenu = new HashMap<>();
    public HashMap<String, HashMap<String, Object>> BanMenu = new HashMap<>();
    public HashMap<String, HashMap<String, Object>> AbandonConfirmMenu = new HashMap<>();
    public HashMap<String, HashMap<String, Object>> TrustedSettingsMenu = new HashMap<>();

    @Override
    public void onEnable() {

        instance = this;

        saveDefaultConfig();
        saveConfig();

        FastInvManager.register(this);
        languageLoader = new LanguageLoader(this);

        gpIntegration = new GPIntegration(this);
        deluxeCombatIntegration = new CombatIntegration();

        configurationManager = new ConfigurationManager();
        configurationManager.loadConfigurations();

        dataManager = new DataManager(this);
        dataManager.loadClaims();

        vaultIntegration = new VaultIntegration(this);

        guiManager = new GUIManager(this);
        guiManager.loadGuis();

        if (getConfig().getBoolean("auto-save.enabled")) {
            TaskAutoSave taskAutoSave = new TaskAutoSave();
            taskAutoSave.runTaskTimerAsynchronously(this, getConfig().getLong("auto-save.interval") * 20, getConfig().getLong("auto-save.interval") * 20);
        }

        taskClaimTimer = new TaskClaimTimer();
        taskClaimTrustTimer = new TaskClaimTrustEvent();
        taskClaimEnter = new TaskClaimEnter();



        claimCheckTime = getConfig().getLong("settings.claim-check-time", 1) * 20;
        taskClaimTimer.runTaskTimer(this, 40, claimCheckTime);
        taskClaimTrustTimer.runTaskTimer(this, 40, claimCheckTime);
        taskClaimEnter.runTaskTimer(this, 40, claimCheckTime);

        getServer().getPluginManager().registerEvents(new ClaimCreateListener(), this);
        getServer().getPluginManager().registerEvents(new ClaimDeleteListener(), this);
        getServer().getPluginManager().registerEvents(new ClaimSettingsListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(), this);
        getServer().getPluginManager().registerEvents(new ClaimTrustListener(), this);
        getServer().getPluginManager().registerEvents(new ClaimTransferListener(), this);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            ClaimManager.startAllClaims();

            dataManager.addClaimsToTimer();
        }, 40L);


        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderIntegration().register();
            NLogger.info("PlaceholderAPI found. Registered placeholders.");
        }

        getCommand("claimlist").setExecutor(new CommandPlayer());

        getCommand("xgpaddon").setExecutor(new CommandAdmin());

    }

    @Override
    public void onDisable() {
        dataManager.saveClaims();
        configurationManager.saveConfigurations();
    }



    public static XGPAddon getInstance() {
        return instance;
    }

    public LanguageLoader getLanguageLoader() {
        return languageLoader;
    }

    public VaultIntegration getVaultIntegration() {
        return vaultIntegration;
    }
    public CombatIntegration getDeluxeCombatIntegration() {
        return deluxeCombatIntegration;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public TaskClaimTimer getTaskClaimTimer() {
        return taskClaimTimer;
    }

    public long getClaimCheckTime() {
        return claimCheckTime;
    }

    public GUIManager getGuiManager() { return guiManager; }
}
