package gov.nara.opa.api.validation.search;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SolrParamsValidator implements InitializingBean {

  private List<String> blackListedValues = new ArrayList<String>();
  
  @Value("${solrBlackList}")
  private String blackListPath;

  @Override
  public void afterPropertiesSet() throws Exception {
	File file = new File(blackListPath);
	if (!file.exists()) {
		throw new OpaRuntimeException(String.format("File not found $1", blackListPath));
	}
	Scanner scanner = new Scanner(file);

    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      blackListedValues.add(line);
    }

    scanner.close();
  }

  public boolean validate(ValidationResult validationResult,
      Map<String, String[]> queryParameters) {
    for (String parameterName : queryParameters.keySet()) {
      if (blackListedValues.contains(parameterName)) {
        ValidationError error = new ValidationError();
        error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER);
        error.setErrorMessage(String.format(
            ArchitectureErrorMessageConstants.PARAMETER_NOT_ALLOWED,
            parameterName));
        validationResult.addCustomValidationError(error);
        return false;
      }
    }

    return true;
  }
}
