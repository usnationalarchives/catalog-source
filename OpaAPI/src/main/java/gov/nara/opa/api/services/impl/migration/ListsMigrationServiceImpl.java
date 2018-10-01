package gov.nara.opa.api.services.impl.migration;

import gov.nara.opa.api.dataaccess.impl.user.lists.TempUserListItemRowMapper;
import gov.nara.opa.api.dataaccess.impl.user.lists.TempUserListRowMapper;
import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.dataaccess.utils.OpenJDBCTemplate;
import gov.nara.opa.api.services.migration.ListItemsMigrationService;
import gov.nara.opa.api.services.migration.ListsMigrationService;
import gov.nara.opa.api.services.user.lists.CreateUserListService;
import gov.nara.opa.api.user.lists.UserList;
import gov.nara.opa.api.user.lists.UserListItem;
import gov.nara.opa.api.utils.SearchUtils;
import gov.nara.opa.api.validation.migration.ListsMigrationRequestParameters;
import gov.nara.opa.api.valueobject.migration.ListItemsMigrationValueObject;
import gov.nara.opa.api.valueobject.migration.ListsMigrationValueObject;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ListsMigrationServiceImpl implements ListsMigrationService {

  private static OpaLogger logger = OpaLogger
      .getLogger(ListsMigrationServiceImpl.class);

  @Autowired
  private UserAccountDao userAccountDao;

  @Autowired
  private SearchUtils searchByIdUtils;

  @Autowired
  private OpenJDBCTemplate template;

  @Autowired
  private CreateUserListService createUserListService;

  @Autowired
  private ListItemsMigrationService itemMigrationService;

  private final String SELECT_LISTS = "SELECT COUNT(1) FROM %1$s.SR_SAVED_RESULT_LISTS";

  private final String INSERT_TEMP_LISTS_STATEMENT = "INSERT INTO tmp_lists(list_id, list_name, list_ts, account_id) "
      + "SELECT SR_SAVED_RESULT_LISTS_ID, SR_LIST_NAME, now(), SR_USER_ID "
      + "FROM %1$s.SR_SAVED_RESULT_LISTS ";

  private final String CLEAR_TMP_TABLE = "DELETE FROM tmp_lists WHERE 1=1";

  @Override
  public ListsMigrationValueObject doMigration(
      ListsMigrationRequestParameters requestParameters,
      ValidationResult validationResult) {

    ListsMigrationValueObject resultValueObject = new ListsMigrationValueObject();
    resultValueObject.setFullDetail(requestParameters.getFullDetail());

    String sourceDatabaseName = requestParameters.getSourceDatabaseName();

    int listsRead = template.getJdbcTemplate().queryForInt(
        String.format(SELECT_LISTS, sourceDatabaseName));
    resultValueObject.setListsRead(listsRead);
    logger.info(String.format("List Migration - Lists read: %1$d", listsRead));

    // Clear tmp tables
    template.getJdbcTemplate().update(CLEAR_TMP_TABLE);

    // Import data from source Database into temp table
    int listsWritten = template.getJdbcTemplate().update(
        String.format(INSERT_TEMP_LISTS_STATEMENT, sourceDatabaseName));
    resultValueObject.setListsWritten(listsWritten);
    logger.info(String.format("List Migration - Lists written: %1$d",
        listsWritten));

    if (requestParameters.getAction().equals("load")) {

    }

    // Import list item data
    ListItemsMigrationValueObject itemValueObject = itemMigrationService
        .doMigration(requestParameters.getAction(),
            requestParameters.getSourceDatabaseName(), validationResult,
            requestParameters.getFullDetail());

    resultValueObject.setItemsValueObject(itemValueObject);

    // Import to accounts_lists and accounts_lists_entries

    try {
      List<UserList> tempLists = selectTmpLists();
      for (UserList tempList : tempLists) {

        UserAccountValueObject user = userAccountDao
            .migrationSelectByTempAccountId(tempList.getAccountId());

        if (user != null) {
          insertList(tempList);
          List<UserListItem> tempListItemsList = selectTempListItems(tempList
              .getListId());
          for (UserListItem tempListItem : tempListItemsList) {
            createListEntries(tempListItem, tempList);
          }
        }
      }
    } catch (DataAccessException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    return resultValueObject;
  }

  public boolean insertList(UserList userList) throws DataAccessException,
      UnsupportedEncodingException {
    boolean result = false;

    // Create and fill the query with the specific parameters
    String sql = " INSERT INTO opadb.accounts_lists (list_name, list_ts, account_id) "
        + " VALUES (?, now(), (select account_id from opadb.accounts where user_name = (select UM_USER_NAME from migration.UM_USERS where UM_USER_ID = ? LIMIT 1)) ) ";

    // the update() will return the number of rows modified, so greater than
    // 0 means that the query was executed succesful
    result = (template.getJdbcTemplate().update(
        sql,
        new Object[] { userList.getListName().getBytes("UTF-8"),
            userList.getAccountId() }) > 0);

    return result;
  }

  public int createListEntries(UserListItem tempListItem, UserList userList)
      throws DataAccessException, UnsupportedEncodingException {

    String opaId = searchByIdUtils.getOpaId("iapi", tempListItem.getNaId(),
    tempListItem.getObjectId());

    if (!StringUtils.isNullOrEmtpy(opaId)) {
      // Create and fill the query with the specific parameters
      String sql = "INSERT INTO opadb.accounts_lists_items(list_id, na_id, object_id, opa_id, item_ts) "
          + "VALUES ( (SELECT list_id from accounts_lists where list_name = ? "
          + "AND account_id = (select account_id from opadb.accounts where user_name = (select UM_USER_NAME from migration.UM_USERS where UM_USER_ID = ? LIMIT 1)) LIMIT 1),?,?,?,now()) ";
      try {
        return template.getJdbcTemplate().update(
            sql,
            new Object[] { userList.getListName(), userList.getAccountId(),
                tempListItem.getNaId(), tempListItem.getObjectId(), opaId });
      } catch (Exception e) {
        throw new OpaRuntimeException(e);
      }
    }
    return 0;

  }

  public List<UserList> selectTmpLists() throws DataAccessException,
      UnsupportedEncodingException {

    // Create and fill the query with the specific parameters
    String sql = "SELECT tl.list_id, tl.list_name,tl.list_ts, tl.account_id "
        + "FROM opadb.tmp_lists tl ";

    return template.getJdbcTemplate().query(sql, new Object[] {},
        new TempUserListRowMapper());
  }

  public List<UserListItem> selectTempListItems(int listId) {

    // Create and fill the query with the specific parameters
    String sql = "SELECT tli.list_item_id, tli.list_id, tli.na_id, tli.object_id, tli.opa_id, tli.item_ts "
        + " FROM opadb.tmp_lists_items tli  WHERE list_id = ? ";

    return template.getJdbcTemplate().query(sql, new Object[] { listId },
        new TempUserListItemRowMapper());
  }

}
