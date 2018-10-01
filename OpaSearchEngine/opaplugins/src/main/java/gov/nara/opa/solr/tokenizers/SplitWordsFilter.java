package gov.nara.opa.solr.tokenizers;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * This filter factory will be a standard token-splitter factory which does the following:
 * 1.Split words on any white space.
 * 		For example:  �computer programmer�  => [computer, programmer]
 * 2.Split words on any punctuation character
 * 		For example:  �computer-programmer�  =>  [computer, programmer]
 * 3.Remove words which are all punctuation
 * 		For example:  �&*@$*_� =>  []
 * 4.Remove punctuation from the beginnings and endings of words:
 * 		For example:  �--this++�  =>  [this]
 * 5.Split words on case change from lower-case to upper case
 * 		For example:  �computerProgrammer�  => [computer, Programmer]
 * 6.Split words on character type change from alpha to digit
 * 		For example:  �rx300a� =>  [rx, 300, a]
 * 7.Leaves XML tokens from the XML Tokenizer unmodified
 * 		For example:  �<tag>�  =>  [<tag>]
 * 
 * XML tags will be tagged as special token types by the XML tokenizer.
 * Therefore, the SplitWordsFilterFactory should look for the flag from the XML tokenizer.
 * It should not do pattern matching to identify XML tokens.
 * 
 * Justification:
 *
 * Typically, the solr.WordDelimiterFilterFactory would be used for this function, however, 
 * this factory cannot be used in combination with the XML tokenizer without destroying 
 * the punctuation which is critical for marking XML tokens in the token stream.
 * And so, to ensure tokenization consistency, we recommend creating a new word 
 * splitter which can be used for both of the xml_fdsys and the text_fdsys analyzers.
 * 
 */
public class SplitWordsFilter extends TokenFilter {


	//Logging classes:
	final Logger logger = LoggerFactory.getLogger(SplitWordsFilter.class);
	
	//Setup the attributes we will be using:

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

	SplitWordFilterSplitter splitter = null;

	boolean pendingTokens=false;
	
	public SplitWordsFilter(TokenStream input) {
		super(input);
		splitter = new SplitWordFilterSplitter();
	}
	
	public SplitWordsFilter(
			boolean splitOnWhitespace, 
			boolean splitOnPunctuation, 
			boolean splitOnAlphaDigit, 
			boolean splitOnCaseChange, 
			TokenStream input) 
	{
		super(input);
		int CONFIG = 0;
		if (splitOnWhitespace) CONFIG |= SplitWordFilterSplitter.WHITESPACE;
		if (splitOnPunctuation) CONFIG |= SplitWordFilterSplitter.PUNCTUATION;
		if (splitOnAlphaDigit) CONFIG |= SplitWordFilterSplitter.ALPHA_TO_DIGIT;
		if (splitOnCaseChange) CONFIG |= SplitWordFilterSplitter.CASE_CHANGE;
		
		splitter = new SplitWordFilterSplitter(CONFIG);
	}

	@Override
	public void reset() throws IOException{
		super.reset();
		splitter.reset();
		pendingTokens=false;
		/*try {
			input.reset();
		} catch (IOException e) {
			logger.warn("Unable to reset input TokenStream "+input.getClass().getName(),e);
		}*/
	}
	
	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.TokenStream#incrementToken()
	 */
	@Override
	public final boolean incrementToken() throws IOException {
//			System.out.println("in SWF.incrementToken");

			if (!pendingTokens){
				if (!input.incrementToken()) {
					return false;
				}
				// We have a new token. Let's see the type
				//Get the token type - if it is XML, then we can skip it.
				//			 * 7.Leaves XML tokens from the XML Tokenizer unmodified
				//			 * 		For example:  �<tag>�  =>  [<tag>]
				String tokenType = typeAtt.type();
				if (tokenType!=null && tokenType.equals(XMLTokenizer.XML_TAG)){
					return true;
				}

				//It isn't an XML token, so now we check if we need to break it up.
				//Get the current token information:
				char[] termBuffer = Arrays.copyOfRange(termAtt.buffer(), 0, termAtt.length());
				int offset = offsetAtt.startOffset();

				splitter.reset (termBuffer,offset);
				
				splitter.next();
				char[] word = splitter.getWord();
				
				//If the token is just some punctuation marks and space, for example, we'll get an empty char[] back:
				if (word == null || word.length==0){
					//So let's see if there are more tokens:
					return incrementToken();
				}

				int os = splitter.getOffset();

				setToken(word, os, os+word.length);
				if (splitter.hasNextWord()){
					pendingTokens=true;
				} else {
					pendingTokens=false;
				}
				return true;
				
			} else {
				//We have more tokens left after splitting the word:
				
				if (splitter.hasNextWord()){
					splitter.next();
					char[] word = splitter.getWord();
					int os = splitter.getOffset();
					setToken(word, os, os+word.length);
					if (splitter.hasNextWord()){
						pendingTokens=true;
					} else {
						pendingTokens=false;
					}
					return true;
				} 
				
			}
			return false;
	}
	
	private void setToken(char[] tmp, int startOffset, int endOffset){//, String type, int startOffset, int endOffset) {
		termAtt.setEmpty();
		if (termAtt.length()<tmp.length){
			termAtt.resizeBuffer(tmp.length);
		}
		termAtt.copyBuffer(tmp, 0, tmp.length);
		termAtt.setLength(tmp.length);
		offsetAtt.setOffset(startOffset,endOffset);
		posIncrAtt.setPositionIncrement(1);
	}

}
