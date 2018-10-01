/**
 * 
 */
package gov.nara.opa.api.utils;

import java.io.FileOutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Holds an XML Transformer, a compiled representation of an XSLT transform,
 * which can be used to transform XML DOMs into other XML DOMs. Typically used
 * by PPackage to transform the raw.xml into the output.xml, but can also be
 * used by any parser which needs to parse XML Documents.
 * <P>
 * Use PParser.getTransformFromPool() to fetch an instance of the transform that
 * you need and then use PTransformer.returnToPool() to return the transformer
 * back to the pool so it can be used by other threads without having to
 * recompile it each time.
 * <P>
 */
public class PTransformer {
  Transformer myTransformer;

  public PTransformer(Transformer transform) {
    this.myTransformer = transform;
  }

  public void returnToPool() {
    myTransformer.clearParameters();
  }

  /**
   * Transform a DOM Node into another DOM Node using an XSLT transformer.
   * 
   * @param source
   *          The source node(s).
   * @return The resulting node(s).
   * @throws TransformerException
   */
  public Element transform(Node source) throws TransformerException {
    DOMResult outputDR = new DOMResult();
    myTransformer.transform(new DOMSource(source), outputDR);
    return ((Document) outputDR.getNode()).getDocumentElement();
  }

  /**
   * Transform a DOM Node into another DOM Node using an XSLT transformer.
   * 
   * @param source
   *          The source node(s).
   * @return The resulting node(s).
   * @throws TransformerException
   */
  public void transform(Node source, String outputFileName) throws Exception {
    FileOutputStream outFS = new FileOutputStream(outputFileName);
    Result result = new StreamResult(outFS);
    myTransformer.transform(new DOMSource(source), result);
    outFS.close();
  }

  /**
   * Transform a DOM Node into another DOM Node using an XSLT transformer.
   * 
   * @param inputFileName
   *          The input XML file to transform.
   * @param outputFileName
   *          The output XML file where to store the transform.
   * @throws Exception
   */
  public String transform(String input) throws Exception {
    StringWriter outWriter = new StringWriter();
    StringReader reader = new StringReader(input);
    Result result = new StreamResult(outWriter);
    myTransformer.transform(
        new javax.xml.transform.stream.StreamSource(reader), result);
    return outWriter.toString();
  }

  public void setParam(String paramName, String paramValue) {
    myTransformer.setParameter(paramName, paramValue);
  }
}
