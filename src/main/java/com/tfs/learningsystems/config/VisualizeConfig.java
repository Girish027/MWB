package com.tfs.learningsystems.config;

import com.tfs.learningsystems.ui.model.ReportField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("visualizeConfigBean")
public class VisualizeConfig {

  @Autowired
  private AppConfig appConfig;

  public List<ReportField> getDefaultFields() {
    List<ReportField> DEFAULT_FIELDS = Collections.unmodifiableList(new ArrayList<ReportField>() {{
      add(new ReportField("Manual Tags", "pie", getPieUrlTemplate("intent-added", "intent")));
      add(new ReportField("Manual Tags Deleted", "pie",
          getPieUrlTemplate("intent-deleted", "intent")));
      add(new ReportField("Suggested Category", "pie", getPieUrlTemplate("original", "auto_tag")));
      add(new ReportField("Commented By", "histogram",
          getHistogramUrlTemplate("comment-added", "commentedBy", "commentedAt", "Comments%20Added",
              "Commented%20At")));
      add(new ReportField("Tagged By", "histogram",
          getHistogramUrlTemplate("intent-added", "taggedBy", "taggedAt", "Entries%20Tagged",
              "Tagged%20At")));
      add(new ReportField("Deleted By", "histogram",
          getHistogramUrlTemplate("intent-deleted", "deletedBy", "deletedAt", "Entries%20Deleted",
              "Deleted%20At")));
      add(new ReportField("Collected At", "line",
          getLineUrlTemplate("original", "collectedAt", "Collected%20At")));
      add(new ReportField("Annotation", "pie", getPieUrlTemplate("original", "annotation")));
    }});
    return DEFAULT_FIELDS;
  }

  public Map<String, ReportField> getOptionalFields() {
    Map<String, ReportField> OPTIONAL_FIELDS = Collections
        .unmodifiableMap(new HashMap<String, ReportField>() {{
          put("inheritedIntent", new ReportField("Inherited Manual Tag", "pie",
              getPieUrlTemplate("original", "inheritedIntent")));
        }});
    return OPTIONAL_FIELDS;
  }

  /* A pie chart for a given field
   * Front end will configure:
   * {DATASET_ID} : Dataset id
   * {PROJECT_ID} : Project id
   * {DISPLAY_COUNT} : Max number of slices to display
   */
  private String getPieUrlTemplate(String documentType, String field) {
    return appConfig.getKibanaURL()
        + "#/visualize/create?type=pie&indexPattern=nltools&embed=true&_g=(refreshInterval:(display:Off,pause:!f,value:0),time:(from:now-15m,mode:quick,to:now))&_a=(filters:!(),linked:!f,query:(query_string:(analyze_wildcard:!t,query:'"
        + "datasetId:" + "{DATASET_ID}" + "%20AND%20"
        + "projectId:" + "{PROJECT_ID}" + "%20AND%20"
        + "documentType:" + documentType
        + "')),uiState:(vis:(legendOpen:!t)),vis:(aggs:!((id:'1',params:(),schema:metric,type:count),(id:'2',params:("
        + "field:" + field + ",order:desc,orderBy:'1',"
        + "size:" + "{DISPLAY_COUNT}"
        + "),schema:segment,type:terms)),listeners:(),params:(addLegend:!t,addTooltip:!t,isDonut:!f,shareYAxis:!t),type:pie))";
  }

  /* A histogram for a given series (field) and date field (timeField)
   * Front end will configure:
   * {DATASET_ID} : Dataset id
   * {PROJECT_ID} : Project id
   * {DISPLAY_INTERVAL} : Period spanned by one bar
   */
  private String getHistogramUrlTemplate(String documentType, String field, String timeField,
      String yAxisLabel, String xAxisLabel) {
    return appConfig.getKibanaURL()
        + "#/visualize/create?embed=true&type=histogram&indexPattern=nltools&_g=(refreshInterval:(display:Off,pause:!f,value:0),time:(from:now-15m,mode:quick,to:now))&_a=(filters:!(),linked:!f,query:(query_string:(analyze_wildcard:!t,query:'"
        + "datasetId:" + "{DATASET_ID}" + "%20AND%20"
        + "projectId:" + "{PROJECT_ID}" + "%20AND%20"
        + timeField + ": {DATERANGE}" + "%20AND%20"
        + "documentType:" + documentType + "')),uiState:(),vis:(aggs:!((id:'1',params:("
        + "customLabel:'" + yAxisLabel + "'),schema:metric,type:count),(id:'3',params:("
        + "customInterval:'" + "{DISPLAY_INTERVAL}" + "',"
        + "customLabel:'" + xAxisLabel + "',extended_bounds:(),"
        + "field:" + timeField
        + ",interval:custom,min_doc_count:1),schema:segment,type:date_histogram),(id:'4',params:("
        + "field:" + field
        + ",order:desc,orderBy:'1',size:20),schema:group,type:terms)),listeners:(),params:(addLegend:!t,addTimeMarker:!f,addTooltip:!t,defaultYExtents:!f,mode:stacked,scale:linear,setYExtents:!f,shareYAxis:!t,times:!(),yAxis:()),type:histogram))";
  }

  /* A line graph for a given date field
   * Front end will configure:
   * {DATASET_ID} : Dataset id
   * {PROJECT_ID} : Project id
   * {DISPLAY_INTERVAL} : Period spanned by one tick
   */
  private String getLineUrlTemplate(String documentType, String timeField, String xAxisLabel) {
    return appConfig.getKibanaURL()
        + "#/visualize/create?embed=true&type=line&indexPattern=nltools&_g=(refreshInterval:(display:Off,pause:!f,value:0),time:(from:now-15m,mode:quick,to:now))&_a=(filters:!(),linked:!f,query:(query_string:(analyze_wildcard:!t,query:'"
        + "datasetId:" + "{DATASET_ID}" + "%20AND%20"
        + "projectId:" + "{PROJECT_ID}" + "%20AND%20"
        + timeField + ": {DATERANGE}" + "%20AND%20"
        + "documentType:" + documentType
        + "')),uiState:(vis:(legendOpen:!f)),vis:(aggs:!((id:'1',params:(),schema:metric,type:count),(id:'2',params:("
        + "customInterval:'" + "{DISPLAY_INTERVAL}" + "',"
        + "customLabel:'" + xAxisLabel + "',extended_bounds:(),"
        + "field:" + timeField
        + ",interval:custom,min_doc_count:1),schema:segment,type:date_histogram)),listeners:(),params:(addLegend:!t,addTimeMarker:!f,addTooltip:!t,defaultYExtents:!f,drawLinesBetweenPoints:!t,interpolate:linear,radiusRatio:9,scale:linear,setYExtents:!f,shareYAxis:!t,showCircles:!t,smoothLines:!f,times:!(),yAxis:()),type:line))";
  }

}
