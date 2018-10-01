package gov.nara.opa.solr.analysis;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.TrieDateField;

public class RoundingTrieDateField extends TrieDateField {

	protected boolean bRoundDown = true;
	
	protected void init(IndexSchema schema, Map<String, String> args) {
		super.init(schema, args);
		String arg = args.get("roundDown");
		if ("false".equals(arg)) {
			bRoundDown = false;
		}
	}

	@Override
	public IndexableField createField(SchemaField field, Object value,
			float boost) {
		// TODO Auto-generated method stub
		String newValue = null;
		if (value instanceof String) {
			RoundingCalendar rc = new RoundingCalendar((String)value, bRoundDown);
			newValue = rc.utcString();
		}
		if (newValue != null) {
			return super.createField(field, newValue, boost);
		}
		return super.createField(field, value, boost);
	}

	@Override
	public List<IndexableField> createFields(SchemaField field, Object value,
			float boost) {
		return Collections.singletonList(createField(field, value, boost));
	}

	@Override
	public boolean isTokenized() {
		return (properties & TOKENIZED)!=0;
	}

	@Override
	protected IndexableField createField(String name, String val,
			FieldType type, float boost) {
		// TODO Auto-generated method stub
		return super.createField(name, val, type, boost);
	}

	//	@Override
	//	public void setAnalyzer(Analyzer analyzer) {
	//		this.analyzer = analyzer;
	//	}
	//
	//	@Override
	//	public void setQueryAnalyzer(Analyzer analyzer) {
	//		this.queryAnalyzer = analyzer;
	//	}

}
