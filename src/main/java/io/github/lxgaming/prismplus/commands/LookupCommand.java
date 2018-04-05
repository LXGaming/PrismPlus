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

import com.helion3.prism.api.query.QuerySession;
import io.github.lxgaming.prismplus.PrismPlus;
import io.github.lxgaming.prismplus.util.SpongeHelper;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LookupCommand extends Command {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        QuerySession session = new QuerySession(src);
        String parameters = args.<String>getOne("parameters").orElse(null);

        src.sendMessage(Text.of(SpongeHelper.getTextPrefix(), TextColors.WHITE, "Querying records..."));

        try {
            CompletableFuture<Void> future = session.newQueryFromArguments(parameters);
            future.thenAccept((v) -> {
                PrismPlus.getInstance().getPrismManager().lookup(session);
            });
        } catch (Exception ex) {
            src.sendMessage(Text.of(SpongeHelper.getTextPrefix(), TextColors.RED, "An error occurred. Please check the console."));
            ex.printStackTrace();
        }

        return CommandResult.success();
    }

    @Override
    public String getName() {
        return "Lookup";
    }

    @Override
    public String getUsage() {
        return "[params]";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("L");
    }

    @Override
    public String getPermission() {
        return "prism.lookup";
    }

    @Override
    public List<CommandElement> getArguments() {
        return Arrays.asList(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("parameters"))));
    }
}