package gov.nara.opa.common.dataaccess.impl.annotation.transcriptions;

import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.common.dataaccess.annotation.transcriptions.TranscriptionDao;
import gov.nara.opa.common.dataaccess.utils.GenericRowMapper;
import gov.nara.opa.common.valueobject.annotation.transcriptions.TranscriptionValueObject;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TranscriptionsJDBCTemplate extends AbstractOpaDbJDBCTemplate
    implements TranscriptionDao {

  public static String SELECT_TRANSCRIPTIONS_BASE;
  static {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT annotation_id,annotation,saved_vers_num,annotation_md5, ");
    sb.append("first_annotation_id,status,na_id,object_id,page_num,opa_id,t.account_id, ");
    sb.append("annotation_ts, a.user_name, a.display_name_flag, a.full_name, a.is_nara_staff ");
    sb.append("FROM annotation_transcriptions t, accounts a ");
    sb.append("WHERE a.account_id = t.account_id and na_id = ? ");
    sb.append("AND (t.status = 0 OR t.status = 1) ");
    sb.append("ORDER BY object_id, saved_vers_num");
    SELECT_TRANSCRIPTIONS_BASE = sb.toString();
  }

  @Override
  public List<TranscriptionValueObject> selectTranscriptionsByNaid(String naId) {
    return getJdbcTemplate().query(SELECT_TRANSCRIPTIONS_BASE,
        new Object[] { naId }, new GenericRowMapper<TranscriptionValueObject>(new TranscriptionValueObjectResultSetExtractor()));
  }
  
  public List<TranscriptionValueObject> selectFullTranscription(String naId,
      String objectId, int versionNumber) {
    
    
    
    return null;
  }

}
