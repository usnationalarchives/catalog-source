package gov.nara.opa.api.utils;

import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileReaderUtils {

  private static OpaLogger logger = OpaLogger.getLogger(FileReaderUtils.class);

  public static String readFile(String filePath, String fileName,
      String encoding) {
    String fileToRead = filePath + "/" + fileName;
    String output = "";
    BufferedReader fileReader = null;
    try {

      // Validate the content directory exists
      File f = new File(fileToRead);
      if (!f.exists()) {
        return null;
      } else {

        // Read the file
        fileReader = new BufferedReader(new InputStreamReader(
            new FileInputStream(fileToRead), encoding));
        output = org.apache.commons.io.IOUtils.toString(fileReader);
        fileReader.close();
      }
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }

    return output;
  }

}
