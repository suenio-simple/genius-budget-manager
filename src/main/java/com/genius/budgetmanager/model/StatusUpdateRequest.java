package com.genius.budgetmanager.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class StatusUpdateRequest {
  private String status;

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  @JsonAnySetter
  public void rejectUnknownField(String name, Object value) {
    throw new IllegalArgumentException("Campo no permitido: " + name);
  }
}
