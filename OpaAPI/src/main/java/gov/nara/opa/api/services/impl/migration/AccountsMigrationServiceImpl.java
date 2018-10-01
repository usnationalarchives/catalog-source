package gov.nara.opa.api.services.impl.migration;

import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.dataaccess.utils.OpenJDBCTemplate;
import gov.nara.opa.api.services.migration.AccountsMigrationService;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.system.OpaErrorCodeConstants;
import gov.nara.opa.api.utils.PasswordUtils;
import gov.nara.opa.api.validation.migration.AccountsMigrationRequestParameters;
import gov.nara.opa.api.valueobject.migration.AccountsMigrationValueObject;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class AccountsMigrationServiceImpl implements AccountsMigrationService {

  static OpaLogger logger = OpaLogger
      .getLogger(AccountsMigrationServiceImpl.class);

  private static final String SELECT_USER_NAME_EXISTS = "SELECT uid FROM temp_accounts WHERE uid=?";
  private static final String SELECT_USER_EMAIL_EXISTS = "SELECT mail FROM temp_accounts WHERE mail=?";
  
  private static final String CREATE_ACCOUNT_LOGS = "INSERT INTO accounts_log(account_id, status, action, log_ts) "
      + "SELECT account_id, 1, 'ADD', account_created_ts "
      + "FROM accounts "
      + "WHERE account_id not in (select account_id from accounts_log)";


  @Value("${import.input.location}")
  private String importFolder;

  @Autowired
  private OpenJDBCTemplate openJDBC;

  @Autowired
  private UserAccountDao accountDao;
  
  @Autowired
  private ConfigurationService configService;

  @Override
  public AccountsMigrationValueObject doMigration(
      AccountsMigrationRequestParameters requestParameters,
      ValidationResult validationResult) {

    AccountsMigrationValueObject resultValueObject = new AccountsMigrationValueObject();
    resultValueObject.setFullDetail(requestParameters.getFullDetail());

    ArrayList<String> usernames = new ArrayList<String>();
    ArrayList<String> emails = new ArrayList<String>();

    // Get file name
    String accountsFileName = requestParameters.getDataFile();
    String accountsFilePath = importFolder + accountsFileName;

    if (isFileExists(accountsFilePath)) {
      File accountsFile = new File(accountsFilePath);

      try {
        // Parse file
        ArrayList<LinkedHashMap<String, Object>> accounts = fillAccountsArray(accountsFile);
        ArrayList<LinkedHashMap<String, Object>> insertedAccounts = new ArrayList<LinkedHashMap<String, Object>>();

        // Get statistics
        getDuplicateInfo(accounts, usernames, emails);
        resultValueObject.setDuplicateUserNames(usernames);
        resultValueObject.setTotalDuplicateUserNames(usernames.size());

        resultValueObject.setDuplicateEmails(emails);
        resultValueObject.setTotalDuplicateEmails(emails.size());

        resultValueObject.setTotalAccountsRead(accounts.size());
        logger.info(String.format("Account Migration - Accounts read: %1$d",
            accounts.size()));

        // Read into temp table
        Integer insertCount = writeIntoTempTables(accounts, insertedAccounts);
        resultValueObject.setTotalAccountsWritten(insertCount);
        logger.info(String.format("Account Migration - Accounts written: %1$d",
            insertCount));

        // Import into system table
        if (requestParameters.getAction().equals("load")) {
          createSystemAccounts(insertedAccounts, resultValueObject);
          logger.info(String.format("Account Migration - Accounts migrated: %1$d",
              resultValueObject.getTotalAccountsMigrated()));        
        }

      } catch (IOException e) {
        logger.error(e.getMessage(), e);
        throw new OpaRuntimeException(e);
      }

    } else {
      ValidationError error = new ValidationError();
      error.setErrorCode(OpaErrorCodeConstants.FILE_NOT_FOUND);
      error.setErrorMessage(String.format(ErrorConstants.FILE_NOT_FOUND,
          accountsFilePath));

      validationResult.addCustomValidationError(error);
    }

    return resultValueObject;

  }

  /**
   * Get map of all the different columns that exist on the specified File
   * 
   * @param file
   *          File we are importing form
   * @return LinkedHashMap<String, String> with the collection of distinct
   *         columns
   * @throws IOException
   */
  public LinkedHashMap<String, Object> getDefaultColumns() throws IOException {

    LinkedHashMap<String, Object> theColumns = new LinkedHashMap<String, Object>();

    theColumns.put("dn", "");
    theColumns.put("userPassword", "");
    theColumns.put("title", "");
    theColumns.put("description", "");
    theColumns.put("uid", "");
    theColumns.put("cn", "");
    theColumns.put("sn", "");
    theColumns.put("givenName", "");
    theColumns.put("mail", "");
    theColumns.put("accountStatus", "");
    theColumns.put("appAccess", "");
    theColumns.put("telephoneNumber", "");
    theColumns.put("objectClass1", "");
    theColumns.put("objectClass2", "");
    theColumns.put("objectClass3", "");
    theColumns.put("objectClass4", "");
    theColumns.put("objectClass5", "");

    return theColumns;
  }

  /**
   * Fill an array list with all the accounts that are going to be imported.
   * 
   * @param file
   * @return array list with all the accounts that are going to be imported
   * @throws IOException
   */
  private ArrayList<LinkedHashMap<String, Object>> fillAccountsArray(File file)
      throws IOException {

	  BufferedReader br = null;
	  try {
		  br = new BufferedReader(new InputStreamReader(
				  new FileInputStream(file)));
		  LinkedHashMap<String, Object> accountMap = getDefaultColumns();
		  ArrayList<LinkedHashMap<String, Object>> resultsValueList = new ArrayList<LinkedHashMap<String, Object>>();

		  String line = br.readLine();

		  while (line != null) {
			  if (!line.isEmpty()) {
				  if (line.split(":")[0].equals("objectClass")) {
					  int i = 1;

					  while (!line.isEmpty() && line != null
							  && line.split(":")[0].equals("objectClass")) {
						  accountMap.put((line.split(":")[0]) + i, line.split(":")[1]
								  .toLowerCase().trim());
						  line = br.readLine();
						  i++;
					  }
				  } else {
					  accountMap.put(line.split(":")[0], line.split(":")[1].toLowerCase()
							  .trim());
					  line = br.readLine();
				  }
			  }
			  if (line == null || line.isEmpty()) {
				  resultsValueList.add(accountMap);
				  accountMap = getDefaultColumns();
				  line = br.readLine();
			  }
		  }
		  br.close();
		  return resultsValueList;
	  } catch (IOException e) {
		  if (br != null) {
			  br.close();
		  }
		  throw e;
	  }
  }

  /**
   * @param accounts
   * @param usernames
   * @param emails
   */
  private void getDuplicateInfo(
      ArrayList<LinkedHashMap<String, Object>> accounts,
      ArrayList<String> usernames, ArrayList<String> emails) {
    HashSet<String> userNameDummySet = new HashSet<String>();
    HashSet<String> emailDummySet = new HashSet<String>();

    for (LinkedHashMap<String, Object> account : accounts) {

      String accountId = account.get("uid").toString();
      if (userNameDummySet.contains(accountId)) {
        if (!usernames.contains(accountId)) {
          usernames.add(accountId);
        }
      } else {
        userNameDummySet.add(accountId);
      }

      String email = account.get("mail").toString();
      if (emailDummySet.contains(email)) {
        if (!emails.contains(email)) {
          emails.add(email);
        }
      } else {
        emailDummySet.add(email);
      }
    }
  }

  /**
   * Check if the file exists in the specified path
   * 
   * @param fileName
   *          Full path of the file we are reading
   * @return true/false if file exist
   */
  private boolean isFileExists(String fileName) {
    File file = new File(fileName);
    return file.exists();
  }

  
  /**
   * @param accounts
   * @param insertedAccounts
   * @return
   * @throws DataAccessException
   * @throws IOException
   */
  private Integer writeIntoTempTables(
      ArrayList<LinkedHashMap<String, Object>> accounts,
      ArrayList<LinkedHashMap<String, Object>> insertedAccounts)
      throws DataAccessException, IOException {
    JdbcTemplate template = openJDBC.getJdbcTemplate();
    Integer insertCount = 0;

    for (LinkedHashMap<String, Object> account : accounts) {
      if (insert(account, template)) {
        insertCount++;
        insertedAccounts.add(account);

      }
    }

    return insertCount;
  }

  /**
   * Create and configure the parameters of the insert query
   * 
   * @param account
   *          LinkedHashMap<String, Object> with the accounts to create
   * @throws DataAccessException
   * @throws IOException
   */
  private boolean insert(LinkedHashMap<String, Object> account,
      JdbcTemplate jdbcTemplate) throws DataAccessException, IOException {
    boolean result = false;
    if (!verifyIfUserNameExists(account.get("uid").toString(), jdbcTemplate)
        && !verifyIfEmailExists(account.get("mail").toString(), jdbcTemplate)) {

      String sql = "INSERT INTO opadb.temp_accounts (dn,userPassword,title,description,uid,cn,sn,givenName,mail,accountStatus,appAccess,objectClass1,objectClass2,objectClass3,objectClass4,objectClass5) "
          + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      jdbcTemplate.update(sql, toObjectArray(account));
      result = true;
    } else {
      result = false;
    }
    return result;
  }

  /**
   * Convert the values HashMap to an Object[] to send the parameters to the
   * query.
   * 
   * @param account
   * @return Object[] filled with the paramters to insert
   */
  private Object[] toObjectArray(LinkedHashMap<String, Object> account) {

    return new Object[] {
        (account.get("dn") == null ? null : account.get("dn")),
        (account.get("userPassword") == null ? null : account
            .get("userPassword")),
        (account.get("title") == null ? null : account.get("title")),
        (account.get("description") == null ? null : account.get("description")),
        (account.get("uid") == null ? null : account.get("uid")),
        (account.get("cn") == null ? null : account.get("cn")),
        (account.get("sn") == null ? null : account.get("sn")),
        (account.get("givenName") == null ? null : account.get("givenName")),
        (account.get("mail") == null ? null : account.get("mail")),
        (account.get("accountStatus") == null ? null : account
            .get("accountStatus")),
        (account.get("appAccess") == null ? null : account.get("appAccess")),
        (account.get("objectClass1") == null ? null : account
            .get("objectClass1")),
        (account.get("objectClass2") == null ? null : account
            .get("objectClass2")),
        (account.get("objectClass3") == null ? null : account
            .get("objectClass3")),
        (account.get("objectClass4") == null ? null : account
            .get("objectClass4")),
        (account.get("objectClass5") == null ? null : account
            .get("objectClass5")) };
  }

  /**
   * Verifies if the user email exists in the account table.
   * 
   * @param email
   *          The value for the user email
   * @return true of false
   */
  private boolean verifyIfEmailExists(String email, JdbcTemplate jdbcTemplate) {
    List<String> userEmails = jdbcTemplate.queryForList(
        SELECT_USER_EMAIL_EXISTS, String.class, email);
    return userEmails.size() > 0 ? true : false;
  }

  /**
   * Verifies if the userName exists in the account table.
   * 
   * @param userName
   *          The value for the user name
   * @return true of false
   */
  private boolean verifyIfUserNameExists(String userName,
      JdbcTemplate jdbcTemplate) {
    List<String> userNames = jdbcTemplate.queryForList(SELECT_USER_NAME_EXISTS,
        String.class, userName);

    return userNames.size() > 0 ? true : false;
  }

  private void createSystemAccounts(
      ArrayList<LinkedHashMap<String, Object>> accounts,
      AccountsMigrationValueObject resultsValueObject) {

    int migratedAccounts = 0;
    
    for (LinkedHashMap<String, Object> account : accounts) {
      UserAccountValueObject accountObject = null;
      try {
        // Create value object
        accountObject = getValueObject(account);

        // Validation
        if (ValidateAccountObject(accountObject, resultsValueObject)) {
          // Insert account
          accountDao.create(accountObject);
          migratedAccounts++;
        }
        
      } catch (Exception e) {
        if (accountObject != null) {
          resultsValueObject.getFailedAccounts().put(
              accountObject.getUserName(), e.getMessage());
        } else {
          resultsValueObject.getFailedAccounts().put(
              account.get("uid").toString(), e.getMessage());
        }
      }
    }

    openJDBC.getJdbcTemplate().update(CREATE_ACCOUNT_LOGS);

    resultsValueObject.setTotalAccountsMigrated(migratedAccounts);
  }
  

  /**
   * @param accountMap
   * @return
   */
  private UserAccountValueObject getValueObject(
      LinkedHashMap<String, Object> accountMap) {
    UserAccountValueObject result = new UserAccountValueObject();

    String email = accountMap.get("mail").toString();
    String userName = accountMap.get("uid").toString();

    // Default values
    result.setAccountType("standard");
    result.setAccountRights("regular");
    result.setDisplayFullName(email.contains(configService.getConfig().getNaraEmail()));
    result.setNaraStaff(email.contains(configService.getConfig().getNaraEmail()));
    result.setAccountStatus(true);
    result.setpWord(PasswordUtils.saltPassword("12345678"));
    result.setLastNotificationId(0);
    //PasswordUtils.setSecurityInformation(result);
    result.setpWordChangeId(0);
    result.setLoginAttempts(0);
    result.setLastActionTS(TimestampUtils.getUtcTimestamp());

    // TODO: Use the one provided when available
    result.setAccountCreatedTS(TimestampUtils.getUtcTimestamp());

    result.setUserName(userName);
    result.setFullName(userName);
    result.setEmailAddress(email);

    return result;
  }

  private boolean ValidateAccountObject(UserAccountValueObject accountObject,
      AccountsMigrationValueObject resultsValueObject) {
    boolean result = true;

    String userName = accountObject.getUserName();

    // Email
    String email = accountObject.getEmailAddress();
    if (StringUtils.isNullOrEmtpy(email)) {
      resultsValueObject.getFailedAccounts().put(userName,
          "Invalid email address");
      result = false;
    }

    UserAccountValueObject existingAccount = accountDao.selectByEmail(email);
    if (existingAccount != null) {
      resultsValueObject.getFailedAccounts().put(userName,
          "Duplicate email address");
    }

    // User name
    existingAccount = accountDao.selectByUserName(userName);
    if (existingAccount != null) {
      resultsValueObject.getFailedAccounts().put(userName,
          "Duplicate user name");
    }

    return result;
  }

}
