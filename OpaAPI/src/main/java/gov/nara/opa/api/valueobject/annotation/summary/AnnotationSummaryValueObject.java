package gov.nara.opa.api.valueobject.annotation.summary;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;
import gov.nara.opa.common.valueobject.annotation.transcriptions.TranscriptionValueObject;

public class AnnotationSummaryValueObject extends AbstractWebEntityValueObject
		implements AnnotationSummaryValueObjectConstants {

	private List<TagValueObject> tags;
	private List<TranscriptionValueObject> transcriptions;
	private List<CommentValueObject> comments;

	public List<TagValueObject> getTags() {
		return tags;
	}

	public void setTags(List<TagValueObject> tags) {
		this.tags = tags;
	}

	public List<TranscriptionValueObject> getTranscriptions() {
		return transcriptions;
	}

	public void setTranscriptions(List<TranscriptionValueObject> transcriptions) {
		this.transcriptions = transcriptions;
	}

	public List<CommentValueObject> getComments() {
		return comments;
	}

	public void setComments(List<CommentValueObject> comments) {
		this.comments = comments;
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		// TODO Auto-generated method stub
		return null;
	}

}
