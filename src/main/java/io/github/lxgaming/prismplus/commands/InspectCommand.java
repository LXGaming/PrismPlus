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

import java.util.Arrays;
import java.util.List;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.helion3.prism.Prism;

import io.github.lxgaming.prismplus.util.SpongeHelper;

public class InspectCommand extends Command {
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) {
			src.sendMessage(Text.of(SpongeHelper.getTextPrefix(), TextColors.RED, "You must be a player to use this command."));
			return CommandResult.success();
		}
		
		Player player = (Player) src;
		if (Prism.getActiveWands().contains(player.getUniqueId())) {
			Prism.getActiveWands().remove(player.getUniqueId());
			player.sendMessage(Text.of(SpongeHelper.getTextPrefix(), TextColors.WHITE, "Inspection wand ", TextColors.RED, "disabled", TextColors.WHITE, "."));
		} else {
			Prism.getActiveWands().add(player.getUniqueId());
			player.sendMessage(Text.of(SpongeHelper.getTextPrefix(), TextColors.WHITE, "Inspection wand ", TextColors.GREEN, "enabled", TextColors.WHITE, "."));
		}
		
		return CommandResult.success();
	}
	
	@Override
	public String getName() {
		return "Inspect";
	}
	
	@Override
	public List<String> getAliases() {
		return Arrays.asList("I");
	}
	
	@Override
	public String getPermission() {
		return "prism.inspect";
	}
}