package gov.nara.opa.api.valueobject.migration;

import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;

public interface ListItemsMigrationObjectConstants extends
    CommonValueObjectConstants {
  public static final String LIST_ITEMS_READ = "@listItemsRead";
  public static final String LIST_ITEMS_WRITTEN = "@listItemsWritten";
  public static final String NOT_IN_ASSET_RECORD = "idsNotInAssetRecord";

}
