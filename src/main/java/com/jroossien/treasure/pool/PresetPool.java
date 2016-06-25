package com.jroossien.treasure.pool;

import com.jroossien.boxx.util.Parse;
import com.jroossien.boxx.util.item.ItemParser;
import com.jroossien.treasure.TreasurePlugin;
import com.jroossien.treasure.presets.Preset;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PresetPool {
    private YamlConfiguration cfg;

    private String name;
    private Map<String, Integer> presets = new HashMap<>();

    public PresetPool(YamlConfiguration cfg, String name) {
        this.cfg = cfg;
        this.name = name;

        presets.clear();
        if (cfg.contains("presets")) {
            for (Map<?, ?> data : cfg.getMapList("presets")) {
                if (!data.containsKey("preset") || !data.containsKey("weight")) {
                    TreasurePlugin.get().warn("Failed to load preset pool " + name + "! Missing data for preset " + data.get("preset") +
                            " must have a weight and preset key/value.");
                    continue;
                }
                Integer weight = Parse.Int((String)data.get("weight"));
                if (weight == null || weight < 1) {
                    TreasurePlugin.get().warn("Failed to load preset pool " + name + "! Weight for preset " + data.get("preset") +
                            " must be a positive integer.");
                    continue;
                }
                presets.put((String)data.get("preset"), weight);
            }
        }
    }

    public YamlConfiguration getCfg() {
        return cfg;
    }

    public void save() {
        TreasurePlugin.get().getPPM().setPool(this);
        File file = new File(TreasurePlugin.get().getPPM().getPoolDir(), getName() + ".yml");
        try {
            getCfg().save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getName() {
        return name;
    }

    public Map<String, Integer> getPresets() {
        return presets;
    }
}
