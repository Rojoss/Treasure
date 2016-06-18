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

import com.jroossien.boxx.menu.Menu;
import com.jroossien.boxx.messages.Msg;
import com.jroossien.boxx.messages.Param;
import com.jroossien.boxx.nms.NMS;
import com.jroossien.boxx.nms.sign.SignGUICallback;
import com.jroossien.boxx.util.*;
import com.jroossien.boxx.util.item.EItem;
import com.jroossien.treasure.TreasurePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.List;

public class LootMenu extends Menu {

    private TreasurePlugin tr;
    private LootManager lm;

    private final int ITEM_SLOTS = 5 * 9;

    public LootMenu() {
        super(TreasurePlugin.get(), "lootmenu", 6, Msg.getString("lootmenu.title"));
        tr = TreasurePlugin.get();
        lm = tr.getLM();

        // Menu bar (previous page | info | next page)
        setSlots(new EItem(Material.STAINED_GLASS_PANE, (short)11).setName(""), 1,2,3,5,6,7);
        setSlot(0, new EItem(Material.SKULL_ITEM).setOwner("MHF_ArrowLeft").setName(Msg.getString("lootmenu.prev-title")).setLore(Msg.getString("lootmenu.prev-lore")));
        setSlot(4, new EItem(Material.EMPTY_MAP).setName(Msg.getString("lootmenu.info-title")).setLore(Msg.getString("lootmenu.info-lore")));
        setSlot(8, new EItem(Material.SKULL_ITEM).setOwner("MHF_ArrowRight").setName(Msg.getString("lootmenu.next-title")).setLore(Msg.getString("lootmenu.next-lore")));
    }

    @Override
    protected void onDestroy() {}

    @Override
    protected void onShow(InventoryOpenEvent event) {
        Player player = (Player)event.getPlayer();
        updateContent(player);
    }

    @Override
    protected void onClose(InventoryCloseEvent event) {}

    @Override
    protected void onClick(InventoryClickEvent event) {
        event.setCancelled(true);

        final Player player = (Player)event.getWhoClicked();

        int slot = event.getRawSlot();
        final EItem item = new EItem(event.getCurrentItem());

        // Add item from inventory...
        if (slot >= getSlots()) {
            if (item == null || item.getType() == Material.AIR) {
                return;
            }

            NMS.get().getSignGUI().show(player, new SignGUICallback() {
                @Override
                public void onEdit(String[] lines) {
                    Integer rarity = Parse.Int(lines[0].trim());
                    if (rarity == null) {
                        Msg.get("lootmenu.rarity-notanumber", Param.P("input", lines[0])).send(player);
                        show(player);
                        return;
                    }
                    if (rarity < 1 || rarity > 100) {
                        Msg.get("lootmenu.rarity-invalid", Param.P("input", lines[0])).send(player);
                        show(player);
                        return;
                    }
                    lm.addLoot(item, rarity);
                    show(player);
                }
            });
            return;
        }

        // Click on loot item. (change rarity or remove item)
        if (slot > 8) {
            if (item == null || item.getType() == Material.AIR) {
                return;
            }

            final int index = Parse.Int(Str.stripColor(item.getLore(1)));
            final Loot lootItem = lm.getLoot().get(index);
            if (!ItemUtil.compare(lootItem.getItem(), item, false)) {
                // Item index has changed (another player edited the loot)
                Msg.get("lootmenu.invalid-item-index").send(player);
                updateContent(player);
                return;
            }

            // Remove item
            if (event.isShiftClick() && event.isRightClick()) {
                lm.deleteLoot(index);
                updateContent(player);
                Msg.get("lootmenu.item-removed").send(player);
                return;
            }

            // Edit item
            NMS.get().getSignGUI().show(player, new SignGUICallback() {
                @Override
                public void onEdit(String[] lines) {
                    Integer rarity = Parse.Int(lines[0].trim());
                    if (rarity == null) {
                        Msg.get("lootmenu.rarity-notanumber", Param.P("input", lines[0])).send(player);
                        show(player);
                        return;
                    }
                    if (rarity < 1 || rarity > 100) {
                        Msg.get("lootmenu.rarity-invalid", Param.P("input", lines[0])).send(player);
                        show(player);
                        return;
                    }
                    lootItem.setRarity(rarity);
                    lm.setLoot(index, lootItem);
                    show(player);
                }
            });
        }

        int page = 0;
        if (hasData(player.getName() + "-page")) {
            page = (int)getData(player.getName() + "-page");
        }

        // Previous page
        if (slot == 0) {
            if (page <= 0) {
                return;
            }
            setData(player.getName() + "-page", page--);
            updateContent(player);
            return;
        }

        // Next page
        if (slot == 8) {
            if (page >= Math.ceil(lm.getLoot().size() / ITEM_SLOTS)) {
                return;
            }
            setData(player.getName() + "-page", page++);
            updateContent(player);
            return;
        }
    }

    private void updateContent(Player player) {
        int page = 0;
        if (hasData(player.getName() + "-page")) {
            page = (int)getData(player.getName() + "-page");
        }

        List<Loot> loot = lm.getLoot();
        int pageOffset = page * ITEM_SLOTS;
        int slotOffset = 9;
        int i = 0;

        for (; i < ITEM_SLOTS; i++) {
            if (i + pageOffset >= loot.size()) {
                break;
            }
            Loot lootItem = loot.get(i + pageOffset);
            EItem item = lootItem.getItem().clone();
            item.addLore(0, Msg.getString("lootmenu.item-rarity", Param.P("rarity", lootItem.getRarity())));
            item.addLore(1, "&0" + (i + pageOffset));
            item.addLore(2, "&7- - - - - - - -");
            setSlot(i + slotOffset, item, player);
        }

        // Clear slots that weren't filled
        for (; i < ITEM_SLOTS; i++) {
            setSlot(i + slotOffset, EItem.AIR, player);
        }
    }
}
