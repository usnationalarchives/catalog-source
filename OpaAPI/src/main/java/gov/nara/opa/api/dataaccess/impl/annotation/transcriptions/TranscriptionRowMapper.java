package gov.nara.opa.api.dataaccess.impl.annotation.transcriptions;

import java.sql.ResultSet;
import java.sql.SQLException;

import gov.nara.opa.api.annotation.transcriptions.Transcription;

import org.springframework.jdbc.core.RowMapper;

public class TranscriptionRowMapper implements RowMapper<Transcription> {

  @Override
  public Transcription mapRow(ResultSet resultSet, int rowNum)
      throws SQLException {
    TranscriptionExtractor extractor = new TranscriptionExtractor();
    return extractor.extractData(resultSet);
  }

}
