package gov.nara.opa.common.services.docstransforms.impl.normalize;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.services.SingletonServices;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;


public abstract class AbstractXslValueExtractor extends AbstractValueExtractor {

  public static String XSL_FILE_PATH;

  private XsltExecutable xsltExecutable;

  private String xslFileName;

  static OpaLogger logger = OpaLogger
      .getLogger(AbstractXslValueExtractor.class);

  public AbstractXslValueExtractor(String xslFileName) {
	
	logger.debug(String.format("xslFileName is %1$s, XSL_FILE_PATH is %2$s", xslFileName, XSL_FILE_PATH));
	  
    this.xslFileName = xslFileName;
    xsltExecutable = createXPathExecutable(XSL_FILE_PATH
        + xslFileName);

  }

  public static XsltExecutable createXPathExecutable(String xslFileName) {
    try {
      File getPathFile = new File(xslFileName);
      String pathToFile = getPathFile.getParent();
      InputStream is = getXslFileAsResource(xslFileName);
      XsltCompiler compiler = SingletonServices.SAXON_PROCESSOR
          .newXsltCompiler();
      StreamSource xslStream = new StreamSource(is);
      xslStream.setSystemId(pathToFile);
      return compiler.compile(xslStream);
    } catch (SaxonApiException | IOException e) {
      if (e instanceof FileNotFoundException) {
        if (XSL_FILE_PATH != null) {
    		logger.warn(
        		String.format("Cannot load and compile xsl file. Please verify your application.properties (%1$s). Error message: %2$s"
                , XSL_FILE_PATH, e.getMessage())
                );
    	}
        return null;
      } else {
        logger.error(String.format("XSL FILE PATH: %1$s", XSL_FILE_PATH));
    	throw new OpaRuntimeException(e);
      }

    }
  }

  public static InputStream getXslFileAsResource(String xslFilePath)
      throws IOException {

    File xslFile = new File(xslFilePath);
    return new FileInputStream(xslFile);

  }

  protected XsltExecutable getXsltExecutable() {

	if (xsltExecutable == null) {
	    xsltExecutable = createXPathExecutable(XSL_FILE_PATH
	           + xslFileName);
	}

    return xsltExecutable;
  }

  public static XdmNode transform(XdmNode compiledOpaXml,
      XsltExecutable xsltExecutable) {
	if (xsltExecutable == null) {
		logger.error(String.format("Can not find xslt executable in path: %1$s", XSL_FILE_PATH));
		return null;
	}
    XsltTransformer transformer = xsltExecutable.load();
    transformer.setInitialContextNode(compiledOpaXml);
    XdmDestination xdmDestination = new XdmDestination();
    transformer.setDestination(xdmDestination);

    try {
      transformer.transform();
    } catch (SaxonApiException e) {
      new OpaRuntimeException(e);
    }
    return xdmDestination.getXdmNode();
  }

}
