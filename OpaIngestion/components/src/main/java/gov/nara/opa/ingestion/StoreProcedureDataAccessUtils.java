package gov.nara.opa.ingestion;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class StoreProcedureDataAccessUtils {

	/**
	 * Returns a statement which is a call to an Ingestion stored procedure.
	 * @param con
	 * @param spName
	 * @param naId
	 * @param objectId
	 * @return
	 * @throws SQLException
   */
	public static CallableStatement callProcedure(Connection con, String spName,
			Integer naId, String objectId) throws SQLException {

		CallableStatement cs = con.prepareCall("{call " + spName + "(?,?)}");
		cs.setInt("naId", naId);
		cs.setString("objectId", objectId);
		cs.execute();

		return cs;
	}
}
