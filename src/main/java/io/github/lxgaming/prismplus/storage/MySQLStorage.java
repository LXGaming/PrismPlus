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
import com.helion3.prism.storage.mysql.MySQLStorageAdapter;
import com.helion3.prism.util.DateUtil;
import io.github.lxgaming.prismplus.PrismPlus;
import io.github.lxgaming.prismplus.util.Toolbox;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class MySQLStorage extends MySQLStorageAdapter {
    
    private final String expiration = Prism.getConfig().getNode("storage", "expireRecords").getString();
    private final String tablePrefix = Prism.getConfig().getNode("db", "mysql", "tablePrefix").getString();
    
    public void purge() {
        PrismPlus.getInstance().getLogger().info("Purging Prism MySQL database...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        purgeRecords().ifPresent(count -> PrismPlus.getInstance().getLogger().info("Deleted {} records", count));
        purgeExtra().ifPresent(count -> PrismPlus.getInstance().getLogger().info("Deleted {} extra", count));
        PrismPlus.getInstance().getLogger().info("Finished purging Prism MySQL database ({})", Toolbox.getTimeString(stopwatch.stop().elapsed(TimeUnit.MILLISECONDS)));
    }
    
    private Optional<Integer> purgeExtra() {
        try (SQLStorageAdapter storageAdapter = new SQLStorageAdapter(getConnection())) {
            storageAdapter.prepareStatement(formatStatement("DELETE FROM `[PREFIX]extra` WHERE `[PREFIX]extra`.`record_id` NOT IN (SELECT `[PREFIX]records`.`id` FROM `[PREFIX]records`);"));
            return Optional.of(storageAdapter.getPreparedStatement().executeUpdate());
        } catch (SQLException ex) {
            PrismPlus.getInstance().getLogger().error("Encountered an error processing {}::purgeExtra", getClass().getSimpleName(), ex);
            return Optional.empty();
        }
    }
    
    private Optional<Integer> purgeRecords() {
        try (SQLStorageAdapter storageAdapter = new SQLStorageAdapter(getConnection())) {
            storageAdapter.prepareStatement(formatStatement("DELETE FROM `[PREFIX]records` WHERE `[PREFIX]records`.`created` <= ?;"));
            storageAdapter.getPreparedStatement().setLong(1, DateUtil.parseTimeStringToDate(getExpiration(), false).getTime() / 1000);
            return Optional.of(storageAdapter.getPreparedStatement().executeUpdate());
        } catch (SQLException ex) {
            PrismPlus.getInstance().getLogger().error("Encountered an error processing {}::purgeRecords", getClass().getSimpleName(), ex);
            return Optional.empty();
        }
    }
    
    private String formatStatement(String statement) {
        return StringUtils.replace(statement, "[PREFIX]", getTablePrefix());
    }
    
    private String getExpiration() {
        return expiration;
    }
    
    private String getTablePrefix() {
        return tablePrefix;
    }
}