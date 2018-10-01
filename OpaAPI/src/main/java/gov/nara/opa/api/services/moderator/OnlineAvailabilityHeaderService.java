package gov.nara.opa.api.services.moderator;

import gov.nara.opa.api.validation.moderator.OnlineAvailabilityHeaderModeratorRequestParameters;
import gov.nara.opa.api.validation.moderator.OnlineAvailabilityHeaderRequestParameters;
import gov.nara.opa.api.valueobject.moderator.OnlineAvailabilityHeaderValueObject;

public interface OnlineAvailabilityHeaderService {

	OnlineAvailabilityHeaderValueObject getOnlineAvailabilityHeaderByNaId(
			String naId);

	OnlineAvailabilityHeaderValueObject getOnlineAvailabilityHeaderByNaIdForModerator(
			String naId);

	OnlineAvailabilityHeaderValueObject addOnlineAvalilabilityHeader(
			OnlineAvailabilityHeaderRequestParameters requestParameters);

	OnlineAvailabilityHeaderValueObject updateOnlineAvalilabilityHeader(
			OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader,
			OnlineAvailabilityHeaderRequestParameters requestParameters);

	OnlineAvailabilityHeaderValueObject removeOnlineAvailabilityHeader(
			OnlineAvailabilityHeaderModeratorRequestParameters requestParameters);

	OnlineAvailabilityHeaderValueObject restoreOnlineAvailabilityHeader(
			OnlineAvailabilityHeaderModeratorRequestParameters requestParameters);
}
