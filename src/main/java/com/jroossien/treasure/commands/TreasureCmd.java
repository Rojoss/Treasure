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

package com.jroossien.treasure.commands;

import com.jroossien.boxx.commands.api.*;
import com.jroossien.boxx.commands.api.data.ArgRequirement;
import com.jroossien.boxx.commands.api.parse.SubCmdO;
import com.jroossien.boxx.messages.Msg;
import com.jroossien.boxx.messages.Param;
import com.jroossien.boxx.util.Str;
import com.jroossien.treasure.TreasurePlugin;

import java.io.File;
import java.util.Set;

public class TreasureCmd extends BaseCmd {

    public TreasureCmd(File file) {
        super("treasure", "treasures", "tr", "lootchest", "lootchests", "lc", "lchest", "lchests", "lootc");
        file(file);
        desc("Main Treasure command.");

        addArgument("action", ArgRequirement.REQUIRED, new SubCmdO(new Info(this), new Reload(this))).desc("A sub command.");
    }

    @Override
    public void onCommand(CmdData data) {}


    public class Info extends SubCmd<TreasureCmd> {
        private Info(TreasureCmd cmd) {
            super(cmd, "info", "plugin", "details", "detail");
            desc("Display plugin information.");
        }

        @Override
        public void onCommand(CmdData data) {
            data.getSender().sendMessage(Str.color("&8===== &4&lTreasure &8=====\n" +
                    "&6&lAuthors&8&l: &a" + Str.implode(TreasurePlugin.get().getDescription().getAuthors(), ", ", " & ") + "\n" +
                    "&6&lVersion&8&l: &a" + TreasurePlugin.get().getDescription().getVersion()));
        }
    }


    public class Reload extends SubCmd<TreasureCmd> {
        private Reload(TreasureCmd cmd) {
            super(cmd, "reload", "load");
            desc("Reload all the configuration files including commands and such.");
            perm("treasure.cmd.reload");
        }

        @Override
        public void onCommand(CmdData data) {
            //TODO: Reload configs

            Set<BaseCmd> cmds = CmdRegistration.getCommands(TreasurePlugin.class);
            if (cmds != null) {
                for (BaseCmd cmd : cmds) {
                    cmd.load();
                }
            }

            Msg.get("treasure.reloaded", Param.P("type", "all")).send(data.getSender());
        }
    }
}
