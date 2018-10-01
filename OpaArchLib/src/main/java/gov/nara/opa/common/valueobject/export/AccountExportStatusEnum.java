package gov.nara.opa.common.valueobject.export;

import gov.nara.opa.architecture.exception.OpaRuntimeException;

public enum AccountExportStatusEnum {

  QUEUED("Queued"), PROCESSING("Processing"), COMPLETED("Completed"), DOWNLOADED(
      "Donwloaded"), FAILED("Failed"), SCHEDULED("Scheduled"), PACKAGING(
      "Packaging"), TIMEDOUT("Timed Out");

  private String value;

  AccountExportStatusEnum(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static AccountExportStatusEnum fromString(String value) {
    if (value == null) {
      return null;
    }
    if (value.equals("Processing")) {
      return PROCESSING;
    } else if (value.equals("Queued")) {
      return QUEUED;
    } else if (value.equals("Downloaded")) {
      return DOWNLOADED;
    } else if (value.equals("Failed")) {
      return FAILED;
    } else if (value.equals("Scheduled")) {
      return SCHEDULED;
    } else if (value.equals("Completed")) {
      return COMPLETED;
    } else if (value.equals("Packaging")) {
      return PACKAGING;
    } else if (value.equals("Timed Out")) {
      return TIMEDOUT;
    }

    throw new OpaRuntimeException("Value '" + value
        + " can't be converted to a valid AccountExporStatusEnum");
  }
}
