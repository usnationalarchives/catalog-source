package gov.nara.opa.architecture.dataaccess;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ObjectExtractorHelper {

  public static String getStringIfItExists(ResultSet rs, String columnName)
      throws SQLException {
    int reasonColumnIndex = getColumnIndex(rs, columnName);
    if (reasonColumnIndex >= 0) {
      return rs.getString(reasonColumnIndex);
    }
    return null;
  }

  public static Integer getIntIfItExists(ResultSet rs, String columnName)
      throws SQLException {
    int reasonColumnIndex = getColumnIndex(rs, columnName);
    if (reasonColumnIndex >= 0) {
      return rs.getInt(reasonColumnIndex);
    }
    return null;
  }

  public static Boolean getBooleanIfItExists(ResultSet rs, String columnName)
      throws SQLException {
    int reasonColumnIndex = getColumnIndex(rs, columnName);
    if (reasonColumnIndex >= 0) {
      return rs.getBoolean(reasonColumnIndex);
    }
    return null;
  }

  private static int getColumnIndex(ResultSet rs, String columnName) {
    int reasonColumnIndex = -1;
    try {
      reasonColumnIndex = rs.findColumn(columnName);
    } catch (SQLException e) {
      // ignore exception; the column could not be found
    }
    return reasonColumnIndex;
  }
}
