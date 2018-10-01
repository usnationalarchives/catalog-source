package gov.nara.opa.api.dataaccess.impl.annotation.tags;

import gov.nara.opa.api.annotation.TranscriptedOpaTitle;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class TranscriptedOpaTitleRowMapper implements
    RowMapper<TranscriptedOpaTitle> {

  @Override
  public TranscriptedOpaTitle mapRow(ResultSet rs, int line)
      throws SQLException {
    TranscriptedOpaTitleResultSetExtractor extractor = new TranscriptedOpaTitleResultSetExtractor();
    return extractor.extractData(rs);
  }
}
