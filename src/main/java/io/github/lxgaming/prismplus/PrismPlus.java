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
import io.github.lxgaming.prismplus.commands.PrismPlusCommand;
import io.github.lxgaming.prismplus.configuration.Config;
import io.github.lxgaming.prismplus.configuration.Configuration;
import io.github.lxgaming.prismplus.listeners.CommandListener;
import io.github.lxgaming.prismplus.listeners.InteractListener;
import io.github.lxgaming.prismplus.listeners.InventoryListener;
import io.github.lxgaming.prismplus.managers.CommandManager;
import io.github.lxgaming.prismplus.managers.PrismManager;
import io.github.lxgaming.prismplus.util.Reference;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.nio.file.Path;
import java.util.Optional;

@Plugin(
        id = Reference.PLUGIN_ID,
        name = Reference.PLUGIN_NAME,
        version = Reference.PLUGIN_VERSION,
        description = Reference.DESCRIPTION,
        authors = {Reference.AUTHORS},
        url = Reference.WEBSITE,
        dependencies = {@Dependency(id = "prism")}
)
public class PrismPlus {
    
    private static PrismPlus instance;
    
    @Inject
    private PluginContainer pluginContainer;
    
    @Inject
    private Logger logger;
    
    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path path;
    
    private Configuration configuration;
    
    @Listener
    public void onGameConstruction(GameConstructionEvent event) {
        instance = this;
        configuration = new Configuration(getPath());
    }
    
    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        getConfiguration().loadConfiguration();
    }
    
    @Listener
    public void onGameInitialization(GameInitializationEvent event) {
        CommandManager.registerCommand(PrismPlusCommand.class);
        Sponge.getEventManager().registerListeners(getPluginContainer(), new CommandListener());
        Sponge.getEventManager().registerListeners(getPluginContainer(), new InteractListener());
        Sponge.getEventManager().registerListeners(getPluginContainer(), new InventoryListener());
    }
    
    @Listener
    public void onGamePostInitialization(GamePostInitializationEvent event) {
        getConfiguration().saveConfiguration();
    }
    
    @Listener(order = Order.LATE)
    public void onGameStartedServer(GameStartedServerEvent event) {
        PrismManager.removeListener();
        PrismManager.updateDateFormat();
        PrismManager.purgeDatabase();
        getLogger().info("{} v{} started.", Reference.PLUGIN_NAME, Reference.PLUGIN_VERSION);
    }
    
    @Listener
    public void onGameStopping(GameStoppingEvent event) {
        getLogger().info("{} v{} stopped.", Reference.PLUGIN_NAME, Reference.PLUGIN_VERSION);
    }
    
    public void debugMessage(String format, Object... arguments) {
        if (getConfig().map(Config::isDebug).orElse(false)) {
            getLogger().info(format, arguments);
        }
    }
    
    public static PrismPlus getInstance() {
        return instance;
    }
    
    public PluginContainer getPluginContainer() {
        return pluginContainer;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public Path getPath() {
        return path;
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }
    
    public Optional<Config> getConfig() {
        if (getConfiguration() != null) {
            return Optional.ofNullable(getConfiguration().getConfig());
        }
        
        return Optional.empty();
    }
}