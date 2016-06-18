package com.jroossien.treasure;

import com.jroossien.boxx.commands.api.CmdRegistration;
import com.jroossien.boxx.commands.api.exception.CmdAlreadyRegisteredException;
import com.jroossien.boxx.messages.MessageConfig;
import com.jroossien.treasure.commands.LootCmd;
import com.jroossien.treasure.commands.TreasureCmd;
import com.jroossien.treasure.config.LootCfg;
import com.jroossien.treasure.listeners.MainListener;
import com.jroossien.treasure.loot.LootManager;
import com.jroossien.treasure.loot.LootMenu;
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

    private LootMenu lootMenu;


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

        registerCommands();
        registerListeners();

        lootMenu = new LootMenu();

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
        } catch (CmdAlreadyRegisteredException e) {
            e.printStackTrace();
        }
    }

    private void loadMessages() {
        new MessageConfig(this, "messages");
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


    public LootMenu getLootMenu() {
        return lootMenu;
    }
}
