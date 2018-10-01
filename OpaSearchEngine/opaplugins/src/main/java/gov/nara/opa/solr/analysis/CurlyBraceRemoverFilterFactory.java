package gov.nara.opa.solr.analysis;

import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

public class CurlyBraceRemoverFilterFactory extends TokenFilterFactory{

	public CurlyBraceRemoverFilterFactory(Map<String, String> args) {
		super(args);
	}

	@Override
	public TokenStream create(TokenStream input) { 
		return new CurlyBraceRemoverFilter(input);
	}
	
}
