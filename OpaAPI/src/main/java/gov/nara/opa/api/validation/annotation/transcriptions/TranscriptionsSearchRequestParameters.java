package gov.nara.opa.api.validation.annotation.transcriptions;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TranscriptionsSearchRequestParameters extends
    AbstractRequestParameters {

  private Map<String, String[]> queryParameters;

  @Override
  public boolean bypassExtraneousHttpParametersValidation() {
    return true;
  }

  public String getTranscription() {
    return transcription;
  }

  public void setTranscription(String transcription) {
    this.transcription = transcription;
  }

  @OpaNotNullAndNotEmpty
  String transcription;

  public static final String TRANSCRIPTION_TEXT_REQ_ASP = "transcriptionText";

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    requestParams.put(TRANSCRIPTION_TEXT_REQ_ASP, getTranscription());
    return requestParams;
  }

  public Map<String, String[]> getQueryParameters() {
    return queryParameters;
  }

  public void setQueryParameters(Map<String, String[]> queryParameters) {
    this.queryParameters = new HashMap<String, String[]>();
    this.queryParameters.putAll(queryParameters);
  }

}
