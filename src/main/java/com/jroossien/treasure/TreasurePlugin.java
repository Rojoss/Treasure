package com.jroossien.treasure;

import com.jroossien.boxx.commands.api.CmdRegistration;
import com.jroossien.boxx.commands.api.exception.CmdAlreadyRegisteredException;
import com.jroossien.boxx.messages.MessageConfig;
import com.jroossien.treasure.commands.TreasureCmd;
import com.jroossien.treasure.listeners.MainListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public class TreasurePlugin extends JavaPlugin {

    private static TreasurePlugin instance;
    private final Logger log = Logger.getLogger("Treasure");


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
        //TODO: Init configs

        //TODO: Init managers

        registerCommands();
        registerListeners();

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
}
