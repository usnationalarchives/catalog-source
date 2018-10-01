package gov.nara.opa.api.validation.search;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.architecture.utils.SolrUtils;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectHelper;

@Component
public class PagedContentDetailValidator extends OpaApiAbstractValidator {

  public static final String CONTENT_DETAIL_ACTION = "contentDetail";
  
  @Autowired
  private SearchValidationUtils searchValidationUtils;
  
  @Autowired
  private SolrUtils solrUtils;
  
  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    
    PagedContentDetailRequestParameters requestParameters = (PagedContentDetailRequestParameters) validationResult
        .getValidatedRequest();
    
    String query = "?" + request.getQueryString();
    
    Map<String, String[]> paramMap = solrUtils.makeParamMap(query);
    
    requestParameters.setQueryParameters(AccountExportValueObjectHelper
        .scrubQueryParameters(paramMap,
            requestParameters.getApiType()));
    
    if(validationResult.isValid()) {
      searchValidationUtils.isRowcountAllowed(requestParameters.getOffset(), requestParameters.getRows(), validationResult, CONTENT_DETAIL_ACTION, requestParameters.getAccountType());
      searchValidationUtils.validateDateRangeParams(validationResult, requestParameters.getQueryParameters(), CONTENT_DETAIL_ACTION);
    }
    
  }

}
