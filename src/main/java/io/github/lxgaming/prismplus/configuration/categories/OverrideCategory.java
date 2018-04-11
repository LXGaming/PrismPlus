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
public class OverrideCategory {
    
    @Setting(value = "command", comment = "If 'true', PrismPlus will override certain Prism commands")
    private boolean command = true;
    
    @Setting(value = "inspect", comment = "If 'true', PrismPlus will")
    private boolean inspect = true;
    
    @Setting(value = "purge", comment = "If 'true', Records older than  in your H2 or MySQL")
    private boolean purge = false;
    
    public boolean isCommand() {
        return command;
    }
    
    public boolean isInspect() {
        return inspect;
    }
    
    public boolean isPurge() {
        return purge;
    }
}