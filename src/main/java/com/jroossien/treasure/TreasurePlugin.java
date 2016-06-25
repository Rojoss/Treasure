package com.jroossien.treasure;

import com.jroossien.boxx.commands.api.CmdRegistration;
import com.jroossien.boxx.commands.api.exception.CmdAlreadyRegisteredException;
import com.jroossien.boxx.messages.MessageConfig;
import com.jroossien.treasure.commands.LootCmd;
import com.jroossien.treasure.commands.PresetsCmd;
import com.jroossien.treasure.commands.TreasureCmd;
import com.jroossien.treasure.config.LootCfg;
import com.jroossien.treasure.listeners.MainListener;
import com.jroossien.treasure.loot.LootManager;
import com.jroossien.treasure.loot.LootMenu;
import com.jroossien.treasure.pool.PresetPoolManager;
import com.jroossien.treasure.pool.PresetPoolMenu;
import com.jroossien.treasure.presets.PresetManager;
import com.jroossien.treasure.presets.PresetMenu;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public class TreasurePlugin extends JavaPlugin {

    public static final int MIN_RARITY = 1;
    public static final int MAX_RARITY = 100;

    private static TreasurePlugin instance;
    private final Logger log = Logger.getLogger("Treasure");

    private LootCfg lootCfg;

    private LootManager lm;
    private PresetManager pm;
    private PresetPoolManager ppm;

    private LootMenu lootMenu;
    private PresetMenu presetMenu;
    private PresetPoolMenu presetPoolMenu;


    @Override
    public void onDisable() {
        instance = null;
        log("disabled");
    }

    @Override
    public void onEnable() {
        instance = this;
        log.setParent(this.getLogger());

        loadMessages();

        lootCfg = new LootCfg("plugins/Treasure/data/Loot.yml");

        lm = new LootManager(this);
        pm = new PresetManager(this);
        pm.loadPresets();
        ppm = new PresetPoolManager(this);
        ppm.loadPools();

        registerCommands();
        registerListeners();

        lootMenu = new LootMenu();
        presetMenu = new PresetMenu();
        presetPoolMenu = new PresetPoolMenu();

        log("loaded successfully");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new MainListener(this), this);
        //TODO: Register listeners
    }

    private void registerCommands() {
        File configFile = new File(getDataFolder(), "commands.yml");
        try {
            CmdRegistration.register(this, new TreasureCmd(configFile));
            CmdRegistration.register(this, new LootCmd(configFile));
            CmdRegistration.register(this, new PresetsCmd(configFile));
        } catch (CmdAlreadyRegisteredException e) {
            e.printStackTrace();
        }
    }

    private void loadMessages() {
        new MessageConfig(this, "messages");
        new MessageConfig(this, "lootmenu");
        new MessageConfig(this, "presetmenu");
    }

    public void log(Object msg) {
        log.info("[Treasure " + getDescription().getVersion() + "] " + msg.toString());
    }

    public void warn(Object msg) {
        log.warning("[Treasure " + getDescription().getVersion() + "] " + msg.toString());
    }

    public static TreasurePlugin get() {
        return instance;
    }


    public LootCfg getLootCfg() {
        return lootCfg;
    }


    public LootManager getLM() {
        return lm;
    }

    public PresetManager getPM() {
        return pm;
    }

    public PresetPoolManager getPPM() {
        return ppm;
    }


    public LootMenu getLootMenu() {
        return lootMenu;
    }

    public PresetMenu getPresetMenu() {
        return presetMenu;
    }

    public PresetPoolMenu getPresetPoolMenu() {
        return presetPoolMenu;
    }
}
