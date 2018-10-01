package gov.nara.opa.common.services.docstransforms.impl.normalize.valueextractor;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.services.docstransforms.impl.normalize.AbstractXslValueExtractor;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.ValueHolderValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import net.sf.saxon.s9api.XdmNode;

public class FieldsListXslValueExtractor extends AbstractXslValueExtractor {

  private static OpaLogger logger = OpaLogger.getLogger(FieldsListXslValueExtractor.class);
  
  public FieldsListXslValueExtractor(String xslFileName) {
    super(xslFileName);
  }

  @Override
  protected Object getValue(String valueGenerationInstruction,
      SearchRecordValueObject document, String type,
      AccountExportValueObject accountExport, String section) {

    return getValue(document.getCompiledOpaXml(), type, section);
  }

  public Object getValue(XdmNode compiledOpaXml, String type, String section) {

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
      return createTypedList(rootXml.getChildren(), type, section);
    } catch (AspireException e) {
      throw new OpaRuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  private LinkedList<ValueHolderValueObject> createTypedList(
      List<AspireObject> values, String type, String section)
      throws AspireException {

    @SuppressWarnings("rawtypes")
    LinkedList returnValues = new LinkedList<ValueHolderValueObject>();

    for (AspireObject value : values) {
      ValueHolderValueObject valueObject = new ValueHolderValueObject();
      AspireObject nameAspire = value.get("name");
      Object label = value.get("label").getContent();
      Object name = null;
      if (nameAspire == null) {
        if(label != null) {
          name = ((String) label).replaceAll("\\s*", "");
        } else {
          logger.info(String.format("Label is null: %1$s", value.toString()));
          name = "";
        }
      } else {
        name = nameAspire.getContent();
      }
      valueObject.setName(scrubFieldName((String) name));
      valueObject.setLabel((String) label);
      valueObject.setSection(section);
      AspireObject o = value.get("value");
      if (o.get("stringList") != null) {
        Object v = ListXslValueExtractor.createTypedList(o.get("stringList")
            .getChildren(), "stringList");
        valueObject.setValue(v);
      } else {
        valueObject.setValue(getCastedObjectValue(o.getContent(), type));
      }

      returnValues.add(valueObject);
    }

    if (returnValues.size() == 0) {
      return null;
    }
    return returnValues;
  }

  private String scrubFieldName(String in) {
    return in.replace("/", "_");
  }
}
