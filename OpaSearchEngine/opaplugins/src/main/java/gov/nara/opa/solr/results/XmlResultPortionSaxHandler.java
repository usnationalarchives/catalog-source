package gov.nara.opa.solr.results;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.helpers.AttributesImpl;

public class XmlResultPortionSaxHandler extends DefaultHandler {

	static private Writer writer; 
	List<XmlResultPortion> allPortions = null;
	int numPrinting = 0;
	Stack<ElementData> elementStack = new Stack<ElementData>();
	boolean isDebug = false;
	boolean isFullPath = false;
	Writer debugWriter = null;

	public XmlResultPortionSaxHandler(List<XmlResultPortion> allPortions) throws UnsupportedEncodingException {
		this(allPortions, new OutputStreamWriter(System.out, "UTF8"), false);
	}

	public XmlResultPortionSaxHandler(List<XmlResultPortion> allPortions, Writer writer, boolean bFullPath) {
		this.allPortions = allPortions;
		this.isFullPath = bFullPath;
		XmlResultPortionSaxHandler.writer = writer;
	}

	public void startElement(String uri, String localName, String qualifiedName, Attributes attributes) throws SAXException {

		if (qualifiedName == null) 
			return;
		ElementData elementData = new ElementData(uri, localName, qualifiedName, attributes);
		emitDebug("Start element: <" + elementData.getElementName() + ">, pushing elementData onto stack: " + elementData.toString());
		elementStack.push(elementData);
		for (XmlResultPortion portion : allPortions) {
			if (! portion.fullMatch()) {
				if (matches(qualifiedName, portion.nextPartToMatch())) {
					emitDebug(qualifiedName + " matches portion " + portion.nextPartToMatch());
					portion.setMatching(true);
					elementData.isMatching = true;
					if (portion.fullMatch() && isExactPath(elementStack, portion)) {
						emitDebug("fullMatch, startPrinting");
						elementData.isFinalElement = true;
						elementData.matchingPortion = portion;
						startPrinting(portion, elementData);
					}
				}
			}
			else{
				if (portion.fullMatch() && isExactPath(elementStack, portion)) {
					emitDebug("fullMatch, startPrinting");
					elementData.isFinalElement = true;
					elementData.matchingPortion = portion;
					startPrinting(portion, elementData);
				}
			}
		}
		if (numPrinting > 0 && !elementData.hasStarted) {
			emit(elementData.startElementString());
			elementData.hasStarted = true;
		}
	}

	private void startPrinting(XmlResultPortion portion, ElementData elementData) throws SAXException {
		if (numPrinting > 0) {
			emit(elementData.startElementString());
			elementData.hasStarted = true;
		} else {
			for (ElementData stackElementData : elementStack) {
				if ( ! stackElementData.hasStarted) {
					if (isFullPath || (stackElementData.isMatching || stackElementData.isFinalElement)) {
						emit(stackElementData.startElementString());
						stackElementData.hasStarted = true;
					}
				}
			}
		}
		numPrinting++;
	}

	private boolean matches(String qName, String string) {
		if (qName == null) return false;
		return qName.equals(string);
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {

		ElementData poppedElement = elementStack.pop();
		emitDebug("End Element: <" + qName + ">, " + "elementStack now: " + elementStack);
		if (poppedElement.hasStarted) {
			emit(poppedElement.endElementString());
			if (poppedElement.isFinalElement) {
				numPrinting--;
				if (poppedElement.matchingPortion != null) {
					poppedElement.matchingPortion.setMatching(false);
				}
			}
		}
		emitDebug("end of endElement, numPrinting: " + numPrinting);
	}

	public void characters(char ch[], int start, int length) throws SAXException {
		if (numPrinting > 0) {
			String escapedStr = StringEscapeUtils.escapeXml(new String(ch, start, length));
			emit(escapedStr);
		}
	}
	
	private void emit(String s) throws SAXException
	{
		emitDebug("emit: " + s);
		try {
			writer.write(s);
			writer.flush();
		} catch (IOException e) {
			throw new SAXException("I/O error", e);
		}
	}

	public void setDebug(boolean b) {
		this.isDebug = b;
		if (this.isDebug && debugWriter == null) {
			try {
				debugWriter = new OutputStreamWriter(System.out, "UTF8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	private void emitDebug(String s) throws SAXException {
		if (! isDebug) return;
		try {
			debugWriter.write(">>>> " + s + " <<<<" + "\n");
			debugWriter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isExactPath(Stack<ElementData> elementStack, XmlResultPortion portion) {
		//This is a fix for NARA-OPA Jira - 142. This method will check the following.
		// [item, naId] = item.naId
		StringBuilder sb = new StringBuilder();
		Object[] objArray = elementStack.toArray();
		
		for(int i=0; i < objArray.length; i++){
			if(sb.length() == 0)
				sb.append(((ElementData)objArray[i]).qualifiedName);
			else
				sb.append("." + ((ElementData)objArray[i]).qualifiedName);
		}
		System.out.println(sb.toString());
		if(sb.toString().equalsIgnoreCase(portion.portionPath))
			return true;
		return false;
	}


	private class ElementData {
		String uri;
		String localName;
		String qualifiedName;
		Attributes attributes;
		XmlResultPortion matchingPortion = null;
		boolean hasStarted = false;
		boolean isFinalElement = false;
		boolean isMatching = false;

		public ElementData(String uri, String localName, String qualifiedName,
				Attributes attributes) {
			super();
			this.uri = uri;
			this.localName = localName;
			this.qualifiedName = qualifiedName;
			this.attributes = new AttributesImpl(attributes);
		}

		@Override
		public String toString() {
			return getElementName();
		}

		String getElementName() {
			String elementName = localName; // element name
			if ("".equals(elementName))
				elementName = qualifiedName; // not namespace-aware
			return elementName;
		}
		
		public String startElementString() {
			StringBuilder sb = new StringBuilder();
			String elementName = getElementName();
			sb.append("<"+elementName);
			attributes = this.attributes;
			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					String aName = attributes.getLocalName(i); // Attr name
					if ("".equals(aName))
						aName = attributes.getQName(i);
					sb.append(" ");
					sb.append(aName+"=\""+attributes.getValue(i)+"\"");
				}
			}
			sb.append(">");
			return sb.toString();
		}

		public String endElementString() {
			String elementName = getElementName();
			String ret =  "</" + elementName + ">";
			return ret;
		}
	}
	
	
	


}
