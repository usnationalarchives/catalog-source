package gov.nara.opa.solr.analysis;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

public class CurlyBraceRemoverFilter extends TokenFilter {

	@Override
	public void reset() throws IOException {
		// TODO Auto-generated method stub
		super.reset();
		savedToken = null;
		savedTokenOffsetStart = 0;
	}

	private CharTermAttribute termAtt;
	private PositionIncrementAttribute posIncrAtt;	//yet to use
	private OffsetAttribute offsetAtt;				
	String savedToken = null;
	int savedTokenOffsetStart;

	protected CurlyBraceRemoverFilter(TokenStream input) {
		super(input);
		termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);
		posIncrAtt = (PositionIncrementAttribute)
			 	addAttribute(PositionIncrementAttribute.class);

		offsetAtt =	(OffsetAttribute) addAttribute(OffsetAttribute.class);
	}

	//@Override
	public final boolean ANOTHER_incrementToken() throws IOException {
		if(!input.incrementToken())
			return false;								
		int origOffset = offsetAtt.startOffset();
		String inputText = termAtt.toString();
		int curlyIdxStart = inputText.indexOf('{');
		if ( -1 == curlyIdxStart) {
			return true;
		}
		int curlyIdxEnd = findCurlyEnd(inputText, curlyIdxStart+1);
		if (curlyIdxEnd != -1) {
			if (curlyIdxEnd == inputText.length() && curlyIdxStart == 0) {
				// whole thing is a {curly}
				return false;
			}
			if (curlyIdxEnd == inputText.length()) {
				// aa{xx}, so return aa
				termAtt.setLength(0);
				termAtt.append(inputText.substring(0, curlyIdxStart));
				offsetAtt.setOffset(origOffset, origOffset + termAtt.length());
				return true;
			} else if (curlyIdxStart == 0) {
				int nextCurlyIdxStart = inputText.indexOf('{', curlyIdxEnd);
				if (nextCurlyIdxStart == -1) {
					// {xx}aa, and no other curlies after {xx}
					termAtt.setLength(0);
					termAtt.append(inputText.substring(curlyIdxEnd));
					int offset = origOffset + curlyIdxEnd;
					offsetAtt.setOffset(offset, offset + termAtt.length());
					return true;
				}

			}
		} else {
			// has an open curly, but no close curly
		}
		return false;
	}
	
	// find the closing curly brace... if another open curly brace occurs right away, keep going
	// returns index of char one past the '}'
	static public int findCurlyEnd(String term, int start) {
		int foundIdx = -1;
		for (int i = start; i < term.length(); ++i) {
			if (foundIdx >= 0) {
				if (term.charAt(i) == '{') {
					// {asdf}{wer}
					foundIdx = -1;
				} else {
					break;
				}
			}
			if (term.charAt(i) == '}') {
				foundIdx = i + 1;
			}
		}
		return foundIdx;
	}

	@Override
	public final boolean incrementToken() throws IOException {	

		@SuppressWarnings("unused")
		int fake = 0;
		fake++;
		boolean inBrace = false;
		while (true) {
			char[] buffer;
			int origOffset;
			if (savedToken != null) {
				buffer = savedToken.toCharArray();
				origOffset = savedTokenOffsetStart;
				savedToken = null;
			} else {
				if(!input.incrementToken())
					return false;								
				origOffset = offsetAtt.startOffset();
				buffer = termAtt.toString().toCharArray();
			}
			
			int offset = origOffset;
			StringBuffer sb = new StringBuffer();
			for(int i = 0; i < buffer.length; i++){
				if(inBrace){
					if(buffer[i] == '}'){						
						if(sb.length()	> 0){							
							savedToken = termAtt.toString().substring(i+1);	
							savedTokenOffsetStart = i+offset+1;
							break; 
						}
						offset = origOffset+i+1;
						inBrace = false;
					}
				}			
				else{
					if(buffer[i] == '{'){
						if (sb.length() > 0) {
							savedToken = new String(Arrays.copyOfRange(buffer, i, buffer.length));	
							savedTokenOffsetStart = i + origOffset;//offset+i;
							//savedTokenOffsetStart = i;//offset+i;
							break;
						}
						inBrace = true;
					}
					else{									
						sb.append(buffer[i]);
					}					
				}
			}	
			if (inBrace) continue;
			String token = sb.toString();
			if(token.length() > 0){												
				token = token.replaceFirst("(%7B)", "{");		// { } in the input doc will be encoded as %7B, %7D, this has to be replaced with { }.
				token = token.replaceFirst("(%7D)", "}");
				termAtt.setLength(0);
				termAtt.append(token);
				if(token.length() >= 1 && token.startsWith("{"))						
					offsetAtt.setOffset(offset+2, offset+2+termAtt.length());
				else
					offsetAtt.setOffset(offset, offset+termAtt.length());
				return true; 
			} else {
				if (offset > origOffset && savedToken == null) {
					// a token like '{asdf}' was passed in from input, continue
					continue;
				}
				return false;
			}
			
			//return false;
		}
		//}

		//		
		//		boolean tokenSkipped = true;			
		//		
		//		//If there is a saved token, then return it. Set saved token to null.
		//		if(savedToken != null){		
		//			String token = savedToken.toString();
		//			savedToken = null;
		//			if(token.length() > 0){												
		//				token = token.replaceFirst("(%7B)", "{");
		//				token = token.replaceFirst("(%7D)", "}");
		//				//System.out.println("termAtt Returned => " + token);
		//				termAtt.setLength(0);
		//				termAtt.append(token);
		//				offsetAtt.setOffset(savedTokenOffsetStart, savedTokenOffsetEnd);
		//				return true; 
		//			}
		//		}
		//			
		//		//Skip the token if it a curly brace marker like {...}.  
		//		while(tokenSkipped){
		//
		//			if(!input.incrementToken())
		//				return false;								
		//			
		//			boolean inBrace = false;
		//			
		//			char[] buffer = termAtt.toString().toCharArray();
		//			StringBuffer sb = new StringBuffer();
		//			int origOffset = offsetAtt.startOffset();
		//			int offset = origOffset;
		//			for(int i = 0; i < buffer.length; i++){
		//				if(inBrace){
		//					if(buffer[i] == '}'){						
		//						if(sb.length()	> 0){							
		//							savedToken = termAtt.toString().substring(i+1);	
		//							savedTokenOffsetStart = offset+i+1;
		//							savedTokenOffsetEnd = savedTokenOffsetStart + savedToken.length();
		//							break; 
		//						}
		//						offset = origOffset+i+1;
		//						inBrace = false;
		//					}
		//				}			
		//				else{
		//					if(buffer[i] == '{'){													
		//						inBrace = true;
		//					}
		//					else{									
		//						sb.append(buffer[i]);
		//					}					
		//				}
		//			}			
		//			String token = sb.toString();
		//			if(token.length() > 0){												
		//				token = token.replaceFirst("(%7B)", "{");		// { } in the input doc will be encoded as %7B, %7D, this has to be replaced with { }.
		//				token = token.replaceFirst("(%7D)", "}");
		//				termAtt.setLength(0);
		//				termAtt.append(token);
		//				if(token.length() >= 1 && token.startsWith("{"))						
		//						offsetAtt.setOffset(offset+2, offset+termAtt.length());
		//				else
		//						offsetAtt.setOffset(offset, offset+termAtt.length());
		//				return true; 
		//			}									
		//		}
		//		return false;
		//	}
	}
}


