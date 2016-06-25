package com.jroossien.treasure.pool;

import com.jroossien.boxx.util.Utils;
import com.jroossien.treasure.TreasurePlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PresetPoolManager {

    private TreasurePlugin tr;
    private File poolDir;

    private Map<String, PresetPool> pools = new HashMap<>();


    public PresetPoolManager(TreasurePlugin tr) {
        this.tr = tr;

        poolDir = new File(tr.getDataFolder(), "\\data\\pools");
        poolDir.mkdirs();
    }

    public void loadPools() {
        Map<String, PresetPool> poolMap = new HashMap<>();
        Map<String, File> configFiles = Utils.getFiles(poolDir, "yml");

        for (Map.Entry<String, File> entry : configFiles.entrySet()) {
            YamlConfiguration presetCfg = YamlConfiguration.loadConfiguration(entry.getValue());
            poolMap.put(entry.getKey().toLowerCase(), new PresetPool(presetCfg, entry.getKey()));
        }

        pools = poolMap;
    }

    public void savePools() {
        for (PresetPool pool : pools.values()) {
            pool.save();
        }
    }

    public PresetPool createPool(String name) {
        if (hasPool(name)) {
            return getPool(name);
        }

        File file = new File(poolDir, name + ".yml");
        YamlConfiguration presetCfg = YamlConfiguration.loadConfiguration(file);

        PresetPool pool = new PresetPool(presetCfg, name);
        pools.put(name.toLowerCase(), pool);

        return pool;
    }

    public void setPool(PresetPool pool) {
        pools.put(pool.getName().toLowerCase(), pool);
    }

    public PresetPool getPool(String name) {
        if (name == null || !hasPool(name.toLowerCase())) {
            return null;
        }
        return pools.get(name.toLowerCase());
    }

    public boolean hasPool(String name) {
        return pools.containsKey(name.toLowerCase());
    }

    public Map<String, PresetPool> getPools() {
        return pools;
    }

    public File getPoolDir() {
        return poolDir;
    }



}
