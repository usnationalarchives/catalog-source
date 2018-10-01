package gov.nara.opa.solr.util;

import java.io.File;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Bits;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class IndexLister {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		boolean listFields = false;
		boolean listChildren = false;
		boolean tabDelimitMode = false;
		boolean gatherUniqueFieldNames = false;
		int firstArg = 0;
		HashSet<String> fieldsToShowSet = null;
		ArrayList<String> fieldsToShowList = null;
		HashSet<String> fieldsToShowListLookupSet = null;
		for (int i = 0; i < args.length; i++) {
			System.out.println(i + ": " + args[i] + ".");
		}
		int iArg = 0;
		if (args.length == 0) {
			System.out.println("index dir must be first arg");
			System.exit(1);
		}
		String fs = args[iArg++];
		if (args.length > iArg && ("-f".equals(args[iArg]))) {
			listFields = true;
		} else if (args.length > iArg && ("-F".equals(args[iArg]))) {
			gatherUniqueFieldNames = true;
		} else if (args.length > iArg && ("-c".equals(args[iArg]))) {
			listChildren = true;
		} else {
			int i = 0;
			if (args.length > iArg && ("-t".equals(args[iArg]))) {
				tabDelimitMode = true;
				fieldsToShowList = new ArrayList<String>();
				fieldsToShowListLookupSet = new HashSet<String>();
				for ( ; iArg < args.length; iArg++) {
					fieldsToShowList.add(args[iArg]);
					fieldsToShowListLookupSet.add(args[iArg]);
				}
				i++;
			} else {
				fieldsToShowSet = new HashSet<String>();
				for ( ; iArg < args.length; iArg++) {
					fieldsToShowSet.add(args[iArg]);
				}
			}
		}
		HashMap<String, List<String>> childToParentsMap = new HashMap<String, List<String>>();
		HashMap<String, HashMap<String, String>> entityToDoc = new HashMap<String, HashMap<String, String>>();
		ArrayList<String> entities = new ArrayList<String>();
		HashSet<String> uniqueFieldNames = new HashSet<String>();
		try {
			SimpleFSDirectory fsd = new SimpleFSDirectory(new File(fs));//, new NoLockFactory());
			ExecutorService es = null;
			
			//IndexSearcher is = new IndexSearcher(fsd);//, es);
//			IndexSearcher is = new IndexSearcher(fsd, true);
			IndexReader reader = IndexReader.open(fsd);//is.getIndexReader();
			int maxDoc = reader.maxDoc();
			Bits liveDocs = MultiFields.getLiveDocs(reader);
			System.out.println("maxDoc: " + maxDoc);
			for (int iDoc = 0; iDoc < maxDoc; iDoc++) {
				Document doc = reader.document(iDoc);
				List<IndexableField> fields = doc.getFields();
				if (gatherUniqueFieldNames) {
					for (IndexableField field: fields) {
						uniqueFieldNames.add(field.name());
					}
				} else if (fieldsToShowSet != null) {
					System.out.println("doc: " + iDoc);
					for (IndexableField field: fields) {
						if (fieldsToShowSet.contains(field.name())) {
							System.out.println(field.name() + ":" + field.stringValue());
						}
					}
					System.out.println();
				} else if (fieldsToShowList != null) {
					HashMap<String, String> tempMap = new HashMap<String, String>();
					StringBuilder out = new StringBuilder(""+iDoc);
					for (IndexableField field: fields) {
						if (fieldsToShowListLookupSet.contains(field.name())) {
							String value = field.stringValue();
							if (tempMap.containsKey(field.name())) {
								value = tempMap.get(field.name()) + "%%%" + value;
							}
							tempMap.put(field.name(), value);
						}
					}
					for (String fieldToShow : fieldsToShowList) {
						out.append("\t");
						if ("DELETED".equals(fieldToShow)) {
							if ( ! liveDocs.get(iDoc)) { 
								out.append("DELETED");
							} else {
								out.append("OK");
							}
						} else if (tempMap.containsKey(fieldToShow)) {
							out.append(tempMap.get(fieldToShow));
						}
					}
					System.out.println(out.toString());
				} else if (listChildren) {
					String []childLinks = doc.getValues("childLinks");
					String []childLinkTypes = doc.getValues("childLinkTypes");
					HashMap<String, String> hdoc = new HashMap<String, String>();
					hdoc.put("id", doc.get("id"));
					hdoc.put("entityType", doc.get("entityType"));
					hdoc.put("docType", doc.get("docType"));
					hdoc.put("numChildLinks", ""+childLinkTypes.length);
					hdoc.put("titleDisplay", doc.get("titleDisplay"));
					entityToDoc.put(doc.get("id"), hdoc);
					entities.add(doc.get("id"));
					for (int iChild = 0; iChild < childLinkTypes.length; iChild++) {
						List<String> list = childToParentsMap.get(childLinks[iChild]);
						if (list == null) {
							childToParentsMap.put(childLinks[iChild], list = new ArrayList<String>());
						}
						list.add(doc.get("id"));
					}
				} else if (listFields) {
					for (IndexableField field: fields) {
						System.out.println(field.name() + ":" + field.stringValue());
					}
					System.exit(0);
				}
			}
			if (gatherUniqueFieldNames) {
				for (String fn : uniqueFieldNames) {
					System.out.println(fn);
				}
			} else {
				System.out.println(String.format("entity count (map): %d", entityToDoc.size()));
				System.out.println(String.format("entity count (list): %d", entities.size()));
				System.out.println(String.format("child count: %d", childToParentsMap.size()));
				for (String entity : entities) {
					String out = entity;
					List<String> parentList = childToParentsMap.get(entity);
					if (parentList != null && parentList.size() > 0) {
						for (String parent : parentList) {
							out += "\t" + parent;  
						}
					}
					System.out.println(out);
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
}
