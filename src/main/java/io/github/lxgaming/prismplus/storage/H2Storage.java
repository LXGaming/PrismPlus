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

package io.github.lxgaming.prismplus.storage;

import com.google.common.base.Stopwatch;
import com.helion3.prism.Prism;
import com.helion3.prism.storage.h2.H2StorageAdapter;
import com.helion3.prism.util.DateUtil;
import io.github.lxgaming.prismplus.PrismPlus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class H2Storage extends H2StorageAdapter {

    private final String expiration;
    private final String tablePrefix;

    public H2Storage() {
        super();
        expiration = Prism.getConfig().getNode("storage", "expireRecords").getString();
        tablePrefix = Prism.getConfig().getNode("db", "h2", "tablePrefix").getString();
    }

    public void purge() {
        PrismPlus.getInstance().getLogger().info("Purging Prism H2 database...");
        String sql = String.format(""
                        + "DELETE FROM `records`, `extra` "
                        + "USING %srecords AS `records`, %sextra AS `extra` "
                        + "WHERE `records`.`id` = `extra`.`record_id` AND `records`.`Created` <= ?;",
                getTablePrefix(), getTablePrefix());

        Stopwatch stopwatch = Stopwatch.createStarted();
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql);) {
            preparedStatement.setLong(1, DateUtil.parseTimeStringToDate(getExpiration(), false).getTime() / 1000);
            preparedStatement.executeUpdate();
            PrismPlus.getInstance().getLogger().info("Successfully purged Prism H2 database ({}ms).", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
        } catch (SQLException ex) {
            PrismPlus.getInstance().getLogger().error("Encountered an error processing {}::purge", getClass().getSimpleName(), ex);
            ex.printStackTrace();
            stopwatch.stop();
        }
    }

    private String getExpiration() {
        return expiration;
    }

    private String getTablePrefix() {
        return tablePrefix;
    }
}