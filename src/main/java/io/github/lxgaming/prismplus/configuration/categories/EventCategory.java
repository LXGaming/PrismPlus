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

package io.github.lxgaming.prismplus.configuration.categories;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class EventCategory {
    
    @Setting(value = "command", comment = "Log when commands are executed")
    private boolean command = false;
    
    @Setting(value = "insert", comment = "Log when an item is inserted into a container")
    private boolean insert = true;
    
    @Setting(value = "remove", comment = "Log when an item is removed from a container")
    private boolean remove = true;
    
    public boolean isCommand() {
        return command;
    }
    
    public boolean isInsert() {
        return insert;
    }
    
    public boolean isRemove() {
        return remove;
    }
}