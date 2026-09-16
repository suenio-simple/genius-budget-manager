package com.genius.budgetmanager.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CampaignStatus {
  ACTIVE("active"),
  PAUSED("paused"),
  CLOSED("closed"),
  DRAFT("draft");

  private String status;

  private CampaignStatus(String status) {
    this.status = status;
  }

  @JsonValue
  public String getStatusValue() { return status; }
  
  @Override 
  public String toString() {
    return status;
  }
}
