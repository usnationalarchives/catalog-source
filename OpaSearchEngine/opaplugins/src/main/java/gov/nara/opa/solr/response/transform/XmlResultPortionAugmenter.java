package gov.nara.opa.solr.response.transform;

import gov.nara.opa.solr.results.XmlResultPortion;
import gov.nara.opa.solr.results.XmlResultPortionSaxHandler;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.LazyDocument.LazyField;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.transform.TransformerWithContext;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlResultPortionAugmenter extends TransformerWithContext {

	String xmlFieldNamesStr;
	Set<String> xmlFieldNames;
	SolrParams solrParams;
	SolrQueryRequest req;
	SAXParserFactory factory;
	SAXParser saxParser;
	boolean isFullPath;

	public XmlResultPortionAugmenter(String xmlFieldNamesStr, boolean isFullPath, SolrParams solrParams, 
			SolrQueryRequest req)
	{
		this.xmlFieldNamesStr = xmlFieldNamesStr;
		this.isFullPath = isFullPath;
		this.solrParams = solrParams;
		this.req = req;
		String[] xmlFieldNamesArr = xmlFieldNamesStr.split(",");
		xmlFieldNames = new HashSet<String>(Arrays.asList(xmlFieldNamesArr));
		factory = SAXParserFactory.newInstance();
		try {
			saxParser = factory.newSAXParser();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return "xmlPortions";
	}

	private String getPortion(String fieldXmlValue, List<XmlResultPortion> xmlPortions) throws SAXException, IOException {
		Writer writer = new StringWriter();
		XmlResultPortionSaxHandler handler = new XmlResultPortionSaxHandler(xmlPortions, writer, isFullPath);
		handler.setDebug(false);
		saxParser.parse(new InputSource(new StringReader(fieldXmlValue)), handler);
		return writer.toString();
	}

	@Override
	public void transform(SolrDocument doc, int docid) throws IOException {
		String resultFields = context.req.getParams().get("resultFields");
		Map<String, List<XmlResultPortion>> xmlPortions = XmlResultPortion.parseFieldListString(resultFields, xmlFieldNames);
		if (xmlPortions == null) return;
		for (String key : xmlPortions.keySet()) {
			Object fieldXmlValueObj = doc.get(key);
			if (fieldXmlValueObj != null) {
				String fieldXmlValue = null;
				if (fieldXmlValueObj instanceof String) {
					fieldXmlValue = (String)fieldXmlValueObj;
				}
				else if (fieldXmlValueObj instanceof Field) {
					Field xmlField = (Field)fieldXmlValueObj;
					fieldXmlValue = xmlField.stringValue();
				} else if (fieldXmlValueObj instanceof LazyField) {
					LazyField xmlField = (LazyField)fieldXmlValueObj;
					fieldXmlValue = xmlField.stringValue();
				}
				try {
					String xmlPortion = getPortion(fieldXmlValue, xmlPortions.get(key));
					doc.setField(key,  xmlPortion);
				} catch (SAXException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
