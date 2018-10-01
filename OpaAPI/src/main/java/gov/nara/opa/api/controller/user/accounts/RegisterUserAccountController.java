package gov.nara.opa.api.controller.user.accounts;

import gov.nara.opa.api.services.user.accounts.RegisterUserAccountService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.common.accounts.CommonUserAccountValidator;
import gov.nara.opa.api.validation.user.accounts.ForgotUserNameRequestParameters;
import gov.nara.opa.api.validation.user.accounts.ForgotUserNameValidator;
import gov.nara.opa.api.validation.user.accounts.RegisterAccountRequestParameters;
import gov.nara.opa.api.validation.user.accounts.RegisterAccountValidator;
import gov.nara.opa.api.validation.user.accounts.ResendVerificationRequestParameters;
import gov.nara.opa.api.validation.user.accounts.ResendVerificationValidator;
import gov.nara.opa.api.validation.user.accounts.VerifyAccountRequestParameters;
import gov.nara.opa.api.validation.user.accounts.VerifyAccountValidator;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class RegisterUserAccountController extends AbstractBaseController {

	@Autowired
	private RegisterUserAccountService registerAccountService;

	@Autowired
	private RegisterAccountValidator registerAccountValidator;

	@Autowired
	private VerifyAccountValidator verifyAccountValidator;

	@Autowired
	private ResendVerificationValidator resendVerificationValidator;

	@Autowired
	ForgotUserNameValidator forgotUserNameValidator;

	public static final String REGISTER_ACCOUNT_ACTION = "registerUserAccount";

	public static final String VERIFY_ACCOUNT_ACTION = "activateUser";

	public static final String VERIFY_EMAIL_ACTION = "changeEmail";

	public static final String RESEND_VERIFICATION_ACTION = "resendVerification";

	public static final String RECOVER_USER_NAME_ACTION = "recoverUserName";

	public static final String USER_PARENT_ENTITY_NAME = "user";

	@RequestMapping(value = {
			"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM + "/users",
			"/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
			+ "/accounts/register" }, method = RequestMethod.POST)
	public ResponseEntity<String> registerUserAccount(
			@Valid RegisterAccountRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request) {

		ValidationResult validationResult = registerAccountValidator.validate(
				bindingResult, request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					REGISTER_ACCOUNT_ACTION);
		}

		UserAccountValueObject userAccount = registerAccountService
				.registerAccount(requestParameters);

		return createSuccessResponseEntity(USER_PARENT_ENTITY_NAME,
				requestParameters, userAccount, request, REGISTER_ACCOUNT_ACTION);
	}

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
			+ "/accounts/resendverification" }, method = RequestMethod.GET)
	public ResponseEntity<String> resendVerificationEmail(
			@Valid ResendVerificationRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request) {

		ValidationResult validationResult = resendVerificationValidator.validate(
				bindingResult, request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					RESEND_VERIFICATION_ACTION);
		}

		UserAccountValueObject userAccount = (UserAccountValueObject) validationResult
				.getContextObjects().get(
						CommonUserAccountValidator.USER_ACCOUNT_OBJECT_KEY);

		registerAccountService.resendVerification(userAccount);

		return createSuccessResponseEntity(USER_PARENT_ENTITY_NAME,
				requestParameters, userAccount, request, RESEND_VERIFICATION_ACTION);
	}

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
			+ "/accounts/verifyemail" }, method = RequestMethod.GET)
	public ResponseEntity<String> verifyUserAccountEmail(
			@Valid VerifyAccountRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request) {

		ValidationResult validationResult = verifyAccountValidator.validate(
				bindingResult, request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					VERIFY_ACCOUNT_ACTION);
		}

		UserAccountValueObject userAccount = (UserAccountValueObject) validationResult
				.getContextObjects().get(
						CommonUserAccountValidator.USER_ACCOUNT_OBJECT_KEY);
		
		//Get referring url
		String referringUrl = userAccount.getReferringUrl();
		
		registerAccountService.verifyAccount(userAccount,
				requestParameters.getShowPwdSet());

		if (!requestParameters.getShowPwdSet()) {
			//Resetting referring url after cleared in the update
			userAccount.setReferringUrl(referringUrl);
			
			return createSuccessResponseEntity(USER_PARENT_ENTITY_NAME,
					requestParameters, userAccount, request, VERIFY_ACCOUNT_ACTION);
		} else {
			LinkedHashMap<String, Object> results = new LinkedHashMap<String, Object>();
			results.put(USER_PARENT_ENTITY_NAME, userAccount);
			results.put("activationCode", userAccount.getVerificationGuid());
			
			if(!StringUtils.isNullOrEmtpy(referringUrl)) {
				results.put("referringUrl", referringUrl);
			}

			return createSuccessResponseEntity(requestParameters, results, request,
					VERIFY_ACCOUNT_ACTION);
		}
	}

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
			+ "/accounts/forgotname" }, method = RequestMethod.GET)
	public ResponseEntity<String> forgotUserAccountName(
			@Valid ForgotUserNameRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request) {

		ValidationResult validationResult = forgotUserNameValidator.validate(
				bindingResult, request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					RECOVER_USER_NAME_ACTION);
		}

		UserAccountValueObject userAccount = (UserAccountValueObject) validationResult
				.getContextObjects().get(
						ForgotUserNameValidator.USER_ACCOUNT_OBJECT_KEY);
		registerAccountService.forgotUserName(userAccount);

		LinkedHashMap<String, Object> responseValues = new LinkedHashMap<String, Object>();
		responseValues.put(USER_PARENT_ENTITY_NAME, "success");

		return createSuccessResponseEntity(requestParameters, responseValues,
				request, RECOVER_USER_NAME_ACTION);
	}

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
			+ "/accounts/verifyemailchange" }, method = RequestMethod.POST)
	public ResponseEntity<String> verifyUserAccountEmailChanged(
			@Valid VerifyAccountRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request) {

		ValidationResult validationResult = verifyAccountValidator.validate(
				bindingResult, request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					VERIFY_EMAIL_ACTION);
		}

		UserAccountValueObject userAccount = (UserAccountValueObject) validationResult
				.getContextObjects().get(
						CommonUserAccountValidator.USER_ACCOUNT_OBJECT_KEY);
		userAccount = registerAccountService.verifyEmailChange(userAccount);
		return createSuccessResponseEntity(USER_PARENT_ENTITY_NAME,
				requestParameters, userAccount, request, VERIFY_EMAIL_ACTION);
	}
}
