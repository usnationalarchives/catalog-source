package gov.nara.opa.solr.analysis;

/**
 * <p>Title: </p>
 * <p>Description: This filter transforms an input word into its stemmed form
 * using GCIDE dictionary.</p>
 */

import com.searchtechnologies.aspire.lemma.Lemmatizer;
import com.searchtechnologies.aspire.lemma.LemmatizerException;
import com.searchtechnologies.aspire.lemma.LemmatizerFactory;

import java.io.IOException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;


/** Transforms the token stream according to the reduction rules.
    Note: the input to the stemming filter must already be in lower case,
    so you will need to use LowerCaseFilter or LowerCaseTokenizer farther
    down the Tokenizer chain in order for this to work properly!
    <P>
    To use this filter with other analyzers, you'll want to write an
    Analyzer class that sets up the TokenStream chain as you want it.
    To use this with LowerCaseTokenizer, for example, you'd write an
    analyzer like this:
    <P>
    <PRE>
    class MyAnalyzer extends Analyzer {
      public final TokenStream tokenStream(String fieldName, Reader reader) {
        return new KStemStemFilter(new LowerCaseTokenizer(reader));
      }
    }
    </PRE>

*/

public class LemmatizerFilter extends TokenFilter {
    private Lemmatizer lemma;
    private CharTermAttribute charTermAttr;
    private OffsetAttribute offsetAttribute;
    
    private final KeywordAttribute keywordAttr = addAttribute(KeywordAttribute.class);

    Token nextToken = null;
    
    /**Create a Lemmatizer with the given cache size.
     * @param in The TokenStream whose output will be the input to KStemFilter.
     *  @param cacheSize Maximum number of entries to store in the
     *  Stemmer's cache (stems stored in this cache do not need to be
     *  recomputed, speeding up the stemming process).
     */
//    public LemmatizerFilter(TokenStream in, int cacheSize, String dictFileName) {
//      super(in);
//      lemma = LemmatizerFactory.createLemmatizer(cacheSize,dictFileName);
//      this.charTermAttr = addAttribute(CharTermAttribute.class);
//      this.offsetAttribute = addAttribute(OffsetAttribute.class);
//    }

    /** Create a Lemmatizer with the default cache size of 20 000 entries.
     * @param in The TokenStream whose output will be the input to KStemFilter.
     * @throws LemmatizerException 
     */
    public LemmatizerFilter(TokenStream in, String dictFileName) throws LemmatizerException {
      super(in);
      lemma = LemmatizerFactory.createLemmatizer(dictFileName);
      this.charTermAttr = addAttribute(CharTermAttribute.class);
      this.offsetAttribute = addAttribute(OffsetAttribute.class);
    }

   

    /*public final Token next(Token in) throws IOException {

      if(nextToken != null) {
        Token saveToken = nextToken;
        nextToken = null;
        return saveToken;
      }
      
      Token token = input.next(in);
      if (token==null) return null;
      
      char[] tokenChars = token.termBuffer();
      if (tokenChars[0] != 'L' && tokenChars[1] == '/') {
        return token;
      }
      
      String tokenString = String.copyValueOf(token.termBuffer(), 2, token.termLength()-2);
      String s = lemma.stem(tokenString);
      tokenChars = s.toCharArray();
      nextToken = token.clone(tokenChars, 0, tokenChars.length, token.startOffset(), token.endOffset());

      return token;
    }*/

    @Override
    public final boolean incrementToken() throws IOException {
      // TODO Auto-generated method stub
  
      if (!input.incrementToken()) {
        return false;
      }
      
      //if the word is protected by KeywordMarkerFilterFactory, then don't change
      //the word.
      if (keywordAttr.isKeyword())
    	  return true;
      
      int startOffset = offsetAttribute.startOffset();
      int endOffset = offsetAttribute.endOffset();
      int length = charTermAttr.length();
      char[] buffer = new char[length];
      for (int i = 0; i < length; i++) {
        buffer[i] = charTermAttr.buffer()[i];
      }
      clearAttributes();
      offsetAttribute.setOffset(startOffset,startOffset +length);
      
      String tokenString = String.copyValueOf(buffer).trim();
      String s = lemma.stem(tokenString);
      clearAttributes();

     // System.out.println(String.format("\t%s(%d): [%s](%d)\t\t%d-%d\t\tnew %d-%d\n\n",tokenString, length, s, s.length(), startOffset, endOffset, startOffset, (startOffset + s.length())));
      
      char[] newBuffer = new char[s.length()];
      newBuffer = s.toCharArray();
      
      charTermAttr.setEmpty();
      charTermAttr.copyBuffer(newBuffer, 0, newBuffer.length);
      offsetAttribute.setOffset(startOffset,startOffset +length);
      
      return true;
    
      
    }

	@Override
	public void reset() throws IOException {
		super.reset();
		nextToken = null;
		
	}
    
    
}