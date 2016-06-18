package com.jroossien.treasure.commands;

import com.jroossien.treasure.TreasurePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class Commands {
    private TreasurePlugin tr;

    public Commands(TreasurePlugin tr) {
        this.tr = tr;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return false;
    }
}