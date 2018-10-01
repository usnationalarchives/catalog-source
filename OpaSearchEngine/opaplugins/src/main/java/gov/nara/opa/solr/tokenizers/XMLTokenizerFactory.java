package gov.nara.opa.solr.tokenizers;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeSource.AttributeFactory;

public class XMLTokenizerFactory extends TokenizerFactory implements ResourceLoaderAware{

	public static String MARK_DEPTH = "markDepth";

	private String exclusionsFile = "";
	private final boolean DEBUG = false;
	private boolean isMarkDepth = false;
	
	public XMLTokenizerFactory(Map<String, String> args) {
		super(args);
		exclusionsFile = get(args,EXCLUDE_ATTRIBUTES); 
		isMarkDepth = Boolean.valueOf(get(args, MARK_DEPTH, "false")); 
	}


	//	private HashMap<String,String> exclusions = new HashMap<String,String>();
	CharArraySet exclusions = null;
	
	public static String EXCLUDE_ATTRIBUTES="excludeAttributes";
	
//	Removed during the upgrade from 4.2 to 4.3
//	@Override
//	public void init(Map<String, String> args) {
//		// TODO Auto-generated method stub
//		super.init(args);
//	}


	//@Override
	public void inform(ResourceLoader loader) throws IOException {
		//String exclusionsFile = get(getOriginalArgs(),EXCLUDE_ATTRIBUTES);
		if (exclusionsFile!=null){
			exclusions = getWordSet(loader, exclusionsFile, true);
		} else {
			exclusions = new CharArraySet(getLuceneMatchVersion(), 0, true);
		}
	}

	@Override
	public Tokenizer create(AttributeFactory attributeFactory, Reader input) {
		if (DEBUG) System.out.println("=============================================================================");
		if (DEBUG) System.out.println("Creating XMLTokenizer for input:"+input.toString());
		if (DEBUG) System.out.println("=============================================================================");
		XMLTokenizer tokenizer = new XMLTokenizer(input, exclusions);
		tokenizer.setIsNamespaceAware(false);
		tokenizer.setMarkDepth(isMarkDepth);
		return tokenizer;
	}

}
