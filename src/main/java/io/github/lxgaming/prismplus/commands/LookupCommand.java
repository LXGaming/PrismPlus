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
import io.github.lxgaming.prismplus.managers.PrismManager;
import io.github.lxgaming.prismplus.util.Toolbox;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LookupCommand extends AbstractCommand {
    
    public LookupCommand() {
        addAlias("lookup");
        addAlias("l");
        setPermission("prism.lookup");
        setUsage("[params]");
    }
    
    @Override
    public CommandResult execute(CommandSource commandSource, List<String> arguments) {
        QuerySession session = new QuerySession(commandSource);
        commandSource.sendMessage(Text.of(Toolbox.getTextPrefix(), TextColors.WHITE, "Querying records..."));
        
        try {
            CompletableFuture<Void> future = session.newQueryFromArguments(StringUtils.defaultIfBlank(StringUtils.join(arguments, " "), null));
            future.thenAccept((v) -> PrismManager.lookup(session));
        } catch (Exception ex) {
            PrismPlus.getInstance().getLogger().error("Encountered an error processing {}::execute", getClass().getSimpleName(), ex);
            commandSource.sendMessage(Text.of(Toolbox.getTextPrefix(), TextColors.RED, "An error occurred. Please check the console."));
        }
        
        return CommandResult.success();
    }
}