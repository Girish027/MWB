/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

public enum ProjectField {
    NAME("/name"),
    DESCRIPTION("/description"),
    VERTICAL("/vertical"),
    LOCALE("/locale"),
    DEPLOYABLEMODELID("/deployableModelId"),
    LIVEMODELID("/liveModelId"),
    PREVIEWMODELID("/previewModelId");

    private final String field;

    ProjectField(String field) {
        this.field = field;
    }

    public String field() {
        return this.field;
    }

    public static ProjectField lookup(String value) {
        for (ProjectField project : ProjectField.values()) {
            if (project.field().equalsIgnoreCase(value)) {
                return project;
            }
        }
        return null;
    }
}
