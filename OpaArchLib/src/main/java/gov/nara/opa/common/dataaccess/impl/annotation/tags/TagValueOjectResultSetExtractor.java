package gov.nara.opa.common.dataaccess.impl.annotation.tags;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObjectConstants;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObjectConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;

public class TagValueOjectResultSetExtractor implements
    ResultSetExtractor<TagValueObject>, UserAccountValueObjectConstants,
    TagValueObjectConstants {

  @Override
  public TagValueObject extractData(ResultSet rs) throws SQLException {
    // tag specific fields
    TagValueObject tag = new TagValueObject();
    tag.setAnnotationId(rs.getInt(ANNOTATION_ID_DB));
    
    //Encode tag text to UTF-8
    try {
      tag.setAnnotation(URLEncoder.encode(rs.getString(ANNOTATION_DB), "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      OpaRuntimeException ex = new OpaRuntimeException(e);
      throw ex;
    }
    
    tag.setAccountId(rs.getInt(TagValueObjectConstants.ACCOUNT_ID_DB));
    tag.setAnnotationMD5(rs.getString(ANNOTATION_MD5_DB));
    tag.setAnnotationTS(rs.getTimestamp(ANNOTATION_TS_DB));
    tag.setNaId(rs.getString(NA_ID_DB));
    tag.setObjectId(rs.getString(OBJECT_ID_DB));
    tag.setOpaId(rs.getString(OPA_ID_DB));
    tag.setPageNum(rs.getInt(PAGE_NUM_DB));
    tag.setStatus(rs.getBoolean(STATUS_DB));

    // account related fields
    tag.setUserName(rs.getString(USER_NAME_DB));
    tag.setFullName(rs.getString(FULL_NAME_DB));
    tag.setIsNaraStaff(rs.getBoolean(IS_NARA_STAFF_DB));
    tag.setDisplayNameFlag(rs.getBoolean(DISPLAY_NAME_FLAG_DB));
    return tag;
  }

}
