package gov.nara.opa.solr.tokenizers;

import java.util.Arrays;

public class SplitWordFilterSplitter {

	private char[] internalBuffer = null;
	private int originalOffSet;
	int startOffset=0;
	int previousType = -1;
	int currentType = -1;
	private boolean isNext=true;
	private boolean hasAlphaNumeric=false;
	private int tokenOffset;
	char[] newWord = null;

	public static int WHITESPACE=1;
	public static int PUNCTUATION=2;
	public static int ALPHA_TO_DIGIT=4;
	public static int CASE_CHANGE=8;
	
	private int whereToSplit = WHITESPACE|PUNCTUATION|ALPHA_TO_DIGIT|CASE_CHANGE;
	
	public SplitWordFilterSplitter() {
		this(WHITESPACE|PUNCTUATION|ALPHA_TO_DIGIT|CASE_CHANGE);
	}
	
	public SplitWordFilterSplitter(int splitOn){
		whereToSplit=splitOn;
	}

	public void reset(char[] termBuffer, int offset){
		//We create a copy of the buffer for internal use:
		//internalBuffer = termBuffer.clone();
		internalBuffer = termBuffer.clone();
		//And save the original offset
		originalOffSet = offset;
		newWord = null;
		
		//Reset all conditions:
		isNext=true;
		hasAlphaNumeric=false;
		startOffset=0;
		previousType = -1;
		currentType = -1;
		tokenOffset = 0;
	}


	public char[] next() {
		if (internalBuffer==null){
			throw new RuntimeException("reset() must be called before processing the tokens");
		}
		//Start consuming the buffer:
		
		int i = startOffset;
		int j = internalBuffer.length;
		if (i==j && j==0){
			return new char[0];
		}
		newWord = null;
		while (true){
			if (i==j){
				//we got to the end of the array, no subwords present
				isNext=false;
				newWord=Arrays.copyOfRange(internalBuffer, startOffset, i);
				tokenOffset=startOffset;
				return newWord;
			}
			if (previousType==-1){
				previousType=-1;
				currentType=Character.getType(internalBuffer[i]);
			} 
			if (isLetter(Character.getType(internalBuffer[i]))||isNumber(Character.getType(internalBuffer[i]))){
				hasAlphaNumeric=true;
			}
			//
			// 1.Split words on any white space.
			// 		For example:  “computer programmer”  => [computer, programmer]
			if (((whereToSplit&WHITESPACE)>0)&&Character.getType(internalBuffer[i]) == Character.SPACE_SEPARATOR){
				if (hasAlphaNumeric){
					newWord = Arrays.copyOfRange(internalBuffer, startOffset, i);
					tokenOffset=startOffset;
					if(i+1 == j){
						isNext=false;
						if (hasAlphaNumeric){
							
							startOffset=i+1;
							return newWord;
						}
						return new char[0];
					}
					startOffset=i+1;
					break;
				} else {
					i++;
					startOffset++;
					continue;
				}
			}

			// 2.Split words on any punctuation character
			// 		For example:  “computer-programmer”  =>  [computer, programmer]
			// 3.Remove words which are all punctuation
			// 		For example:  “&//@$//_” =>  []
			// 4.Remove punctuation from the beginnings and endings of words:
			// 		For example:  “--this++”  =>  [this]
			if (((whereToSplit&PUNCTUATION)>0)&&isPunctuationOrSymbol( Character.getType(internalBuffer[i]) )){
				//newWord = Arrays.copyOfRange(internalBuffer, startOffset, i);
				if (hasAlphaNumeric){
					newWord = Arrays.copyOfRange(internalBuffer, startOffset, i);
					tokenOffset=startOffset;
					if(i+1 == j){
						isNext=false;
						if (hasAlphaNumeric){
							
							return newWord;
						}
						return new char[0];
					}
					startOffset=i+1;
					break;
				} else {
					i++;
					startOffset++;
					continue;
				}
			}
			// 5.Split words on case change from lower-case to upper case
			// 		For example:  “computerProgrammer”  => [computer, Programmer]
			if (((whereToSplit&CASE_CHANGE)>0)&&caseChanged(previousType,Character.getType(internalBuffer[i]))){
				newWord = Arrays.copyOfRange(internalBuffer, startOffset, i);
				tokenOffset=startOffset;
				startOffset=i;
				break;
			}
			// 6.Split words on character type change from alpha to digit
			// 		For example:  “rx300a” =>  [rx, 300, a]

			if (((whereToSplit&ALPHA_TO_DIGIT)>0)&&numberAlphaChanged(previousType,Character.getType(internalBuffer[i]))){
				newWord = Arrays.copyOfRange(internalBuffer, startOffset, i);
				tokenOffset=startOffset;
				startOffset=i;
				break;
			}

			// 7.Leaves XML tokens from the XML Tokenizer unmodified
			// 		
			// For example:  “<tag>”  =>  [<tag>]		return null;
			//
			//taken care of by the 
			previousType=Character.getType(internalBuffer[i]);
			i++;
		}
		isNext=true;
		hasAlphaNumeric=false;
		previousType=-1;
		return newWord;
	}

	private boolean numberAlphaChanged(int previous, int type) {
		return
				(
						isLetter(previous)&&isNumber(type)
						||
						isNumber(previous)&&isLetter(type)
						);
	}

	private boolean isNumber(int charType) {
		return (
				charType==Character.DECIMAL_DIGIT_NUMBER ||
				charType==Character.OTHER_NUMBER
				);
	}

	private boolean isLetter(int charType){
		return 
				charType==Character.UPPERCASE_LETTER ||
				charType==Character.LOWERCASE_LETTER ||
				charType==Character.TITLECASE_LETTER ||
				charType==Character.MODIFIER_LETTER ||
				charType==Character.OTHER_LETTER ||
				charType==Character.LETTER_NUMBER ;
	}

	private boolean caseChanged(int previous, int type) {
		return (
					(
						type==Character.UPPERCASE_LETTER
						&&
						previous==Character.LOWERCASE_LETTER
					)
				);
	}

	public boolean isPunctuationOrSymbol(int charType){
		return (
				charType==Character.MATH_SYMBOL
				||
				charType==Character.OTHER_PUNCTUATION
				||
				charType==Character.OTHER_SYMBOL
				||
				charType==Character.UNASSIGNED
				||
				charType==Character.CONNECTOR_PUNCTUATION
				||
				charType==Character.CONTROL
				||				
				charType==Character.CURRENCY_SYMBOL
				||						
				charType==Character.DASH_PUNCTUATION
				||	
				charType==Character.ENCLOSING_MARK
				||	
				charType==Character.END_PUNCTUATION
				||	
				charType==Character.FINAL_QUOTE_PUNCTUATION
				||	
				charType==Character.FORMAT
				||	
				charType==Character.INITIAL_QUOTE_PUNCTUATION
				||	
				charType==Character.MODIFIER_SYMBOL
				||	
				charType==Character.START_PUNCTUATION
				);
	}

	public char[] getWord(){
		return newWord;
	}
	
	public boolean hasNextWord() {
		if (isNext){
			//This means there were chars available in the buffer, but we
			//can't tell if they are valid bytes. Let's see if at least one of them is an alphanumeric character:
			for (int i = startOffset;i<internalBuffer.length;i++){
				if (isLetter(Character.getType(internalBuffer[i]))||isNumber(Character.getType(internalBuffer[i]))){
					return true;
				}
			} 
			return false;
		}
		return isNext;
	}

	public int getOffset() {
		return originalOffSet+tokenOffset;
	}

	public void reset() {
		
		reset(new char[]{},0);
	}

}
