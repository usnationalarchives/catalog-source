package gov.nara.opa.api.validation.annotation.comments;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObjectConstants;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommentsSearchRequestParameters extends AbstractRequestParameters
		implements CommentValueObjectConstants {

	private Map<String, String[]> queryParameters;

	@Override
	public boolean bypassExtraneousHttpParametersValidation() {
		return true;
	}

	@OpaNotNullAndNotEmpty
	String comment;

	public String getComment() {
		return comment;
	}

	public void setComment(String text) {
		this.comment = text;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		initRequestParamsMap();
		requestParams.put(COMMENT_TEXT_REQ_ASP, getComment());
		return requestParams;
	}

	public Map<String, String[]> getQueryParameters() {
		return queryParameters;
	}

	public void setQueryParameters(Map<String, String[]> queryParameters) {
		this.queryParameters = new HashMap<String, String[]>();
		this.queryParameters.putAll(queryParameters);
	}
}
