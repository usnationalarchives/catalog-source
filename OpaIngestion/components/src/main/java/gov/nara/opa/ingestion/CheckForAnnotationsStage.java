/**
 * Copyright Search Technologies 2014
 * for NARA OPA
 */
package gov.nara.opa.ingestion;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.w3c.dom.Element;

import com.searchtechnologies.aspire.rdb.RDBMSConnectionPool;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Job;

/**
 * Stage to determine whether this document has annotations.
 * 
 * @author OPA Ingestion Team
 */
public class CheckForAnnotationsStage extends IngestionStage {

	private static final String SP_CHECK_ANNOTATIONS = "spIngestionCheckAnnotations";
	public final String NO_ANNOTATIONS_EVENT = "noAnnotations";

	private Settings settings;

	/**
	 * For documents coming from bulk load, always skip annotations processing
	 * Determine whether this document has annotations. If not, annotations
	 * processing can be skipped for it.
	 * 
	 * @param j
	 *            The job to process.
	 */
	@Override
	public void process(Job j) throws AspireException {

		debug("Checking Annotations for job: %s", j.getJobId());

		JobInfo info = Jobs.getJobInfo(j);
		Integer naid = info.getNAID();
		String objectId = info.getObjectId();

		// Get a connection
		try (Connection connection = settings.getDbConnection()) {

			if (connection == null) {
				createBranch(j, "Couldn't get an RDB connection");
			} else {
				try {
					debug("CheckAnnotations: rdbConn %s,naid %s, objectId %s",
							connection, naid, objectId);
					boolean hasAnnotations = checkAnnotations(connection, naid,
							objectId);
					if (!hasAnnotations) {
						createBranch(j, "No annotations found");
					}
				} catch (Exception e) {
					createBranch(j, "Couldn't check for annotations");
				}
			}
		} catch (Throwable e) {
			error(e, "%s", info.getDescription());
			createBranch(j, "Couldn't get an RDB connection");
		}

		debug("CheckForAnnotationsStage: final branch = " + j.getBranch());
	}

	@Override
	public void initialize(Element config) throws AspireException {

		settings = Components.getSettings(this);
	}

	private boolean checkAnnotations(Connection conn, Integer naid,
			String objectid) throws SQLException {
		boolean annotationsFound = false;

		// Single query to all annotations tables, limiting to finding first
		// applicable row in any table
		try (CallableStatement statement = StoreProcedureDataAccessUtils.callProcedure(conn,SP_CHECK_ANNOTATIONS, naid, objectid)){
			ResultSet resultSet = statement.getResultSet();

			// Only one row needs to be returned for annotations to be
			// processed.
			if (resultSet.first()) {
				annotationsFound = true;
			}
		}

		return annotationsFound;
	}

	private void createBranch(Job j, String reason) {
		debug("CheckForAnnotationsStage - Skipping Annotations for job: %s (%s)",
				j.getJobId(), reason);
		j.setBranch(NO_ANNOTATIONS_EVENT);
	}
}
