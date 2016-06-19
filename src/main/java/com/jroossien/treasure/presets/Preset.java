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

import com.jroossien.boxx.options.single.MaterialO;
import com.jroossien.boxx.util.item.EItem;
import com.jroossien.boxx.util.item.ItemParser;
import com.jroossien.treasure.TreasurePlugin;
import com.jroossien.treasure.treasure.ContentMode;
import com.jroossien.treasure.treasure.InteractMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.material.MaterialData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Preset {

    private YamlConfiguration cfg;

    private String name;

    private int minRarity;
    private int maxRarity;
    private List<EItem> customItems = new ArrayList<>();

    private int minItems;
    private int maxItems;
    private int itemLimit;

    private double rareChance;

    private MaterialData treasureBlock;
    private String title;
    private String hologram;

    private boolean protection;
    private String permission;

    private EItem key;
    private boolean consumeKey;

    private int refillTime; // [seconds]
    private boolean refillRecreate;
    private int lockTime; // [seconds]
    private int despawnTime; // [seconds]

    private InteractMode interactMode;
    private ContentMode contentMode;

    public Preset(YamlConfiguration cfg, String name) {
        this.cfg = cfg;
        this.name = name;

        if (!cfg.contains("items.minRarity") || !cfg.isInt("items.minRarity"))
            cfg.set("items.minRarity", 1);
        this.minRarity = cfg.getInt("items.minRarity");
        if (!cfg.contains("items.maxRarity") || !cfg.isInt("items.maxRarity"))
            cfg.set("items.maxRarity", 100);
        this.maxRarity = cfg.getInt("items.maxRarity");

        if (!cfg.contains("items.minCount") || !cfg.isInt("items.minCount"))
            cfg.set("items.minCount", 3);
        this.minItems = cfg.getInt("items.minCount");
        if (!cfg.contains("items.maxCount") || !cfg.isInt("items.maxCount"))
            cfg.set("items.maxCount", 8);
        this.maxItems = cfg.getInt("items.maxCount");

        if (!cfg.contains("items.playerLimit") || !cfg.isInt("items.playerLimit"))
            cfg.set("items.playerLimit", 0);
        this.itemLimit = cfg.getInt("items.playerLimit");

        customItems.clear();
        if (cfg.contains("items.custom")) {
            for (String item : cfg.getStringList("items.custom")) {
                ItemParser parser = new ItemParser(item, TreasurePlugin.get().getServer().getConsoleSender(), true);
                if (parser.isValid()) {
                    customItems.add(parser.getItem());
                } else {
                    TreasurePlugin.get().warn("Failed to load custom item for preset '" + name + "' Error: '" + parser.getError() + "' Item string: '" + item + "'");
                }
            }
        }

        if (!cfg.contains("items.rareChance") || !cfg.isDouble("items.rareChance"))
            cfg.set("items.rareChance", 0);
        this.rareChance = Math.min(Math.max(cfg.getDouble("items.rareChance"), 0d), 1d);


        MaterialO materialO = new MaterialO().blocks(true).def(new MaterialData(Material.CHEST));
        if (!cfg.contains("treasure.block") || !cfg.isString("items.block") || !materialO.parse(cfg.getString("treasure.block")))
            cfg.set("treasure.block", "chest");
        this.treasureBlock = materialO.getValue();

        if (!cfg.contains("treasure.title") || !cfg.isString("treasure.title"))
            cfg.set("treasure.title", "&9&lTreasure");
        this.title = cfg.getString("treasure.title");

        if (!cfg.contains("treasure.hologram") || !cfg.isString("treasure.hologram"))
            cfg.set("treasure.hologram", "");
        this.hologram = cfg.getString("treasure.hologram");

        if (!cfg.contains("treasure.protected") || !cfg.isBoolean("treasure.protected"))
            cfg.set("treasure.protected", false);
        this.protection = cfg.getBoolean("treasure.protected");

        if (!cfg.contains("treasure.permission") || !cfg.isString("treasure.permission"))
            cfg.set("treasure.permission", "");
        this.permission = cfg.getString("treasure.permission");

        if (!cfg.contains("treasure.keyItem") || !cfg.isString("treasure.keyItem"))
            cfg.set("treasure.keyItem", "");
        ItemParser parser = new ItemParser(cfg.getString("treasure.keyItem"), TreasurePlugin.get().getServer().getConsoleSender(), true);
        key = parser.getItem();

        if (!cfg.contains("treasure.consumeKey") || !cfg.isBoolean("treasure.consumeKey"))
            cfg.set("treasure.consumeKey", true);
        this.consumeKey = cfg.getBoolean("treasure.consumeKey");

        if (!cfg.contains("treasure.timer.refill") || !cfg.isInt("treasure.timer.refill"))
            cfg.set("treasure.timer.refill", 0);
        this.refillTime = cfg.getInt("treasure.timer.refill");
        if (!cfg.contains("treasure.timer.refillRecreate") || !cfg.isBoolean("treasure.timer.refillRecreate"))
            cfg.set("treasure.timer.refillRecreate", false);
        this.refillRecreate = cfg.getBoolean("treasure.timer.refillRecreate");

        if (!cfg.contains("treasure.timer.lock") || !cfg.isInt("treasure.timer.lock"))
            cfg.set("treasure.timer.lock", 0);
        this.lockTime = cfg.getInt("treasure.timer.lock");

        if (!cfg.contains("treasure.timer.despawn") || !cfg.isInt("treasure.timer.despawn"))
            cfg.set("treasure.timer.despawn", 0);
        this.despawnTime = cfg.getInt("treasure.timer.despawn");


        if (!cfg.contains("treasure.interactMode") || !cfg.isString("treasure.interactMode") || InteractMode.fromString(cfg.getString("treasure.interactMode")) == null)
            cfg.set("treasure.interactMode", InteractMode.OPEN.toString());
        this.interactMode = InteractMode.fromString(cfg.getString("treasure.interactMode"));

        if (!cfg.contains("treasure.contentMode") || !cfg.isString("treasure.contentMode") || ContentMode.fromString(cfg.getString("treasure.contentMode")) == null)
            cfg.set("treasure.contentMode", ContentMode.GLOBAL.toString());
        this.contentMode = ContentMode.fromString(cfg.getString("treasure.contentMode"));

        save();
    }

    public YamlConfiguration getCfg() {
        return cfg;
    }

    public void save() {
        TreasurePlugin.get().getPM().setPreset(this);
        File file = new File(TreasurePlugin.get().getPM().getPresetDir(), getName() + ".yml");
        try {
            getCfg().save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getName() {
        return name;
    }


    public int getMinRarity() {
        return minRarity;
    }

    public void setMinRarity(int minRarity) {
        this.minRarity = Math.min(Math.max(minRarity, TreasurePlugin.MIN_RARITY), TreasurePlugin.MAX_RARITY);
        cfg.set("items.minRarity", this.minRarity);
    }

    public int getMaxRarity() {
        return maxRarity;
    }

    public void setMaxRarity(int maxRarity) {
        this.maxRarity = Math.min(Math.max(maxRarity, TreasurePlugin.MIN_RARITY), TreasurePlugin.MAX_RARITY);
        cfg.set("items.maxRarity", this.maxRarity);
    }

    public List<EItem> getCustomItems() {
        return customItems;
    }

    public void setCustomItems(List<EItem> customItems) {
        this.customItems = customItems;
    }

    public int getMinItems() {
        return minItems;
    }

    public void setMinItems(int minItems) {
        this.minItems = minItems;
        cfg.set("items.minCount", this.minItems);
    }

    public int getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(int maxItems) {
        this.maxItems = maxItems;
        cfg.set("items.maxCount", this.maxItems);
    }

    public int getItemLimit() {
        return itemLimit;
    }

    public void setItemLimit(int itemLimit) {
        this.itemLimit = itemLimit;
        cfg.set("items.playerLimit", itemLimit);
    }

    public double getRareChance() {
        return rareChance;
    }

    public void setRareChance(double rareChance) {
        this.rareChance = Math.min(Math.max(rareChance, 0d), 1d);
        cfg.set("items.rareChance", this.rareChance);
    }

    public MaterialData getTreasureBlock() {
        return treasureBlock;
    }

    public void setTreasureBlock(MaterialData treasureBlock) {
        this.treasureBlock = treasureBlock;
        cfg.set("treasure.block", this.treasureBlock);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        cfg.set("treasure.title", this.title);
    }

    public String getHologram() {
        return hologram;
    }

    public void setHologram(String hologram) {
        this.hologram = hologram;
        cfg.set("treasure.hologram", this.hologram);
    }

    public boolean isProtection() {
        return protection;
    }

    public void setProtection(boolean protection) {
        this.protection = protection;
        cfg.set("treasure.protected", this.protection);
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
        cfg.set("treasure.permission", this.permission);
    }

    public EItem getKey() {
        return key;
    }

    public void setKey(EItem key) {
        this.key = key;
        cfg.set("treasure.keyItem", new ItemParser(this.key).getString());
    }

    public boolean isConsumeKey() {
        return consumeKey;
    }

    public void setConsumeKey(boolean consumeKey) {
        this.consumeKey = consumeKey;
        cfg.set("treasure.consumeKey", this.consumeKey);
    }

    public int getRefillTime() {
        return refillTime;
    }

    public void setRefillTime(int refillTime) {
        this.refillTime = refillTime;
        cfg.set("treasure.timer.refill", this.refillTime);
    }

    public boolean isRefillRecreate() {
        return refillRecreate;
    }

    public void setRefillRecreate(boolean refillRecreate) {
        this.refillRecreate = refillRecreate;
        cfg.set("treasure.timer.refillRecreate", this.refillRecreate);
    }

    public int getLockTime() {
        return lockTime;
    }

    public void setLockTime(int lockTime) {
        this.lockTime = lockTime;
        cfg.set("treasure.timer.lock", this.lockTime);
    }

    public int getDespawnTime() {
        return despawnTime;
    }

    public void setDespawnTime(int despawnTime) {
        this.despawnTime = despawnTime;
        cfg.set("treasure.timer.despawn", this.despawnTime);
    }

    public InteractMode getInteractMode() {
        return interactMode;
    }

    public void setInteractMode(InteractMode interactMode) {
        this.interactMode = interactMode;
        cfg.set("treasure.interactMode", this.interactMode.toString());
    }

    public ContentMode getContentMode() {
        return contentMode;
    }

    public void setContentMode(ContentMode contentMode) {
        this.contentMode = contentMode;
        cfg.set("treasure.contentMode", this.contentMode.toString());
    }
}
