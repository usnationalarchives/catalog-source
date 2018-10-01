package gov.nara.opa.api.valueobject.moderator;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BackgroundImageCollectionValueObject extends
		AbstractWebEntityValueObject {

	List<BackgroundImageValueObject> images;

	public BackgroundImageCollectionValueObject(
			List<BackgroundImageValueObject> images) {
		if (images == null) {
			throw new OpaRuntimeException("The images parameter cannot be null");
		}

		this.images = images;
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
		if (images != null && images.size() > 0) {
			aspireContent.put("images", images);
		}
		return aspireContent;
	}

	public List<BackgroundImageValueObject> getBackgroundImages() {
		return images;
	}
}
