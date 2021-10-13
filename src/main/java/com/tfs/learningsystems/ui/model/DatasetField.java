/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

public enum DatasetField {
    NAME("/name"),
    DESCRIPTION("/description");

    private final String field;

    DatasetField(String field) {
        this.field = field;
    }

    public String field() {
        return this.field;
    }

    public static DatasetField lookup(String value) {
        for (DatasetField datasetField : DatasetField.values()) {
            if (datasetField.field().equalsIgnoreCase(value)) {
                return datasetField;
            }
        }
        return null;
    }
}
