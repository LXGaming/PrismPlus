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

import com.helion3.prism.Prism;
import com.helion3.prism.api.flags.Flag;
import com.helion3.prism.api.query.ConditionGroup;
import com.helion3.prism.api.query.QuerySession;
import io.github.lxgaming.prismplus.PrismPlus;
import io.github.lxgaming.prismplus.util.SpongeHelper;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class InteractListener {

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onInteractBlock(InteractBlockEvent event, @First Player player) {
        if (PrismPlus.getInstance().getConfig() == null || !PrismPlus.getInstance().getConfig().isInspectOverride()) {
            return;
        }

        if (!Prism.getActiveWands().contains(player.getUniqueId())) {
            return;
        }

        // Prevents Prism RequiredInteractListener from receiving the event.
        event.setCancelled(true);

        if (event.getTargetBlock().equals(BlockSnapshot.NONE) || !event.getTargetBlock().getLocation().isPresent()) {
            return;
        }

        if (event instanceof InteractBlockEvent.Primary.OffHand || event instanceof InteractBlockEvent.Secondary.OffHand) {
            return;
        }

        Location<World> location = event.getTargetBlock().getLocation().get();
        if (event instanceof InteractBlockEvent.Secondary) {
            location = location.getRelative(event.getTargetSide());
        }

        if (location == null) {
            return;
        }

        QuerySession session = new QuerySession(player);
        //session.addFlag(Flag.EXTENDED);
        session.addFlag(Flag.NO_GROUP);
        session.newQuery().addCondition(ConditionGroup.from(location));

        player.sendMessage(Text.of(
                SpongeHelper.getTextPrefix(), TextColors.GOLD,
                "--- Inspecting ", SpongeHelper.getItemText(location.getBlockType().getId(), true),
                " at ", location.getBlockX(), " ", location.getBlockY(), " ", location.getBlockZ(), " ---"));

        PrismPlus.getInstance().getPrismManager().lookup(session);
    }
}