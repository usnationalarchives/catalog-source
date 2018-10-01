package gov.nara.opa.common.services.docstransforms.impl.normalize.valueextractor;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.common.services.docstransforms.impl.normalize.AbstractXslValueExtractor;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.XdmNode;

public class ListXslValueExtractor extends AbstractXslValueExtractor {

  public ListXslValueExtractor(String xslFileName) {
    super(xslFileName);
  }

  @Override
  protected Object getValue(String valueGenerationInstruction,
      SearchRecordValueObject document, String type,
      AccountExportValueObject accountExport, String section) {

    return getValue(document.getCompiledOpaXml(), type);
  }

  public Object getValue(XdmNode compiledOpaXml, String type) {

    try {

      XdmNode node = transform(compiledOpaXml, getXsltExecutable());
      if (node == null) {
        return null;
      }

      @SuppressWarnings("resource")
      AspireObject obj = new AspireObject("root");
      String xml = node.toString();
      if (xml == null || xml.trim().equals("")) {
        return null;
      }
      obj.loadXML(new StringReader(xml));
      AspireObject rootXml = obj.getChildren().get(0);
      String listType = rootXml.getName();

      if (!listType.equals(type)) {
        throw new OpaRuntimeException(
            String
                .format(
                    "The expected value of the field type from the field definition "
                        + "(%1$s) does not match the type coming back from the xsl (%2$s)",
                    type, listType));
      }

      return createTypedList(rootXml.getChildren(), listType);
    } catch (AspireException e) {
      throw new OpaRuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  protected static Object createTypedList(List<AspireObject> values,
      String listType) {
    if (values == null || values.size() == 0) {
      return null;
    }

    String type = null;
    @SuppressWarnings("rawtypes")
    List returnValues = null;
    if (listType.equals("stringList")) {
      returnValues = new ArrayList<String>();
      type = "string";
    } else if (listType.equals("integerList")) {
      returnValues = new ArrayList<String>();
      type = "integer";
    } else if (listType.equals("booleanList")) {
      returnValues = new ArrayList<String>();
      type = "boolean";
    } else {
      throw new OpaRuntimeException("Invalid list type: " + listType);
    }
    for (AspireObject value : values) {
      Object content = value.getContent();
      if (content == null) {
        if (type.equals("string")) {
          content = "";
        } else if (type.equals("integer")) {
          content = 0;
        } else if (type.equals("boolean")) {
          content = false;
        }
      }
      returnValues.add(getCastedObjectValue(content, type));
    }
    // remove the last item in the list if it is null;
    int valuesSize = returnValues.size() - 1;
    Object lastObject = returnValues.get(valuesSize);
    if (lastObject == null
        || (lastObject instanceof String && ((String) lastObject).trim()
            .equals(""))) {
      returnValues.remove(valuesSize);
    }
    return returnValues;
  }
}
