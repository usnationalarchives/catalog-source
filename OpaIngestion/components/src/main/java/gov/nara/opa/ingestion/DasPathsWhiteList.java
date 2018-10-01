package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.framework.ComponentImpl;
import com.searchtechnologies.aspire.services.AspireException;
import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by john.henson@ppc.com on 3/14/17.
 */
public class DasPathsWhiteList extends ComponentImpl {
    private Settings settings;
    private static String pathToWhiteList;
    private static String pathToLeafWhiteList;
    public static final Set<String> DAS_WHITE_LIST;
    public static final Set<String> DAS_LEAF_WHITE_LIST;

    static {
        DAS_WHITE_LIST = new HashSet();
        DAS_LEAF_WHITE_LIST = new HashSet();
    }

    @Override
    public void initialize(Element config) throws AspireException {
        this.settings = Components.getSettings(this);
        this.pathToWhiteList = settings.getDasWhiteList();
        this.pathToLeafWhiteList = settings.getDasLeafWhiteList();
        try {
            loadFileIntoSet(pathToWhiteList, DAS_WHITE_LIST,"DAS white list");
            loadFileIntoSet(pathToLeafWhiteList, DAS_LEAF_WHITE_LIST, "DAS leaf white list");
        } catch (IOException e) {
            e.printStackTrace();
            throw new AspireException("ErrorLoadingDasWhiteList",e);
        }
    }

    @Override
    public void close() {
    }

    private void loadFileIntoSet(String filePath, Set<String> destinationSet, String setName) throws IOException {
        if (destinationSet.size() > 0) {
            return;
        }

        long startTime = (new Date()).getTime();
        info("Loading " + setName + " records from file: " + filePath);
        BufferedReader reader = null;
        int i = 0;

        try {
            reader = new BufferedReader(new FileReader(filePath));

            String line = reader.readLine();
            while (line != null) {
                destinationSet.add(line.trim());
                line = reader.readLine();
                i++;
            }
            reader.close();
        } catch (IOException e) {
            if (reader != null) {
                reader.close();
            }
            throw e;
        }
        long totalTime = (new Date()).getTime() - startTime;
        info(i + " records were loaded in " + setName + " list in "
                + totalTime + " seconds.");
    }
}
