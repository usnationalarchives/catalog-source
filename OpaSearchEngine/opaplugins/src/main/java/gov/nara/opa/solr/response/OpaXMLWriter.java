package gov.nara.opa.solr.response;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.XML;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.ResultContext;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.XMLWriter;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrIndexSearcher;

import com.searchtechnologies.qpl.solr.schema.XmlField;


public class OpaXMLWriter extends XMLWriter {

	private static final char[] XML_START1="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".toCharArray();
	private static final char[] XML_STYLESHEET="<?xml-stylesheet type=\"text/xsl\" href=\"".toCharArray();
	private static final char[] XML_STYLESHEET_END="\"?>\n".toCharArray();

	//private static final char[] XML_START2_NOSCHEMA=("<opa-response>\n").toCharArray();
	//Set<String> xmlFieldNames = null;
	boolean bEncodeXml = true;
	boolean bXmlArray = false;
	Set<String> xmlFieldNames = null;

	public OpaXMLWriter(Writer writer, SolrQueryRequest req, SolrQueryResponse rsp, Set<String> xmlFieldNames) {
		super(writer, req, rsp);
		if (xmlFieldNames == null) {
			if (req != null && req.getSchema() != null && req.getSchema().getFields() != null) {
				Set<String> schemaXmlFieldNames = new HashSet<String>();
				Map<String, SchemaField> fieldMap = req.getSchema().getFields();
				for (String key : fieldMap.keySet()) {
					SchemaField field = fieldMap.get(key);
					FieldType fieldType = field.getType();
					if (fieldType instanceof XmlField) {
						schemaXmlFieldNames.add(field.getName());
					}
				}
				this.xmlFieldNames = schemaXmlFieldNames;
			}
		} else {
			this.xmlFieldNames = xmlFieldNames;
		}
		//this.xmlFieldNames = xmlFieldNames;
		Boolean bTemp = req.getParams().getBool("opaXmlEncode");
		if (bTemp != null) bEncodeXml = bTemp.booleanValue();
	}

	@Override
	public void writeStr(String name, String val, boolean escape) throws IOException {
		if (bXmlArray && name != null) {
			bXmlArray = false;
		}
		if (bXmlArray || xmlFieldNames.contains(name)) {
			val = val.replaceAll("\\{.*?\\}", "");
			val = val.replaceAll("%7[bB]", "{");
			val = val.replaceAll("%7[dD]", "}");
			escape = bEncodeXml;
		}
		super.writeStr(name,val,escape);
	}

	@Override
	public void writeArray(String name, Object[] val) throws IOException {
		// TODO Auto-generated method stub
		super.writeArray(name, val);
	}

	@Override
	public void writeArray(String name, Iterator iter) throws IOException {
		if (xmlFieldNames.contains(name)) {
			bXmlArray = true;
		}
		super.writeArray(name, iter);
	}

	//@Override
	public void writeResponse_AAAAAAAAAA() throws IOException {


		//writer.write(XML_START1);

		String stylesheet = req.getParams().get("stylesheet");
		stylesheet = null;
		if (stylesheet != null && stylesheet.length() > 0) {
			writer.write(XML_STYLESHEET);
			XML.escapeAttributeValue(stylesheet, writer);
			writer.write(XML_STYLESHEET_END);
		}

		/*
	    String noSchema = req.getParams().get("noSchema");
	    // todo - change when schema becomes available?
	    if (false && noSchema == null)
	      writer.write(XML_START2_SCHEMA);
	    else
	      writer.write(XML_START2_NOSCHEMA);
		 ***/
		writer.write("\n<opa-response>\n");	

		// dump response values
		NamedList<?> lst = rsp.getValues();
		Boolean omitHeader = req.getParams().getBool(CommonParams.OMIT_HEADER);
		if(omitHeader != null && omitHeader) lst.remove("responseHeader");

		SolrIndexSearcher searcher = req.getSearcher();


		int resultSize = lst.size();
		int start=0;

		for (int i=start; i<resultSize; i++) {
			if (lst.getName(i).equals("responseHeader")) {
				continue;
			}
			if (lst.getName(i).equals("response")) {
				ResultContext res = (ResultContext) lst.getVal(i);
				DocList ids = res.docs;
				int sz = ids.size();
				Set<String> fnames = new HashSet<String>();//fields.getLuceneFieldNames();
				fnames.add("description");
				fnames.add("authority");
				fnames.add("objects");
				for (int iii=0; iii<sz; iii++) {
					int id = ids.iterator().nextDoc();
					Document doc = searcher.doc(id, fnames);
					writer.write("<doc " + "naId=\"" + doc.getField("naId").stringValue() + "\">\n");

					for (String fname : fnames) {
						IndexableField f = doc.getField(fname);
						if (f != null) {
							//			    		  writer.write("name: " + f.name() + " ");
							//			    		  writer.write("type: " + f.fieldType() + " ");
							//			    		  writer.write("stringValue: " + f.stringValue() + " ");
							writer.write("<" + fname + ">\n");
							writer.write(f.stringValue());
							writer.write("</" + fname + ">\n");
						}
					}
					writer.write("</doc>");
					//writer.write(doc.get("naid"));
				}
				continue;
			}
			//FKAwriteVal(lst.getName(i),lst.getVal(i));
		}

		writer.write("\n</opa-response>\n");	
	}
}
