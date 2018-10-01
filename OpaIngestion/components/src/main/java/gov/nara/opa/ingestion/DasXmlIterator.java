package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;

import java.io.File;

public interface DasXmlIterator {

	boolean hasNext() throws AspireException;

	File next() throws AspireException;
	
	void close() throws AspireException;

    String getName();
}
