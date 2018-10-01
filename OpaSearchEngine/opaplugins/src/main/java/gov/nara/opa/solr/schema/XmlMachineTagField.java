package gov.nara.opa.solr.schema;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;

import com.searchtechnologies.qpl.solr.schema.XmlField;

public class XmlMachineTagField extends XmlField {
	
	protected void init(IndexSchema schema, Map<String, String> args) {
		super.init(schema, args);
	}

	
//	Query Operator	Description	Examples
//	{namespace}:{field}:{expr}	Search for a query expression within a specific field and namespace.
//	Note that any query expression from section 3.5.1 can be put inside of {expr}.
//	nara:title:"president lincoln"
//	________________________________________nara:title:(president or congressman)
	
//	{namespace}:{field}:*	Search for anything which has the specified namespace and field in a tag.	nara:title:*
//	________________________________________tc:date:*
	
//	*:{field}:{expr}	Search for a query expression within a specific field for any namespace.
//	Note that any query expression from section 3.5.1 can be put inside of {expr}.
//	title:"president lincoln"
//	________________________________________title:(president or congressman)
	
//	*:{field}:*	Search for any machine tag with the specified field in any namespace.	*:title:*
//	________________________________________*:date:*
	
//	{namespace}:*:{expr}	Search for a query expression within a specific namespace across all fields.
//	Note that any query expression from section 3.5.1 can be put inside of {expr}.
//	nara:*:"president lincoln"
//	________________________________________nara:*:(president or congressman)
	
//	{namespace}:*:*	Search for any instance of the specified namespace across all fields.	nara:*:*
//	________________________________________tc:*:*
	
//	{expr}	Search for a query expression within any machine tag.
//	Note that any query expression from section 3.5.1 can be put inside of {expr}.
//	"president lincoln"
//	________________________________________(president or congressman)

	
	public static String tagToXml(String tag) {
		if (tag == null) return null;
		String prefixNamespace = "ns___";
		String prefixField = "fld___";
		String[] nameValue = tag.split("=", 2);
		StringBuilder sb = null;
		if (nameValue != null && nameValue.length == 2) {
			String[] namespaceName = nameValue[0].split(":", 2);
			if (namespaceName != null && namespaceName.length >= 1) {
				sb = new StringBuilder();
				if (namespaceName.length > 1) {
					sb.append("<").append(prefixNamespace).append(namespaceName[0]).append(">");
					sb.append("<").append(prefixField).append(namespaceName[1]).append(">");
				} else {
					sb.append("<").append(prefixField).append(namespaceName[0]).append(">");
				}
				sb.append(nameValue[1]);
				if (namespaceName.length > 1) {
					sb.append("</").append(prefixField).append(namespaceName[1]).append(">");
					sb.append("</").append(prefixNamespace).append(namespaceName[0]).append(">");
				} else {
					sb.append("</").append(prefixField).append(namespaceName[0]).append(">");
				}
			}
		}
		return sb == null ? null : sb.toString();
	}

	@Override
	public IndexableField createField(SchemaField field, Object value, float boost) {
		String newValue = null;
		if (value instanceof String) {
			String foo = tagToXml((String)value);
			newValue = foo;
		}
		if (newValue != null) {
			return super.createField(field, newValue, boost);
		}
		return super.createField(field, value, boost);
	}

	@Override
	public List<IndexableField> createFields(SchemaField field, Object value, float boost) {
		return Collections.singletonList(createField(field, value, boost));
	}

	@Override
	public boolean isTokenized() {
		return (properties & TOKENIZED)!=0;
	}

	@Override
	protected IndexableField createField(String name, String val, FieldType type, float boost) {
		return super.createField(name, val, type, boost);
	}

}
