package gov.nara.opa.jp2conversion;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@link ItemReader} with hard-coded input data.
 */

@Component("reader")
public class Jp2ItemReader implements ItemReader<String> {
	
/*	private String[] input =
		{"C:\\Dev\\Data\\NARA\\JP2 Files\\Expanded\\tape_migration\\TAPE_SHIPMENT1\\M1469_Fiche_008151-008200\\M1469_Fiche_008151-008200\\images\\000001-00-00-000000.jp2", 
		 "C:\\Dev\\Data\\NARA\\JP2 Files\\Expanded\\tape_migration\\TAPE_SHIPMENT1\\M1469_Fiche_008151-008200\\M1469_Fiche_008151-008200\\images\\000001-01-01-000001.jp2"};
*/	
	@Value("${initial.wait.seconds}")
	private int initialWait;
	
	private int WAIT_BETWEEN_RETIRES = 1000;
	
	private static final Log log = LogFactory.getLog(Jp2ItemReader.class);
	
	private int waited = 0;
	/**
	 * Reads next record from input
	 */
	public String read() throws Exception {

		File jp2File = Jp2TempWorkDataHolders.WORK_QUEUE.poll();
		//waiting for a bit as the distributor might not have had a chance to put work on the queue yet.
		while (jp2File == null && waited < initialWait * 1000){
			log.info("Waiting " + WAIT_BETWEEN_RETIRES/1000 + " seconds to check for more work....");
			Thread.sleep(WAIT_BETWEEN_RETIRES);
			waited = waited + WAIT_BETWEEN_RETIRES;
		}
		if (jp2File == null){
			return null;
		}
		return jp2File.getAbsolutePath();
	}

}
