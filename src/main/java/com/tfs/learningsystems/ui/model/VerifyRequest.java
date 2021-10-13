package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * VerifyRequest
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-08-21T16:55:37.740-07:00")
public class VerifyRequest {

  private String configId = null;

  private VerifyRequestFilter filter = new VerifyRequestFilter();

  public VerifyRequest configId(String configId) {
    this.configId = configId;
    return this;
  }

  /**
   * id of the model configuration
   *
   * @return configId
   **/
  @ApiModelProperty(value = "id of the model configuration")
  public String getConfigId() {
    return configId;
  }

  public void setConfigId(String configId) {
    this.configId = configId;
  }

  public VerifyRequest filter(VerifyRequestFilter filter) {
    this.filter = filter;
    return this;
  }

  /**
   * Get filter
   *
   * @return filter
   **/
  @ApiModelProperty(value = "")
  public VerifyRequestFilter getFilter() {
    return filter;
  }

  public void setFilter(VerifyRequestFilter filter) {
    this.filter = filter;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VerifyRequest verifyRequest = (VerifyRequest) o;
    return Objects.equals(this.configId, verifyRequest.configId) &&
        Objects.equals(this.filter, verifyRequest.filter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(configId, filter);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VerifyRequest {\n");

    sb.append("    configId: ").append(toIndentedString(configId)).append("\n");
    sb.append("    filter: ").append(toIndentedString(filter)).append("\n");
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

