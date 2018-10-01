package gov.nara.opa.api.services.impl.annotation.tags;

import gov.nara.opa.api.annotation.TagErrorCode;
import gov.nara.opa.api.dataaccess.annotation.logs.AnnotationLogDao;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.annotation.tags.ViewTagService;
import gov.nara.opa.api.services.system.ConnectionManager;
import gov.nara.opa.api.validation.annotation.tags.TagsViewRequestParameters;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.dataaccess.annotation.tags.TagDao;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.Tag;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.TagsCollectionValueObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class ViewTagServiceImpl implements ViewTagService {

	private static OpaLogger logger = OpaLogger
			.getLogger(ViewTagServiceImpl.class);

	@Autowired
	private TagDao tagDao;

	@Autowired
	private AnnotationLogDao logDao;

	@Autowired
	private ConnectionManager connectionManager;

	@Override
	public ServiceResponseObject viewTagById(int annotationId) {
		HashMap<String, Object> resultHashMap = new HashMap<String, Object>();
		TagErrorCode errorCode = TagErrorCode.NONE;

		try {

			// Get tag
			Tag tag = tagDao.select(annotationId);
			if (tag == null) {
				errorCode = TagErrorCode.NO_TAGS_FOUND;
				errorCode.setErrorMessage("Tag not found.");
			} else {
				// Get latest log entry
				List<AnnotationLogValueObject> logs = logDao.select("TG", annotationId);
				// Get the first list entry
				AnnotationLogValueObject log = null;
				if (logs != null && logs.size() > 0) {
					log = logs.get(0);
				} else {
					errorCode = TagErrorCode.INTERNAL_ERROR;
					errorCode.setErrorMessage("No log entry found for tag.");
				}

				resultHashMap.put("Tag", tag);
				resultHashMap.put("AnnotationLog", log);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			errorCode = TagErrorCode.INTERNAL_ERROR;
		}

		return new ServiceResponseObject(errorCode, resultHashMap);
	}

	@Override
	public TagsCollectionValueObject getTags(
			TagsViewRequestParameters tagsParameters) {
		List<TagValueObject> tags = tagDao.selectAllTags(tagsParameters.getNaId(),
				tagsParameters.getObjectId(), tagsParameters.getText(), true);
		return new TagsCollectionValueObject(tags);
	}

	@Override
	public TagsCollectionValueObject getTagsByNaIds(String[] naIdsList)
			throws DataAccessException, UnsupportedEncodingException {
		List<TagValueObject> tags = tagDao.selectAllTagsByNaIds(naIdsList);
		return new TagsCollectionValueObject(tags);
	}
}
