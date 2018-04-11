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
import com.helion3.prism.util.DataQueries;
import io.github.lxgaming.prismplus.PrismPlus;
import io.github.lxgaming.prismplus.configuration.Config;
import io.github.lxgaming.prismplus.configuration.categories.EventCategory;
import io.github.lxgaming.prismplus.entries.PrismPlusRecord;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.ContainerShulkerBox;
import net.minecraft.inventory.ContainerWorkbench;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class InventoryListener {
    
    @Listener(order = Order.POST)
    public void onClickInventory(ClickInventoryEvent event, @Root Player player) {
        if (event.isCancelled() || event.getTransactions().isEmpty()) {
            return;
        }
        
        for (SlotTransaction slotTransaction : event.getTransactions()) {
            if (!(slotTransaction.getSlot().parent() instanceof CarriedInventory)) {
                return;
            }
            
            CarriedInventory<? extends Carrier> carriedInventory = (CarriedInventory<? extends Carrier>) slotTransaction.getSlot().parent();
            if (!(carriedInventory instanceof Container) || !isVanillaContainer((Container) carriedInventory)) {
                return;
            }
            
            Location<World> location = carriedInventory.getCarrier()
                    .filter(Locatable.class::isInstance)
                    .map(Locatable.class::cast)
                    .map(Locatable::getLocation)
                    .orElse(player.getLocation());
            
            int index = slotTransaction.getSlot().getProperty(SlotIndex.class, "slotindex").map(SlotIndex::getValue).orElse(-1);
            int capacity = carriedInventory.first().capacity();
            if (index >= capacity) {
                return;
            }
            
            // Insert
            if (slotTransaction.getFinal().getType() != ItemTypes.NONE || slotTransaction.getFinal().getQuantity() > slotTransaction.getOriginal().getQuantity()) {
                if (PrismPlus.getInstance().getConfig().map(Config::getEventCategory).map(EventCategory::isInsert).orElse(false)) {
                    String itemId = slotTransaction.getFinal().getType().getId();
                    int itemQuantity = slotTransaction.getFinal().getQuantity();
                    if (slotTransaction.getOriginal().getType() != ItemTypes.NONE) {
                        itemQuantity -= slotTransaction.getOriginal().getQuantity();
                    }
                    
                    PrismPlus.getInstance().debugMessage("Inventory insert - {} x{}", itemId, itemQuantity);
                    PrismPlusRecord prismPlusRecord = PrismPlusRecord.create().source(event.getCause()).event("insert").build();
                    prismPlusRecord.getDataContainer().set(DataQueries.Location, location.toContainer());
                    prismPlusRecord.getDataContainer().set(DataQueries.Target, itemId);
                    prismPlusRecord.getDataContainer().set(DataQueries.Quantity, itemQuantity);
                    prismPlusRecord.save();
                }
            }
            
            // Remove
            if (slotTransaction.getFinal().getType() == ItemTypes.NONE || slotTransaction.getFinal().getQuantity() < slotTransaction.getOriginal().getQuantity()) {
                if (PrismPlus.getInstance().getConfig().map(Config::getEventCategory).map(EventCategory::isRemove).orElse(false)) {
                    String itemId = slotTransaction.getOriginal().getType().getId();
                    int itemQuantity = slotTransaction.getOriginal().getQuantity();
                    if (slotTransaction.getFinal().getType() != ItemTypes.NONE) {
                        itemQuantity -= slotTransaction.getFinal().getQuantity();
                    }
                    
                    PrismPlus.getInstance().debugMessage("Inventory remove - {} x{}", itemId, itemQuantity);
                    PrismPlusRecord prismPlusRecord = PrismPlusRecord.create().source(event.getCause()).event("remove").build();
                    prismPlusRecord.getDataContainer().set(DataQueries.Location, location.toContainer());
                    prismPlusRecord.getDataContainer().set(DataQueries.Target, itemId);
                    prismPlusRecord.getDataContainer().set(DataQueries.Quantity, itemQuantity);
                    prismPlusRecord.save();
                }
            }
        }
    }
    
    @Listener(order = Order.POST)
    public void onChangeInventoryPickup(ChangeInventoryEvent.Pickup event, @Root Player player) {
        if (event.isCancelled() || event.getTransactions().isEmpty() || !Prism.listening.PICKUP) {
            return;
        }
        
        for (SlotTransaction slotTransaction : event.getTransactions()) {
            String itemId = slotTransaction.getFinal().getType().getId();
            int itemQuantity = slotTransaction.getFinal().getQuantity();
            if (slotTransaction.getOriginal().getType() != ItemTypes.NONE) {
                itemQuantity -= slotTransaction.getOriginal().getQuantity();
            }
            
            
            PrismPlus.getInstance().debugMessage("Inventory pickup - {} x{}", itemId, itemQuantity);
            PrismPlusRecord prismPlusRecord = PrismPlusRecord.create().source(event.getCause()).event("pickup").build();
            prismPlusRecord.getDataContainer().set(DataQueries.Location, player.getLocation().toContainer());
            prismPlusRecord.getDataContainer().set(DataQueries.Target, itemId);
            prismPlusRecord.getDataContainer().set(DataQueries.Quantity, itemQuantity);
            prismPlusRecord.save();
        }
    }
    
    @Listener(order = Order.POST)
    public void onDropItem(DropItemEvent.Dispense event, @Root Player player) {
        if (event.isCancelled() || event.getEntities().isEmpty() || !Prism.listening.DROP) {
            return;
        }
        
        for (Entity entity : event.getEntities()) {
            if (!(entity instanceof Item) || !((Item) entity).item().exists()) {
                return;
            }
            
            ItemStackSnapshot itemStackSnapshot = ((Item) entity).item().get();
            
            PrismPlus.getInstance().debugMessage("Inventory dropped - {} x{}", itemStackSnapshot.getType().getId(), itemStackSnapshot.getQuantity());
            PrismPlusRecord prismPlusRecord = PrismPlusRecord.create().source(event.getCause()).event("dropped").build();
            prismPlusRecord.getDataContainer().set(DataQueries.Location, player.getLocation().toContainer());
            prismPlusRecord.getDataContainer().set(DataQueries.Target, itemStackSnapshot.getType().getId());
            prismPlusRecord.getDataContainer().set(DataQueries.Quantity, itemStackSnapshot.getQuantity());
            prismPlusRecord.save();
        }
    }
    
    private boolean isVanillaContainer(Container container) {
        return container instanceof ContainerBeacon
                || container instanceof ContainerBrewingStand
                || container instanceof ContainerChest
                || container instanceof ContainerDispenser
                || container instanceof ContainerEnchantment
                || container instanceof ContainerFurnace
                || container instanceof ContainerHopper
                || container instanceof ContainerHorseInventory
                || container instanceof ContainerMerchant
                || container instanceof ContainerRepair
                || container instanceof ContainerShulkerBox
                || container instanceof ContainerWorkbench;
    }
}