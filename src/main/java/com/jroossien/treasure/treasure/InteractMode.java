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

package com.jroossien.treasure.treasure;

import org.bukkit.Material;

public enum InteractMode {
    /** Chest will open so player can take content */
    OPEN(Material.WOOD),
    /** Items will be added to player inventory if there is space and otherwise dropped on the ground/chest. */
    COLLECT(Material.WOOD_STAIRS),
    /** Items will drop on the ground/chest. */
    DROP(Material.WOOD_STEP),

    /** Same as {@link #COLLECT} but also destroys the chest. */
    COLLECT_DESTROY(Material.RED_SANDSTONE_STAIRS),
    /** Same as {@link #DROP} but also destroys the chest. */
    DROP_DESTROY(Material.STONE_SLAB2),

    /** Same as {@link #COLLECT} but if there is no space it opens the chest instead of dropping items. */
    COLLECT_OPEN(Material.WOOD, (byte)4),
    /** Same as {@link #COLLECT_OPEN} but destroys the chest when it's collected or the chest is closed. */
    COLLECT_OPEN_DESTROY(Material.ACACIA_STAIRS),

    /** Chest opens normally and gets destroyed when it's closed and remaining items will disappear */
    DESTROY_ON_CLOSE(Material.NETHER_BRICK),
    /** Chest opens normally and gets destroyed when it's closed and remaining items will be added to the players inventory. */
    COLLECT_ON_CLOSE(Material.NETHER_BRICK_STAIRS),
    /** Chest opens normally and gets destroyed when it's closed and remaining items will drop on the ground. */
    DROP_ON_CLOSE(Material.STEP, (byte)6),

    /** Chest opens normally and gets destroyed when all items are taken. */
    DESTROY_ON_ALL_ITEMS_TAKEN(Material.RED_NETHER_BRICK);

    private Material item;
    private byte data;

    InteractMode(Material item) {
        this.item = item;
        this.data = 0;
    }

    InteractMode(Material item, byte data) {
        this.item = item;
        this.data = data;
    }

    public Material getItem() {
        return item;
    }

    public byte getData() {
        return data;
    }

    public static InteractMode fromString(String name) {
        name = name.toLowerCase().replace("_","");
        for (InteractMode mode : values()) {
            if (name.equalsIgnoreCase(mode.toString())) {
                return mode;
            }
        }
        return null;
    }
}
