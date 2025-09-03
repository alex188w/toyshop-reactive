package com.example.openapi.server.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ConfirmResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.12.0")
public class ConfirmResponse {

  private String orderId;

  private String transactionId;

  private Boolean confirmed;

  private @Nullable String message;

  public ConfirmResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ConfirmResponse(String orderId, String transactionId, Boolean confirmed) {
    this.orderId = orderId;
    this.transactionId = transactionId;
    this.confirmed = confirmed;
  }

  public ConfirmResponse orderId(String orderId) {
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

  public ConfirmResponse transactionId(String transactionId) {
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

  public ConfirmResponse confirmed(Boolean confirmed) {
    this.confirmed = confirmed;
    return this;
  }

  /**
   * Get confirmed
   * @return confirmed
   */
  @NotNull 
  @Schema(name = "confirmed", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("confirmed")
  public Boolean getConfirmed() {
    return confirmed;
  }

  public void setConfirmed(Boolean confirmed) {
    this.confirmed = confirmed;
  }

  public ConfirmResponse message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Get message
   * @return message
   */
  
  @Schema(name = "message", example = "Заказ успешно оформлен", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConfirmResponse confirmResponse = (ConfirmResponse) o;
    return Objects.equals(this.orderId, confirmResponse.orderId) &&
        Objects.equals(this.transactionId, confirmResponse.transactionId) &&
        Objects.equals(this.confirmed, confirmResponse.confirmed) &&
        Objects.equals(this.message, confirmResponse.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderId, transactionId, confirmed, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConfirmResponse {\n");
    sb.append("    orderId: ").append(toIndentedString(orderId)).append("\n");
    sb.append("    transactionId: ").append(toIndentedString(transactionId)).append("\n");
    sb.append("    confirmed: ").append(toIndentedString(confirmed)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
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

