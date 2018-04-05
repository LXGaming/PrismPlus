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

package io.github.lxgaming.prismplus.entries;

import com.helion3.prism.Prism;
import com.helion3.prism.queues.RecordingQueue;
import com.helion3.prism.util.DataQueries;
import net.minecraft.util.EntityDamageSource;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;

import java.util.Date;
import java.util.Optional;

public class PrismPlusRecord {

    private final String event;
    private final Object source;
    private final DataContainer dataContainer;

    protected PrismPlusRecord(String event, Object source, DataContainer dataContainer) {
        this.event = event;
        this.source = source;
        this.dataContainer = dataContainer;
    }

    public void save() {
        getDataContainer().set(DataQueries.Created, new Date());
        getDataContainer().set(DataQueries.EventName, getEvent());

        DataQuery causeKey = DataQueries.Cause;
        String causeValue = "environment";
        if (getSource() instanceof Player) {
            causeKey = DataQueries.Player;
            causeValue = ((Player) getSource()).getUniqueId().toString();
        } else if (getSource() instanceof Entity) {
            causeValue = ((Entity) getSource()).getType().getName();
        }

        getDataContainer().set(causeKey, causeValue);
        if (!Prism.getFilterList().allowsSource(getSource())) {
            return;
        }

        RecordingQueue.add(getDataContainer());
    }

    public static PrismPlusRecord.SourceBuilder create() {
        return new PrismPlusRecord.SourceBuilder();
    }

    public String getEvent() {
        return event;
    }

    public Object getSource() {
        return source;
    }

    public DataContainer getDataContainer() {
        return dataContainer;
    }

    public static class EventBuilder {

        private final Object source;
        private String event;
        private DataContainer dataContainer;

        protected EventBuilder(Object source) {
            this.source = source;
            this.event = "Unknown";
            this.dataContainer = new MemoryDataContainer();
        }

        public PrismPlusRecord build() {
            return new PrismPlusRecord(getEvent(), getSource(), getDataContainer());
        }

        private Object getSource() {
            return source;
        }

        private String getEvent() {
            return event;
        }

        public EventBuilder event(String event) {
            this.event = event;
            return this;
        }

        private DataContainer getDataContainer() {
            return dataContainer;
        }

        public EventBuilder dataContainer(DataContainer dataContainer) {
            this.dataContainer = dataContainer;
            return this;
        }
    }

    public static class SourceBuilder {

        public PrismPlusRecord.EventBuilder source(Cause cause) {
            Object source = null;

            Optional<Player> player = cause.first(Player.class);
            if (player.isPresent()) {
                source = player.get();
            }

            Optional<EntityDamageSource> attacker = cause.first(EntityDamageSource.class);
            if (attacker.isPresent()) {
                source = attacker.get().getSourceOfDamage();
            }

            Optional<IndirectEntityDamageSource> indirectAttacker = cause.first(IndirectEntityDamageSource.class);
            if (indirectAttacker.isPresent()) {
                source = indirectAttacker.get().getIndirectSource();
            }

            if (source == null) {
                source = cause.all().get(0);
            }

            return new PrismPlusRecord.EventBuilder(source);
        }

        public PrismPlusRecord.EventBuilder player(Player player) {
            return new PrismPlusRecord.EventBuilder(player);
        }

        public PrismPlusRecord.EventBuilder entity(Entity entity) {
            return new PrismPlusRecord.EventBuilder(entity);
        }
    }
}