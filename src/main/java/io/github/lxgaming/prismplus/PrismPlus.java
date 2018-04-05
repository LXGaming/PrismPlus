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

package io.github.lxgaming.prismplus;

import com.google.inject.Inject;
import io.github.lxgaming.prismplus.configuration.Configuration;
import io.github.lxgaming.prismplus.entries.Config;
import io.github.lxgaming.prismplus.managers.PrismManager;
import io.github.lxgaming.prismplus.managers.RegistryManager;
import io.github.lxgaming.prismplus.util.Metrics;
import io.github.lxgaming.prismplus.util.Reference;
import org.slf4j.Logger;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import java.nio.file.Path;

@Plugin(
        id = Reference.PLUGIN_ID,
        name = Reference.PLUGIN_NAME,
        version = Reference.PLUGIN_VERSION,
        description = Reference.DESCRIPTION,
        authors = {Reference.AUTHORS},
        url = Reference.WEBSITE,
        dependencies = {@Dependency(id = "prism", optional = false)}
)
public class PrismPlus {

    private static PrismPlus instance;

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path path;

    // @Inject - Metrics are disabled due to an issue that can cause the server to crash on startup.
    private Metrics metrics;

    private Configuration configuration;
    private PrismManager prismManager;
    private RegistryManager registryManager;

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        instance = this;
        configuration = new Configuration();
        prismManager = new PrismManager();
        registryManager = new RegistryManager();
    }

    @Listener
    public void onGameInitialization(GameInitializationEvent event) {
        getConfiguration().loadConfiguration();
        getConfiguration().saveConfiguration();
        getRegistryManager().register();
    }

    @Listener(order = Order.LAST)
    public void onGameStartedServer(GameStartedServerEvent event) {
        getLogger().info("{} v{} started.", Reference.PLUGIN_NAME, Reference.PLUGIN_VERSION);
        getPrismManager().checkVersion();
        getPrismManager().updateDateFormat();
        getPrismManager().purgeDatabase();
    }

    @Listener
    public void onGameStopping(GameStoppingEvent event) {
        getLogger().info("{} v{} stopped.", Reference.PLUGIN_NAME, Reference.PLUGIN_VERSION);
    }

    public void debugMessage(String message, Object... objects) {
        if (getConfig() != null && getConfig().isDebug()) {
            getLogger().info(message, objects);
        }
    }

    public static PrismPlus getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getPath() {
        return path;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Config getConfig() {
        if (getConfiguration() != null) {
            return getConfiguration().getConfig();
        }

        return null;
    }

    public PrismManager getPrismManager() {
        return prismManager;
    }

    public RegistryManager getRegistryManager() {
        return registryManager;
    }
}