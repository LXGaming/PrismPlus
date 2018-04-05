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

package io.github.lxgaming.prismplus.configuration;

import io.github.lxgaming.prismplus.PrismPlus;
import io.github.lxgaming.prismplus.entries.Config;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;

public class Configuration {

    private final ConfigurationLoader<CommentedConfigurationNode> configurationLoader;
    private final ConfigurationOptions configurationOptions;
    private CommentedConfigurationNode configurationNode;
    private Config config;

    public Configuration() {
        configurationLoader = HoconConfigurationLoader.builder().setPath(PrismPlus.getInstance().getPath()).build();
        configurationOptions = ConfigurationOptions.defaults().setShouldCopyDefaults(true);
    }

    public void loadConfiguration() {
        try {
            setConfigurationNode(getConfigurationLoader().load(getConfigurationOptions()));
            setConfig(new Config());

            getConfig().setDebug(getConfigurationNode().getNode("general", "debug").getBoolean(false));
            getConfig().setCommandEvent(getConfigurationNode().getNode("events", "command").getBoolean(false));
            getConfig().setInsertEvent(getConfigurationNode().getNode("events", "insert").getBoolean(true));
            getConfig().setRemoveEvent(getConfigurationNode().getNode("events", "remove").getBoolean(true));
            getConfig().setCommandOverride(getConfigurationNode().getNode("overrides", "command").getBoolean(true));
            getConfig().setInspectOverride(getConfigurationNode().getNode("overrides", "inspect").getBoolean(true));
            getConfig().setPurgeOverride(getConfigurationNode().getNode("overrides", "purge").getBoolean(false));

            PrismPlus.getInstance().getLogger().info("Successfully loaded configuration file.");
        } catch (IOException | RuntimeException ex) {
            PrismPlus.getInstance().getLogger().error("Encountered an error processing {}::loadConfiguration", getClass().getSimpleName(), ex);
            ex.printStackTrace();
        }
    }

    public void saveConfiguration() {
        try {
            getConfigurationNode().getNode("general", "debug").setValue(getConfig().isDebug());
            getConfigurationNode().getNode("events", "command").setValue(getConfig().isCommandEvent());
            getConfigurationNode().getNode("events", "insert").setValue(getConfig().isInsertEvent());
            getConfigurationNode().getNode("events", "remove").setValue(getConfig().isRemoveEvent());
            getConfigurationNode().getNode("overrides", "command").setValue(getConfig().isCommandOverride());
            getConfigurationNode().getNode("overrides", "inspect").setValue(getConfig().isInspectOverride());
            getConfigurationNode().getNode("overrides", "purge").setValue(getConfig().isPurgeOverride());

            getConfigurationLoader().save(getConfigurationNode());
            PrismPlus.getInstance().getLogger().info("Successfully saved configuration file.");
        } catch (IOException | RuntimeException ex) {
            PrismPlus.getInstance().getLogger().error("Encountered an error processing {}::saveConfiguration", getClass().getSimpleName(), ex);
            ex.printStackTrace();
        }
    }

    private ConfigurationLoader<CommentedConfigurationNode> getConfigurationLoader() {
        return configurationLoader;
    }

    private ConfigurationOptions getConfigurationOptions() {
        return configurationOptions;
    }

    private CommentedConfigurationNode getConfigurationNode() {
        return configurationNode;
    }

    private void setConfigurationNode(CommentedConfigurationNode configurationNode) {
        this.configurationNode = configurationNode;
    }

    public Config getConfig() {
        return config;
    }

    private void setConfig(Config config) {
        this.config = config;
    }
}