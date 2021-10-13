/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

public enum GlobalModelName {
    ROOT_INTENT("Root_Intent"),
    SOCIAL("Social"),
    SENTIMENT("Sentiment"),
    YES_NO("Yes_No");

    private final String globalName;

    GlobalModelName(String globalName) {
        this.globalName = globalName;
    }

    public String globalName() {
        return this.globalName;
    }

    public static GlobalModelName lookup(String value) {
        for (GlobalModelName name : GlobalModelName.values()) {
            if (name.globalName().equalsIgnoreCase(value)) {
                return name;
            }
        }
        return null;
    }
}
