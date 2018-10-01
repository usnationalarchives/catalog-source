package gov.nara.opa.api.dataaccess.impl.moderator;

import gov.nara.opa.api.dataaccess.moderator.BackgroundImageDao;
import gov.nara.opa.api.valueobject.moderator.BackgroundImageValueObject;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.common.dataaccess.utils.GenericRowMapper;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class BackgroundImageJDBCTemplate extends AbstractOpaDbJDBCTemplate
		implements BackgroundImageDao {

	@SuppressWarnings("unchecked")
	@Override
	public BackgroundImageValueObject getRandomBackgroundImage() {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		List<BackgroundImageValueObject> images = (List<BackgroundImageValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetBackgroundImage",
						new GenericRowMapper<BackgroundImageValueObject>(
								new BackgroundImageValueObjectExtractor()),
						inParamMap);

		BackgroundImageValueObject backgroundImage = null;
		if (images != null && images.size() > 0) {
			backgroundImage = images.get(0);
		}

		if (backgroundImage == null) {
			backgroundImage = getRandomDefaultBackgroundImage();
		}

		return backgroundImage;
	}

	@Override
	public BackgroundImageValueObject getRandomBackgroundImageWithException(
			String naId, String objectId) {
		BackgroundImageValueObject backgroundImage = getRandomBackgroundImage();
		if (backgroundImage != null) {
			int numTries = 0;
			int maxTries = 3;
			while (backgroundImage.getNaId().equals(naId)
					&& backgroundImage.getObjectId().equals(objectId)
					&& numTries < maxTries) {
				backgroundImage = getRandomBackgroundImage();
				++numTries;
			}
		}

		return backgroundImage;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BackgroundImageValueObject> getAllBackgroundImages() {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		List<BackgroundImageValueObject> images = (List<BackgroundImageValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetAllBackgroundImages",
						new GenericRowMapper<BackgroundImageValueObject>(
								new BackgroundImageValueObjectExtractor()),
						inParamMap);
		return images;
	}

	@Override
	public BackgroundImageValueObject addBackgroundImage(String naId,
			String objectId, String title, String url, Boolean isDefault) {

		BackgroundImageValueObject backgroundImage = null;

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("naId", naId);
		inParamMap.put("objectId", objectId);
		inParamMap.put("imgTitle", title);
		inParamMap.put("imgUrl", url);
		inParamMap.put("isDefaultImage", isDefault);

		StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spInsertBackgroundImage", inParamMap);

		backgroundImage = getBackgroundImage(naId, objectId, isDefault);

		return backgroundImage;
	}

	@Override
	public BackgroundImageValueObject deleteBackgroundImage(String naId,
			String objectId, boolean isDefault) {
		BackgroundImageValueObject backgroundImage = getBackgroundImage(naId,
				objectId, isDefault);
		if (backgroundImage != null) {
			Map<String, Object> inParamMap = new HashMap<String, Object>();
			inParamMap.put("naId", naId);
			inParamMap.put("objectId", objectId);
			inParamMap.put("isDefaultImage", isDefault);

			StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
					"spDeleteBackgroundImage", inParamMap);
		}

		return backgroundImage;
	}

	@SuppressWarnings("unchecked")
	@Override
	public BackgroundImageValueObject getBackgroundImage(String naId,
			String objectId, boolean isDefault) {
		BackgroundImageValueObject backgroundImage = null;
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("naId", naId);
		inParamMap.put("objectId", objectId);
		inParamMap.put("isDefaultImage", isDefault);
		List<BackgroundImageValueObject> images = (List<BackgroundImageValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(),
						"spGetBackgroundImageWithNaIdAndObjectId",
						new GenericRowMapper<BackgroundImageValueObject>(
								new BackgroundImageValueObjectExtractor()),
						inParamMap);
		if (images != null && images.size() > 0) {
			backgroundImage = images.get(0);
		}
		return backgroundImage;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BackgroundImageValueObject> getAllDefaultBackgroundImages() {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		List<BackgroundImageValueObject> images = (List<BackgroundImageValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetAllDefaultBackgroundImages",
						new GenericRowMapper<BackgroundImageValueObject>(
								new BackgroundImageValueObjectExtractor()),
						inParamMap);
		return images;
	}

	@SuppressWarnings("unchecked")
	private BackgroundImageValueObject getRandomDefaultBackgroundImage() {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		List<BackgroundImageValueObject> images = (List<BackgroundImageValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetDefaultBackgroundImage",
						new GenericRowMapper<BackgroundImageValueObject>(
								new BackgroundImageValueObjectExtractor()),
						inParamMap);

		BackgroundImageValueObject backgroundImage = null;
		if (images != null && images.size() > 0) {
			backgroundImage = images.get(0);
		}

		return backgroundImage;
	}
}
