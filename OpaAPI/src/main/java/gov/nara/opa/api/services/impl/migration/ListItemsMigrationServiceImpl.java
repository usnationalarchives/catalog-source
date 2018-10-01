package gov.nara.opa.api.services.impl.migration;

import java.util.LinkedHashMap;
import java.util.List;

import gov.nara.opa.api.dataaccess.utils.OpenJDBCTemplate;
import gov.nara.opa.api.services.migration.ListItemsMigrationService;
import gov.nara.opa.api.validation.migration.ListItemsMigrationRequestParameters;
import gov.nara.opa.api.valueobject.migration.ListItemsMigrationValueObject;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ListItemsMigrationServiceImpl implements ListItemsMigrationService {

  private static OpaLogger logger = OpaLogger
      .getLogger(ListItemsMigrationServiceImpl.class);

  @Autowired
  private OpenJDBCTemplate template;

  private final String SELECT_LIST_ITEMS = "SELECT COUNT(1) FROM %1$s.SR_LIST_TO_ASSET_LINK";

  private final String INSERT_TEMP_LIST_ITEMS_STATEMENT = "INSERT INTO tmp_lists_items(list_id, na_id, item_ts) "
      + "SELECT lta.SR_LIST_ID, ar.SOURCE_UID, now() "
      + "FROM %1$s.SR_LIST_TO_ASSET_LINK lta "
      + "JOIN %1$s.ASSET_RECORD ar ON lta.SR_AR_ID = ar.AR_ID ";
  
  private final String NOT_IN_ASSET_RECORD = "SELECT DISTINCT lta.SR_AR_ID "
      + "FROM %1$s.SR_LIST_TO_ASSET_LINK lta "
      + "WHERE lta.SR_AR_ID NOT IN "
      + "(SELECT ar.AR_ID FROM migration.ASSET_RECORD ar) ";

  private final String CLEAR_TMP_TABLE = "DELETE FROM tmp_lists_items WHERE 1=1";

  @Override
  public ListItemsMigrationValueObject doMigration(
      ListItemsMigrationRequestParameters requestParameters,
      ValidationResult validationResult) {
    return doMigration(requestParameters.getAction(),
        requestParameters.getSourceDatabaseName(), validationResult,
        requestParameters.getFullDetail());
  }

  @Override
  public ListItemsMigrationValueObject doMigration(String action,
      String sourceDatabaseName, ValidationResult validationResult,
      Boolean fullDetail) {
    ListItemsMigrationValueObject resultValueObject = new ListItemsMigrationValueObject();
    resultValueObject.setFullDetail(fullDetail);

    int listItemsRead = template.getJdbcTemplate().queryForInt(
        String.format(SELECT_LIST_ITEMS, sourceDatabaseName));
    resultValueObject.setListItemsRead(listItemsRead);
    logger.info(String.format("List Item Migration - List Items read: %1$d",
        listItemsRead));

    // Clear tmp tables
    template.getJdbcTemplate().update(CLEAR_TMP_TABLE);

    // Import data from source Database into temp table
    int listItemsWritten = template.getJdbcTemplate().update(
        String.format(INSERT_TEMP_LIST_ITEMS_STATEMENT, sourceDatabaseName));
    resultValueObject.setListItemsWritten(listItemsWritten);
    logger.info(String.format("List Item Migration - List Items written: %1$d",
        listItemsWritten));
    
    //Get list items with no reference in asset record
    List<Integer> notInAssetRecord = getNotInAssetRecord(sourceDatabaseName);
    resultValueObject.setNotInAssetRecord(notInAssetRecord);
    

    if (action.equals("load")) {

    }

    return resultValueObject;
  }
  
  private List<Integer> getNotInAssetRecord(String sourceDatabaseName) {
    List<Integer> result = null;
    
    String sql = String.format(NOT_IN_ASSET_RECORD, sourceDatabaseName);
    
    result = template.getJdbcTemplate().queryForList(sql, Integer.class);    
    
    return result;
  }
  

}
