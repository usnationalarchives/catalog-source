package gov.nara.opa.architecture.web.valueobject;

import gov.nara.opa.architecture.exception.OpaRuntimeException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;

/**
 * The main class that handles the recursion to transform a hierachical
 * structure of AbstractWebValueObjects into a nested AspireObject
 * 
 * When returning a Map with data content to be included in response messages an
 * AbstractWebValueObject can include in this Map these types of objects (as the
 * values in the map):
 * 
 * 1). another AbstractWebValueObject
 * 
 * 2). a List of objects (and some of these objects can be
 * AbstractWebValueObject)
 * 
 * 3). any other object type
 * 
 * The recursion logic will continue down the parsing tree if an
 * AbstractWebValueObject is encountered by getting its Map of aspire content
 * and walking through it. If a List implementation is encountered for the value
 * of the map entries this code will recurse through that list too.
 * 
 * @author aolaru
 * @date Jun 7, 2014
 * 
 */
public class ValueObjectUtils {

  public static final String DB_FIELD_NAME_SUFFIX = "_DB";

  /**
   * Create an AspireObject from the Map aspire content to be returned by an
   * AbstractWebValueObject. Recursively go through any object value in these
   * maps that AbstractWebValueObject. The recursion will be done over any
   * object value that is an implementation of the List interface
   * 
   * @param objectName
   *          The name that will be given to the AspireObject that will be
   *          created
   * @param aspireObjectCreator
   *          The object whose aspire content will be fed into the AspireOject
   * @return A newly create AspireObject that will contain all the
   *         children/granchildren of the entire structure of
   *         AbstractWebValueObjects
   * @throws AspireException
   */
  public static AspireObject createAspireObjectFromContent(String objectName,
      AspireObjectCreator aspireObjectCreator, String action)
      throws AspireException {
    AspireObject aspireObject = new AspireObject(objectName);
    Map<String, Object> aspireCollectionContent = aspireObjectCreator
        .getAspireObjectContent(action);

    if (aspireCollectionContent == null) {
      return aspireObject;
    }

    addAspireMapToParent(aspireCollectionContent, action, aspireObject);
    return aspireObject;
  }

  public static void addAspireMapToParent(
      Map<String, Object> aspireCollectionContent, String action,
      AspireObject aspireObject) throws AspireException {

    for (String childObjectName : aspireCollectionContent.keySet()) {
      // look what type of object is the content associated with the object name
      Object content = aspireCollectionContent.get(childObjectName);
      AspireObject childObject = null;
      if (content instanceof AspireObjectCreator) {
        verifyIsNotAttribute(childObjectName, "AspireObjectCreator");
        childObject = createAspireObjectFromContent(childObjectName,
            (AspireObjectCreator) content, action);
      } else if (content instanceof List<?>) {
        verifyIsNotAttribute(childObjectName, "List");
        populateAspireObjectFromList(aspireObject, childObjectName,
            (List<?>) content, action);
      } else if (content instanceof Map<?, ?>) {
        @SuppressWarnings("unchecked")
        Map<String, Object> mapContent = (Map<String, Object>) content;
        verifyIsNotAttribute(childObjectName, "AspireObjectCreator");
        AspireObject mapOject = new AspireObject(childObjectName);
        addAspireMapToParent(mapContent, action, mapOject);
        aspireObject.add(mapOject);
      } else {
        if (childObjectName.startsWith("@")) {
          if (content == null) {
            content = "";
          }
          aspireObject.add(childObjectName, content.toString());
        } else {
          aspireObject.add(childObjectName, content);
        }

      }
      if (childObject != null) {
        aspireObject.add(childObject);
      }
    }
  }

  private static void populateAspireObjectFromList(
      AspireObject parentAspireObject, String objectName,
      List<?> aspireCollectionContent, String action) throws AspireException {
    AspireObject childObject = null;
    List<AspireObject> children = new ArrayList<AspireObject>();
    for (Object content : aspireCollectionContent) {
      if (content instanceof AspireObjectCreator) {
        verifyIsNotAttribute(objectName, "AspireObjectCreator");
        childObject = createAspireObjectFromContent(objectName,
            (AspireObjectCreator) content, action);
      } else if (content instanceof Map<?, ?>) {
        @SuppressWarnings("unchecked")
        Map<String, Object> mapContent = (Map<String, Object>) content;
        verifyIsNotAttribute(objectName, "AspireObjectCreator");
        AspireObject mapOject = new AspireObject(objectName);
        addAspireMapToParent(mapContent, action, mapOject);
        childObject = mapOject;
      } else {
        childObject = new AspireObject(objectName, content);
      }
      children.add(childObject);
    }
    parentAspireObject.setPrepared(objectName, children);
  }

  private static void verifyIsNotAttribute(String name, String type) {
    if (name.startsWith("@")) {
      throw new OpaRuntimeException("The value for the attribute " + name
          + " cannot be a " + type + " instance. "
          + "Please set a value that has a well defined "
          + "toString method - e.g. a primitive wrapper");
    }
  }

  /**
   * *ValueObjectConstants interfaces include a number of fields (endingin _DB)
   * which represent column names in the database. This method names allow for
   * converting all those fields into a set of DB field names sorted
   * alphabetically
   * 
   * @param valueObjectConstantInterface
   *          ValueObjectConstant interfaces whose _DB field names are being
   *          retrieved through reflection
   * @return An order set of DB field names
   */
  public static SortedSet<String> getDBFieldsFromValueObjectConstants(
      Class<? extends CommonValueObjectConstants> valueObjectConstantInterface) {
    SortedSet<String> fieldNames = new TreeSet<String>();
    for (Field field : valueObjectConstantInterface.getDeclaredFields()) {
      String fieldName = field.getName();
      int suffixLength = DB_FIELD_NAME_SUFFIX.length();
      if (fieldName.endsWith(DB_FIELD_NAME_SUFFIX)) {
        fieldNames
            .add(fieldName.substring(0, fieldName.length() - suffixLength));
      }
    }
    return fieldNames;
  }

  /**
   * Creates insert statement by constructing the list of columns and the list
   * of parameterized names for the values
   * 
   * @param insertStatementStart
   *          The beginning of the insert statement. Typically something like
   *          "INSERT INTO
   *          <TABLE NAME>
   *          "
   * 
   * @param valueObjectConstantInterface
   *          The interface where the column names will be extracted from
   * @param ignoreFields
   *          A list of column names that are NOT to be included in the insert
   *          statement (e.g. autogenerated keys)
   * @return
   */
  public static String createInsertStatement(String insertStatementStart,
      Class<? extends CommonValueObjectConstants> valueObjectConstantInterface,
      List<String> ignoreFields) {
    String returnValue = insertStatementStart
        + createColumnNamesList(valueObjectConstantInterface, ignoreFields);

    if (ignoreFields == null) {
      ignoreFields = new ArrayList<String>();
    }
    SortedSet<String> fieldNames = getDBFieldsFromValueObjectConstants(valueObjectConstantInterface);
    returnValue = returnValue + " VALUES (";
    String valueNames = "";
    for (String fieldName : fieldNames) {
      if (ignoreFields.contains(fieldName)) {
        continue;
      }
      valueNames = valueNames + ", :" + fieldName;
    }
    returnValue = returnValue + valueNames.substring(2) + ")";
    return returnValue;
  }

  public static String createColumnNamesList(
      Class<? extends CommonValueObjectConstants> valueObjectConstantInterface,
      List<String> ignoreFields) {
    if (ignoreFields == null) {
      ignoreFields = new ArrayList<String>();
    }
    SortedSet<String> fieldNames = getDBFieldsFromValueObjectConstants(valueObjectConstantInterface);
    if (fieldNames.size() == 0) {
      throw new OpaRuntimeException(
          "No DB Constants fields found in this interface :"
              + valueObjectConstantInterface.getName());
    }
    StringBuffer columnNames = new StringBuffer("");
    for (String fieldName : fieldNames) {
      if (ignoreFields.contains(fieldName)) {
        continue;
      }
      columnNames = columnNames.append(", " + fieldName);
    }
    return " (" + columnNames.toString().substring(2) + ") ";
  }

  public static String createColumnNamesList(
      Class<? extends CommonValueObjectConstants> valueObjectConstantInterface) {
    return createColumnNamesList(valueObjectConstantInterface, null);
  }

  /**
   * Creates update statement by constructing the list of columns and the list
   * of parameterized names for the values. This UPDATE statement needs to be
   * appended with a WHERE clause by the caller using this function
   * 
   * @param UPDATE
   *          StatementStart The beginning of the insert statement. Typically
   *          something like "UPDATE
   *          <TABLE NAME>
   *          "
   * 
   * @param valueObjectConstantInterface
   *          The interface where the column names will be extracted from
   * @param ignoreFields
   *          A list of column names that are NOT to be included in the update
   *          statement (e.g. autogenerated keys)
   * @return
   */
  public static String createUpdateStatement(String updateStatementStart,
      Class<? extends CommonValueObjectConstants> valueObjectConstantInterface,
      List<String> ignoreFields) {
    StringBuffer returnValue = new StringBuffer(updateStatementStart + " SET");

    if (ignoreFields == null) {
      ignoreFields = new ArrayList<String>();
    }

    SortedSet<String> fieldNames = getDBFieldsFromValueObjectConstants(valueObjectConstantInterface);
    if (fieldNames.size() == 0) {
      throw new OpaRuntimeException(
          "No DB Constants fields found in this interface :"
              + valueObjectConstantInterface.getName());
    }

    for (String fieldName : fieldNames) {
      if (ignoreFields.contains(fieldName)) {
        continue;
      }
      returnValue = returnValue.append(" " + fieldName + "=:" + fieldName
          + ", ");
    }
    return returnValue.toString().substring(0,
        returnValue.toString().length() - 2)
        + " ";
  }

}
