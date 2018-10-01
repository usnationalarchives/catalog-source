package gov.nara.opa.api.controller;

public class UnknownResourceException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public UnknownResourceException(String msg) {
    super(msg, null, true, false);
  }

}
