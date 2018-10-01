/**
 * Copyright Search Technologies 2013
 */
package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.scanner.ItemType;
import com.searchtechnologies.aspire.scanner.ItemTypeEnum;

/**
 * Represents an item type from the content source. 
 * @author Javier Mendez
 */
public class DasExportItemType extends ItemType {

  /**
   * Enumeration of object types found in DasExport.
   */
  enum DasExportItemTypeEnum implements ItemTypeEnum {
    /* 
     * TODO: Add a list of the item types available on the content source.
     * 
     * For example for a File System:
     * file,
     * folder,
     * fileSystem,
     * 
     * You need to list the source type as well (i.e. fileSystem).
     * 
     */
	file,
    dasExport //The actual source type
  }


  /**
   * Converts a string to the enum value and sets it to the itemType Id.  Can be retrieved by the getValue() method
   * @param s String to parse to the enum
   */
  public void setValue(String s){
    //Don't change.
    id = DasExportItemTypeEnum.valueOf(s);
  }
}
