package gov.nara.opa.api.services.impl.user.contributions;

import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.controller.annotation.summary.SummaryController;
import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.dataaccess.user.contributions.UserContributionsDao;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.services.user.contributions.UserContributionsService;
import gov.nara.opa.api.user.contributions.UserContributedTags;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.annotation.summary.SummaryRequestParameters;
import gov.nara.opa.api.valueobject.user.contributions.UserContributedCommentValueObject;
import gov.nara.opa.api.valueobject.user.contributions.UserContributionValueObject;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class UserContributionsServiceImpl implements UserContributionsService {

	private static OpaLogger logger = OpaLogger
			.getLogger(UserContributionsServiceImpl.class);

	SecureRandom random;

	@Autowired
	private UserContributionsDao userContributionsDao;

	@Autowired
	private UserAccountDao administratorUserAccountDao;

	@Autowired
	private ConfigurationService config;

	/**
	 * Get totals tags for the account logged
	 * 
	 * @param accountId
	 *            Id of the account logged
	 * @return totals tags for the account logged
	 */
	public int getTotalTags(int accountId) {
		int total = 0;
		try {
			total = userContributionsDao.getTotalTags(accountId);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return total;

	}

	/**
	 * Get totals tags for the account logged
	 * 
	 * @param accountId
	 *            Id of the account logged
	 * @return totals tags for the account logged
	 */
	public int getTotalTags(String userName) {
		int total = 0;
		try {
			total = userContributionsDao.getTotalTags(userName);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return total;

	}

	public int getTotalComments(String userName) {
		int total = 0;
		try {
			UserAccountValueObject user = administratorUserAccountDao
					.selectByUserName(userName);
			if (user != null) {
				total = userContributionsDao.getTotalComments(user
						.getAccountId());
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return total;
	}

	/**
	 * Get the detailed summary for the account logged
	 * 
	 * @param accountId
	 *            Id of the account logged
	 * @return Values required on the Summary
	 */
	@Override
	public UserContributionValueObject getUserContributionsDetailSummary(
			int accountId) {

		UserContributionValueObject userContributions = null;
		try {
			userContributions = userContributionsDao
					.getUserContributionsDetailSummary(accountId);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return userContributions;
	}

	/**
	 * Select the rows from the annotation_tag table based on the parameters
	 * received
	 * 
	 * @param accountId
	 *            accountId used to filter the query
	 * @param naId
	 *            naId used to filter the query
	 * @param objectId
	 *            objectId used to filter the query
	 * @param tagText
	 *            tagText used to filter the query
	 * @param status
	 *            status used to filter the query
	 * @return Collection of the Tags found on the database
	 */
	@Override
	public List<UserContributedTags> viewUserTags(String userName,
			String tagText, int status, int offset, int rows, String sort) {

		try {

			// Retrieve the <Tag> object
			List<UserContributedTags> list = userContributionsDao
					.selectUserTags(userName, tagText, status, offset, Math
							.min(rows, config.getConfig()
									.getMaxContributionRows()), sort);
			if (list != null && list.size() > 0) {
				return list;
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 * Get the brief summary for the account logged
	 * 
	 * @param accountId
	 *            Id of the account logged
	 * @return Values required on the Summary
	 */
	@Override
	public UserContributionValueObject getUserContributionsBriefSummary(
			int accountId) {

		UserContributionValueObject userContributions = null;
		try {
			userContributions = userContributionsDao
					.getUserContributionsBriefSummary(accountId);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			// throw new OpaRuntimeException(e);
		}

		return userContributions;
	}

	/**
	 * Validate the username exists
	 * 
	 * @param userName
	 *            Username to be validated
	 * @return true/false if user exists
	 */
	@Override
	public boolean isValidUserName(String userName) {
		return administratorUserAccountDao.verifyIfUserNameExists(userName);
	}

	/**
	 * Select the rows from the annotation_transcription table based on the
	 * parameters received
	 * 
	 * @param accountId
	 *            accountId used to filter the query
	 * @param naId
	 *            naId used to filter the query
	 * @param objectId
	 *            objectId used to filter the query
	 * @return Collection of the Transcription found on the database
	 */
	@Override
	public List<Transcription> viewTranscriptions(int accountId, String naId,
			String objectId, int offset, int rows) {

		try {

			// Retrieve the <Tag> object
			List<Transcription> list = userContributionsDao
					.selectTranscriptions(accountId, naId, objectId, offset,
							rows);
			if (list != null && list.size() > 0) {
				return list;
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 * Select the rows from the annotation_transcription table based on the
	 * parameters received
	 * 
	 * @param accountId
	 *            accountId used to filter the query
	 * @param naId
	 *            naId used to filter the query
	 * @param objectId
	 *            objectId used to filter the query
	 * @return Collection of the Transcription found on the database
	 */
	@Override
	public List<Transcription> viewTranscriptions(String userName, String naId,
			String objectId, int offset, int rows) {

		try {

			// Retrieve the <Tag> object
			List<Transcription> list = userContributionsDao
					.selectTranscriptions(userName, naId, objectId, offset,
							Math.min(rows, config.getConfig()
									.getMaxContributionRows()));
			if (list != null && list.size() > 0) {
				return list;
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 * Select the rows from the annotation_tag table based on the parameters
	 * received
	 * 
	 * @param accountId
	 *            accountId used to filter the query
	 * @param naId
	 *            naId used to filter the query
	 * @param objectId
	 *            objectId used to filter the query
	 * @param tagText
	 *            tagText used to filter the query
	 * @param status
	 *            status used to filter the query
	 * @return Collection of the Tags found on the database
	 */
	@Override
	public List<TagValueObject> viewTags(int accountId, String naId,
			String objectId, String tagText, int status, int offset, int rows) {

		try {

			List<TagValueObject> list = userContributionsDao
					.selectTagValueObjects(accountId, naId, objectId, tagText,
							status, offset, rows);

			if (list != null && list.size() > 0) {
				return list;
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public List<TagValueObject> viewTags(
			SummaryRequestParameters requestParameters,
			ValidationResult validationResult, String tagText, int status) {

		try {

			UserAccountValueObject user = administratorUserAccountDao
					.selectByUserName(requestParameters.getUserName());
			if (user == null) {
				ValidationUtils.setValidationError(validationResult,
						ArchitectureErrorCodeConstants.USER_NOT_FOUND,
						ArchitectureErrorMessageConstants.INVALID_USER_NAME,
						SummaryController.VIEW_ACTION);
			} else {

				List<TagValueObject> list = viewTags(user.getAccountId(),
						requestParameters.getNaId(),
						requestParameters.getObjectId(), tagText, status,
						requestParameters.getOffset(),
						requestParameters.getRows());

				if (list != null && list.size() > 0) {
					return list;
				}
			}

		} catch (Exception e) {
			throw new OpaRuntimeException(e);
		}

		return null;
	}

	public List<UserContributedCommentValueObject> selectUserComments(
			String userName, String title, int offset, int rows, boolean descOrder) {
		try {
			UserAccountValueObject user = administratorUserAccountDao
					.selectByUserName(userName);
			if (user != null) {
				List<UserContributedCommentValueObject> list = userContributionsDao
						.selectUserComments(user.getAccountId(), title, offset, rows,
								descOrder);

				if (list != null && list.size() > 0) {
					return list;
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
}
