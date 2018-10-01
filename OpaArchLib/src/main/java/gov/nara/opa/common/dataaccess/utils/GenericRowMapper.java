package gov.nara.opa.common.dataaccess.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

public class GenericRowMapper<T> implements RowMapper<T> {

	ResultSetExtractor<T> extractor;

	public GenericRowMapper(ResultSetExtractor<T> extractor) {
		this.extractor = extractor;
	}

	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		return extractor.extractData(rs);
	}

}
