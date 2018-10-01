package gov.nara.opa.api.controller.administrator;

import gov.nara.opa.api.services.user.accounts.AccountMaintenanceService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.administrator.AccountMaintenanceRequestParameters;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AccountMaintenanceController extends AbstractBaseController {

	private static OpaLogger logger = OpaLogger.getLogger(AccountMaintenanceController.class);

	@Autowired
	private AccountMaintenanceService accountMaintenanceService;

	@RequestMapping(value = {
			"/"+ Constants.INTERNAL_API_PATH +"/" + Constants.API_VERS_NUM + "/administrator/accounts/auto-disable" }, 
			method = RequestMethod.POST)
	public ResponseEntity<String> disableIdleAccounts(
			@Valid AccountMaintenanceRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request) {
		int response = 0;
		logger.info("Auto disable idle accounts task has been called");
		try {
			response = accountMaintenanceService.disableIdleAccounts();
			return new ResponseEntity<String>(String.valueOf(response), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<String>("FAILED", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = {
			"/"+ Constants.INTERNAL_API_PATH +"/" + Constants.API_VERS_NUM + "/administrator/accounts/remove-unverified" }, 
			method = RequestMethod.POST)
	public ResponseEntity<String> removeUnverifiedAccounts(
			@Valid AccountMaintenanceRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request) {
		int response = 0;
		logger.info("Auto remove unverified accounts task has been called");
		try {
			response = accountMaintenanceService.removeUnverifiedAccounts();
			return new ResponseEntity<String>(String.valueOf(response), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<String>("FAILED", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = {
			"/"+ Constants.INTERNAL_API_PATH +"/" + Constants.API_VERS_NUM + "/administrator/accounts/cancel-email-modifications" }, 
			method = RequestMethod.POST)
	public ResponseEntity<String> cancelEmailChanges(
			@Valid AccountMaintenanceRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request) {
		int response = 0;
		logger.info("Auto cancel unverified email changes task has been called");
		try {
			response = accountMaintenanceService.removeUnverifiedEmailChanges();
			return new ResponseEntity<String>(String.valueOf(response), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<String>("FAILED", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = {
			"/"+ Constants.INTERNAL_API_PATH +"/" + Constants.API_VERS_NUM + "/administrator/exports/remove-expired-exports" }, 
			method = RequestMethod.POST)
	public ResponseEntity<String> removeExpiredExports(
			@Valid AccountMaintenanceRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request) {
		int response = 0;
		logger.info("Remove expired exports task has been called");
		try {
			response = accountMaintenanceService.removeExpiredExports();
			logger.info(String.format("Removed %1$d expired exports", response));
			return new ResponseEntity<String>(String.valueOf(response), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<String>("FAILED", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
}
