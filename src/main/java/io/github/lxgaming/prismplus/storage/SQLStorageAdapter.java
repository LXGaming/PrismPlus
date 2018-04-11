/*
 * Copyright 2018 Alex Thomson
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class SQLStorageAdapter extends StorageAdapter {
    
    private final Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    
    public SQLStorageAdapter(Connection connection) {
        this.connection = connection;
    }
    
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        if (getConnection() == null) {
            throw new SQLException("Connection is null");
        }
        
        setPreparedStatement(getConnection().prepareStatement(sql));
        return getPreparedStatement();
    }
    
    @Override
    public void close() {
        close("ResultSet", getResultSet());
        close("PreparedStatement", getPreparedStatement());
        close("Connection", getConnection());
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }
    
    protected void setPreparedStatement(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }
    
    public ResultSet getResultSet() {
        return resultSet;
    }
    
    protected void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }
}