package gov.nara.opa.api.dataaccess.impl.moderator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nara.opa.api.dataaccess.moderator.OnlineAvailabilityHeaderDao;
import gov.nara.opa.api.valueobject.moderator.OnlineAvailabilityHeaderActionValueObject;
import gov.nara.opa.api.valueobject.moderator.OnlineAvailabilityHeaderValueObject;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.common.dataaccess.utils.GenericRowMapper;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class OnlineAvailabilityHeaderJDBCTemplate extends
		AbstractOpaDbJDBCTemplate implements OnlineAvailabilityHeaderDao {

	@SuppressWarnings("unchecked")
	@Override
	public OnlineAvailabilityHeaderValueObject getOnlineAvailabilityHeaderByNaId(
			String naId) {
		OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader = null;

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("naId", naId);
		List<OnlineAvailabilityHeaderValueObject> onlineAvailabilityHeaders = (List<OnlineAvailabilityHeaderValueObject>) StoredProcedureDataAccessUtils
				.execute(
						getJdbcTemplate(),
						"spGetOnlineAvailabilityHeader",
						new GenericRowMapper<OnlineAvailabilityHeaderValueObject>(
								new OnlineAvailabilityHeaderValueObjectExtractor()),
						inParamMap);
		if (onlineAvailabilityHeaders != null
				&& onlineAvailabilityHeaders.size() > 0) {
			onlineAvailabilityHeader = onlineAvailabilityHeaders.get(0);
		}

		if (onlineAvailabilityHeader == null) {
			String title = getOpaTitleByNaId(naId);
			if (title != null) {
				onlineAvailabilityHeader = new OnlineAvailabilityHeaderValueObject();
				onlineAvailabilityHeader.setNaId(naId);
				onlineAvailabilityHeader.setTitle(title);
				onlineAvailabilityHeader.setStatus(false);
				onlineAvailabilityHeader.setHeader(null);
				onlineAvailabilityHeader.setAvailabilityTS(null);
			}
		} else {
			List<OnlineAvailabilityHeaderActionValueObject> actions = getOnlineAvailiabilityHeaderActions(naId);
			if (actions != null) {
				onlineAvailabilityHeader.setActions(actions);
			}
		}

		return onlineAvailabilityHeader;
	}

	@Override
	public OnlineAvailabilityHeaderValueObject createOnlineAvailabilityHeader(
			String naId, String headerText, boolean status) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("naId", naId);
		inParamMap.put("headerText", headerText);
		inParamMap.put("headerStatus", status);

		StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spInsertOnlineAvailabilityHeader", inParamMap);

		OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader = getOnlineAvailabilityHeaderByNaId(naId);
		return onlineAvailabilityHeader;
	}

	@Override
	public OnlineAvailabilityHeaderValueObject updateOnlineAvailabilityHeader(
			String naId, String headerText, boolean status) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("naId", naId);
		inParamMap.put("headerText", headerText);
		inParamMap.put("headerStatus", status);

		StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spUpdateOnlineAvailabilityHeaderById", inParamMap);

		OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader = getOnlineAvailabilityHeaderByNaId(naId);
		return onlineAvailabilityHeader;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<OnlineAvailabilityHeaderValueObject> getAllOnlineAvailabilityHeaders() {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		List<OnlineAvailabilityHeaderValueObject> images = (List<OnlineAvailabilityHeaderValueObject>) StoredProcedureDataAccessUtils
				.execute(
						getJdbcTemplate(),
						"spGetAllOnlineAvailabilityHeaders",
						new GenericRowMapper<OnlineAvailabilityHeaderValueObject>(
								new OnlineAvailabilityHeaderValueObjectExtractor()),
						inParamMap);
		return images;
	}

	@Override
	public OnlineAvailabilityHeaderValueObject deleteOnlineAvailabilityHeader(
			String naId) {
		OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader = getOnlineAvailabilityHeaderByNaId(naId);
		if (onlineAvailabilityHeader != null) {
			Map<String, Object> inParamMap = new HashMap<String, Object>();
			inParamMap.put("naId", naId);
			StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
					"spDeleteOnlineAvailabilityHeader", inParamMap);
		}

		return onlineAvailabilityHeader;
	}

	@SuppressWarnings("unchecked")
	private List<OnlineAvailabilityHeaderActionValueObject> getOnlineAvailiabilityHeaderActions(
			String naId) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("naId", naId);
		List<OnlineAvailabilityHeaderActionValueObject> actions = (List<OnlineAvailabilityHeaderActionValueObject>) StoredProcedureDataAccessUtils
				.execute(
						getJdbcTemplate(),
						"spGetOnlineAvailabilityHeaderActionsByNaId",
						new GenericRowMapper<OnlineAvailabilityHeaderActionValueObject>(
								new OnlineAvailabilityHeaderActionValueObjectExtractor()),
						inParamMap);
		return actions;
	}

	private String getOpaTitleByNaId(String naId) {
		String title = null;

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("naId", naId);
		title = StoredProcedureDataAccessUtils.executeWithStringResult(
				getJdbcTemplate(), "spGetOpaTitleByNaId", inParamMap, "title");
		return title;
	}
}
