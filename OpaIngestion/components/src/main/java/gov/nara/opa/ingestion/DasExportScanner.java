/**
 * Copyright Search Technologies 2013
 */
package gov.nara.opa.ingestion;

import java.util.List;

import org.w3c.dom.Element;

import com.searchtechnologies.aspire.framework.utilities.Utilities;
import com.searchtechnologies.aspire.groupexpansion.mapdb.SpecialAclDB;
import com.searchtechnologies.aspire.groupexpansion.mapdb.UserGroupDB;
import com.searchtechnologies.aspire.groupexpansion.server.UserOrGroup;
import com.searchtechnologies.aspire.scanner.AbstractLinearScanner;
import com.searchtechnologies.aspire.scanner.ItemType;
import com.searchtechnologies.aspire.scanner.SourceInfo;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.logging.ALogger;

public class DasExportScanner extends AbstractLinearScanner {

	/**
	 * Does any additional component initialization required by the scanner.
	 * Occurs during component initialization.
	 * 
	 * @param config
	 *            Element object with the component configuration from the
	 *            application bundle.
	 */
	@Override
	public void doAdditionalInitialization(Element config)
			throws AspireException {
	}

	/**
	 * Initializes a new instance of DasInfo. Occurs every time a new source is
	 * created on the user interface right after the first scan is fired.
	 * 
	 * @param propertiesXml
	 *            AspireObject containing the "connectorSource" information that
	 *            is provided by the user interface.
	 * @return A new DasInfo object that contains all the necessary data to
	 *         start scanning.
	 * @throws AspireException
	 *             If there are issues parsing the given parameters.
	 */
	@Override
	public SourceInfo initializeSourceInfo(AspireObject propertiesXml)
			throws AspireException {

		// Create a new source info (remember there is one per each content
		// source defined on the user interface).

		DasExportSourceInfo info = new DasExportSourceInfo();
		info.useSnapshots(false);

		// Source info instances are associated to a source ID. If you hit
		// start, stop, pause or resume from the user interface, the same source
		// info instance
		// is updated with the respective status automatically.
		return info;
	}

	/**
	 * Creates a new instance of the ItemType specific to the Scanner.
	 * 
	 * @return a new ItemType instance.
	 */
	@Override
	public ItemType newItemType() {
		return new DasExportItemType();
	}

	/**
	 * If caching groups and users for expansion, this method is called
	 * periodically to download all users and groups for expansion
	 */
	@Override
	public boolean downloadUsersAndGroups(ALogger logger, SourceInfo si,
			UserGroupDB userGroupMap, List<UserOrGroup> externalUserGroupList)
			throws AspireException {
		return true;
	}

	/**
	 * Download a list of any special acls and return them to the scanner for
	 * use later
	 */
	@Override
	public boolean downloadSpecialAcls(ALogger logger, SourceInfo si,
			SpecialAclDB specialAcls) throws AspireException {
		return true;
	}

	/**
	 * This method will be called for each user and for each special acl that
	 * has been downloaded. The user and all its groups are passed along with
	 * the special acl. Decide if the user has access and return true if so and
	 * false otherwise
	 */
	@Override
	public boolean canAccessSpecialAcl(String specialAcl, UserOrGroup uog,
			List<UserOrGroup> grps) {
		return true;
	}
}
