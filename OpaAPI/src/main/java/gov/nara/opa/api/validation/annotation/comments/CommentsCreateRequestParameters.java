package gov.nara.opa.api.validation.annotation.comments;

import gov.nara.opa.api.validation.annotation.AnnotationsRequestParameters;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObjectConstants;

import java.util.LinkedHashMap;

public class CommentsCreateRequestParameters extends AnnotationsRequestParameters
		implements CommentValueObjectConstants {

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		initRequestParamsMap();
		requestParams.put(NA_ID_REQ_ASP, getNaId());
		if (getObjectId() != null && !getObjectId().isEmpty()) {
			requestParams.put(OBJECT_ID_REQ_ASP, getObjectId());
		}
		requestParams.put(PAGE_NUM_REQ_ASP, getPageNum());
		requestParams.put(COMMENT_TEXT_REQ_ASP, getText());
		requestParams.put(COMMENT_ID_REQ_ASP, getAnnotationId());
		return requestParams;
	}
}