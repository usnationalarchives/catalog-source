package gov.nara.opa.solr.handler.component;

import gov.nara.opa.solr.highlighter.FieldProvenanceMap;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.lucene.document.Document;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.handler.component.ShardDoc;
import org.apache.solr.handler.component.ShardRequest;
import org.apache.solr.handler.component.ShardResponse;
import org.apache.solr.util.SolrPluginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class OpaLastSearchComponent extends SearchComponent {
	
	final static Logger logger = LoggerFactory.getLogger(OpaLastSearchComponent.class);

	@Override
	public void prepare(ResponseBuilder rb) throws IOException {
		String s = "OpaLastSearchComponent::prepare";
		System.out.println(s);
	}

	@Override
	public void process(ResponseBuilder rb) throws IOException {
		String s = "OpaLastSearchComponent::process";
		NamedList debugInfo = rb.getDebugInfo();
		if (debugInfo != null) {
			if (rb.req.getContext().get("debug_qplQuery") != null) {
				String qplDebug = (String) rb.req.getContext().get("debug_qplQuery");
				debugInfo.add("qplQuery", qplDebug);
			}
		}
		System.out.println(s);
	}

	@Override
	public void finishStage(ResponseBuilder rb) {
		boolean briefResultsFound = false;
		boolean webResultsGroupFound = false;
		int webResultsGroupSize = 0;
		
		if (rb.stage == ResponseBuilder.STAGE_GET_FIELDS) {
			Map.Entry<String, Object>[] arr = new NamedList.NamedListEntry[rb.resultIds.size()];
			LinkedHashMap<String, Object> webResultsGroup = null;
			NamedList<Object> thesaurusArr = new NamedList<Object>();
			NamedList<Object> customRespArr = new NamedList<Object>();
			
			for (ShardRequest sreq : rb.finished) {
				if ((sreq.purpose & ShardRequest.PURPOSE_GET_FIELDS) == 0) continue;
				for (ShardResponse srsp : sreq.responses) {
					Object br = srsp.getSolrResponse().getResponse().get("briefResults");
					LinkedHashMap<String, Object> hl = (LinkedHashMap<String, Object>)srsp.getSolrResponse().getResponse().get("briefResults");
					if(hl != null){
						briefResultsFound = true;
						for (String key : hl.keySet()) {
							ShardDoc sdoc = rb.resultIds.get(key);
							if(sdoc != null){
								int idx = sdoc.positionInResponse;
								arr[idx] = new NamedList.NamedListEntry<Object>(key, hl.get(key));
							}							
						}
					}
				}
			}
			
			for (ShardRequest sreq : rb.finished) {
				//if ((sreq.purpose & ShardRequest.PURPOSE_GET_FIELDS) == 0) continue;
				for (ShardResponse srsp : sreq.responses) {
					if(!webResultsGroupFound || (webResultsGroupFound && !(webResultsGroupSize > 2))){
						webResultsGroup = (LinkedHashMap<String, Object>)srsp.getSolrResponse().getResponse().get("webResultsGroup");
						if(webResultsGroup != null){
							webResultsGroupFound = true;
							webResultsGroupSize = webResultsGroup.size();
						}
					}
				}
			}
			
			boolean errRespAdded = false;
			for (ShardRequest sreq : rb.finished) {
				for (ShardResponse srsp : sreq.responses) {
					LinkedHashMap<String, Object> thesaurusResp = (LinkedHashMap<String, Object>)srsp.getSolrResponse().getResponse().get("thesaurus");
					if(thesaurusResp != null){
						logger.info("Thesaurus Object Found in Shard Response.");
						for(Object k : thesaurusResp.keySet()){
							thesaurusArr.add(k.toString(), thesaurusResp.get(k));
						}
					}
					LinkedHashMap<String, Object> customErrorsResp = (LinkedHashMap<String, Object>) srsp.getSolrResponse().getResponse().get("customErrors");
					if(customErrorsResp != null && !errRespAdded){
						logger.info("Custom Errors Object Found in Shard Response.");
						for(Object k : customErrorsResp.keySet()){
							customRespArr.add(k.toString(), customErrorsResp.get(k));
							errRespAdded = true; //Add only one error to the response
						}
					}					
				}
			}
			
			for (ShardRequest sreq : rb.finished) {
		        if ((sreq.purpose & ShardRequest.PURPOSE_GET_HIGHLIGHTS) == 0) continue;
		        for (ShardResponse srsp : sreq.responses) {
		        	NamedList<Object> highlighting = (NamedList<Object>) srsp.getSolrResponse().getResponse().get("highlighting");
		    		
		    		if (highlighting == null) {
		    			return;
		    		}
		    		Iterator<Map.Entry<String, Object>> highlightingIter = highlighting.iterator();
		    		
		    		SolrDocumentList  docs = (SolrDocumentList)rb.rsp.getValues().get("response");
		    		SolrDocument document = null;
		    		if(docs != null & docs.size() == 1){														
							document = docs.get(0);																			
					}
						
		    		
		    		
		    		if ( ! highlightingIter.hasNext() ) {
		    			return;
		    		}
		    	
					Map.Entry<String, Object> hl = highlightingIter.next();
					
					FieldProvenanceMap fieldMap = new FieldProvenanceMap();
					NamedList<Object> fields = (NamedList<Object>) hl.getValue();
					for (int i = 0; i < fields.size(); i++) {
						ArrayList<String> fieldValues = (ArrayList<String>) fields.getVal(i);
						try {
							fieldMap.processString(fieldValues.get(0));
						} catch (ParseException e) {
							e.printStackTrace();
						}				
					}
		    			
					if(document != null){
						String descriptionXml = (String) document.get("description");
						if(descriptionXml != null){
							String highlightedDescriptionXml = replaceXMLWithHighlights(descriptionXml, fieldMap);
							rb.rsp.add("highlightedDescriptionXml", highlightedDescriptionXml);
						}
						
						String authorityXml = (String) document.get("authority");
						if(authorityXml != null){
							String highlightedAuthorityXml = replaceXMLWithHighlights(authorityXml, fieldMap);
							rb.rsp.add("highlightedAuthorityXml", highlightedAuthorityXml);
						}
						
						String objectXml = (String) document.get("objects");
						if(objectXml != null){
							String highlightedObjectsXml = replaceXMLWithHighlights(objectXml, fieldMap);
							rb.rsp.add("highlightedObjectsXml", highlightedObjectsXml);
						}
					}
		        }
			}
			
			// remove nulls in case not all docs were able to be retrieved
			try{
				if(arr != null && arr.length > 0 && briefResultsFound)	
					rb.rsp.add("briefResults", SolrPluginUtils.removeNulls(new SimpleOrderedMap(arr)));  
			}catch(Exception e){
				e.printStackTrace();
			}
			if(webResultsGroup != null && webResultsGroup.size() > 0)	
				rb.rsp.add("webGroupResults", webResultsGroup);  
			
			//Add the Merged custom Error Response 
			if(customRespArr != null) 
				rb.rsp.add("customErrors", customRespArr);
			
			//Further process thesaurus, deep merging, before adding to response.
			HashMap thesaurusMergeMap = new HashMap();
			Iterator it = thesaurusArr.iterator();
			
			for(int i =0; i < thesaurusArr.size(); i++){
				HashMap value = (HashMap) thesaurusArr.getVal(i);
				String key = thesaurusArr.getName(i);
				if(!thesaurusMergeMap.containsKey(key))
					thesaurusMergeMap.put(key, value);
				else{
					HashMap temp = (HashMap) thesaurusMergeMap.get(key);
					ArrayList broaderTermsExisting = (ArrayList) temp.get("broaderTerms");
					ArrayList broaderTermsNew = (ArrayList) value.get("broaderTerms");
					if(broaderTermsExisting == null)
						broaderTermsExisting = new ArrayList();
					if(broaderTermsNew != null){
						for (Object x : broaderTermsNew){
							   if (broaderTermsExisting !=null && !broaderTermsExisting.contains(x))
								   broaderTermsExisting.add(x);
						}
						if (broaderTermsExisting !=null) 
							java.util.Collections.sort(broaderTermsExisting);
						temp.remove("broaderTerms");
						temp.put("broaderTerms", broaderTermsExisting);
					}
					
					
					ArrayList narrowerTermsExisting = (ArrayList) temp.get("narrowerTerms");
					ArrayList narrowerTermsNew = (ArrayList) value.get("narrowerTerms");
					if(narrowerTermsExisting == null)
						narrowerTermsExisting = new ArrayList();
					if(narrowerTermsNew != null){
						for (Object x : narrowerTermsNew){
							   if (narrowerTermsExisting != null && !narrowerTermsExisting.contains(x))
								   narrowerTermsExisting.add(x);
						}
						if (narrowerTermsExisting !=null) 
							java.util.Collections.sort(narrowerTermsExisting);
						temp.remove("narrowerTerms");
						temp.put("narrowerTerms", narrowerTermsExisting);
					}
					
					
					ArrayList relatedTermsExisting = (ArrayList) temp.get("relatedTerms");
					ArrayList relatedTermsNew = (ArrayList) value.get("relatedTerms");
					if(relatedTermsExisting == null)
						relatedTermsExisting = new ArrayList();
					if(relatedTermsNew != null){
						for (Object x : relatedTermsNew){
							   if (relatedTermsExisting != null && !relatedTermsExisting.contains(x))
								   relatedTermsExisting.add(x);
						}
						if (relatedTermsExisting !=null)
							java.util.Collections.sort(relatedTermsExisting);
						temp.remove("relatedTerms");
						temp.put("relatedTerms", relatedTermsExisting);
					}
					
					thesaurusMergeMap.remove(key);
					thesaurusMergeMap.put(key, temp);
				}
			}
			
			System.out.println(thesaurusMergeMap);
			if(thesaurusArr != null) {
				rb.rsp.add("thesaurus", thesaurusMergeMap);
			}
		}
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		String s = "this is the description of OpaLastSearchComponent";
		return null;
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return super.getVersion();
	}

	@Override
	public NamedList getStatistics() {
		// TODO Auto-generated method stub
		return super.getStatistics();
	}

	@Override
	public String getSource() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String replaceXMLWithHighlights(String xmlString, FieldProvenanceMap fieldMap){
		System.out.println(xmlString);
		
		//Convert String to DOM document 
		//Iterate through fieldMap, for each XPath found in the Map, Replace the Node Value
		//at that XPath with the highlighted value
	    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	    domFactory.setNamespaceAware(true); 
	    DocumentBuilder builder = null;
	    org.w3c.dom.Document xmlDoc = null;
	    XPath xpath = XPathFactory.newInstance().newXPath();
	    InputSource is = new InputSource();
	    is.setCharacterStream(new StringReader(xmlString));
		try {
			builder = domFactory.newDocumentBuilder();
			xmlDoc =  builder.parse(is);
					
			XPathExpression expr ;
			
			Iterator<String> fieldMapIterator = fieldMap.keySet().iterator();
			String key;
			while(fieldMapIterator.hasNext()){
				
				key = fieldMapIterator.next().toString();
				expr = xpath.compile(key +"/text()");
				Object result = expr.evaluate(xmlDoc, XPathConstants.NODESET);
				NodeList nodes = (NodeList) result;
				for (int i = 0; i < nodes.getLength(); i++) {
					
					nodes.item(i).setNodeValue(fieldMap.get(key));
					System.out.println(nodes.item(i).getNodeValue());
				}
			}							
		} catch (ParserConfigurationException e) {			
			e.printStackTrace();
		} catch (SAXException e) {			
			e.printStackTrace();
		} catch (XPathExpressionException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	   //Convert DOM Document back to String
	   DOMSource domSource = new DOMSource(xmlDoc);
       StringWriter writer = new StringWriter();
       StreamResult result = new StreamResult(writer);
       TransformerFactory tf = TransformerFactory.newInstance();
       Transformer transformer;
		try {
			transformer = tf.newTransformer();
			transformer.transform(domSource, result); 
		} catch (TransformerConfigurationException e) {		
			e.printStackTrace();
		} catch (TransformerException e) {		
			e.printStackTrace();
		}
		return writer.toString();
		
	}

}
