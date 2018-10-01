package gov.nara.opa.api.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class NaIdUtils {

  /**
   * Method to create the level 1 and level 2 directories using an naId
   * 
   * @param naId
   *          NAID
   * @return List<String> (level1, level2)
   */
  public static List<String> getNaIdDirectories(String naId) {
    List<String> naIdDirectories = new ArrayList<String>();

    // If naId is not only numerics - return NULL
    if (!naId.matches("[0-9]+")) {
      return null;
    }
    BigInteger naIdInt = new BigInteger(naId);
    int level1 = 0;
    int level2 = 0;

    // Establish level 1
    level1 = (naIdInt.mod(new BigInteger("100"))).intValue();

    // If naId contains a second level - establish level 2
    if (naId.length() > 2) {
      naIdInt = new BigInteger(naId.substring(0, naId.length() - 2));
      level2 = (naIdInt.mod(new BigInteger("10000"))).intValue();
    }

    // Add levels to retrun array
    naIdDirectories.add(String.valueOf(level1));
    naIdDirectories.add(String.valueOf(level2));

    return naIdDirectories;
  }
}
