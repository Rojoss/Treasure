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

import com.jroossien.boxx.options.single.IntO;
import com.jroossien.boxx.options.single.ItemO;
import com.jroossien.boxx.util.item.EItem;
import com.jroossien.treasure.TreasurePlugin;

import java.util.*;

public class Loot implements Comparable<Loot> {

    private EItem item;
    private Integer rarity;

    public Loot(EItem item, int rarity) {
        this.item = item;
        this.rarity = Math.min(Math.max(rarity, TreasurePlugin.MIN_RARITY), TreasurePlugin.MAX_RARITY);
    }

    public Loot(Map<String, String> data) {
        if (data.containsKey("item")) {
            ItemO itemO = new ItemO();
            if (itemO.parse(data.get("item"))) {
                item = itemO.getValue();
            } else {
                TreasurePlugin.get().warn("Failed to load loot item: '" + data.get("item") + "' Error: " + itemO.getError());
            }
        }
        if (data.containsKey("rarity")) {
            IntO intO = new IntO().min(TreasurePlugin.MIN_RARITY).max(TreasurePlugin.MAX_RARITY);
            if (intO.parse(data.get("rarity"))) {
                rarity = intO.getValue();
            } else {
                TreasurePlugin.get().warn("Failed to load loot item: '" + data.get("item") + "' because it has an invalid rarity. Error: " + intO.getError());
            }
        }
    }

    public Map<String, String> getData() {
        Map<String, String> data = new HashMap<>();
        if (item != null) {
            data.put("item", ItemO.serialize(item));
        }
        if (rarity != null) {
            data.put("rarity", rarity.toString());
        }
        return data;
    }

    public boolean isValid() {
        return item != null && rarity != null;
    }

    public EItem getItem() {
        return item;
    }

    public Integer getRarity() {
        return rarity;
    }

    public void setRarity(int rarity) {
        this.rarity = Math.min(Math.max(rarity, TreasurePlugin.MIN_RARITY), TreasurePlugin.MAX_RARITY);
    }

    @Override
    public int compareTo(Loot o) {
        if (this.rarity == null || o.getRarity() == null) {
            return 0;
        }
        return this.rarity - o.getRarity();
    }
}
