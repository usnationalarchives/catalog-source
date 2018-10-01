package gov.nara.opa.api.validation.annotation.comments;

import gov.nara.opa.api.validation.annotation.AnnotationsViewRequestParameters;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObjectConstants;

import java.util.LinkedHashMap;

public class CommentsViewRequestParameters extends AnnotationsViewRequestParameters
		implements CommentValueObjectConstants {

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		initRequestParamsMap();
		requestParams.put(NA_ID_REQ_ASP, getNaId());
		if (getObjectId() != null && !getObjectId().isEmpty()) {
			requestParams.put(OBJECT_ID_REQ_ASP, getObjectId());
		}
		if (getText() != null && !getText().isEmpty()) {
			requestParams.put(COMMENT_TEXT_REQ_ASP, getText());
		}

		return requestParams;
	}
}
