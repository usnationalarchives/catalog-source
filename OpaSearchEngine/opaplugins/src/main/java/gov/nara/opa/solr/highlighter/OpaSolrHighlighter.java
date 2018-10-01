package gov.nara.opa.solr.highlighter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.highlight.DefaultSolrHighlighter;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.TextField;
import org.apache.solr.search.SolrIndexSearcher;

import com.searchtechnologies.qpl.solr.schema.XmlField;

/*
 * the only purpose of this class is to remove XML fields from the field list.
 * this is necessary if one wants to use "hl.fl=*"
 */
public class OpaSolrHighlighter extends DefaultSolrHighlighter {

	public OpaSolrHighlighter(SolrCore solrCore) {
		super(solrCore);
	}

	@Override
	public String[] getHighlightFields(Query query, SolrQueryRequest request, String[] defaultFields) {
		String[] fields = super.getHighlightFields(query, request, defaultFields);
		if (fields != null) {
			List<String> modifiedFieldList = new ArrayList<String>();
			for (int i = 0; i < fields.length; i++) {
				FieldType fieldType = request.getSchema().getFieldType(fields[i]);
				if (fieldType instanceof XmlField) {
					// skip this xml field
					int breakpoint = 0;
				} else {
					modifiedFieldList.add(fields[i]);
				}
			}
			fields = modifiedFieldList.toArray(new String[] {});
		}
		return fields;
	}
}
