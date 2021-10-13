package com.tfs.learningsystems.ui.model;

import java.util.Objects;

public class DatasetIntentDetail {
    String intent;
    String datasetSource;
    Integer datasetId;

    public DatasetIntentDetail(String intent, String datasetSource, Integer datasetId) {
        this.intent = intent;
        this.datasetSource = datasetSource;
        this.datasetId = datasetId;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public void setDatasetSource(String datasetSource) {
        this.datasetSource = datasetSource;
    }

    public void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
    }

    public String getIntent() {
        return intent;
    }

    public String getDatasetSource() {
        return datasetSource;
    }

    public Integer getDatasetId() {
        return datasetId;
    }

    public void updateValues(String intent, String datasetSource, Integer datasetId) {
        this.setIntent(intent);
        this.setDatasetSource(datasetSource);
        this.setDatasetId(datasetId);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || getClass() != obj.getClass())
            return false;
        if (obj == this)
            return true;
        return this.getDatasetId() != null && this.getDatasetId().equals(((DatasetIntentDetail) obj).getDatasetId())
                && this.getDatasetSource() != null && this.getDatasetSource().equals(((DatasetIntentDetail) obj).getDatasetSource())
                && this.getIntent() != null && this.getIntent().equals(((DatasetIntentDetail) obj).getIntent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(intent, datasetSource, datasetId);
    }
}
