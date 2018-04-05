/*
 * Copyright 2017 Alex Thomson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.lxgaming.prismplus.listeners;

import com.helion3.prism.util.DataQueries;
import io.github.lxgaming.prismplus.PrismPlus;
import io.github.lxgaming.prismplus.entries.PrismPlusRecord;
import io.github.lxgaming.prismplus.util.Reference;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.First;

public class CommandListener {

    @Listener(order = Order.POST)
    public void onSendCommand(SendCommandEvent event, @First Player player) {
        if (PrismPlus.getInstance().getConfig() != null && PrismPlus.getInstance().getConfig().isCommandEvent()) {
            PrismPlusRecord prismPlusRecord = PrismPlusRecord.create().player(player).event("command").build();
            prismPlusRecord.getDataContainer().set(DataQueries.Location, player.getLocation().toContainer());
            prismPlusRecord.getDataContainer().set(DataQueries.Target, event.getCommand());
            prismPlusRecord.getDataContainer().set(DataQueries.Player, player.getUniqueId().toString());
            prismPlusRecord.save();
        }

        if (redirectCommand(event.getCommand(), event.getArguments())) {
            PrismPlus.getInstance().debugMessage("Redirecting Command.");
            event.setCommand(Reference.PLUGIN_NAME);
        }
    }

    private boolean redirectCommand(String command, String arguments) {
        if (StringUtils.isAnyBlank(command, arguments)) {
            return false;
        }

        if (PrismPlus.getInstance().getConfig() == null || !PrismPlus.getInstance().getConfig().isCommandOverride()) {
            return false;
        }

        if (!StringUtils.equalsIgnoreCase(command, "pr") && !StringUtils.equalsIgnoreCase(command, "prism")) {
            return false;
        }

        if (StringUtils.startsWithAny(arguments.toLowerCase(), "i", "inspect", "l", "lookup", "near")) {
            return true;
        }

        return false;
    }
}