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

public class Config {

    private boolean debug;
    private boolean commandEvent;
    private boolean insertEvent;
    private boolean removeEvent;
    private boolean commandOverride;
    private boolean inspectOverride;
    private boolean purgeOverride;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isCommandEvent() {
        return commandEvent;
    }

    public void setCommandEvent(boolean commandEvent) {
        this.commandEvent = commandEvent;
    }

    public boolean isInsertEvent() {
        return insertEvent;
    }

    public void setInsertEvent(boolean insertEvent) {
        this.insertEvent = insertEvent;
    }

    public boolean isRemoveEvent() {
        return removeEvent;
    }

    public void setRemoveEvent(boolean removeEvent) {
        this.removeEvent = removeEvent;
    }

    public boolean isCommandOverride() {
        return commandOverride;
    }

    public void setCommandOverride(boolean commandOverride) {
        this.commandOverride = commandOverride;
    }

    public boolean isInspectOverride() {
        return inspectOverride;
    }

    public void setInspectOverride(boolean inspectOverride) {
        this.inspectOverride = inspectOverride;
    }

    public boolean isPurgeOverride() {
        return purgeOverride;
    }

    public void setPurgeOverride(boolean purgeOverride) {
        this.purgeOverride = purgeOverride;
    }
}