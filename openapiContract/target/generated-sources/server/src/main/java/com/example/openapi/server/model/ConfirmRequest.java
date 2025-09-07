package com.example.openapi.server.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ConfirmRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.12.0")
public class ConfirmRequest {

  private String orderId;

  private String transactionId;

  public ConfirmRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ConfirmRequest(String orderId, String transactionId) {
    this.orderId = orderId;
    this.transactionId = transactionId;
  }

  public ConfirmRequest orderId(String orderId) {
    this.orderId = orderId;
    return this;
  }

  /**
   * Get orderId
   * @return orderId
   */
  @NotNull 
  @Schema(name = "orderId", example = "12345", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("orderId")
  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public ConfirmRequest transactionId(String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  /**
   * Get transactionId
   * @return transactionId
   */
  @NotNull 
  @Schema(name = "transactionId", example = "tx_987654", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("transactionId")
  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConfirmRequest confirmRequest = (ConfirmRequest) o;
    return Objects.equals(this.orderId, confirmRequest.orderId) &&
        Objects.equals(this.transactionId, confirmRequest.transactionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderId, transactionId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConfirmRequest {\n");
    sb.append("    orderId: ").append(toIndentedString(orderId)).append("\n");
    sb.append("    transactionId: ").append(toIndentedString(transactionId)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

