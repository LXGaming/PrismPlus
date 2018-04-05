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

import io.github.lxgaming.prismplus.PrismPlus;
import io.github.lxgaming.prismplus.commands.Command;
import io.github.lxgaming.prismplus.commands.PrismPlusCommand;
import io.github.lxgaming.prismplus.listeners.CommandListener;
import io.github.lxgaming.prismplus.listeners.InteractListener;
import io.github.lxgaming.prismplus.listeners.InventoryListener;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistryManager {

    public void register() {
        registerCommand(new PrismPlusCommand());
        registerListener(new CommandListener());
        registerListener(new InteractListener());
        registerListener(new InventoryListener());
    }

    public void registerCommand(Command command) {
        if (command == null) {
            PrismPlus.getInstance().getLogger().warn("Provided command is null!");
            return;
        }

        Sponge.getCommandManager().register(PrismPlus.getInstance(), generateSpongeCommand(command, new ArrayList<String>()).build(), getAliases(command));
    }

    public void registerListener(Object object) {
        if (object == null) {
            PrismPlus.getInstance().getLogger().warn("Provided listener is null!");
            return;
        }

        Sponge.getEventManager().registerListeners(PrismPlus.getInstance(), object);
    }

    private CommandSpec.Builder generateSpongeCommand(Command command, List<String> commandClasses) {
        if (commandClasses.contains(command.getClass().getName())) {
            PrismPlus.getInstance().getLogger().warn("Duplicate registration for command {} detected!", command.getName());
            return null;
        }

        commandClasses.add(command.getClass().getName());
        CommandSpec.Builder commandBuilder = CommandSpec.builder();
        if (command.getArguments() != null && !command.getArguments().isEmpty()) {
            commandBuilder.arguments((CommandElement[]) command.getArguments().toArray());
        }

        commandBuilder.description(command.getDescription());
        commandBuilder.executor(command);
        if (StringUtils.isNotBlank(command.getPermission())) {
            commandBuilder.permission(command.getPermission());
        }

        if (commandClasses == null || command.getSubCommands() == null || command.getSubCommands().isEmpty()) {
            return commandBuilder;
        }

        Map<List<String>, CommandCallable> children = new HashMap<List<String>, CommandCallable>();
        for (Command subCommand : command.getSubCommands()) {
            CommandSpec.Builder subCommandBuilder = generateSpongeCommand(subCommand, commandClasses);
            List<String> aliases = getAliases(subCommand);
            if (subCommandBuilder != null && aliases != null && !aliases.isEmpty()) {
                children.put(aliases, subCommandBuilder.build());
            }
        }

        if (!children.isEmpty()) {
            commandBuilder.children(children);
        }

        return commandBuilder;
    }

    private List<String> getAliases(Command command) {
        List<String> aliases = new ArrayList<String>();
        if (command != null && StringUtils.isNotBlank(command.getName())) {
            aliases.add(command.getName());
        }

        if (command != null && command.getAliases() != null) {
            aliases.addAll(command.getAliases());
        }

        if (command == null || aliases.isEmpty()) {
            PrismPlus.getInstance().getLogger().warn("Failed to get aliases!");
        }

        return aliases;
    }
}