package gov.nara.opa.api.utils;

import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class LoadAccountsFromTextFileUtils extends AbstractOpaDbJDBCTemplate {

  private static final String SELECT_USER_NAME_EXISTS = "SELECT uid FROM temp_accounts WHERE uid=?";
  private static final String SELECT_USER_EMAIL_EXISTS = "SELECT mail FROM temp_accounts WHERE mail=?";


  /**
   * Count the number of empty lines on the file specified, (that means the
   * number of accounts provided)
   * 
   * @param file
   *          File we are importing form
   * @return number of lines (accounts) provided on the file
   * @throws IOException
   */
  public int countEmptyLines(File file) throws IOException {
	  BufferedReader br = null;
	  int numEmptyLines = 1;
	  try {
		  br = new BufferedReader(new InputStreamReader(
				  new FileInputStream(file)));
		  String line = br.readLine();
		  while (line != null) {
			  if (line.isEmpty())
				  numEmptyLines++;
			  line = br.readLine();
		  }
		  br.close();
	  } catch (IOException e) {
		  if (br != null) {
			  br.close();
		  }
		  throw e;
	  }
    return numEmptyLines;
  }

  /**
   * Fill an array list with all the accounts that are going to be imported.
   * 
   * @param file
   * @return array list with all the accounts that are going to be imported
   * @throws IOException
   */
  public ArrayList<LinkedHashMap<String, Object>> fillAccountsArray(File file)
      throws IOException {

	  BufferedReader br = null;
	  ArrayList<LinkedHashMap<String, Object>> resultsValueList = new ArrayList<LinkedHashMap<String, Object>>();

	  try {
		  br = new BufferedReader(new InputStreamReader(
				  new FileInputStream(file)));

		  LinkedHashMap<String, Object> accountMap = getDefaultColumns();

		  String line = br.readLine();

		  while (line != null) {
			  if (!line.isEmpty()) {
				  if (line.split(":")[0].equals("objectClass")) {
					  int i = 1;

					  while (!line.isEmpty() && line != null
							  && line.split(":")[0].equals("objectClass")) {
						  accountMap.put((line.split(":")[0]) + i, line.split(":")[1]);
						  line = br.readLine();
						  i++;
					  }
				  } else {
					  accountMap.put(line.split(":")[0], line.split(":")[1]);
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
	  } catch (IOException e) {
		  if (br != null) {
			  br.close();
		  }
		  throw e;
	  }
	  return resultsValueList;
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
  public LinkedHashMap<String, Object> getColumsFromFile(File file)
      throws IOException {

	  BufferedReader br = null;
	  LinkedHashMap<String, Object> theColumns = new LinkedHashMap<String, Object>();

	  try {
		  br = new BufferedReader(new InputStreamReader(
				  new FileInputStream(file)));

		  String line = br.readLine();

		  while (line != null) {
			  String columnName = line.split(":")[0];
			  if (!line.isEmpty()) {
				  if (!theColumns.containsKey(columnName) 
						  && !columnName.equals("objectClass")) {
					  theColumns.put(columnName, "");
				  }
			  }
			  line = br.readLine();
		  }
		  theColumns.put("objectClass1", "");
		  theColumns.put("objectClass2", "");
		  theColumns.put("objectClass3", "");
		  theColumns.put("objectClass4", "");
		  theColumns.put("objectClass5", "");

		  br.close();
	  } catch (IOException e) {
		  if (br != null) {
			  br.close();
		  }
		  throw e;
	  }
	  return theColumns;
  }

  /**
   * Get map of all the different columns that exist on the specified File
   * 
   * @param file
   *          File we are importing form
   * @return LinkedHashMap<String, String> with the collection of distinct
   *         columns
   */
  public LinkedHashMap<String, Object> getDefaultColumns() {

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
   * Get map of all the different columns that exist on the specified File
   * 
   * @param file
   *          File we are importing form
   * @return LinkedHashMap<String, String> with the collection of distinct
   *         columns
   * @throws IOException
   */
  public int getMaxObjectsColumns(File file) throws IOException {

	  BufferedReader br = null;
	  int maxObjects = 0;

	  try {
		  br = new BufferedReader(new InputStreamReader(
				  new FileInputStream(file)));

		  int counter = 0;
		  String line = br.readLine();

		  while (line != null) {
			  if (line.isEmpty()) {
				  if (counter > maxObjects) {
					  maxObjects = counter;
					  counter = 0;
				  } else {
					  counter = 0;
				  }
				  line = br.readLine();
			  } else {
				  String columnName = line.split(":")[0];
				  if (columnName.equals("objectClass")) {
					  counter++;
				  }
				  line = br.readLine();
			  }
		  }
		  br.close();
	  } catch (IOException e) {
		  if (br != null) {
			  br.close();
		  }
		  throw e;
	  }
	  return maxObjects;
  }

  /**
   * Create and configure the parameters of the insert query
   * 
   * @param account
   *          LinkedHashMap<String, Object> with the accounts to create
   */
  public boolean insert(LinkedHashMap<String, Object> account,
      JdbcTemplate jdbcTemplate) {
    boolean result = false;
    if (!verifyIfUserNameExists(account.get("uid").toString(), jdbcTemplate)
        && !verifyIfEmailExists(account.get("mail").toString(), jdbcTemplate)) {

      String sql = "INSERT INTO opadb.temp_accounts (dn,userPassword,title,description,uid,cn,sn,givenName,mail,accountStatus,appAccess,objectClass1,objectClass2,objectClass3,objectClass4,objectClass5) "
          + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      result = true;
      jdbcTemplate.update(sql, toObjectArray(account));
    } else {
      result = false;
    }
    return result;
  }

  /**
   * Check if the file exists in the specified path
   * 
   * @param fileName
   *          Full path of the file we are reading
   * @return true/false if file exist
   */
  public boolean isFileExists(String fileName) {
    File file = new File(fileName);
    return file.exists();
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
  public boolean verifyIfEmailExists(String email, JdbcTemplate jdbcTemplate) {
    List<String> userEmails = jdbcTemplate.queryForList(
        SELECT_USER_EMAIL_EXISTS, String.class, email);
    if (userEmails.size() > 0) {
      System.out.println("duplicate email: " + email);
    }
    return userEmails.size() > 0 ? true : false;
  }

  /**
   * Verifies if the userName exists in the account table.
   * 
   * @param userName
   *          The value for the user name
   * @return true of false
   */
  public boolean verifyIfUserNameExists(String userName,
      JdbcTemplate jdbcTemplate) {
    List<String> userNames = jdbcTemplate.queryForList(SELECT_USER_NAME_EXISTS,
        String.class, userName);

    if (userNames.size() > 0) {
      System.out.println("duplicate email: " + userName);
    }

    return userNames.size() > 0 ? true : false;
  }

}
