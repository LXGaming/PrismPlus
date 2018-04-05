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
import com.helion3.prism.storage.h2.H2StorageAdapter;
import com.helion3.prism.storage.mongodb.MongoStorageAdapter;
import com.helion3.prism.storage.mysql.MySQLStorageAdapter;
import com.helion3.prism.util.AsyncCallback;
import com.helion3.prism.util.AsyncUtil;
import io.github.lxgaming.prismplus.PrismPlus;
import io.github.lxgaming.prismplus.entries.LookupCallback;
import io.github.lxgaming.prismplus.storage.H2Storage;
import io.github.lxgaming.prismplus.storage.MySQLStorage;
import io.github.lxgaming.prismplus.util.Reference;
import io.github.lxgaming.prismplus.util.SpongeHelper;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class PrismManager {

    public void checkVersion() {
        if (!isPrismAvailable()) {
            PrismPlus.getInstance().getLogger().warn("Prism is not available!");
            return;
        }

        PluginContainer pluginContainer = Sponge.getPluginManager().getPlugin("prism").orElse(null);
        if (pluginContainer == null) {
            PrismPlus.getInstance().getLogger().warn("Failed to get Prism information!");
            return;
        }

        String version = pluginContainer.getVersion().orElse(null);
        if (StringUtils.isNotBlank(version) && getSupportedVersions().contains(version)) {
            PrismPlus.getInstance().getLogger().info("Found supported Prism v{}", version);
            return;
        }

        PrismPlus.getInstance().getLogger().warn("Found unsupported Prism v{}, This version may not work well with {}!", version, Reference.PLUGIN_NAME);
    }

    /**
     * Appends AM / PM to the default 12-Hour format.
     */
    public void updateDateFormat() {
        if (!isPrismAvailable()) {
            return;
        }

        String dateFormat = Prism.getConfig().getNode("display", "dateFormat").getString();
        if (StringUtils.isNotBlank(dateFormat) && dateFormat.equals("d/M/yy hh:mm:ss")) {
            Prism.getConfig().getNode("display", "dateFormat").setValue(dateFormat + " a");
        }
    }

    public void purgeDatabase() {
        if (!isPrismAvailable() || PrismPlus.getInstance().getConfig() == null || !PrismPlus.getInstance().getConfig().isPurgeOverride()) {
            return;
        }

        if (Prism.getStorageAdapter() instanceof H2StorageAdapter) {
            Sponge.getGame().getScheduler().createTaskBuilder().async().execute(() -> {
                new H2Storage().purge();
            }).submit(PrismPlus.getInstance());
            return;
        }

        if (Prism.getStorageAdapter() instanceof MySQLStorageAdapter) {
            Sponge.getGame().getScheduler().createTaskBuilder().async().execute(() -> {
                new MySQLStorage().purge();
            }).submit(PrismPlus.getInstance());
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
    public void lookup(QuerySession session) {
        if (!isPrismAvailable()) {
            return;
        }

        session.getQuery().setLimit(Prism.getConfig().getNode("query", "lookup", "limit").getInt());
        invokeAsync(session, new LookupCallback(session));
    }

    /**
     * Invokes the private method
     * {@link com.helion3.prism.util.AsyncUtil#async(QuerySession, AsyncCallback) async(QuerySession, AsyncCallback)}.
     *
     * @param session  The {@link com.helion3.prism.api.query.QuerySession QuerySession}.
     * @param callback The {@link com.helion3.prism.util.AsyncCallback AsyncCallback}.
     */
    public void invokeAsync(QuerySession session, AsyncCallback callback) {
        try {
            Method asyncMethod = AsyncUtil.class.getDeclaredMethod("async", QuerySession.class, AsyncCallback.class);
            asyncMethod.setAccessible(true);
            asyncMethod.invoke(null, session, callback);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | RuntimeException ex) {
            PrismPlus.getInstance().getLogger().error("Encountered an error processing {}::invokeAsync", SpongeHelper.class.getSimpleName(), ex);
            ex.printStackTrace();
        }
    }

    public boolean isPrismAvailable() {
        if (!Sponge.getPluginManager().isLoaded("prism")) {
            return false;
        }

        if (Prism.getPlugin() != null && Prism.getConfig() != null) {
            return true;
        }

        return false;
    }

    public List<String> getSupportedVersions() {
        return Arrays.asList("3.0.0");
    }
}