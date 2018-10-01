package gov.nara.opa.api.services.impl.migration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.nara.opa.api.dataaccess.utils.OpenJDBCTemplate;
import gov.nara.opa.api.services.migration.TagsMigrationService;
import gov.nara.opa.api.utils.SearchUtils;
import gov.nara.opa.api.validation.migration.TagsMigrationRequestParameters;
import gov.nara.opa.api.valueobject.migration.TagsMigrationValueObject;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class TagsMigrationServiceImpl implements TagsMigrationService {
  
  private static OpaLogger logger = OpaLogger.getLogger(TagsMigrationServiceImpl.class); 
  
  @Autowired
  private OpenJDBCTemplate template;
  
  @Autowired
  private SearchUtils opaIdRetriever;
  
  private static final String SELECT_TAGS = "SELECT COUNT(1) "
      + "FROM %1$s.UT_APPROVED_TAGS uat "
      + "JOIN %1$s.UT_TAGS ut ON uat.UT_TAG_ID = ut.UT_TAG_ID "
      + "JOIN %1$s.ASSET_RECORD ar ON uat.UT_AR_ID = ar.AR_ID "
      + "JOIN %1$s.UM_USERS uu ON uat.CREATED_BY = uu.UM_USER_NAME ";
  
  //Tags temp table name
  private static final String INSERT_TEMP_TAGS_STATEMENT = "INSERT INTO tmp_tags(annotation_id, annotation, na_id, account_id, annotation_ts) "
      + "SELECT uat.UT_TAG_ID, ut.UT_TAG, ar.SOURCE_UID, uu.UM_USER_ID, uat.CREATED_TS "
      + "FROM %1$s.UT_APPROVED_TAGS uat "
      + "JOIN %1$s.UT_TAGS ut ON uat.UT_TAG_ID = ut.UT_TAG_ID "
      + "JOIN %1$s.ASSET_RECORD ar ON uat.UT_AR_ID = ar.AR_ID "
      + "JOIN %1$s.UM_USERS uu ON uat.CREATED_BY = uu.UM_USER_NAME ";
  
  private static final String INSERT_TEMP_LOGS_STATEMENT = "INSERT INTO tmp_log(annotation_type, annotation_id, na_id, account_id, action, log_ts) "
      + "SELECT 'TG', annotation_id, na_id, account_id, 'ADD', annotation_ts "
      + "FROM tmp_tags ";
  
  private static final String ORPHANED_TAGS = "SELECT annotation FROM tmp_tags t "
      + "JOIN %1$s.UM_USERS uu ON t.account_id = uu.UM_USER_ID "
      + "WHERE uu.UM_USER_NAME NOT IN (SELECT uid FROM temp_accounts) LIMIT 0, 10000 ";
  
  private static final String CLEAR_TMP_TABLE = "DELETE FROM tmp_tags WHERE 1=1";
  
  private static final String CLEAR_TMP_LOG_TABLE = "DELETE FROM tmp_log WHERE 1=1";
  
  private static final String MIGRATE_TAGS = "INSERT INTO annotation_tags(annotation, annotation_MD5, "
      + "account_id, na_id, annotation_ts, status) "
      + "SELECT annotation, MD5(annotation), ac.account_id, na_id, annotation_ts, 1 "
      + "FROM tmp_tags t "
      + "JOIN %1$s.UM_USERS uu ON t.account_id = uu.UM_USER_ID "
      + "JOIN accounts ac ON uu.UM_USER_NAME = ac.user_name "
      + "WHERE CONCAT(annotation, na_id, object_id) NOT IN "
      + "(SELECT CONCAT(annotation, na_id, object_id) FROM annotation_tags) ";
  
  private static final String TAGS_TO_MIGRATE = "SELECT annotation, MD5(annotation), ac.account_id, na_id, annotation_ts, 1 "
      + "FROM tmp_tags t "
      + "JOIN %1$s.UM_USERS uu ON t.account_id = uu.UM_USER_ID "
      + "JOIN accounts ac ON uu.UM_USER_NAME = ac.user_name "
      + "WHERE CONCAT(annotation, na_id, object_id) NOT IN "
      + "(SELECT CONCAT(annotation, na_id, object_id) FROM annotation_tags) ";
  
  private static final String CREATE_LOG_ENTRIES = "INSERT INTO annotation_log(annotation_type, annotation_id, "
      + "na_id, account_id, annotation_md5, action, log_ts, session_id) "
      + "SELECT 'TG', annotation_id, na_id, account_id, annotation_md5, 'ADD', annotation_ts, ? "
      + "FROM annotation_tags "
      + "WHERE CONCAT(CONVERT(annotation_md5 using UTF8), na_id, object_id) NOT IN "
      + "(SELECT CONCAT(CONVERT(annotation_md5 using UTF8), na_id, object_id) FROM annotation_log) ";
  
  private static final String DUPLICATE_TAGS = "SELECT t.annotation, t.na_id, t.object_id "
      + "FROM tmp_tags t "
      + "JOIN annotation_tags tags ON t.na_id = tags.na_id "
      + "AND t.annotation = tags.annotation "
      + "AND (t.object_id = tags.object_id OR (t.object_id IS NULL AND tags.object_id IS NULL)) ";
  
  private static final String SELECT_TMP_TAGS = "SELECT na_id, object_id FROM tmp_tags ";
  
  private static final String UPDATE_OPA_ID = "UPDATE tmp_tags SET opa_id = ? WHERE annotation_id = ? ";

  
  @Override
  public TagsMigrationValueObject doMigration(
      TagsMigrationRequestParameters requestParameters, ValidationResult validationResult) {
    TagsMigrationValueObject resultValueObject = new TagsMigrationValueObject();
    resultValueObject.setFullDetail(requestParameters.getFullDetail());
    
    String sourceDatabaseName = requestParameters.getSourceDatabaseName();
    
    int tagsRead = template.getJdbcTemplate().queryForInt(String.format(SELECT_TAGS, sourceDatabaseName));
    resultValueObject.setTagsRead(tagsRead);
    logger.info(String.format("Tag Migration - Tags read: %1$d", tagsRead));
    
    //Clear tmp tables
    template.getJdbcTemplate().update(CLEAR_TMP_TABLE);
    template.getJdbcTemplate().update(CLEAR_TMP_LOG_TABLE);
    
    //Import data from source Database into temp table
    int tagsWritten = template.getJdbcTemplate().update(String.format(INSERT_TEMP_TAGS_STATEMENT, sourceDatabaseName));
    resultValueObject.setTagsWritten(tagsWritten);
    logger.info(String.format("Tag Migration - Tags written: %1$d", tagsWritten));
    
    //Create temp log entries
    template.getJdbcTemplate().update(INSERT_TEMP_LOGS_STATEMENT);
    
    //Get orphaned tags
    List<String> orphanedTags = template.getJdbcTemplate().queryForList(String.format(ORPHANED_TAGS, sourceDatabaseName), String.class);
    resultValueObject.setTotalOrphanedTags(orphanedTags.size());
    resultValueObject.setOrphanedTags(orphanedTags);
    

    if(requestParameters.getAction().equals("load")) {
      migrateTags(resultValueObject, sourceDatabaseName, requestParameters);
      logger.info(String.format("Tag Migration - Tags migrated: %1$d", resultValueObject.getTotalMigratedTags()));
    }

    return resultValueObject;
  }
  
  private void migrateTags(TagsMigrationValueObject migrationValueObject, String sourceDatabaseName, TagsMigrationRequestParameters requestParameters) {
   
    getDuplicateTags(migrationValueObject);
    logger.info(String.format("Tag Migration - Getting duplicate tags"));
    
    List<Map<String, Object>> tagsToMigrate = getTagsToMigrate(sourceDatabaseName);
    
    int migratedTags = template.getJdbcTemplate().update(String.format(MIGRATE_TAGS, sourceDatabaseName));
    migrationValueObject.setTotalMigratedTags(migratedTags);
    if(migratedTags > 0) {
      template.getJdbcTemplate().update(CREATE_LOG_ENTRIES, new Object[] { requestParameters.getHttpSessionId() });
    }
  }
  
  private void getDuplicateTags(TagsMigrationValueObject migrationValueObject) {
    List<Map<String, Object>> duplicates = template.getJdbcTemplate().queryForList(DUPLICATE_TAGS);
    migrationValueObject.setTotalDuplicateTags(duplicates.size());
    migrationValueObject.setDuplicateTags(duplicates);
  }
  
  private List<Map<String, Object>> getTagsToMigrate(String sourceDatabaseName) {
    List<Map<String, Object>> result = template.getJdbcTemplate().queryForList(String.format(TAGS_TO_MIGRATE, sourceDatabaseName));
       
    return result;
  }
  
  
}
