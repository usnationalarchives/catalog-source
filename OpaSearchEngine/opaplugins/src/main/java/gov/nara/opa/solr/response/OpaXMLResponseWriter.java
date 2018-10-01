package gov.nara.opa.solr.response;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.XMLResponseWriter;
import org.apache.solr.response.XMLWriter;

public class OpaXMLResponseWriter extends XMLResponseWriter {

	Set<String> xmlFieldNames = null;

	@Override
	public void init(NamedList n) {
		super.init(n);
		List<String> xmlFields = n.getAll("xmlField");
		if (xmlFields != null && xmlFields.size() > 0) {
			xmlFieldNames = new HashSet<String>();
			for (String xmlField : xmlFields) {
				xmlFieldNames.add(xmlField);
			}
		}
	}

	/**
<opa-response>
	<header status="400" time="8">
		<request path="/iapi/v1">
			<action>search</action>
			<q>truman</q>
			<rows>10000</rows>
		</request>
	</header>
    <errors>
  	  <error code="ROW_LIMIT_EXCEEDED">
		<request>10000</request>
		<max>200</max>
	  </error>
    </errors>

	 */
	@Override
	public void write(Writer writer, SolrQueryRequest req, SolrQueryResponse rsp)
			throws IOException {
		OpaXMLWriter w = new OpaXMLWriter(writer, req, rsp, xmlFieldNames);
		try {
			w.writeResponse();
		} finally {
			w.close();
		}
	}

}
