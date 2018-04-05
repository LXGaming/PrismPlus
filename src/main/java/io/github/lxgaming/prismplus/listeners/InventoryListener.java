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
import net.minecraft.inventory.AnimalChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.inventory.Slot;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.world.Locatable;

public class InventoryListener {

    @Listener(order = Order.POST)
    public void onClickInventory(ClickInventoryEvent event) {
        if (event.isCancelled() || event.getTransactions().size() <= 0) {
            return;
        }

        for (SlotTransaction slotTransaction : event.getTransactions()) {
            writeItemTransaction(slotTransaction, event.getCause());
        }
    }

    private void writeItemTransaction(SlotTransaction slotTransaction, Cause cause) {
        if (slotTransaction == null || !(slotTransaction.getSlot().parent() instanceof CarriedInventory) || !(slotTransaction.getSlot().parent() instanceof Container)) {
            return;
        }

        CarriedInventory<?> carriedInventory = (CarriedInventory<?>) slotTransaction.getSlot().parent();
        if (!carriedInventory.getCarrier().isPresent() || !(carriedInventory.getCarrier().get() instanceof Locatable)) {
            return;
        }

        Locatable locatable = (Locatable) carriedInventory.getCarrier().get();
        Container container = ((Container) slotTransaction.getSlot().parent());
        SlotIndex slotIndex = slotTransaction.getSlot().getProperty(SlotIndex.class, "slotindex").orElse(null);
        if (locatable == null || container == null || slotIndex == null) {
            return;
        }

        Slot slot = container.inventorySlots.get(slotIndex.getValue());
        if (slot == null || slot.inventory instanceof PlayerInventory || slot.inventory instanceof AnimalChest || slot.inventory instanceof ContainerHorseInventory) {
            return;
        }

        // Insert
        if (slotTransaction.getFinal().getType() != ItemTypes.NONE || slotTransaction.getFinal().getCount() > slotTransaction.getOriginal().getCount()) {
            if (PrismPlus.getInstance().getConfig() != null && PrismPlus.getInstance().getConfig().isInsertEvent()) {
                String itemId = slotTransaction.getFinal().getType().getId();
                int itemQuantity = slotTransaction.getFinal().getCount();
                if (slotTransaction.getOriginal().getType() != ItemTypes.NONE) {
                    itemQuantity -= slotTransaction.getOriginal().getCount();
                }

                PrismPlus.getInstance().debugMessage("Inventory insert - {} x{}", itemId, itemQuantity);
                PrismPlusRecord prismPlusRecord = PrismPlusRecord.create().source(cause).event("insert").build();
                prismPlusRecord.getDataContainer().set(DataQueries.Location, locatable.getLocation().toContainer());
                prismPlusRecord.getDataContainer().set(DataQueries.Target, itemId);
                prismPlusRecord.getDataContainer().set(DataQueries.Quantity, itemQuantity);
                prismPlusRecord.save();
            }
        }

        // Remove
        if (slotTransaction.getFinal().getType() == ItemTypes.NONE || slotTransaction.getFinal().getCount() < slotTransaction.getOriginal().getCount()) {
            if (PrismPlus.getInstance().getConfig() != null && PrismPlus.getInstance().getConfig().isRemoveEvent()) {
                String itemId = slotTransaction.getOriginal().getType().getId();
                int itemQuantity = slotTransaction.getOriginal().getCount();
                if (slotTransaction.getFinal().getType() != ItemTypes.NONE) {
                    itemQuantity -= slotTransaction.getFinal().getCount();
                }

                PrismPlus.getInstance().debugMessage("Inventory remove - {} x{}", itemId, itemQuantity);
                PrismPlusRecord prismPlusRecord = PrismPlusRecord.create().source(cause).event("remove").build();
                prismPlusRecord.getDataContainer().set(DataQueries.Location, locatable.getLocation().toContainer());
                prismPlusRecord.getDataContainer().set(DataQueries.Target, itemId);
                prismPlusRecord.getDataContainer().set(DataQueries.Quantity, itemQuantity);
                prismPlusRecord.save();
            }
        }
    }
}