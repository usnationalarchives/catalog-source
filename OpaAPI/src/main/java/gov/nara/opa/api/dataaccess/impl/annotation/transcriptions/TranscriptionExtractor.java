package gov.nara.opa.api.dataaccess.impl.annotation.transcriptions;

import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class TranscriptionExtractor implements
    ResultSetExtractor<Transcription> {
  
  private static OpaLogger logger = OpaLogger.getLogger(TranscriptionExtractor.class);

  @Override
  public Transcription extractData(ResultSet resultSet) throws SQLException,
      DataAccessException {

    Transcription transcription = new Transcription();

    try {
      transcription.setAnnotationId(resultSet.getInt("annotation_id"));
      transcription.setAnnotation(new String(resultSet.getBytes("annotation"),
          "UTF-8"));
      transcription.setSavedVersNum(resultSet.getInt("saved_vers_num"));
      transcription.setAnnotationMD5(resultSet.getString("annotation_md5"));
      transcription.setFirstAnnotationId(resultSet
          .getInt("first_annotation_id"));
      transcription.setStatus(resultSet.getInt("status") > 0);
      transcription.setNaId(new String(resultSet.getBytes("na_id"), "UTF-8"));
      transcription.setObjectId(new String(resultSet.getBytes("object_id"),
          "UTF-8"));
      transcription.setPageNum(resultSet.getInt("page_num"));
      if (resultSet.getString("opa_id") != null) {
        transcription
            .setOpaId(new String(resultSet.getBytes("opa_id"), "UTF-8"));
      }
      transcription.setAccountId(resultSet.getInt("account_id"));
      transcription.setAnnotationTS(resultSet.getTimestamp("annotation_ts"));

    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return transcription;
  }

}
