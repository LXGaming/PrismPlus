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

package io.github.lxgaming.prismplus.commands;

import io.github.lxgaming.prismplus.util.Reference;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;

public abstract class Command implements CommandExecutor {

    public abstract String getName();

    public Text getDescription() {
        return Text.of("No description provided");
    }

    public String getUsage() {
        return null;
    }

    public List<String> getAliases() {
        return null;
    }

    public String getPermission() {
        return Reference.PLUGIN_NAME + ".Command." + getName();
    }

    public List<CommandElement> getArguments() {
        return null;
    }

    public List<Command> getSubCommands() {
        return null;
    }

    protected boolean showHelp(CommandSource commandSource, String format) {
        if (getSubCommands() == null) {
            return false;
        }

        List<Text> messages = new ArrayList<Text>();
        boolean missingPermissions = false;
        for (Command command : getSubCommands()) {
            if (StringUtils.isNotBlank(command.getPermission()) && !commandSource.hasPermission(command.getPermission())) {
                missingPermissions = true;
                continue;
            }

            String commandName = format.replace("[COMMAND]", command.getName());
            Text.Builder textBuilder = Text.builder();
            textBuilder.onClick(TextActions.suggestCommand(commandName));
            if (StringUtils.isNotBlank(command.getUsage())) {
                textBuilder.onHover(TextActions.showText(Text.of(commandName, " ", command.getUsage())));
                textBuilder.append(Text.of(TextColors.GREEN, commandName, " ", TextColors.WHITE, command.getUsage()));
            } else {
                textBuilder.onHover(TextActions.showText(Text.of(commandName)));
                textBuilder.append(Text.of(TextColors.GREEN, commandName));
            }

            messages.add(textBuilder.build());
        }

        if (missingPermissions) {
            messages.add(Text.of(TextColors.RED, "You are missing permissions for one or more commands!"));
        }

        if (messages.isEmpty()) {
            return false;
        }

        PaginationList.Builder paginationBuilder = PaginationList.builder();
        paginationBuilder.title(Text.of(TextColors.DARK_GREEN, getName() + " Commands:"));
        paginationBuilder.padding(Text.of(TextColors.DARK_GREEN, "="));
        paginationBuilder.contents(messages);
        paginationBuilder.build().sendTo(commandSource);
        return true;
    }
}