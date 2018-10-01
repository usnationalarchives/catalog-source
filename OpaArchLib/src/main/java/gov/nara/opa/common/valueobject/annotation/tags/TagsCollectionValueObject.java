package gov.nara.opa.common.valueobject.annotation.tags;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TagsCollectionValueObject extends AbstractWebEntityValueObject {

	List<TagValueObject> tags;
	Integer totalTags;
	Timestamp lastModified;

	public TagsCollectionValueObject(List<TagValueObject> tags) {
		if (tags == null) {
			throw new OpaRuntimeException("The tags parameter cannot be null");
		}
		this.tags = tags;
		if (tags != null) {
			totalTags = tags.size();
		}
		Timestamp lastModified = null;
		if (tags.size() > 0) {
			lastModified = tags.get(0).getAnnotationTS();
		}
		for (TagValueObject tag: tags) {
			if (tag.getAnnotationTS().after(lastModified)) {
				lastModified = tag.getAnnotationTS();
			}
		}
		if (lastModified != null) {
			setLastModified(lastModified);
		}
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		return null;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
		if (tags != null && tags.size() > 0) {
			aspireContent.put("@lastModified", TimestampUtils.getUtcString(getLastModified()));
			aspireContent.put("@total", tags.size());
			aspireContent.put("tag", tags);
		}
		return aspireContent;
	}

	public Integer getTotalTags() {
		return totalTags;
	}

	public List<TagValueObject> getTags() {
		return tags;
	}

	public Timestamp getLastModified() {
		return lastModified;
	}

	public void setLastModified(Timestamp lastModified) {
		this.lastModified = lastModified;
	}
}
