/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * ReportField
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-16T04:38:35.460-04:00")
public class ReportField {

  private String urlTemplate = null;

  private String name = null;

  private String chartType = null;

  public ReportField(String name, String chartType, String urlTemplate) {
    this.name = name;
    this.chartType = chartType;
    this.urlTemplate = urlTemplate;

  }

  public ReportField() {
  }


  public ReportField urlTemplate(String urlTemplate) {
    this.urlTemplate = urlTemplate;
    return this;
  }

  /**
   * URL template for Kibana chart
   *
   * @return urlTemplate
   **/
  @ApiModelProperty(required = true, value = "URL template for Kibana chart")
  public String getUrlTemplate() {
    return urlTemplate;
  }

  public void setUrlTemplate(String urlTemplate) {
    this.urlTemplate = urlTemplate;
  }

  public ReportField name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Name of the field displayed to user
   *
   * @return name
   **/
  @ApiModelProperty(required = true, value = "Name of the field displayed to user")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ReportField chartType(String chartType) {
    this.chartType = chartType;
    return this;
  }

  /**
   * Type of visualization for this field
   *
   * @return chartType
   **/
  @ApiModelProperty(value = "Type of visualization for this field")
  public String getChartType() {
    return chartType;
  }

  public void setChartType(String chartType) {
    this.chartType = chartType;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReportField reportField = (ReportField) o;
    return Objects.equals(this.urlTemplate, reportField.urlTemplate) &&
        Objects.equals(this.name, reportField.name) &&
        Objects.equals(this.chartType, reportField.chartType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(urlTemplate, name, chartType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReportField {\n");

    sb.append("    query: ").append(toIndentedString(urlTemplate)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    chartType: ").append(toIndentedString(chartType)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first
   * line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}