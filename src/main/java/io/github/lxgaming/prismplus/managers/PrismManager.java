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

package io.github.lxgaming.prismplus.managers;

import com.helion3.prism.Prism;
import com.helion3.prism.api.query.QuerySession;
import com.helion3.prism.listeners.ChangeInventoryListener;
import com.helion3.prism.listeners.DropItemListener;
import com.helion3.prism.storage.h2.H2StorageAdapter;
import com.helion3.prism.storage.mongodb.MongoStorageAdapter;
import com.helion3.prism.storage.mysql.MySQLStorageAdapter;
import com.helion3.prism.util.AsyncCallback;
import com.helion3.prism.util.AsyncUtil;
import io.github.lxgaming.prismplus.PrismPlus;
import io.github.lxgaming.prismplus.configuration.Config;
import io.github.lxgaming.prismplus.configuration.categories.OverrideCategory;
import io.github.lxgaming.prismplus.entries.LookupCallback;
import io.github.lxgaming.prismplus.storage.H2Storage;
import io.github.lxgaming.prismplus.storage.MySQLStorage;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.event.RegisteredListener;
import org.spongepowered.common.event.SpongeEventManager;

import java.lang.reflect.Method;
import java.util.function.Predicate;

public final class PrismManager {
    
    public static void removeListener() {
        if (isPrismAvailable()) {
            invokeUnregister(handler -> handler.getHandle() instanceof ChangeInventoryListener);
            invokeUnregister(handler -> handler.getHandle() instanceof DropItemListener);
        }
    }
    
    /**
     * Appends AM / PM to the default 12-Hour format.
     */
    public static void updateDateFormat() {
        if (isPrismAvailable()) {
            String dateFormat = Prism.getConfig().getNode("display", "dateFormat").getString();
            if (StringUtils.equals(dateFormat, "d/M/yy hh:mm:ss")) {
                Prism.getConfig().getNode("display", "dateFormat").setValue(dateFormat + " a");
            }
        }
    }
    
    public static void purgeDatabase() {
        if (!isPrismAvailable() || !PrismPlus.getInstance().getConfig().map(Config::getOverrideCategory).map(OverrideCategory::isPurge).orElse(false)) {
            return;
        }
        
        if (Prism.getStorageAdapter() instanceof H2StorageAdapter) {
            Sponge.getGame().getScheduler().createTaskBuilder().async().execute(() -> new H2Storage().purge()).submit(PrismPlus.getInstance().getPluginContainer());
            return;
        }
        
        if (Prism.getStorageAdapter() instanceof MySQLStorageAdapter) {
            Sponge.getGame().getScheduler().createTaskBuilder().async().execute(() -> new MySQLStorage().purge()).submit(PrismPlus.getInstance().getPluginContainer());
            return;
        }
        
        if (Prism.getStorageAdapter() instanceof MongoStorageAdapter) {
            return;
        }
        
        PrismPlus.getInstance().getLogger().warn("Cannot purge as selected storage is not supported!");
    }
    
    /**
     * @param session The {@link com.helion3.prism.api.query.QuerySession QuerySession}.
     */
    public static void lookup(QuerySession session) {
        if (isPrismAvailable()) {
            session.getQuery().setLimit(Prism.getConfig().getNode("query", "lookup", "limit").getInt());
            invokeAsync(session, new LookupCallback(session));
        }
    }
    
    /**
     * Invokes the private method
     * {@link com.helion3.prism.util.AsyncUtil#async(QuerySession, AsyncCallback) async(QuerySession, AsyncCallback)}.
     *
     * @param session  The {@link com.helion3.prism.api.query.QuerySession QuerySession}.
     * @param callback The {@link com.helion3.prism.util.AsyncCallback AsyncCallback}.
     */
    private static void invokeAsync(QuerySession session, AsyncCallback callback) {
        try {
            Method asyncMethod = AsyncUtil.class.getDeclaredMethod("async", QuerySession.class, AsyncCallback.class);
            asyncMethod.setAccessible(true);
            asyncMethod.invoke(null, session, callback);
        } catch (Exception ex) {
            PrismPlus.getInstance().getLogger().error("Encountered an error processing {}::invokeAsync", "PrismManager", ex);
        }
    }
    
    /**
     * Invokes the private method
     * {@link org.spongepowered.common.event.SpongeEventManager#unregister(Predicate) unregister(Predicate)}.
     *
     * @param unregister The {@link java.util.function.Predicate Predicate}.
     */
    private static void invokeUnregister(Predicate<RegisteredListener<?>> unregister) {
        try {
            Method unregisterMethod = SpongeEventManager.class.getDeclaredMethod("unregister", Predicate.class);
            unregisterMethod.setAccessible(true);
            unregisterMethod.invoke(Sponge.getEventManager(), unregister);
        } catch (Exception ex) {
            PrismPlus.getInstance().getLogger().error("Encountered an error processing {}::invokeUnregister", "PrismManager", ex);
        }
    }
    
    private static boolean isPrismAvailable() {
        return Sponge.getPluginManager().isLoaded("prism") && Prism.getPlugin() != null;
    }
}