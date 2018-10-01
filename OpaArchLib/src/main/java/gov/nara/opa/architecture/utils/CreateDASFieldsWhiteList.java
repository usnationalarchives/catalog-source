package gov.nara.opa.architecture.utils;

import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.services.docstransforms.Constants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;

public class CreateDASFieldsWhiteList implements Constants {

  static OpaLogger logger = OpaLogger.getLogger(CreateDASFieldsWhiteList.class);

  static Map<String, DASField> dasFields = new ConcurrentHashMap<String, DASField>();
  static List<String> simpleTypes = new ArrayList<String>();

  private static final List<String> DESCRIPTIONS = new ArrayList<String>();
  static {
    DESCRIPTIONS.add(RESULT_TYPE_RECORD_GROUP);
    DESCRIPTIONS.add(RESULT_TYPE_COLLECTION);
    DESCRIPTIONS.add(RESULT_TYPE_SERIES);
    DESCRIPTIONS.add(RESULT_TYPE_FILE_UNIT);
    DESCRIPTIONS.add(RESULT_TYPE_ITEM);
    DESCRIPTIONS.add(RESULT_TYPE_ITEM_AV);
    DESCRIPTIONS.add(RESULT_TYPE_OBJECT);
  }

  private static final List<String> AUTHORITIES = new ArrayList<String>();
  static {
    AUTHORITIES.add(RESULT_TYPE_PERSON);
    AUTHORITIES.add(RESULT_TYPE_ORGANIZATION);
    AUTHORITIES.add(RESULT_TYPE_GEOGRAPHIC_REFERENCE);
    AUTHORITIES.add("topicalSubject");
    AUTHORITIES.add("specificRecordsType");
  }

  public static void createWhiteList(String nasDasFileLocation,
      String whiteListLocation) throws Exception {

    AspireObject schemaAo = getSchemaAsAspireObject(nasDasFileLocation);
    getAllDasFields(schemaAo.get("xs:schema"));
    processDasFields(dasFields);
    populateFieldsList(whiteListLocation);
  }

  private static void populateFieldsList(String whiteListLocation)
      throws IOException {
    File fout = new File(whiteListLocation);
    fout.createNewFile();
    FileOutputStream fos = new FileOutputStream(fout);

    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
    for (String name : dasFields.keySet()) {
      DASField dasField = dasFields.get(name);
      String sorlFieldName = getSolrFieldName(name);
      if (sorlFieldName == null || dasField.getChildFields(name) == null) {
        continue;
      }
      for (String childName : dasField.getChildFields(name)) {
        String dasFieldName = sorlFieldName + childName;
        logger.debug(dasFieldName);
        bw.write(dasFieldName);
        bw.newLine();
      }
    }
    bw.close();
  }

  private static String getSolrFieldName(String fieldName) {
    if (DESCRIPTIONS.contains(fieldName)) {
      return "description.";
    } else if (AUTHORITIES.contains(fieldName)) {
      return "authority.";
    }
    return null;
  }

  private static void processDasFields(Map<String, DASField> dasFields) {
    for (String name : dasFields.keySet()) {
      if (!DESCRIPTIONS.contains(name) && !AUTHORITIES.contains(name)) {
        continue;
      }

      DASField dasField = dasFields.get(name);
      if (dasField.getChildFields(name) == null) {
        String lookupName = null;
        if (dasField.getContent().getAttribute("type") != null) {
          lookupName = dasField.getContent().getAttribute("type").substring(4);
        }

        String childElementName = null;
        if (!name.equals(lookupName)) {
          childElementName = name;
        }
        processField(dasField, new ArrayList<String>(), childElementName);
        System.out.println("PROCESSED FIELD TYPE: " + name);
        // clearData(dasFields);
      }
    }
  }

  private static void processField(DASField field,
      List<String> fieldsInTheHierarchyProcessed, String elementName) {
    // List<String> childFields = field.getChildFields();
    Set<String> childFields = new LinkedHashSet<String>();
    String name = field.getName();
    if (elementName != null) {
      name = elementName;
    }
    fieldsInTheHierarchyProcessed.add(name);

    for (AspireObject element : getDasChildren(field.getContent())) {
      String childName = element.getAttribute("name");
      logger.trace("Processing parent " + name + " child: " + childName);
      if (element.getAttribute("type") == null) {
        logger.debug("null type for element: " + name + " child: " + childName);
      }
      if (element.getAttribute("type").startsWith("das")) {
        String lookupName = element.getAttribute("type").substring(4);
        String childElementName = null;
        if (!childName.equals(lookupName)) {
          childElementName = childName;
        }
        String printOutName = childElementName != null ? childElementName
            : childName;
        // System.out.println("cccc: " + printOutName);
        logger.debug("cccc: " + printOutName);
        if (simpleTypes.contains(lookupName)) {
          childFields.add(childName);
          childFields.add(name + "." + childName);
        } else {
          DASField childDasField = dasFields.get(lookupName);
          if (childDasField == null) {
            logger
                .error("We have a problem: no complex type found in xsd for this field: "
                    + lookupName);
          }
          if (childDasField.getChildFields(childElementName) == null
              && !fieldsInTheHierarchyProcessed.contains(childDasField
                  .getName())) {
            processField(childDasField, fieldsInTheHierarchyProcessed,
                childElementName);
          }
          Set<String> childFieldsForChildDasField = childDasField
              .getChildFields(childElementName);
          if (childFieldsForChildDasField != null) {
            for (String childFieldForChildDasField : childFieldsForChildDasField) {
              if (childFieldForChildDasField.startsWith(childName)) {
                String tmp = name + "." + childFieldForChildDasField;
                childFields.add(tmp);
                if ("fileUnitArray".equals(childName)) {
                  // System.out.println(tmp);
                }
              }
            }
          }
        }
      } else {
        childFields.add(name);
        childFields.add(name + "." + childName);
      }
      fieldsInTheHierarchyProcessed.add(childName);
    }
    field.setChildFields(childFields, elementName);
  }

  private static List<AspireObject> getDasChildren(AspireObject parent) {
    List<AspireObject> children = new ArrayList<AspireObject>();
    for (AspireObject child : parent.getChildren()) {
      if (child.getName().equals("xs:choice")) {
        children.addAll(child.getChildren());
      } else {
        children.add(child);
      }

    }
    return children;
  }

  private static Map<String, DASField> getAllDasFields(
      AspireObject schemaAspireObject) throws AspireException {

    for (AspireObject field : schemaAspireObject.getChildren()) {
      if (field.getName().equals("xs:complexType")) {
        DASField dasField = new DASField();
        dasField.setName(field.getAttribute("name"));
        if (field.get("xs:all") != null) {
          dasField.setContent(field.get("xs:all"));
        } else if (field.get("xs:sequence") != null) {
          dasField.setContent(field.get("xs:sequence"));
        }
        dasFields.put(field.getAttribute("name"), dasField);
      } else if (field.getName().equals("xs:simpleType")) {
        simpleTypes.add(field.getAttribute("name"));
      }
    }

    return dasFields;
  }

  private static AspireObject getSchemaAsAspireObject(String nasDasFileLocation)
      throws FileNotFoundException, AspireException {
    AspireObject aspireObject = new AspireObject("root");
    aspireObject.loadXML(new FileReader(new File(nasDasFileLocation)));
    return aspireObject;
  }

}
