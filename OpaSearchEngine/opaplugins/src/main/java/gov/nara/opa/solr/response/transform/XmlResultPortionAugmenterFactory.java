package gov.nara.opa.solr.response.transform;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.transform.DocTransformer;
import org.apache.solr.response.transform.TransformerFactory;

public class XmlResultPortionAugmenterFactory extends TransformerFactory {


	protected String xmlFieldNames = null;
	protected Boolean isFullPath = false;

	@Override
	public void init(NamedList args) {
		xmlFieldNames = (String)args.get( "xmlFieldNames" );
		isFullPath = (Boolean)args.get("isFullPath");
		if (isFullPath == null) isFullPath = false;
	}

	@Override
	public DocTransformer create(String field, SolrParams params, SolrQueryRequest req) {
		return new XmlResultPortionAugmenter(xmlFieldNames, isFullPath, params, req);
	}

}
