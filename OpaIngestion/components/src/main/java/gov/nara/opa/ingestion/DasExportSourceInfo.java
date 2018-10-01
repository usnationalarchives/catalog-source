/**
 * Copyright Search Technologies 2012
 */
package gov.nara.opa.ingestion;

import java.nio.file.Path;
import java.util.Queue;

import com.searchtechnologies.aspire.framework.Standards.Scanner.Action;
import com.searchtechnologies.aspire.scanner.DSConnection;
import com.searchtechnologies.aspire.scanner.ItemType;
import com.searchtechnologies.aspire.scanner.LinearSourceInfo;
import com.searchtechnologies.aspire.scanner.SourceItem;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.JobEvent;

public class DasExportSourceInfo extends LinearSourceInfo {

	public Queue<Path> files;
	private boolean newExport;

	public DasExportSourceInfo() {
		newExport = true;
		sourceType = "das".toLowerCase();
	}

	@Override
	public SourceItem getNextItem() throws AspireException {

		if (newExport) {
			newExport = false;
			SourceItem subItem = new SourceItem(
					"http://opa.nara.ppc-cloud.com/dasexport/");
			ItemType type = new DasExportItemType();
			type.setValue(DasExportItemType.DasExportItemTypeEnum.dasExport);
			subItem.setItemType(type);
			subItem.setContainer(false);
			subItem.setUrl("http://opa.nara.ppc-cloud.com/dasexport/");
			subItem.setModificationSignature("1");
			return subItem;
		} else {
			return null;
		}
	}

	@Override
	public boolean hasNextItem() throws AspireException {
		return newExport;
	}

	/**
	 * Populates a SourceItem object with all metadata information associated to
	 * it
	 * 
	 * @param item
	 * @throws AspireException
	 */
	@Override
	public void populateSourceItem(SourceItem item) throws AspireException {
	}

	/**
	 * DSConnection factory method
	 * 
	 * @return
	 * @throws AspireException
	 */
	@Override
	public DSConnection newDSConnection() {
		DasDSConnection connection = new DasDSConnection();
		return connection;
	}

	@Override
	public void jobComplete(Action action, JobEvent event)
			throws AspireException {

	}

}
