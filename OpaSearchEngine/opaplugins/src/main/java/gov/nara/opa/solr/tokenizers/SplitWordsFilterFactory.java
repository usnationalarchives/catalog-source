package gov.nara.opa.solr.tokenizers;

import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

public class SplitWordsFilterFactory extends TokenFilterFactory {
	
	public static String SPLIT_ON_WHITESPACE="splitOnWhitespace";
	public static String SPLIT_ON_PUNCTUATION="splitOnPunctuation";
	public static String SPLIT_ON_ALPHA_TO_DIGIT="splitOnAlphaDigit";
	public static String SPLIT_ON_CASE_CHANGE="splitOnCaseChange";

	private final boolean splitOnWhitespace;
	private final boolean splitOnPunctuation;
	private final boolean splitOnAlphaDigit;
	private final boolean splitOnCaseChange;

	public static int WHITESPACE=1;
	public static int PUNCTUATION=2;
	public static int ALPHA_TO_DIGIT=4;
	public static int CASE_CHANGE=8;
	
	public SplitWordsFilterFactory(Map<String,String> args){
		super(args);
		splitOnWhitespace = Boolean.valueOf(get(args,SPLIT_ON_WHITESPACE,"true")); 
		splitOnPunctuation = Boolean.valueOf(get(args,SPLIT_ON_PUNCTUATION,"true")); 
		splitOnAlphaDigit = Boolean.valueOf(get(args,SPLIT_ON_ALPHA_TO_DIGIT,"true")); 
		splitOnCaseChange = Boolean.valueOf(get(args,SPLIT_ON_CASE_CHANGE,"true")); 

	}
	
	@Override
	public TokenStream create(TokenStream input) {
		return new SplitWordsFilter(
				splitOnWhitespace,
				splitOnPunctuation,
				splitOnAlphaDigit,
				splitOnCaseChange,
				input);
	}

}
