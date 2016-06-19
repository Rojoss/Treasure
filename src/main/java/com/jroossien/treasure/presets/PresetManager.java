/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Rojoss <http://jroossien.com>
 * Copyright (c) 2016 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.jroossien.treasure.presets;

import com.jroossien.boxx.util.Utils;
import com.jroossien.treasure.TreasurePlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class PresetManager {

    private TreasurePlugin tr;
    private File presetDir;

    private Map<String, Preset> presets = new HashMap<>();

    public PresetManager(TreasurePlugin tr) {
        this.tr = tr;

        presetDir = new File(tr.getDataFolder(), "\\data\\presets");
        presetDir.mkdirs();
    }

    public void loadPresets() {
        Map<String, Preset> presetMap = new HashMap<>();
        Map<String, File> configFiles = Utils.getFiles(presetDir, "yml");

        for (Map.Entry<String, File> entry : configFiles.entrySet()) {
            YamlConfiguration presetCfg = YamlConfiguration.loadConfiguration(entry.getValue());
            presetMap.put(entry.getKey().toLowerCase(), new Preset(presetCfg, entry.getKey()));
        }

        presets = presetMap;
    }

    public void savePresets() {
        for (Preset preset : presets.values()) {
            preset.save();
        }
    }

    public Preset createPreset(String name) {
        if (hasPreset(name)) {
            return getPreset(name);
        }

        File file = new File(presetDir, name + ".yml");
        YamlConfiguration presetCfg = YamlConfiguration.loadConfiguration(file);

        Preset preset = new Preset(presetCfg, name);
        presets.put(name.toLowerCase(), preset);

        return preset;
    }

    public void setPreset(Preset preset) {
        presets.put(preset.getName().toLowerCase(), preset);
    }

    public Preset getPreset(String name) {
        if (name == null || !hasPreset(name.toLowerCase())) {
            return null;
        }
        return presets.get(name.toLowerCase());
    }

    public boolean hasPreset(String name) {
        return presets.containsKey(name.toLowerCase());
    }

    public Map<String, Preset> getPresets() {
        return presets;
    }

    public File getPresetDir() {
        return presetDir;
    }
}
