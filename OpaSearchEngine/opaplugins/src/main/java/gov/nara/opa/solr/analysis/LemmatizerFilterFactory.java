package gov.nara.opa.solr.analysis;

/**
 * <p>Title: </p>
 * <p>Description: This filter transforms an input word into its stemmed form
 * using GCIDE dictionary.</p>
 */

/*
import com.searchtechnologies.aspire.lemma.LemmatizerException;
import com.searchtechnologies.aspire.lemma.LemmatizerFactory;
*/

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.xml.sax.InputSource;

import com.searchtechnologies.aspire.lemma.LemmatizerException;
import com.searchtechnologies.aspire.lemma.LemmatizerFactory;
import gov.nara.opa.solr.analysis.LemmatizerFilter;

public class LemmatizerFilterFactory extends TokenFilterFactory implements ResourceLoaderAware{


	public LemmatizerFilterFactory(Map<String, String> args) {
		super(args);
		this.dictFileName = get(args, "dictionaryFileName");	
	}

	//dictionary file should exist under solr/conf folder
	private String defaultdictFileName = "dictionary.xml";
	private String dictFileName;

	public TokenStream create(TokenStream input) {
		TokenStream ret = null;
		try {
			ret = new LemmatizerFilter(input, dictFileName);
		} catch (LemmatizerException e) {
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public void inform(ResourceLoader loader) throws IOException {

		System.out.println("LemmatizerFilterFactory:init():localdictFileName ="+dictFileName);
		if (dictFileName == null) {
			dictFileName = defaultdictFileName;
		}
		try {
			org.xml.sax.InputSource inputSource = null;
			if (dictFileName.endsWith(".gz")) {
				try {
					InputStream inputStream = loader.openResource(dictFileName);
					GZIPInputStream gz = new GZIPInputStream(inputStream);
					inputSource = new InputSource(gz);
				} catch (FileNotFoundException e) {
					throw new LemmatizerException("loadDictionary() with gz file", e);
				} catch (IOException e) {
					throw new LemmatizerException("loadDictionary() with gz file", e);
				}
			}
			LemmatizerFactory.createLemmatizer(this.dictFileName, inputSource);
		} catch (LemmatizerException e) {
			e.printStackTrace();
		}
	}

}
