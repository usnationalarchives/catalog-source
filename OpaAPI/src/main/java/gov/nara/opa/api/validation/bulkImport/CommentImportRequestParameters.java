package gov.nara.opa.api.validation.bulkImport;

import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaPattern;

import java.util.LinkedHashMap;

public class CommentImportRequestParameters extends AbstractRequestParameters {

	@OpaNotNullAndNotEmpty
	@OpaPattern(regexp = "(^comment$)", message = ErrorConstants.IMPORT_ENTITY_INVALID)
	private String entity;

	private String content;

	private String sourceFileUrl;

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSourceFileUrl() {
		return sourceFileUrl;
	}

	public void setSourceFileUrl(String sourceFileUrl) {
		this.sourceFileUrl = sourceFileUrl;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
		result.put("@action", action);
		result.put("@Entity", entity);
		return result;
	}
}
