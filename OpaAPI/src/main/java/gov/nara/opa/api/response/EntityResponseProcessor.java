package gov.nara.opa.api.response;

public interface EntityResponseProcessor {
  public void processResponse(AspireObject aspireObject) throws AspireException;
}
