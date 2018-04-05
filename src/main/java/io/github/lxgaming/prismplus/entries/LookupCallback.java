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

package io.github.lxgaming.prismplus.entries;

import com.helion3.prism.api.flags.Flag;
import com.helion3.prism.api.query.QuerySession;
import com.helion3.prism.api.records.Result;
import com.helion3.prism.api.records.ResultAggregate;
import com.helion3.prism.api.records.ResultComplete;
import com.helion3.prism.util.AsyncCallback;
import com.helion3.prism.util.DataQueries;
import io.github.lxgaming.prismplus.PrismPlus;
import io.github.lxgaming.prismplus.util.SpongeHelper;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Customizable {@link com.helion3.prism.Prism Prism} {@link com.helion3.prism.api.records.Result Result}.
 *
 * @see {@link com.helion3.prism.util.AsyncUtil AsyncUtil}
 * @see {@link com.helion3.prism.util.Messages Messages}
 */
public class LookupCallback extends AsyncCallback {

    private final QuerySession querySession;

    public LookupCallback(QuerySession querySession) {
        this.querySession = querySession;
    }

    @Override
    public void success(List<Result> results) {
        List<Text> messages = new ArrayList<Text>();
        for (Result result : results) {
            messages.add(buildResult(result));
        }

        if (messages.isEmpty()) {
            getQuerySession().getCommandSource().sendMessage(Text.of(SpongeHelper.getTextPrefix(), TextColors.RED, "Nothing found. See /pr ? for help."));
            return;
        }

        PaginationList.Builder paginationBuilder = PaginationList.builder();
        paginationBuilder.padding(Text.of(TextColors.DARK_GRAY, "="));
        paginationBuilder.linesPerPage(15);
        paginationBuilder.contents(messages);
        paginationBuilder.build().sendTo(getQuerySession().getCommandSource());
    }

    @Override
    public void empty() {
        getQuerySession().getCommandSource().sendMessage(Text.of(SpongeHelper.getTextPrefix(), TextColors.RED, "Nothing found. See /pr ? for help."));
    }

    @Override
    public void error(Exception ex) {
        getQuerySession().getCommandSource().sendMessage(Text.of(SpongeHelper.getTextPrefix(), TextColors.RED, "An error occurred. Please see the console."));
        PrismPlus.getInstance().getLogger().error("Exception thrown by {}", getClass().getSimpleName(), ex);
        ex.printStackTrace();
    }

    private Text buildResult(Result result) {
        Text.Builder resultMessage = Text.builder();
        Text.Builder hoverMessage = Text.builder();

        hoverMessage.append(SpongeHelper.getTextPrefix(), Text.NEW_LINE);

        resultMessage.append(Text.of(TextColors.DARK_AQUA, result.getSourceName(), " "));
        resultMessage.append(Text.of(TextColors.WHITE, PrismPlusAction.getValue(result.getEventName()).toLowerCase(), " "));
        hoverMessage.append(Text.of(TextColors.DARK_GRAY, "Source: ", TextColors.WHITE, result.getSourceName(), Text.NEW_LINE));
        hoverMessage.append(Text.of(TextColors.DARK_GRAY, "Event: ", TextColors.WHITE, result.getEventName(), Text.NEW_LINE));

        String quantity = result.data.getString(DataQueries.Quantity).orElse(null);
        if (StringUtils.isNotBlank(quantity)) {
            resultMessage.append(Text.of(TextColors.DARK_AQUA, quantity, " "));
            hoverMessage.append(Text.of(TextColors.DARK_GRAY, "Quantity: ", TextColors.WHITE, quantity, Text.NEW_LINE));
        }

        String target = result.data.getString(DataQueries.Target).orElse("Unknown");
        if (StringUtils.isNotBlank(target)) {
            resultMessage.append(Text.of(TextColors.DARK_AQUA, SpongeHelper.getItemText(target, false), " "));
            hoverMessage.append(Text.of(TextColors.DARK_GRAY, "Target: ", TextColors.WHITE, target, Text.NEW_LINE));
        }

        if (result instanceof ResultAggregate) {
            int count = result.data.getInt(DataQueries.Count).orElse(0);
            if (count > 0) {
                resultMessage.append(Text.of(TextColors.GREEN, "x", count, " "));
                hoverMessage.append(Text.of(TextColors.DARK_GRAY, "Count: ", TextColors.WHITE, count));
            }
        }

        if (result instanceof ResultComplete) {
            ResultComplete resultComplete = (ResultComplete) result;

            resultMessage.append(Text.of(TextColors.WHITE, resultComplete.getRelativeTime()));
            hoverMessage.append(Text.of(TextColors.DARK_GRAY, "Time: ", TextColors.WHITE, resultComplete.getTime(), Text.NEW_LINE));

            DataView location = (DataView) resultComplete.data.get(DataQueries.Location).orElse(null);
            if (location != null) {
                int x = location.getInt(DataQueries.X).get();
                int y = location.getInt(DataQueries.Y).get();
                int z = location.getInt(DataQueries.Z).get();

                UUID worldUniqueId = null;
                if (location.get(DataQueries.WorldUuid).get() instanceof UUID) {
                    worldUniqueId = (UUID) location.get(DataQueries.WorldUuid).get();
                } else {
                    worldUniqueId = UUID.fromString(location.getString(DataQueries.WorldUuid).get());
                }

                World world = Sponge.getServer().getWorld(worldUniqueId).orElse(null);
                if (getQuerySession().hasFlag(Flag.EXTENDED)) {
                    resultMessage.append(Text.of(Text.NEW_LINE, TextColors.GRAY, " - ", SpongeHelper.getLocationText(x, y, z, world, true)));
                }

                hoverMessage.append(Text.of(TextColors.DARK_GRAY, "Location: ", TextColors.WHITE, SpongeHelper.getLocationText(x, y, z, world, false)));
            }
        }

        resultMessage.onHover(TextActions.showText(hoverMessage.build()));
        return resultMessage.build();
    }

    private QuerySession getQuerySession() {
        return querySession;
    }
}