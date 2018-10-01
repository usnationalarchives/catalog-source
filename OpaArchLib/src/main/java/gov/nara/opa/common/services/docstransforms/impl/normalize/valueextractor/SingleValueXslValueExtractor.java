package gov.nara.opa.common.services.docstransforms.impl.normalize.valueextractor;

import gov.nara.opa.architecture.exception.OpaSkipRecordException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.services.docstransforms.impl.normalize.AbstractXslValueExtractor;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;

import java.io.StringReader;

import net.sf.saxon.s9api.XdmNode;

public class SingleValueXslValueExtractor extends AbstractXslValueExtractor {

  OpaLogger logger = OpaLogger.getLogger(AbstractXslValueExtractor.class);

  public SingleValueXslValueExtractor(String xslFileName) {
    super(xslFileName);
  }

  @Override
  protected Object getValue(String valueGenerationInstruction,
      SearchRecordValueObject document, String type,
      AccountExportValueObject accountExport, String section) {
    // TODO Auto-generated method stub
    return getValue(document.getCompiledOpaXml(), type);
  }

  public Object getValue(XdmNode compiledOpaXml, String type) {
    String xml = null;
    try {
      XdmNode node = transform(compiledOpaXml, getXsltExecutable());
      if (node == null) {
        return null;
      }

      @SuppressWarnings("resource")
      AspireObject obj = new AspireObject("root");
      xml = node.toString();
      if (xml == null || xml.trim().equals("")) {
        return null;
      }
      obj.loadXML(new StringReader(xml));
      Object value = obj.getChildren().get(0).getContent();
      return getCastedObjectValue(value, type);
    } catch (AspireException e) {
      logger.error(e);
      logger.error("Error with message " + e.getMessage()
          + " while processing this xml: \n" + xml);
      throw new OpaSkipRecordException(e);
    }
  }
}
