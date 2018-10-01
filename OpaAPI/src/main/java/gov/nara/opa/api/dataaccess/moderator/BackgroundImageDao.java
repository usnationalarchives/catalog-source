package gov.nara.opa.api.dataaccess.moderator;

import gov.nara.opa.api.valueobject.moderator.BackgroundImageValueObject;

import java.util.List;

public interface BackgroundImageDao {

	BackgroundImageValueObject getRandomBackgroundImage();

	BackgroundImageValueObject getRandomBackgroundImageWithException(
			String naId, String objectId);

	List<BackgroundImageValueObject> getAllBackgroundImages();

	BackgroundImageValueObject addBackgroundImage(String naId, String objectId,
			String title, String url, Boolean isDefault);

	BackgroundImageValueObject deleteBackgroundImage(String naId,
			String objectId, boolean isDefault);

	BackgroundImageValueObject getBackgroundImage(String naId, String objectId,
			boolean isDefault);

	List<BackgroundImageValueObject> getAllDefaultBackgroundImages();
}
