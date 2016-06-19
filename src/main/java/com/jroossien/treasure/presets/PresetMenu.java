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

import com.jroossien.boxx.input.ItemInput;
import com.jroossien.boxx.input.internal.InputCallback;
import com.jroossien.boxx.menu.Menu;
import com.jroossien.boxx.messages.Msg;
import com.jroossien.boxx.messages.Param;
import com.jroossien.boxx.nms.NMS;
import com.jroossien.boxx.nms.sign.SignGUICallback;
import com.jroossien.boxx.util.ItemUtil;
import com.jroossien.boxx.util.Parse;
import com.jroossien.boxx.util.Str;
import com.jroossien.boxx.util.item.EItem;
import com.jroossien.boxx.util.item.ItemParser;
import com.jroossien.treasure.TreasurePlugin;
import com.jroossien.treasure.treasure.ContentMode;
import com.jroossien.treasure.treasure.InteractMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Map;

public class PresetMenu extends Menu {

    private TreasurePlugin tr;
    private PresetManager pm;

    private final int ITEM_SLOTS = 5 * 9;

    public PresetMenu() {
        super(TreasurePlugin.get(), "presetmenu", 6, Msg.getString("presetmenu.title"));
        tr = TreasurePlugin.get();
        pm = tr.getPM();
    }

    @Override
    protected void onDestroy() {}

    @Override
    protected void onShow(InventoryOpenEvent event) {
        updateContent((Player)event.getPlayer());
    }

    @Override
    protected void onClose(InventoryCloseEvent event) {
        if (hasData(event.getPlayer().getName() + "-preset")) {
            String name = (String)getData(event.getPlayer().getName() + "-preset");
            Preset preset = pm.getPreset(name);
            if (preset != null) {
                preset.save();
            }
        }
    }

    @Override
    protected void onClick(InventoryClickEvent event) {
        event.setCancelled(true);

        final Player player = (Player)event.getWhoClicked();

        int slot = event.getRawSlot();
        final EItem item = new EItem(event.getCurrentItem());

        if (hasData(player.getName() + "-preset")) {
            boolean update = false;

            // Get preset
            String name = (String)getData(player.getName() + "-preset");
            final Preset preset = pm.getPreset(name);
            if (preset == null) {
                removeData(player.getName() + "-preset");
                Msg.get("presetmenu.invalid-preset", Param.P("name", name)).send(player);
                updateContent(player);
                return;
            }

            // Back to preset list.
            if (slot == 8) {
                preset.save();
                removeData(player.getName() + "-preset");
                updateContent(player);
                return;
            }

            // Title | Hologram
            if (slot == 9) {
                NMS.get().getSignGUI().show(player, new SignGUICallback() {
                    @Override
                    public void onEdit(String[] lines) {
                        String title = Str.implode(lines, "");
                        if (title.length() >= 32) {
                            Msg.get("presetmenu.title-too-many-chars", Param.P("name", title)).send(player);
                            show(player);
                            return;
                        }

                        preset.setTitle(title);
                        pm.setPreset(preset);
                        show(player);
                    }
                });
            }
            if (slot == 10) {
                NMS.get().getSignGUI().show(player, new SignGUICallback() {
                    @Override
                    public void onEdit(String[] lines) {
                        String hologram = Str.implode(lines, "");
                        preset.setHologram(hologram);
                        pm.setPreset(preset);
                        show(player);
                    }
                });
            }

            // Min items | Max items | Item limit
            if (slot == 18) {
                preset.setMinItems(Math.min(Math.max(preset.getMinItems() + (event.isLeftClick() ? 1 : -1), 0), 27));
                update = true;
            }
            if (slot == 19) {
                preset.setMaxItems(Math.min(Math.max(preset.getMaxItems() + (event.isLeftClick() ? 1 : -1), 0), 27));
                update = true;
            }
            if (slot == 21) {
                preset.setItemLimit(Math.min(Math.max(preset.getItemLimit() + (event.isLeftClick() ? 1 : -1), 0), 27));
                update = true;
            }

            // Min Rarity | Max rarity | Rare item chance
            if (slot == 27) {
                if (event.isShiftClick() && event.isLeftClick()) {
                    handleRaritySetInput(preset, player, false);
                } else {
                    preset.setMinRarity(Math.min(Math.max(preset.getMinRarity() + (event.isLeftClick() ? 1 : -1), 1), 100));
                    update = true;
                }
            }
            if (slot == 28) {
                if (event.isShiftClick() && event.isLeftClick()) {
                    handleRaritySetInput(preset, player, true);
                } else {
                    preset.setMaxRarity(Math.min(Math.max(preset.getMaxRarity() + (event.isLeftClick() ? 1 : -1), 1), 100));
                    update = true;
                }
            }
            if (slot == 30) {
                NMS.get().getSignGUI().show(player, new SignGUICallback() {
                    @Override
                    public void onEdit(String[] lines) {
                        String input = Str.implode(lines, "").trim();
                        Double chance = Parse.Double(input);
                        if (chance == null) {
                            Msg.get("presetmenu.rareschance-notanumber", Param.P("input", input)).send(player);
                            show(player);
                            return;
                        }
                        if (chance < 0 || chance > 1) {
                            Msg.get("presetmenu.rareschance-invalid", Param.P("input", input)).send(player);
                            show(player);
                            return;
                        }
                        preset.setRareChance(chance);
                        pm.setPreset(preset);
                        show(player);
                    }
                });
            }

            // Custom items
            if (slot == 36) {
                //TODO: Custom items
            }

            // Content mode | Interact mode
            if (slot == 14) {
                preset.setContentMode(preset.getContentMode() == ContentMode.GLOBAL ? ContentMode.PERSONAL : ContentMode.GLOBAL);
                update = true;
            }
            if (slot == 15) {
                if (event.isLeftClick()) {
                    preset.setInteractMode(InteractMode.values()[preset.getInteractMode().ordinal() + 1 >= InteractMode.values().length ? 0 : preset.getInteractMode().ordinal() + 1]);
                } else {
                    preset.setInteractMode(InteractMode.values()[preset.getInteractMode().ordinal() - 1 < 0 ? InteractMode.values().length - 1 : preset.getInteractMode().ordinal() - 1]);
                }
                update = true;
            }

            // Key item | Consume
            if (slot == 23) {
                if (!event.isShiftClick() || (preset.getKey() == null || preset.getKey().getType() == Material.AIR)) {
                    Msg.get("presetmenu.selectkey").send(player);
                    player.closeInventory();
                    player.openInventory(player.getInventory());
                    ItemInput.get(player.getUniqueId(), new InputCallback<EItem>() {
                        @Override
                        public void onSubmit(EItem value) {
                            preset.setKey(value);
                            pm.setPreset(preset);
                            show(player);
                        }
                    });
                } else {
                    if (event.isLeftClick()) {
                        ItemUtil.add(player.getInventory(), preset.getKey().clone());
                        return;
                    } else {
                        preset.setKey(null);
                        update = true;
                    }
                }
            }
            if (slot == 24) {
                preset.setConsumeKey(preset.isConsumeKey() ? false : true);
                update = true;
            }

            // Permission | Protection
            if (slot == 32) {
                NMS.get().getSignGUI().show(player, new SignGUICallback() {
                    @Override
                    public void onEdit(String[] lines) {
                        String permission = Str.implode(lines, "").trim();
                        preset.setPermission(permission);
                        pm.setPreset(preset);
                        show(player);
                    }
                });
            }
            if (slot == 33) {
                preset.setProtection(preset.isProtection() ? false : true);
                update = true;
            }

            // Lock time | Despawn time | Refill time | Refill recreate
            if (slot == 41) {
                handleTimeInput(preset, player, 1);
            }
            if (slot == 42) {
                handleTimeInput(preset, player, 2);
            }
            if (slot == 43) {
                handleTimeInput(preset, player, 3);
            }
            if (slot == 44) {
                preset.setRefillRecreate(preset.isRefillRecreate() ? false : true);
                update = true;
            }


            if (update) {
                pm.setPreset(preset);
                updateContent(player);
            }
        } else {
            if (slot > 8) {
                // Click on preset
                String name = Str.stripColor(item.getName());
                Preset preset = pm.getPreset(name);
                if (preset == null) {
                    Msg.get("presetmenu.invalid-preset", Param.P("name", name)).send(player);
                    return;
                }

                setData(player.getName() + "-preset", name);
                updateContent(player);
            } else {
                // New preset
                if (slot == 4) {
                    NMS.get().getSignGUI().show(player, new SignGUICallback() {
                        @Override
                        public void onEdit(String[] lines) {
                            String name = Str.stripColor(Str.implode(lines, ""));
                            if (name.isEmpty()) {
                                Msg.get("presetmenu.new-noname").send(player);
                                show(player);
                                return;
                            }

                            if (pm.hasPreset(name)) {
                                Msg.get("presetmenu.new-nameused", Param.P("name", name)).send(player);
                                show(player);
                                return;
                            }

                            Preset preset = pm.createPreset(name);
                            setData(player.getName() + "-preset", preset.getName());
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
                    if (page >= Math.ceil(pm.getPresets().size() / ITEM_SLOTS)) {
                        return;
                    }
                    setData(player.getName() + "-page", page++);
                    updateContent(player);
                    return;
                }
            }
        }
    }

    private void updateContent(Player player) {
        if (hasData(player.getName() + "-preset")) {
            // Show specific preset

            // Menu bar (info | preset | back)
            setSlots(new EItem(Material.STAINED_GLASS_PANE, (short)7).setName(""), player, 1,2,3,5,6,7, 13,22,31,40, 45,46,47,48,49,50,51,52,53);
            setSlot(0, new EItem(Material.EMPTY_MAP).setName(Msg.getString("presetmenu.preset-info-title")).setLore(Msg.getString("presetmenu.preset-info-lore")), player);
            setSlot(8, new EItem(Material.REDSTONE_BLOCK).setName(Msg.getString("presetmenu.back")).setLore(Msg.getString("presetmenu.back-lore")), player);

            // Force clear empty slots.
            setSlots(EItem.AIR, player, 11,12, 20, 29, 37,38,39,  16,17, 25,26, 34,35);

            // Get preset
            String name = (String)getData(player.getName() + "-preset");
            Preset preset = pm.getPreset(name);
            if (preset == null) {
                removeData(player.getName() + "-preset");
                Msg.get("presetmenu.invalid-preset", Param.P("name", name)).send(player);
                updateContent(player);
                return;
            }

            setSlot(4, getPresetItem(preset), player);

            // Settings
            setSlot(9, new EItem(Material.NAME_TAG).setName(Msg.getString("presetmenu.preset.title-title", Param.P("value", preset.getTitle())))
                    .setLore(Msg.getString("presetmenu.preset.title-lore", Param.P("value", preset.getTitle()))).setGlowing(!preset.getTitle().isEmpty()), player);
            setSlot(10, new EItem(Material.NAME_TAG).setName(Msg.getString("presetmenu.preset.hologram-title", Param.P("value", preset.getHologram())))
                    .setLore(Msg.getString("presetmenu.preset.hologram-lore", Param.P("value", preset.getHologram()))).setGlowing(!preset.getHologram().isEmpty()), player);

            setSlot(18, new EItem(Material.SUGAR, preset.getMinItems()).setName(Msg.getString("presetmenu.preset.minitems-title", Param.P("value", preset.getMinItems())))
                    .setLore(Msg.getString("presetmenu.preset.minitems-lore", Param.P("value", preset.getMinItems()))), player);
            setSlot(19, new EItem(Material.GLOWSTONE_DUST, preset.getMaxItems()).setName(Msg.getString("presetmenu.preset.maxitems-title", Param.P("value", preset.getMaxItems())))
                    .setLore(Msg.getString("presetmenu.preset.maxitems-lore", Param.P("value", preset.getMaxItems()))), player);
            setSlot(21, new EItem(Material.SULPHUR, preset.getItemLimit()).setName(Msg.getString("presetmenu.preset.itemlimit-title", Param.P("value", preset.getItemLimit())))
                    .setLore(Msg.getString("presetmenu.preset.itemlimit-lore", Param.P("value", preset.getItemLimit()))).setGlowing(preset.getItemLimit() > 0), player);

            setSlot(27, new EItem(Material.IRON_INGOT).setName(Msg.getString("presetmenu.preset.minrarity-title", Param.P("value", preset.getMinRarity())))
                    .setLore(Msg.getString("presetmenu.preset.minrarity-lore", Param.P("value", preset.getMinRarity()))), player);
            setSlot(28, new EItem(Material.GOLD_INGOT).setName(Msg.getString("presetmenu.preset.maxrarity-title", Param.P("value", preset.getMaxRarity())))
                    .setLore(Msg.getString("presetmenu.preset.maxrarity-lore", Param.P("value", preset.getMaxRarity()))), player);
            setSlot(30, new EItem(Material.EMERALD).setName(Msg.getString("presetmenu.preset.rarechance-title", Param.P("value", preset.getRareChance())))
                    .setLore(Msg.getString("presetmenu.preset.rarechance-lore", Param.P("value", preset.getRareChance()))).setGlowing(preset.getRareChance() > 0), player);

            setSlot(36, new EItem(Material.DIAMOND).setName(Msg.getString("presetmenu.preset.customitems-title", Param.P("value", preset.getCustomItems().size())))
                    .setLore(Msg.getString("presetmenu.preset.customitems-lore", Param.P("value", preset.getCustomItems().size()))).setGlowing(preset.getCustomItems().size() > 0), player);

            if (preset.getContentMode() == ContentMode.GLOBAL) {
                setSlot(14, new EItem(Material.ENDER_CHEST).setName(Msg.getString("presetmenu.preset.contentmode-global"))
                        .setLore(Msg.getString("presetmenu.preset.contentmode-global-lore")), player);
            } else {
                setSlot(14, new EItem(Material.CHEST).setName(Msg.getString("presetmenu.preset.contentmode-personal"))
                        .setLore(Msg.getString("presetmenu.preset.contentmode-personal-lore")), player);
            }
            setSlot(15, new EItem(preset.getInteractMode().getItem(), preset.getInteractMode().getData()).setName(Msg.getString("presetmenu.preset.interactmode",
                    Param.P("value", preset.getInteractMode().toString()))).setLore(Msg.getString("presetmenu.preset.interactmode-lore", Param.P("value", preset.getInteractMode().toString()),
                    Param.P("description", Msg.getString("interactmode." + preset.getInteractMode().toString().toLowerCase())),
                    Param.P("next", InteractMode.values()[preset.getInteractMode().ordinal() + 1 >= InteractMode.values().length ? 0 : preset.getInteractMode().ordinal() + 1].toString()),
                    Param.P("prev", InteractMode.values()[preset.getInteractMode().ordinal() - 1 < 0 ? InteractMode.values().length - 1 : preset.getInteractMode().ordinal() - 1].toString()))), player);


            if (preset.getKey() == null || preset.getKey().getType() == Material.AIR) {
                setSlot(23, new EItem(Material.TRIPWIRE_HOOK).setName(Msg.getString("presetmenu.preset.nokey-title")).setLore(Msg.getString("presetmenu.preset.nokey-lore")), player);
            } else {
                setSlot(23, preset.getKey().clone().addLore(Msg.getString("presetmenu.preset.key-lore")), player);
            }
            setSlot(24, new EItem(Material.INK_SACK, preset.isConsumeKey() ? (byte)10 : (byte)8).setName(Msg.getString("presetmenu.preset.consumekey-title", Param.P("value", preset.isConsumeKey())))
                    .setLore(Msg.getString("presetmenu.preset.consumekey-lore", Param.P("value", preset.isConsumeKey()))), player);

            setSlot(32, new EItem(Material.PAPER).setName(Msg.getString("presetmenu.preset.permission-title", Param.P("value", preset.getPermission())))
                    .setLore(Msg.getString("presetmenu.preset.permission-lore", Param.P("value", preset.getPermission()))).setGlowing(!preset.getPermission().isEmpty()), player);
            setSlot(33, new EItem(Material.IRON_FENCE).setName(Msg.getString("presetmenu.preset.protected-title", Param.P("value", preset.isProtection())))
                    .setLore(Msg.getString("presetmenu.preset.protected-lore", Param.P("value", preset.isProtection()))).setGlowing(preset.isProtection()), player);

            setSlot(41, new EItem(Material.WATCH).setName(Msg.getString("presetmenu.preset.locktime-title", Param.P("value", preset.getLockTime())))
                    .setLore(Msg.getString("presetmenu.preset.locktime-lore", Param.P("value", preset.getLockTime()))).setGlowing(preset.getLockTime() > 0), player);
            setSlot(42, new EItem(Material.WATCH).setName(Msg.getString("presetmenu.preset.despawntime-title", Param.P("value", preset.getDespawnTime())))
                    .setLore(Msg.getString("presetmenu.preset.despawntime-lore", Param.P("value", preset.getDespawnTime()))).setGlowing(preset.getDespawnTime() > 0), player);
            setSlot(43, new EItem(Material.WATCH).setName(Msg.getString("presetmenu.preset.refilltime-title", Param.P("value", preset.getRefillTime())))
                    .setLore(Msg.getString("presetmenu.preset.refilltime-lore", Param.P("value", preset.getRefillTime()))).setGlowing(preset.getRefillTime() > 0), player);
            setSlot(44, new EItem(Material.INK_SACK, preset.isRefillRecreate() ? (byte)10 : (byte)8).setName(Msg.getString("presetmenu.preset.refillrecreate-title", Param.P("value", preset.isRefillRecreate())))
                    .setLore(Msg.getString("presetmenu.preset.refillrecreate-lore", Param.P("value", preset.isRefillRecreate()))), player);

        } else {
            // Show list

            // Menu bar (previous page | info | new | next page)
            setSlots(new EItem(Material.STAINED_GLASS_PANE, (short)7).setName(""), player, 2,3,5,6,7);
            setSlot(0, new EItem(Material.SKULL_ITEM).setOwner("MHF_ArrowLeft").setName(Msg.getString("presetmenu.prev-title")).setLore(Msg.getString("presetmenu.prev-lore")), player);
            setSlot(1, new EItem(Material.EMPTY_MAP).setName(Msg.getString("presetmenu.info-title")).setLore(Msg.getString("presetmenu.info-lore")), player);
            setSlot(4, new EItem(Material.NETHER_STAR).setName(Msg.getString("presetmenu.new-title")).setLore(Msg.getString("presetmenu.new-lore")), player);
            setSlot(8, new EItem(Material.SKULL_ITEM).setOwner("MHF_ArrowRight").setName(Msg.getString("presetmenu.next-title")).setLore(Msg.getString("presetmenu.next-lore")), player);

            // Get page
            int page = 0;
            if (hasData(player.getName() + "-page")) {
                page = (int)getData(player.getName() + "-page");
            }

            // Presets
            Map<String, Preset> presets = pm.getPresets();
            int pageOffset = page * ITEM_SLOTS;
            int slotOffset = 9;
            int i = 0;

            for (Map.Entry<String, Preset> entry : presets.entrySet()) {
                if (i + pageOffset >= presets.size()) {
                    break;
                }
                setSlot(i + slotOffset, getPresetItem(entry.getValue()), player);
                i++;
            }

            // Clear slots that weren't filled
            for (; i < ITEM_SLOTS; i++) {
                setSlot(i + slotOffset, EItem.AIR, player);
            }
        }
    }

    private EItem getPresetItem(Preset preset) {
        EItem item = new EItem(preset.getTreasureBlock()).addItemFlags();
        String key = new ItemParser(preset.getKey()).getString();
        item.setName("&6&l" + preset.getName());
        item.setLore(Msg.getString("presetmenu.preset-lore",
                Param.P("minRarity", preset.getMinRarity()),
                Param.P("maxRarity", preset.getMaxRarity()),
                Param.P("customItems", preset.getCustomItems().size()),
                Param.P("rareChance", preset.getRareChance()),
                Param.P("minItems", preset.getMinItems()),
                Param.P("maxItems", preset.getMaxItems()),
                Param.P("itemLimit", preset.getItemLimit()),
                Param.P("permission", preset.getPermission().isEmpty() ? "-" : preset.getPermission()),
                Param.P("title", preset.getTitle().isEmpty() ? "-" : preset.getTitle()),
                Param.P("hologram", preset.getHologram().isEmpty() ? "-" : preset.getHologram()),
                Param.P("contentMode", preset.getContentMode().toString()),
                Param.P("interactMode", preset.getInteractMode().toString()),
                Param.P("key", key == null ? "-" : key),
                Param.P("consumeKey", preset.isConsumeKey()),
                Param.P("protected", preset.isProtection()),
                Param.P("refillTime", preset.getRefillTime()),
                Param.P("refillRecreate", preset.isRefillRecreate()),
                Param.P("lockTime", preset.getLockTime()),
                Param.P("despawnTime", preset.getDespawnTime())));
        return item;
    }

    private void handleRaritySetInput(final Preset preset, final Player player, final boolean max) {
        NMS.get().getSignGUI().show(player, new SignGUICallback() {
            @Override
            public void onEdit(String[] lines) {
                String input = Str.implode(lines, "").trim();
                Integer rarity = Parse.Int(input);
                if (rarity == null) {
                    Msg.get("lootmenu.rarity-notanumber", Param.P("input", input)).send(player);
                    show(player);
                    return;
                }
                if (rarity < 1 || rarity > 100) {
                    Msg.get("lootmenu.rarity-invalid", Param.P("input", input)).send(player);
                    show(player);
                    return;
                }
                if (max) {
                    preset.setMaxRarity(rarity);
                } else {
                    preset.setMinRarity(rarity);
                }
                pm.setPreset(preset);
                show(player);
            }
        });
    }

    private void handleTimeInput(final Preset preset, final Player player, final int type) {
        NMS.get().getSignGUI().show(player, new SignGUICallback() {
            @Override
            public void onEdit(String[] lines) {
                String input = Str.implode(lines, "").trim();
                Integer time = Parse.Int(input);
                if (time == null) {
                    Msg.get("presetmenu.time-notanumber", Param.P("input", input)).send(player);
                    show(player);
                    return;
                }
                if (time < 0) {
                    Msg.get("presetmenu.time-invalid", Param.P("input", input)).send(player);
                    show(player);
                    return;
                }
                if (type == 1) {
                    preset.setLockTime(time);
                } else if (type == 2) {
                    preset.setDespawnTime(time);
                } else {
                    preset.setRefillTime(time);
                }
                pm.setPreset(preset);
                show(player);
            }
        });
    }
}
