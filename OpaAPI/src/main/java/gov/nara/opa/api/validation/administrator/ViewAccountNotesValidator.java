package gov.nara.opa.api.validation.administrator;

import gov.nara.opa.api.dataaccess.user.logs.AccountLogDao;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.valueobject.user.logs.AccountLogValueObject;
import gov.nara.opa.api.valueobject.user.logs.AccountNotesCollectionValueObject;
import gov.nara.opa.architecture.web.validation.AbstractBaseValidator;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ViewAccountNotesValidator extends AbstractBaseValidator {

  @Autowired
  AccountLogDao accountLogDao;

  public static final String ACCOUNT_NOTES_OBJECT_KEY = "accountNotes";

  public static final String ACCOUNT_NOTES_ENTITIES_NAME = "account notes";

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    ViewAccountNotesRequestParameters requestParameters = (ViewAccountNotesRequestParameters) validationResult
        .getValidatedRequest();

    String userName = requestParameters.getUserName();
    List<AccountLogValueObject> notes = accountLogDao.getAccountNotes(userName);
    if (noRecordsFound(notes, validationResult,
        ErrorCodeConstants.NOTES_NOT_FOUND, ACCOUNT_NOTES_ENTITIES_NAME)) {
      return;
    }
    validationResult.addContextObject(ACCOUNT_NOTES_OBJECT_KEY,
        new AccountNotesCollectionValueObject(notes, userName));
  }

}
