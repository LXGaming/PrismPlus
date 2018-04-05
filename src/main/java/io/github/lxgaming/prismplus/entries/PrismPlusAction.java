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

public enum PrismPlusAction {

    BREAK("Broke"), PLACE("Placed"),
    DECAY("Decayed"), GROW("Grew"),
    JOIN("Joined"), QUIT("Left"),
    DEATH("Killed"),
    INSERT("Inserted"), REMOVE("Removed"),
    UNKNOWN("Unknown");

    private final String value;

    private PrismPlusAction(String value) {
        this.value = value;
    }

    public static String getValue(String event) {
        try {
            return PrismPlusAction.valueOf(event.toUpperCase()).getValue();
        } catch (RuntimeException ex) {
            return event;
        }
    }

    public String getValue() {
        return value;
    }
}