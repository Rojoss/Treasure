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

package com.jroossien.treasure.loot;

import com.jroossien.boxx.util.item.EItem;
import com.jroossien.treasure.TreasurePlugin;
import com.jroossien.treasure.config.LootCfg;

import java.util.*;

public class LootManager {

    private TreasurePlugin tr;
    private LootCfg cfg;
    private List<Loot> loot = new ArrayList<Loot>();

    public LootManager(TreasurePlugin tr) {
        this.tr = tr;
        this.cfg = tr.getLootCfg();
        loadLoot();
    }

    public void loadLoot() {
        loot.clear();
        List<Map<String, String>> cfgLoot = new ArrayList<>(cfg.loot);
        for (Map<String, String> entry : cfgLoot) {
            Loot lootItem = new Loot(entry);
            if (!lootItem.isValid()) {
                continue;
            }
            loot.add(lootItem);
        }
        sort();
    }

    public Loot addLoot(EItem item, int rarity) {
        Loot lootItem = getLoot(item, rarity);
        if (lootItem != null) {
            return lootItem;
        }

        lootItem = new Loot(item, rarity);
        if (!lootItem.isValid()) {
            return null;
        }

        loot.add(lootItem);
        cfg.loot.add(lootItem.getData());

        sort();

        cfg.save();
        return lootItem;
    }

    public void deleteLoot(int index) {
        Loot lootItem = loot.get(index);

        if (cfg.loot.contains(lootItem.getData())) {
            cfg.loot.remove(lootItem.getData());
            cfg.save();
        }

        loot.remove(index);
        sort();
    }

    public Loot getLoot(EItem item, int rarity) {
        for (Loot lootItem : loot) {
            if (lootItem.getRarity() == null || lootItem.getRarity() != rarity) {
                continue;
            }
            if (lootItem.getItem() == null || lootItem.getItem().equals(item)) {
                continue;
            }
            return lootItem;
        }
        return null;
    }

    public List<Loot> getLoot(int rarity) {
        List<Loot> lootItems = new ArrayList<>();
        for (Loot lootItem : loot) {
            if (!lootItem.isValid()) {
                continue;
            }
            if (lootItem.getRarity() == rarity) {
                lootItems.add(lootItem);
            }
        }
        return lootItems;
    }

    public List<Loot> getLoot(int minRarity, int maxRarity) {
        List<Loot> lootItems = new ArrayList<>();
        for (Loot lootItem : loot) {
            if (!lootItem.isValid()) {
                continue;
            }
            if (lootItem.getRarity() >= minRarity && lootItem.getRarity() <= maxRarity) {
                lootItems.add(lootItem);
            }
        }
        return lootItems;
    }

    public void setLoot(int index, Loot lootItem) {
        loot.set(index, lootItem);
        sort();
    }

    public List<Loot> getLoot() {
        return loot;
    }

    public void sort() {
        Collections.sort(loot);
    }
}
