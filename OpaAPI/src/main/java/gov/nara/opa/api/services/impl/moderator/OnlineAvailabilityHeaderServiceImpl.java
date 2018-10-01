package gov.nara.opa.api.services.impl.moderator;

import gov.nara.opa.api.dataaccess.annotation.logs.AnnotationLogDao;
import gov.nara.opa.api.dataaccess.moderator.OnlineAvailabilityHeaderDao;
import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.services.moderator.OnlineAvailabilityHeaderService;
import gov.nara.opa.api.utils.SearchUtils;
import gov.nara.opa.api.validation.moderator.OnlineAvailabilityHeaderModeratorRequestParameters;
import gov.nara.opa.api.validation.moderator.OnlineAvailabilityHeaderRequestParameters;
import gov.nara.opa.api.valueobject.moderator.OnlineAvailabilityHeaderValueObject;
import gov.nara.opa.api.valueobject.moderator.OnlineAvailabilityHeaderValueObjectConstants;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.AnnotationConstants;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;

@Component
@Transactional
public class OnlineAvailabilityHeaderServiceImpl implements
		OnlineAvailabilityHeaderService {

	private static OpaLogger logger = OpaLogger
			.getLogger(OnlineAvailabilityHeaderServiceImpl.class);

	@Autowired
	private OnlineAvailabilityHeaderDao onlineAvailabilityHeaderDao;

	@Autowired
	private UserAccountDao userAccountDao;

	@Autowired
	private AnnotationLogDao annotationLogDao;

	@Autowired
	private SearchUtils searchUtils;

	private String DEFAULT_HEADER = "<div><span style=\"font-family: Arial, Helvetica, sans-serif; color: rgb(152, 72, 7);\">"
			+ "<strong>This %1$s contains records, some of which may not be available online.</strong>"
			+ "</span></div><div><span style=\"font-family: sans-serif;\">To obtain a copy or view the records, "
			+ "please contact or visit the National Archives and Records Administration location(s) listed in the "
			+ "Contact information below</span></div>";

	@Override
	public OnlineAvailabilityHeaderValueObject getOnlineAvailabilityHeaderByNaId(
			String naId) {
		OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader = null;
		onlineAvailabilityHeader = onlineAvailabilityHeaderDao
				.getOnlineAvailabilityHeaderByNaId(naId);
		return onlineAvailabilityHeader;
	}

	@Override
	public OnlineAvailabilityHeaderValueObject addOnlineAvalilabilityHeader(
			OnlineAvailabilityHeaderRequestParameters requestParameters) {

		OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader = null;
		try {
			String header = URLDecoder.decode(requestParameters.getHeader(),
					"utf-8");
			onlineAvailabilityHeader = onlineAvailabilityHeaderDao
					.createOnlineAvailabilityHeader(
							requestParameters.getNaId(), header,
							requestParameters.getEnabled());

			if (!StringUtils.isEmpty(header)) {
				addAnnotationLogForAction(
						onlineAvailabilityHeader,
						requestParameters,
						OnlineAvailabilityHeaderValueObjectConstants.ACTION_UPDATE);
			}

			if (!requestParameters.getEnabled()) {
				addAnnotationLogForAction(
						onlineAvailabilityHeader,
						requestParameters,
						OnlineAvailabilityHeaderValueObjectConstants.ACTION_REMOVE);
			}

		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}

		return onlineAvailabilityHeader;
	}

	@Override
	public OnlineAvailabilityHeaderValueObject updateOnlineAvalilabilityHeader(
			OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader,
			OnlineAvailabilityHeaderRequestParameters requestParameters) {
		OnlineAvailabilityHeaderValueObject result = null;
		if (onlineAvailabilityHeader != null) {
			try {

				Boolean status = requestParameters.getEnabled() != null ? requestParameters
						.getEnabled() : onlineAvailabilityHeader.getStatus();
				String text = requestParameters.getHeader() != null ? requestParameters
						.getHeader() : onlineAvailabilityHeader.getHeader();

				if (!StringUtils.isEmpty(text)) {
					text = URLDecoder.decode(text, "utf-8");
				}

				result = onlineAvailabilityHeaderDao
						.updateOnlineAvailabilityHeader(
								onlineAvailabilityHeader.getNaId(), text,
								status);

				if (!text.equals(onlineAvailabilityHeader.getHeader())) {
					addAnnotationLogForAction(
							onlineAvailabilityHeader,
							requestParameters,
							OnlineAvailabilityHeaderValueObjectConstants.ACTION_UPDATE);
				}

				if (status != onlineAvailabilityHeader.getStatus()) {
					String action = OnlineAvailabilityHeaderValueObjectConstants.ACTION_RESTORE;

					if (!status) {
						action = OnlineAvailabilityHeaderValueObjectConstants.ACTION_REMOVE;
					}

					addAnnotationLogForAction(onlineAvailabilityHeader,
							requestParameters, action);
				}
			} catch (BadSqlGrammarException e) {
				logger.error(e.getMessage(), e);
			} catch (DataAccessException e) {
				logger.error(e.getMessage(), e);
			} catch (UnsupportedEncodingException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return result;
	}

	@Override
	public OnlineAvailabilityHeaderValueObject removeOnlineAvailabilityHeader(
			OnlineAvailabilityHeaderModeratorRequestParameters requestParameters) {

		OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader = null;
		try {
			onlineAvailabilityHeader = onlineAvailabilityHeaderDao
					.getOnlineAvailabilityHeaderByNaId(requestParameters
							.getNaId());

			if (onlineAvailabilityHeader.getHeader() != null
					|| onlineAvailabilityHeader.getAvailabilityTS() != null) {
				onlineAvailabilityHeader.setStatus(false);

				onlineAvailabilityHeaderDao.updateOnlineAvailabilityHeader(
						onlineAvailabilityHeader.getNaId(),
						onlineAvailabilityHeader.getHeader(),
						onlineAvailabilityHeader.getStatus());

				UserAccountValueObject userAccount = getSessionUserAccount();
				AnnotationLogValueObject annotationLog = createAnnotationLogForInsert(
						onlineAvailabilityHeader,
						userAccount.getAccountId(),
						requestParameters.getHttpSessionId(),
						OnlineAvailabilityHeaderValueObjectConstants.ACTION_REMOVE,
						requestParameters.getReasonId(),
						requestParameters.getNotes());
				annotationLog.setFirstAccountId(userAccount.getAccountId());
				annotationLog.setAffectsAccountId(userAccount.getAccountId());
				annotationLog.setParentId(0);

				annotationLogDao.insert(annotationLog);
			}
		} catch (BadSqlGrammarException e) {
			logger.error(e.getMessage(), e);
		} catch (DataAccessException e) {
			logger.error(e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}

		return onlineAvailabilityHeader;
	}

	@Override
	public OnlineAvailabilityHeaderValueObject restoreOnlineAvailabilityHeader(
			OnlineAvailabilityHeaderModeratorRequestParameters requestParameters) {

		OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader = null;
		try {
			onlineAvailabilityHeader = onlineAvailabilityHeaderDao
					.getOnlineAvailabilityHeaderByNaId(requestParameters
							.getNaId());

			if (onlineAvailabilityHeader.getHeader() != null
					|| onlineAvailabilityHeader.getAvailabilityTS() != null) {
				onlineAvailabilityHeader.setStatus(true);

				onlineAvailabilityHeaderDao.updateOnlineAvailabilityHeader(
						onlineAvailabilityHeader.getNaId(),
						onlineAvailabilityHeader.getHeader(),
						onlineAvailabilityHeader.getStatus());

				UserAccountValueObject userAccount = getSessionUserAccount();
				AnnotationLogValueObject annotationLog = createAnnotationLogForInsert(
						onlineAvailabilityHeader,
						userAccount.getAccountId(),
						requestParameters.getHttpSessionId(),
						OnlineAvailabilityHeaderValueObjectConstants.ACTION_RESTORE,
						requestParameters.getReasonId(),
						requestParameters.getNotes());
				annotationLog.setFirstAccountId(userAccount.getAccountId());
				annotationLog.setAffectsAccountId(userAccount.getAccountId());
				annotationLog.setParentId(0);

				annotationLogDao.insert(annotationLog);
			}
		} catch (BadSqlGrammarException e) {
			logger.error(e.getMessage(), e);
		} catch (DataAccessException e) {
			logger.error(e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}

		return onlineAvailabilityHeader;
	}

	private void addAnnotationLogForAction(
			OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader,
			OnlineAvailabilityHeaderRequestParameters requestParameters,
			String action) {
		try {
			UserAccountValueObject userAccount = getSessionUserAccount();
			AnnotationLogValueObject annotationLog = createAnnotationLogForInsert(
					onlineAvailabilityHeader, userAccount.getAccountId(),
					requestParameters.getHttpSessionId(), action, null, null);
			annotationLog.setFirstAccountId(userAccount.getAccountId());
			annotationLog.setAffectsAccountId(userAccount.getAccountId());
			annotationLog.setParentId(0);

			annotationLogDao.insert(annotationLog);
		} catch (BadSqlGrammarException e) {
			logger.error(e.getMessage(), e);
		} catch (DataAccessException e) {
			logger.error(e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private AnnotationLogValueObject createAnnotationLogForInsert(
			OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader,
			Integer accountId, String sessionId, String action,
			Integer reasonId, String notes) {
		AnnotationLogValueObject log = new AnnotationLogValueObject();
		log.setAnnotationType(AnnotationConstants.ANNOTATION_TYPE_ONLINE_AVAILABILITY_HEADER);
		log.setAnnotationId(0);
		log.setLanguageISO(null);
		log.setStatus(onlineAvailabilityHeader.getStatus());
		log.setAccountId(accountId);
		log.setAction(action);
		log.setSessionId(sessionId);
		log.setNaId(onlineAvailabilityHeader.getNaId());
		log.setLogTS(new Timestamp((new Date()).getTime()));
		log.setReasonId(reasonId);
		log.setNotes(notes);
		log.setAffectsAccountId(accountId);
		return log;
	}

	private UserAccountValueObject getSessionUserAccount() {
		Integer accountId = OPAAuthenticationProvider
				.getAccountIdForLoggedInUser();
		return userAccountDao.selectByAccountId(accountId);
	}

	@Override
	public OnlineAvailabilityHeaderValueObject getOnlineAvailabilityHeaderByNaIdForModerator(
			String naId) {
		OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader = null;
		onlineAvailabilityHeader = onlineAvailabilityHeaderDao
				.getOnlineAvailabilityHeaderByNaId(naId);
		if (onlineAvailabilityHeader.getHeader() == null) {
			AspireObject objects = searchUtils.getObjects(naId);
			try {
				if (objects.getContent("hasObjects") != null && !(boolean) objects.getContent("hasObjects")) {
					String level = (String) objects.getContent("level");
					onlineAvailabilityHeader.setStatus(true);
					onlineAvailabilityHeader.setHeader(String.format(DEFAULT_HEADER, level));
				}
			} catch (AspireException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return onlineAvailabilityHeader;
	}
}
