package gov.nara.opa.common.services.docstransforms.impl.normalize.valueextractor;

import gov.nara.opa.common.services.docstransforms.impl.normalize.AbstractXpathValueExtractor;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

public class SimpleXpathValueExtractor extends AbstractXpathValueExtractor {

  // XPathExpressionImpl xPathExpression;
  private String xpathQuery;
  private XPathExecutable xpathExecutable;

  public SimpleXpathValueExtractor(String xpathQuery) {
    this.xpathQuery = xpathQuery;
    xpathExecutable = createXPathExecutable(xpathQuery);
  }

  @Override
  protected Object getValue(String valueGenerationInstruction,
      SearchRecordValueObject document, String type,
      AccountExportValueObject accountExport, String section) {
    XdmNode compiledOpaXml = document.getCompiledOpaXml();
    return getValue(compiledOpaXml, type);
  }

  public Object getValue(XdmNode compiledOpaXml, String type) {
    XdmItem valueXml = evaluateSingle(xpathExecutable, compiledOpaXml);

    if (valueXml == null) {
      return null;
    }

    // The code below can be used for additional validations should there be too
    // many errors with parsing xPaths

    // XdmNode node = (XdmNode) valueXml;
    // XdmSequenceIterator iterator = node.axisIterator(Axis.CHILD);
    // if (iterator.hasNext()) {
    // throw new OpaRuntimeException(
    // "This node had children and is supposed to only contain a value. Xpath selector: "
    // + xpathQuery + " document: " + compiledOpaXml.toString());
    // }

    String value = valueXml.getStringValue();
    if (value != null && !value.isEmpty()) {
      return getCastedObjectValue(value, type);
    }
    return null;
  }
}
