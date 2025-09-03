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
 * BalanceResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.12.0")
public class BalanceResponse {

  private Double balance;

  private String currency;

  public BalanceResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public BalanceResponse(Double balance, String currency) {
    this.balance = balance;
    this.currency = currency;
  }

  public BalanceResponse balance(Double balance) {
    this.balance = balance;
    return this;
  }

  /**
   * Get balance
   * @return balance
   */
  @NotNull 
  @Schema(name = "balance", example = "50000.0", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("balance")
  public Double getBalance() {
    return balance;
  }

  public void setBalance(Double balance) {
    this.balance = balance;
  }

  public BalanceResponse currency(String currency) {
    this.currency = currency;
    return this;
  }

  /**
   * Get currency
   * @return currency
   */
  @NotNull 
  @Schema(name = "currency", example = "RUB", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("currency")
  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BalanceResponse balanceResponse = (BalanceResponse) o;
    return Objects.equals(this.balance, balanceResponse.balance) &&
        Objects.equals(this.currency, balanceResponse.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(balance, currency);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BalanceResponse {\n");
    sb.append("    balance: ").append(toIndentedString(balance)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
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

