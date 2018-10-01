/**
 * 
 */
package gov.nara.opa.solr.tokenizers;

import java.io.IOException;
import java.io.Reader;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttributeImpl;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.AttributeSource.AttributeFactory;

import java.nio.CharBuffer;

//Logging:
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * The XML tokenizer will be used to tokenize the <mods> xml so that XML searching can be accomplished using the BETWEEN operator.
 * This tokenizer will create tokens as follows:
 * 	Split tokens on white space
 * 		|this is some-text|  =>  [this, is, some-text]
 * 		Note that tokens with embedded punctuation will be split later
 * 	Simple XML start tags and end tags become separate tokens
 * 		|<tag>this is some-text</tag>|  =>  [<tag>, this, is, some-text, </tag>]
 * 	XML entities are converted to their character equivalents in the token stream
 * 		|this is &amp; some&#45;text|  =>  [this, is, &, some-text]
 * 	XML tags with attributes will need to separate and mark the beginning and ending of each attribute with special <tag/@attribute> tags.
 * 		|<granule type="parent">|  => [<granule>, <granule/@type>, parent, </granule/@type>]
 * 	XML Comments such as <!-- This is a comment --> will need to be removed and ignored.
 * 	Nested <![CDATA[ content ]]> marked character content will need to be converted to an ordinary block of character data.
 * 
 * This will create a stream of tokens with XML tags (with XML tag characters, such as </@> preserved) that are sent to the index and indexed with the XML punctuation intact. 
 * This will have the following advantages:
 * 
 * 1.	XML tags will never be confused with user searches.
 * 	a.	Since all user searches will be completely stripped of punctuation, it will be impossible for a user to search for an XML tag without using the approved �mods:�� field operator syntax.
 * 	b.	For example, a query on |tag| will never match |<tag>| in the XML field.
 * 	c.	Similarly, when the user searches on |<tag>|, the punctuation will be removed and so only instances of |tag| in the text will be found � not XML tags.
 * 	d.	The only method for users to search the XML will be to use the official |mods:| field operator format.
 * 2.	Since XML tags are indexed as part of the same token stream as the content itself, this will allow for searching of content within specified XML tags.
 * 	a.	All XML tags will be counted as words for the purpose of word position computations.
 * 	b.	This will ensure that the BETWEEN operator can correctly search for text which occurs between the specified XML tags when needed.
 * 
 * Implementation
 * It is recommended that this tokenizer be implemented with the Java 1.6 JDK XMLStreamReader interface, for the following reasons:
 * 1.	Lucene tokenizers are initialized with a java Reader as input.
 * 	a.	Likewise, an XMLStreamReader can be created with a Reader as input using the XMLInput class.
 * 2.	The Lucene token stream is a �pull� interface which pulls tokens from the analysis chain.
 * 	a.	The XMLStreamReader is also a pull interface.
 * 	b.	For this reason, the SAX interface is not recommended, because SAX is a push interface.
 * 3.	The XMLStreamReader will take care of all standard XML processing:
 * 	a.	Parsing of XML structures.
 * 	b.	Entity translation.
 * 	c.	Removal of XML comments.
 * Whenever the XMLTokenizer receives an incrementToken() method, it should call the XMLStreamReader.next() method to fetch the next XML structure. Begin and end 
 * tags should then be returned as single tokens (with </> punctuation) and marked as an XML token type so that these structures 
 * will be removed by word-splitters down the analysis chain.
 * Character content received by the XMLTokenizer will need to be further tokenized on white space. 
 * Each token should be returned in turn for each call of incrementToken(), until the character content is exhausted.
 * In this way, the XMLStreamReader class will dramatically simplify the process of converting XML content into the appropriate stream of XML tags and content tokens.
 * 
 * As for the positions, the tokens should try to preserve the position on the original stream, at least to a close approximation.
 * 
 * For example:
 * 
 * 00000000001111111111222222222233333333334444444444
 * 01234567890123456789012345678901234567890123456789
 * <meta name="dc.creator" content="arcuser"/>
 * 
 * 0     6           12        22           24             33     40              42
 * <meta><meta/@name>dc.creator</meta/@name><meta/@content>arcuser</meta/@content></meta>
 * 
 */
public class XMLTokenizer extends Tokenizer implements XMLStreamConstants {

	//Logging classes:
	final Logger logger = LoggerFactory.getLogger(XMLTokenizer.class);

	public static final String XML_TAG = "XML_TAG";
	private static boolean DEBUG = false;
	public static boolean PRECOMPUTE_TEXT_TOKENS=false;

	private boolean bMarkDepth = false; // if true, append depth to tag tokens e.g. <head>0 <oneDeep>1
	private XMLStreamReader xmlInput = null;

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	//	private final PositionLengthAttribute posLengthAtt = addAttribute(PositionLengthAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

	private CharArraySet exclusions = null;

	//Used to split the text at whitespace:
	private SplitWordFilterSplitter splitter=null;

	List<String> tokens = null;
	List<String> types = null;
	
	XMLInputFactory xif = null;


	public XMLTokenizer(Reader input, CharArraySet exclusions) {
		super(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY, input);
		if (DEBUG) System.out.println("==>XMLTokenizer Constructor<==");
		this.exclusions = exclusions;
		xif = XMLInputFactory.newFactory();
//		xif.setProperty(
//				XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, 
//				Boolean.TRUE);
//		try {
//			xmlInput = xif.createXMLStreamReader(input);
//		} catch (XMLStreamException e) {
//			e.printStackTrace();
//		}

		splitter=new SplitWordFilterSplitter(SplitWordFilterSplitter.WHITESPACE);
		splitter.reset();
	}

	public void setIsNamespaceAware(boolean isAware) {
		xif.setProperty(
				XMLInputFactory.IS_NAMESPACE_AWARE, 
				isAware);
	}
	boolean inProgress = false;
	
	@Override
	public final boolean incrementToken() throws IOException {		
		try {
			if (!inProgress){
				if (DEBUG) System.out.println("Creating new xmlInput");
				xmlInput = xif.createXMLStreamReader(input);
			}
			boolean result = nextToken();
			if (DEBUG) System.out.printf("incrementToken() returning result=%b, term: %s\n", result, termAtt.toString());
//			if (!result && !inProgress){
//				if (DEBUG) System.out.println("Creating new xmlInput");
//				xmlInput = xif.createXMLStreamReader(input);
////				if (DEBUG) System.out.println("Calling next Token again");
////				result = nextToken();
//			}
			return result;
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		if (DEBUG) System.out.println("incrementToken() returning => **false**");
		return false;
	}

	//Queue generated tokens to return them later on.
	Queue<XMLTempToken> generatedTokensQueue = new LinkedList<XMLTempToken>();

	//Reusable StringBuilder to increase performance
	StringBuilder sb = new StringBuilder();

	//Used when next() was called but the event wasn't consumed.
	boolean pendingEvent = false;
	int depth = -1;

	private boolean nextToken() throws XMLStreamException {		

		if (false && DEBUG){
//			sb.setLength(0);
//			System.out.println("sb="+sb.toString());
//			char[] asdf = new char[sb.length()];
//			sb.getChars(0, sb.length(), asdf, 0);
//			System.out.println("asdf="+new String(asdf));
//
			System.out.println("nextToken():xmlInput.hasNext():"+xmlInput.hasNext());
		}
		
		//char array to hold the characters of the token
		char[] dst=null;

		//First we return any tokens we generated when splitting a tag or text
		if (generatedTokensQueue.peek()!=null){
			XMLTempToken token = generatedTokensQueue.poll();
			setToken(token.tokenText,token.tokenType, token.startOffset,token.endOffset);
			return true;
		}

		if (splitter.hasNextWord()){
			splitter.next();
			char[] nextWord = splitter.getWord();
			int offset = splitter.getOffset();
			setToken(nextWord, StandardTokenizer.TOKEN_TYPES[StandardTokenizer.ALPHANUM], offset, offset+nextWord.length);
			preOffset+=nextWord.length;
			return true;
		}
		//If there are no generated tokens, we continue with the regular XML processing
		try{
			int event = -1;
			//Check if there are pending events:
			if (pendingEvent){
				event = xmlInput.getEventType();
				logger.debug("Pending Event! type: "+getEventTypeString(event));
				pendingEvent=false;
			} else {
				if (xmlInput.hasNext()){
					event = xmlInput.next();
					String evType = getEventTypeString(event);
					logger.debug("New Event! type: "+evType);
					inProgress=true;
					if (DEBUG) System.out.println("New Event! type: "+getEventTypeString(event));
				} else {
					return false;
				}
			}

			switch (event){
			case START_ELEMENT:

				depth++;
				if (DEBUG) System.out.println("depth increment to " + depth);

				//Clear the buffer:
				sb.setLength(0);

				//Append the first token, corresponds to the tag:
				sb.append("<").append(xmlInput.getLocalName());


				//And if there are attributes:
				int numAttributes = xmlInput.getAttributeCount();
				if (numAttributes==0){
					//There are no attributes, so 
					//let's see if the next element is the closing tag:
					//Whenever the XMLTokenizer receives an incrementToken() method, 
					//it should call the XMLStreamReader.next() method to fetch the next XML structure. 
					//Begin and end tags should then be returned as single tokens (with </> punctuation) 
					//and marked as an XML token type so that these structures will be removed 
					//by word-splitters down the analysis chain.
					int nextEvent = xmlInput.next();
					if (nextEvent == END_ELEMENT){
						sb.append("/>");
						if (bMarkDepth) sb.append(depth);
						dst = new char[sb.length()];
						sb.getChars(0, sb.length(), dst, 0);
						XMLTempToken tmp = new XMLTempToken(dst, XML_TAG,preOffset,preOffset+dst.length);
						setToken(tmp);
						preOffset +=dst.length; 
						depth--;
						if (DEBUG) System.out.println("depth decrement to " + depth);
						return true;
					}  else {
						pendingEvent=true;
						sb.append(">");
						if (bMarkDepth) sb.append(depth);
						dst = new char[sb.length()];
						sb.getChars(0, sb.length(), dst, 0);
						XMLTempToken tmp = new XMLTempToken(dst, XML_TAG,preOffset,preOffset+dst.length-1);
						setToken(tmp);
						preOffset +=dst.length;
						return true;
					}
				} else {
					sb.append(">");
					if (bMarkDepth) sb.append(depth);
					dst = new char[sb.length()];
					sb.getChars(0, sb.length(), dst, 0);
					XMLTempToken tmp = new XMLTempToken(dst, XML_TAG,preOffset,preOffset+dst.length - 1); //We don't count the position of the closing ">"
					setToken(tmp);
					preOffset +=dst.length; 
				}

				//Test CharBuffer performance:
				//					CharBuffer innerCB = null;

				//There are attributes, so now let's add the attributes, per the spec document:
				//
				//XML tags with attributes will need to separate and mark the beginning and ending of each attribute with special <tag/@attribute> tags.
				//
				//  |<granule type="parent">|  
				//	[<granule>, <granule/@type>, parent, </granule/@type>]
				//
				for (int i = 0; i<numAttributes;i++){
					if (exclusions.contains(xmlInput.getAttributeLocalName(i))){
						//This is one of the attributes we have to ignore
						//But first let's move the preOffset to the proper position:
						preOffset+=xmlInput.getAttributeLocalName(i).length()+2; //For the name plus the ="
						//And the length of the value:
						preOffset+=xmlInput.getAttributeValue(i).length();
						continue;
					}

					if (i>0){
						//we already processed an attribute, so let's advance the position by 1
						//because of the blank space between attributes
						preOffset++;
					}
					StringBuilder innerSB = new StringBuilder();
					innerSB.setLength(0);
					// <tag/@attribute> for attribute:
					innerSB.append("<").append(xmlInput.getLocalName());
					innerSB.append("/@");
					if (xmlInput.getAttributeNamespace(i)!=null){
						innerSB.append(xmlInput.getAttributeNamespace(i)).append(":");
					}
					innerSB.append(xmlInput.getAttributeLocalName(i));
					innerSB.append(">");
					if (bMarkDepth) innerSB.append(depth);
					dst=new char[innerSB.length()];
					innerSB.getChars(0, innerSB.length(), dst, 0);
					generatedTokensQueue.add(
							new XMLTempToken(
									dst,
									XML_TAG,
									preOffset,
									preOffset+xmlInput.getAttributeLocalName(i).length()+1 // ="
									)
							);
					preOffset+=xmlInput.getAttributeLocalName(i).length()+2; // ="


					List<String> textSplit = splitText(xmlInput.getAttributeValue(i));
					for (String tok : textSplit){
						dst = tok.toCharArray();
						generatedTokensQueue.add(
								new XMLTempToken(
										dst,
										StandardTokenizer.TOKEN_TYPES[StandardTokenizer.ALPHANUM],
										preOffset,
										preOffset+dst.length
										));
						preOffset+=dst.length+1;
					}

					//We take off one from the last preoffset to account for the lack of separator:
					preOffset--;

					innerSB.setLength(0);
					innerSB.append("</").append(xmlInput.getLocalName());
					innerSB.append("/@");
					if (xmlInput.getAttributeNamespace(i)!=null){
						innerSB.append(xmlInput.getAttributeNamespace(i)).append(":");
					}
					innerSB.append(xmlInput.getAttributeLocalName(i));
					innerSB.append(">");
					if (bMarkDepth) innerSB.append(depth);
					dst=new char[innerSB.length()];
					innerSB.getChars(0, innerSB.length(), dst, 0);
					generatedTokensQueue.add(
							new XMLTempToken(
									dst,
									XML_TAG,
									preOffset,
									preOffset+1

									));
					preOffset+=1; // the closing "
				}
				preOffset+=1; // > the closing tag character
				return true;

			case ATTRIBUTE:
				break;
			case NAMESPACE:
				break;
			case END_ELEMENT:
				sb.setLength(0);
				sb.append("</").append(xmlInput.getLocalName()).append(">");
				if (bMarkDepth)	sb.append(depth);
				dst = new char[sb.length()];
				sb.getChars(0, sb.length(), dst, 0);
				XMLTempToken tmp = new XMLTempToken(dst, XML_TAG,preOffset,preOffset+dst.length);
				setToken(tmp);
				preOffset +=dst.length;
				depth--;
				if (DEBUG) System.out.println("depth decrement to " + depth);

				return true;
			case CHARACTERS:
				//Old style, split the text before hand and store the tokens in the queue
				if(PRECOMPUTE_TEXT_TOKENS){
									if (xmlInput.hasText()){
										List<String> textSplit = splitText(xmlInput.getText());
										for (String tok : textSplit){
											generatedTokensQueue.add(
													new XMLTempToken(
															tok.toCharArray(),
															StandardTokenizer.TOKEN_TYPES[StandardTokenizer.ALPHANUM],
															preOffset,
															preOffset+tok.length()));
											preOffset+=(tok.length()+1);
										}
										return nextToken();
									}
				} else {
//				//New style, using the Splitter
				if (xmlInput.hasText()){
					splitter.reset(xmlInput.getText().toCharArray(), preOffset);
					if (splitter.hasNextWord()){
						splitter.next();
						char[] nextWord = splitter.getWord();
						int os = splitter.getOffset();
						setToken(nextWord, StandardTokenizer.TOKEN_TYPES[StandardTokenizer.ALPHANUM], os, os+nextWord.length);
						preOffset+=nextWord.length;
						return true;
					} else{
						return nextToken();
					}
				}
				}
				return false;
			case CDATA:
				if (xmlInput.hasText()){
					List<String> textSplit = splitText(xmlInput.getText());
					for (String tok : textSplit){
						generatedTokensQueue.add(
								new XMLTempToken(
										tok.toCharArray(),
										StandardTokenizer.TOKEN_TYPES[StandardTokenizer.ALPHANUM],
										preOffset,
										preOffset+tok.length()));
						preOffset+=(tok.length()+1);
					}
					return nextToken();
				}
				return false;
			case COMMENT:
				//We will skip comments
				//Advance the position based on the comment length:
				if (xmlInput.hasText()){
					//We skip the length of the test in the comment:
					int l = xmlInput.getTextLength();
					//Account for the opening bracket <!--(space):
					l+=4;
					//Account for the closing bracket: (space)-->
					l+=3;
					logger.debug("Skipping comment: "+(l));
					preOffset+=l;
				}
				return nextToken();
			case SPACE:
				//Skip the empty space:
				if (xmlInput.hasNext()){
					preOffset+=xmlInput.getTextLength()-1;
					logger.debug("Skipping whitespace: "+(xmlInput.getTextLength()-1));
				}
				return nextToken();
			case START_DOCUMENT:
				//We are at the beginning, so we need to advance to the first "real" data:
				return nextToken();
			case END_DOCUMENT:
				//And we are done
				inProgress=false;
				return false;
			case PROCESSING_INSTRUCTION:
				//We will ignore processing instructions
				return nextToken();
			case ENTITY_REFERENCE:
				//We configured the parser to replace entities, so this shouldn't happen...
				logger.debug("Entity => "+xmlInput.getText());
				return false;
			case DTD:
				//Ignore DTDs
				return nextToken();
			}
			//			}
		}catch (XMLStreamException xme){
			throw xme;
		}

		return false;
	}

	int preOffset=0;

	private void setToken(XMLTempToken token){
		setToken(token.tokenText,token.tokenType,token.startOffset,token.endOffset);
	}

	private void setToken(char[] tmp, String type, int startOffset, int endOffset) {
		termAtt.setEmpty();
		if (termAtt.length()<tmp.length){
			termAtt.resizeBuffer(tmp.length);
		}
		termAtt.copyBuffer(tmp, 0, tmp.length);
		termAtt.setLength(tmp.length);
		offsetAtt.setOffset(startOffset,endOffset);
		typeAtt.setType(type);
		posIncrAtt.setPositionIncrement(1);
		if (DEBUG) System.out.printf("%s\t{offset=[%d,%d],type=%s,posIncr=%d}\n",
				new String(Arrays.copyOfRange(termAtt.buffer(),0,termAtt.length())),
				offsetAtt.startOffset(),offsetAtt.endOffset(),
				typeAtt.type(),
				posIncrAtt.getPositionIncrement()
		);
	}




	public List<String> splitText(String theString) {

		char[] buffer = new char[theString.length()];
		int startOffSet= 0;
		int endOffset=0;
		boolean inAWord=false;
		List<String> tokensL = new LinkedList<String>();
		//Special case for when it is only one character:
		if (theString.length()==1 && !Character.isWhitespace(theString.charAt(0))){
			tokensL.add(theString);
		}
		for (int i = 0;i<theString.length();i++){
			if ( Character.isWhitespace(theString.charAt(i)) || (i+1 == theString.length())){
				if (inAWord){
					endOffset++;
					if ((i+1 == theString.length())) endOffset++;
					String tmp = String.copyValueOf(theString.toCharArray(), startOffSet, endOffset-startOffSet);
					logger.debug("splitText word=|"+tmp+"|");
					tokensL.add(tmp);
					inAWord=false;
				} else {
					startOffSet++;
				}
			} else {
				if (inAWord){
					endOffset++;
				} else {
					inAWord=true;
					startOffSet=i;
					endOffset=startOffSet;
				}
			}
		}

		return tokensL;
	}

	public final static String getEventTypeString(int eventType) {
		switch (eventType) {
		case START_ELEMENT:
			return "START_ELEMENT";
		case END_ELEMENT:
			return "END_ELEMENT";
		case PROCESSING_INSTRUCTION:
			return "PROCESSING_INSTRUCTION";
		case CHARACTERS:
			return "CHARACTERS";
		case COMMENT:
			return "COMMENT";
		case START_DOCUMENT:
			return "START_DOCUMENT";
		case END_DOCUMENT:
			return "END_DOCUMENT";
		case ENTITY_REFERENCE:
			return "ENTITY_REFERENCE";
		case ATTRIBUTE:
			return "ATTRIBUTE";
		case DTD:
			return "DTD";
		case CDATA:
			return "CDATA";
		case SPACE:
			return "SPACE";
		}
		return "UNKNOWN_EVENT_TYPE , " + eventType;
	}
	
	@Override
	public void reset() throws IOException {
		if (DEBUG) System.out.printf("==>RESET CALLED inProgress=%b<==\n",inProgress);
		if (!inProgress){
			super.reset();
			//Create a new xmlInput:
//			if (input!=null){
//				try {
//					xmlInput = xif.createXMLStreamReader(input);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
		}
		pendingEvent=false;
		if (generatedTokensQueue!=null) generatedTokensQueue.clear();
		if (tokens!=null) tokens.clear();
		splitter=new SplitWordFilterSplitter(SplitWordFilterSplitter.WHITESPACE);
		splitter.reset();

	}
	
	@Override
	public void close() throws IOException{
		super.close();
		try {
			xmlInput.close();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	public boolean isMarkDepth() {
		return bMarkDepth;
	}

	public void setMarkDepth(boolean markDepth) {
		this.bMarkDepth = markDepth;
	}

}

class XMLTempToken{

	public char[] tokenText;
	public String tokenType;
	public int startOffset;
	public int endOffset;

	XMLTempToken(char[] text, String type, int sOff, int eOff){
		tokenText = text;
		tokenType = type;
		startOffset = sOff;
		endOffset = eOff;
	}

	public XMLTempToken(char[] text, String type) {
		tokenText = text;
		tokenType = type;
		startOffset = -1;
		endOffset = -1;
	}

}
