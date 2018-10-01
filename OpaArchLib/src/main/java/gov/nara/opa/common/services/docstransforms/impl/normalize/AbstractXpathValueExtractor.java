package gov.nara.opa.common.services.docstransforms.impl.normalize;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.common.services.SingletonServices;
import net.sf.saxon.s9api.*;

public abstract class AbstractXpathValueExtractor extends
    AbstractValueExtractor {

  protected XdmItem evaluateSingle(XPathExecutable xpathExecutable,
                                   XdmNode compiledOpaXml) {
    XPathSelector selector = xpathExecutable.load();
    try {
      selector.setContextItem(compiledOpaXml);
      return selector.evaluateSingle();
    } catch (SaxonApiException e) {
      throw new OpaRuntimeException(e);
    }
  }

  protected XdmNode evaluate(XPathExecutable xpathExecutable,
      XdmNode compiledOpaXml) {
    XPathSelector selector = xpathExecutable.load();
    try {
      selector.setContextItem(compiledOpaXml);
      return (XdmNode) selector.evaluate();
    } catch (SaxonApiException e) {
      throw new OpaRuntimeException(e);
    }
  }

  protected XPathExecutable createXPathExecutable(String xpathQuery) {
    XPathCompiler compiler = SingletonServices.SAXON_PROCESSOR
        .newXPathCompiler();
    try {
      return compiler.compile(xpathQuery);
    } catch (SaxonApiException e) {
      throw new OpaRuntimeException(e);
    }
  }
}
