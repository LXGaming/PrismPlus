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

import com.helion3.prism.Prism;
import io.github.lxgaming.prismplus.util.Toolbox;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

public class InspectCommand extends AbstractCommand {
    
    public InspectCommand() {
        addAlias("inspect");
        addAlias("i");
        setPermission("prism.inspect");
    }
    
    @Override
    public CommandResult execute(CommandSource commandSource, List<String> arguments) {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(Text.of(Toolbox.getTextPrefix(), TextColors.RED, "You must be a player to use this command."));
            return CommandResult.success();
        }
        
        Player player = (Player) commandSource;
        if (Prism.getActiveWands().contains(player.getUniqueId())) {
            Prism.getActiveWands().remove(player.getUniqueId());
            player.sendMessage(Text.of(Toolbox.getTextPrefix(), TextColors.WHITE, "Inspection wand ", TextColors.RED, "disabled", TextColors.WHITE, "."));
        } else {
            Prism.getActiveWands().add(player.getUniqueId());
            player.sendMessage(Text.of(Toolbox.getTextPrefix(), TextColors.WHITE, "Inspection wand ", TextColors.GREEN, "enabled", TextColors.WHITE, "."));
        }
        
        return CommandResult.success();
    }
}