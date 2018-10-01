package gov.nara.opa.api.dataaccess.moderator;

import java.util.List;
import gov.nara.opa.api.valueobject.moderator.OnlineAvailabilityHeaderValueObject;

public interface OnlineAvailabilityHeaderDao {

	OnlineAvailabilityHeaderValueObject getOnlineAvailabilityHeaderByNaId(
			String naId);

	OnlineAvailabilityHeaderValueObject createOnlineAvailabilityHeader(
			String naId, String headerText, boolean status);

	OnlineAvailabilityHeaderValueObject updateOnlineAvailabilityHeader(
			String naId, String headerText, boolean status);

	List<OnlineAvailabilityHeaderValueObject> getAllOnlineAvailabilityHeaders();

	OnlineAvailabilityHeaderValueObject deleteOnlineAvailabilityHeader(
			String naId);
}
