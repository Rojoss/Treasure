package com.jroossien.treasure;

import com.jroossien.treasure.commands.Commands;
import com.jroossien.treasure.listeners.MainListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class TreasurePlugin extends JavaPlugin {

    private static TreasurePlugin instance;
    private final Logger log = Logger.getLogger("Treasure");

    private Commands cmds;


    @Override
    public void onDisable() {
        instance = null;
        log("disabled");
    }

    @Override
    public void onEnable() {
        instance = this;
        log.setParent(this.getLogger());

        //TODO: Init configs

        //TODO: Init managers

        cmds = new Commands(this);

        registerListeners();

        log("loaded successfully");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return cmds.onCommand(sender, cmd, label, args);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new MainListener(this), this);
        //TODO: Register listeners
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
