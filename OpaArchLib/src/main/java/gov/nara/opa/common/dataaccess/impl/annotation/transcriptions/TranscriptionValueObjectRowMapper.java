package gov.nara.opa.common.dataaccess.impl.annotation.transcriptions;

import gov.nara.opa.common.valueobject.annotation.transcriptions.TranscriptionValueObject;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class TranscriptionValueObjectRowMapper implements
    RowMapper<TranscriptionValueObject> {

  @Override
  public TranscriptionValueObject mapRow(ResultSet rs, int line)
      throws SQLException {
    TranscriptionValueObjectResultSetExtractor extractor = new TranscriptionValueObjectResultSetExtractor();
    return extractor.extractData(rs);
  }
}
