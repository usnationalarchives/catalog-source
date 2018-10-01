package gov.nara.opa.api.dataaccess.impl.annotation.locks;

import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.AnnotationConstants;
import gov.nara.opa.common.valueobject.annotation.lock.AnnotationLockValueObject;
import gov.nara.opa.common.valueobject.annotation.lock.AnnotationLockValueObjectConstants;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class AnnotationLockValueObjectExtractor implements 
	ResultSetExtractor<AnnotationLockValueObject>, AnnotationLockValueObjectConstants {

	private static OpaLogger logger = OpaLogger.getLogger(AnnotationLockExtractor.class);

	@Override
	public AnnotationLockValueObject extractData(ResultSet rs)
			throws SQLException, DataAccessException {
		AnnotationLockValueObject lock = new AnnotationLockValueObject();

	    try {
	      lock.setLockId(rs.getInt(LOCK_ID_DB));
	      lock.setAccountId(rs.getInt(ACCOUNT_ID_DB));
	      lock.setNaId(new String(rs.getBytes(NA_ID_DB), AnnotationConstants.UTF8_ENCODING));
	      lock.setObjectId(new String(rs.getBytes(OBJECT_ID_DB), AnnotationConstants.UTF8_ENCODING));
	      if (rs.getString(OPA_ID_DB) != null) {
	        lock.setOpaId(new String(rs.getBytes(OPA_ID_DB), AnnotationConstants.UTF8_ENCODING));
	      }
	      if (rs.getString(LANGUAGE_ISO_DB) != null) {
	        lock.setLanguageISO(new String(rs.getBytes(LANGUAGE_ISO_DB),
	        		AnnotationConstants.UTF8_ENCODING));
	      }
	      lock.setLockTS(rs.getTimestamp(LOCK_TS_DB));
	    } catch (UnsupportedEncodingException e) {
	      logger.error(e.getMessage(), e);
	    }

	    return lock;
	}

}
