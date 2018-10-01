package gov.nara.opa.api.services.impl.print;

import gov.nara.opa.api.services.print.PrintResultsService;
import gov.nara.opa.common.validation.print.PrintResultsRequestParameters;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

@Component
public class PrintResultsServiceImpl implements PrintResultsService {

  @Override
  public LinkedHashMap<String, Object> getPrintResults(
      PrintResultsRequestParameters requestParameters, String opaPath,
      String query) {

    return null;
  }

}
