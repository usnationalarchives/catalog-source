package gov.nara.opa.api.services.moderator;

import gov.nara.opa.api.validation.moderator.CreateDeleteBackgroundImageRequestParameters;
import gov.nara.opa.api.validation.moderator.ViewBackgroundImageRequestParameters;
import gov.nara.opa.api.valueobject.moderator.BackgroundImageCollectionValueObject;
import gov.nara.opa.api.valueobject.moderator.BackgroundImageValueObject;

public interface BackgroundImageService {

	BackgroundImageValueObject getRandomBackgroundImage(
			ViewBackgroundImageRequestParameters requestParameters);

	BackgroundImageCollectionValueObject getAllBackgroundImages();

	BackgroundImageValueObject addBackgroundImage(
			CreateDeleteBackgroundImageRequestParameters requestParameters);

	BackgroundImageValueObject deleteBackgroundImage(
			CreateDeleteBackgroundImageRequestParameters requestParameters);
	
	void loadDefaultBackgroundImages(String images);
}
