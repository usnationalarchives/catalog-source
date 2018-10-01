package gov.nara.opa.api.dataaccess.impl.moderator;

import java.sql.ResultSet;
import java.sql.SQLException;

import gov.nara.opa.api.valueobject.moderator.BackgroundImageValueObject;
import gov.nara.opa.api.valueobject.moderator.BackgroundImageValueObjectConstants;

import org.springframework.jdbc.core.ResultSetExtractor;

public class BackgroundImageValueObjectExtractor implements
		ResultSetExtractor<BackgroundImageValueObject>,
		BackgroundImageValueObjectConstants {

	@Override
	public BackgroundImageValueObject extractData(ResultSet rs)
			throws SQLException {
		BackgroundImageValueObject backgroundImage = new BackgroundImageValueObject();
		backgroundImage.setNaId(rs.getString(NA_ID_DB));
		backgroundImage.setObjectId(rs.getString(OBJECT_ID_DB));
		backgroundImage.setTitle(rs.getString(TITLE_DB));
		backgroundImage.setUrl(rs.getString(URL_DB));
		backgroundImage.setIsDefault(rs.getBoolean(ISDEFAULT_DB));

		return backgroundImage;
	}
}
