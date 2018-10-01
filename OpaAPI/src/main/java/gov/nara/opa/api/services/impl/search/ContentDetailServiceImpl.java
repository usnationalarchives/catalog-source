package gov.nara.opa.api.services.impl.search;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import gov.nara.opa.api.dataaccess.search.ContentDetailDao;
import gov.nara.opa.api.services.moderator.OnlineAvailabilityHeaderService;
import gov.nara.opa.api.services.search.ContentDetailService;
import gov.nara.opa.api.services.search.GetLoadBalancedSolrServer;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.utils.PTransformer;
import gov.nara.opa.api.utils.SearchUtils;
import gov.nara.opa.api.validation.search.ContentDetailRequestParameters;
import gov.nara.opa.api.validation.search.PagedContentDetailRequestParameters;
import gov.nara.opa.api.valueobject.moderator.OnlineAvailabilityHeaderValueObject;
import gov.nara.opa.api.valueobject.search.ContentDetailValueObject;
import gov.nara.opa.api.valueobject.search.SequenceQueryValueObject;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.SolrUtils;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.common.services.solr.impl.LoadBalancedHttpSolrServer;
import gov.nara.opa.common.storage.OpaStorage;
import gov.nara.opa.common.storage.OpaStorageFactory;
import gov.nara.opa.common.storage.S3OpaStorageImpl;

@Component
@Scope("prototype")
public class ContentDetailServiceImpl implements ContentDetailService {

	private static final String RESULT_FIELDS = "&resultFields=naId,opaId,title,source,sourceType,"
			+ "description,highlightedDescriptionXml,authority,"
			+ "highlightedAuthorityXml,objects,teaser,shortContent"
			+ "&f.shortContent.hl.fragsize=0";

	private static final String SEQUENCE_RESULT_FIELDS = "&resultFields=naId";

	private static OpaLogger logger = OpaLogger
			.getLogger(ContentDetailServiceImpl.class);

	@Value("${contentDetailsFilePath}")
	private String contentDetailsFilePath;

	@Autowired
	private ContentDetailDao contentDetailDao;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private GetLoadBalancedSolrServer getLoadBalancedSolrServer;

	@Autowired
	private OnlineAvailabilityHeaderService contentDetailsNotificationService;

	@Autowired
	private OpaStorageFactory opaStorageFactory;

	private LoadBalancedHttpSolrServer solrServer;

	@Value("${naraBaseUrl}")
	private String naraBaseUrl;

	@Override
	public ContentDetailValueObject getContentDetail(
			ContentDetailRequestParameters requestParameters, String opaPath,
			String query) {
		LinkedHashMap<String, Object> contentMap = new LinkedHashMap<String, Object>();
		double queryTime = 0.0;

		// Retrieve the naId from the request parameters
		String naId = requestParameters.getNaId();

		// Set naId in response object: ContentDetailValueObject
		ContentDetailValueObject cdvo = new ContentDetailValueObject();
		cdvo.setNaId(naId);

		query = query + RESULT_FIELDS;

		try {

			// Execute the search
			QueryResponse qryResponse = getQueryResponse(query, opaPath);

			// Extract the search results
			SolrDocumentList resultsList = qryResponse.getResults();

			// Retrieve the query response time
			queryTime = qryResponse.getQTime();

			logger.info(" QueryTime: " + queryTime);

			String opaId = "";
			String title = "";
			String source = "";
			String sourceType = "";
			String descriptionXml = "";
			String authorityXml = "";
			String objectsXml = "";
			String shortContent = "";

			descriptionXml = getStringValue("highlightedDescriptionXml",
					qryResponse);

			authorityXml = getStringValue("highlightedAuthorityXml",
					qryResponse);

			// Process search results
			if (resultsList.size() > 0) {

				SolrDocument doc = resultsList.get(0);

				// Retrieve the opaId
				if (doc.getFieldValue("opaId") != null)
					opaId = (String) doc.getFieldValue("opaId");
				cdvo.setOpaId(opaId);

				// Retrieve the title
				if (doc.getFieldValue("title") != null)
					title = StringUtils.removeMarkUps((String) doc
							.getFieldValue("title"));
				cdvo.setTitle(title);

				if (doc.getFieldValue("shortContent") != null) {
					shortContent = StringUtils.removeMarkUps((String) doc.getFieldValue("shortContent"));
				}

				if (shortContent.isEmpty()) {
					if (doc.getFieldValue("teaser") != null) {
						shortContent = (String) doc.getFieldValue("teaser");
					}
				}

				cdvo.setShortContent(shortContent);

				// Retrieve the source
				if (doc.getFieldValue("source") != null)
					source = (String) doc.getFieldValue("source");

				// Retrieve the description
				if (StringUtils.isNullOrEmtpy(descriptionXml)
						&& doc.getFieldValue("description") != null)
					descriptionXml = (String) doc.getFieldValue("description");

				// Retrieve the authority
				if (StringUtils.isNullOrEmtpy(authorityXml)
						&& doc.getFieldValue("authority") != null)
					authorityXml = (String) doc.getFieldValue("authority");

				// Retrieve the objects
				if (doc.getFieldValue("objects") != null)
					objectsXml = (String) doc.getFieldValue("objects");

				if (source.equals("holdings")) {

					descriptionXml = getOnlineAvailabilityHeader(naId,
							descriptionXml);

					// Add description.xml to contentMap
					contentMap.put("description",
							createHtmlFromXml(descriptionXml));

					// Add modifed objects.xml to contentMap
					contentMap.put("objects",
							processObjectsXml(naId, objectsXml));
					if (contentMap.get("objects") == null) {
						sourceType = "DescriptionsOnly";
					} else {
						sourceType = "OnlineHoldings";
					}

				} else if (source.equals("authorities")) {
					sourceType = "AuthorityRecord";

					// Add authority.xml to contentMap
					contentMap.put("authorities",
							createHtmlFromXml(authorityXml));

				} else {

					sourceType = "WebPages";

					// If not authorities or description --> Source = Web
					contentMap.put("web", null);

				}
				// Add the sourceType to the value object
				cdvo.setSourceType(sourceType);

				// Add the content to the value object
				cdvo.setContent(contentMap);

			} else {
				logger.error(String.format("Result set is empty: %1$s",
						qryResponse.getRequestUrl()));

				// Add the sourceType to the value object
				cdvo.setSourceType(null);

				// Add the content to the value object
				cdvo.setContent(null);
			}

		} catch (SolrServerException e) {
			logger.error(e);
			throw new OpaRuntimeException(e);
		} catch (Exception e) {
			logger.error(e);
			throw new OpaRuntimeException(e);
		}

		return cdvo;
	}

	@Override
	public ContentDetailValueObject getContentDetail(
			PagedContentDetailRequestParameters requestParameters,
			String opaPath, String query) {
		boolean highlight = requestParameters.isHighlight();

		LinkedHashMap<String, Object> contentMap = new LinkedHashMap<String, Object>();
		double queryTime = 0.0;

		ContentDetailValueObject cdvo = new ContentDetailValueObject();

		try {

			/*************** BUILD QUERY ********************/

			SequenceQueryValueObject sequenceQueryVO = new SequenceQueryValueObject();

			// Exclude Web Results if tabType=all
			query = SearchUtils.getQueryTabFilter(
					requestParameters.getTabType(), query);

			setSequenceQuery(sequenceQueryVO, query,
					requestParameters.getOffset(),
					requestParameters.getAccountType());

			setPrevAndNextNaIds(sequenceQueryVO, opaPath, cdvo);

			// Get Highlight settings into query
			query = SearchUtils.getHighlightedQuery(highlight, query, true);

			// Add thesaurus and result fields
			query = SearchUtils.getThesaurusAndResultFields(query,
					RESULT_FIELDS);

			/*********** END BUILD QUERY ********************/

			// Execute the search
			QueryResponse qryResponse = getQueryResponse(query, opaPath);

			// Extract the search results
			SolrDocumentList resultsList = qryResponse.getResults();

			// Retrieve the query response time
			queryTime = qryResponse.getQTime();

			logger.info(" QueryTime: " + queryTime);

			String opaId = "";
			String title = "";
			String source = "";
			String sourceType = "";
			String descriptionXml = "";
			String authorityXml = "";
			String objectsXml = "";
			String naId = "";
			String shortContent = "";

			descriptionXml = getStringValue("highlightedDescriptionXml",
					qryResponse);

			authorityXml = getStringValue("highlightedAuthorityXml",
					qryResponse);

			cdvo.setTotalCount(resultsList.getNumFound());

			// Process search results
			if (resultsList.size() > 0) {

				SolrDocument doc = resultsList.get(0);

				// Retrieve the naId
				if (doc.getFieldValue("naId") != null)
					naId = (String) doc.getFieldValue("naId");

				naId = StringUtils.removeMarkUps(naId);

				cdvo.setNaId(naId);

				// Retrieve the opaId
				if (doc.getFieldValue("opaId") != null)
					opaId = (String) doc.getFieldValue("opaId");
				cdvo.setOpaId(opaId);

				// Retrieve the title
				if (doc.getFieldValue("title") != null)
					title = StringUtils.removeMarkUps((String) doc
							.getFieldValue("title"));
				cdvo.setTitle(title);

				// Retrieve the shortContent/teaser
				if (doc.getFieldValue("shortContent") != null) {
					shortContent = StringUtils.removeMarkUps((String) doc.getFieldValue("shortContent"));
				}

				if (shortContent.isEmpty()) {
					if (doc.getFieldValue("teaser") != null) {
						shortContent = (String) doc.getFieldValue("teaser");
					}
				}

				cdvo.setShortContent(shortContent);

				// Retrieve the source
				if (doc.getFieldValue("source") != null)
					source = (String) doc.getFieldValue("source");

				// Retrieve the description
				if (StringUtils.isNullOrEmtpy(descriptionXml)
						&& doc.getFieldValue("description") != null)
					descriptionXml = (String) doc.getFieldValue("description");

				// Retrieve the authority
				if (StringUtils.isNullOrEmtpy(authorityXml)
						&& doc.getFieldValue("authority") != null)
					authorityXml = (String) doc.getFieldValue("authority");

				// Retrieve the objects
				if (doc.getFieldValue("objects") != null)
					objectsXml = (String) doc.getFieldValue("objects");

				if (source.equals("holdings")) {

					descriptionXml = getOnlineAvailabilityHeader(naId,
							descriptionXml);

					// Add description.xml to contentMap
					contentMap.put("description",
							createHtmlFromXml(descriptionXml));

					// Add modifed objects.xml to contentMap
					contentMap.put("objects",
							processObjectsXml(naId, objectsXml));
					if (contentMap.get("objects") == null) {
						sourceType = "DescriptionsOnly";
					} else {
						sourceType = "OnlineHoldings";
					}

				} else if (source.equals("authorities")) {
					sourceType = "AuthorityRecord";

					// Add authority.xml to contentMap
					contentMap.put("authorities",
							createHtmlFromXml(authorityXml));

				} else {

					sourceType = "WebPages";

					// If not authorities or description --> Source = Web
					contentMap.put("web", null);

				}
				// Add the sourceType to the value object
				cdvo.setSourceType(sourceType);

				// Add the content to the value object
				cdvo.setContent(contentMap);

			} else {
				logger.error(String.format("Result set is empty: %1$s",
						qryResponse.getRequestUrl()));

				// Add the sourceType to the value object
				cdvo.setSourceType(null);

				// Add the content to the value object
				cdvo.setContent(null);
			}

		} catch (SolrServerException e) {
			logger.error(e);
			throw new OpaRuntimeException(e);
		} catch (Exception e) {
			logger.error(e);
			throw new OpaRuntimeException(e);
		}

		return cdvo;
	}

	private void setPrevAndNextNaIds(SequenceQueryValueObject sequenceQueryVO,
			String opaPath, ContentDetailValueObject cdvo) {

		try {
			QueryResponse response = getQueryResponse(
					sequenceQueryVO.getSequenceQuery(), opaPath);

			if (response.getResults() != null
					&& response.getResults().size() > 0) {

				if (sequenceQueryVO.getOriginalOffset() == 0
						&& sequenceQueryVO.getOriginalOffset() == sequenceQueryVO
								.getOffset()) {
					if (response.getResults().size() == 2
							&& response.getResults().get(1)
									.getFieldValue("naId") != null) {
						cdvo.setNextNaId(StringUtils.removeMarkUps(response
								.getResults().get(1).getFieldValue("naId")
								.toString()));
					}
				}

				if (sequenceQueryVO.getOriginalOffset() != sequenceQueryVO
						.getOffset()) {
					if (response.getResults().size() == 2) {
						if (response.getResults().get(0).getFieldValue("naId") != null) {
							cdvo.setPrevNaId(StringUtils.removeMarkUps(response
									.getResults().get(0).getFieldValue("naId")
									.toString()));
						}
					}

					if (response.getResults().size() == 3) {
						if (response.getResults().get(2).getFieldValue("naId") != null) {
							cdvo.setPrevNaId(StringUtils.removeMarkUps(response
									.getResults().get(0).getFieldValue("naId")
									.toString()));
							cdvo.setNextNaId(StringUtils.removeMarkUps(response
									.getResults().get(2).getFieldValue("naId")
									.toString()));
						}
					}
				}

			}

		} catch (Exception e) {
			logger.error("NARA-1902:Error getting prev and next");
			throw new OpaRuntimeException(e);
		}

	}

	private String getStringValue(String fieldName, QueryResponse qryResponse) {
		if (qryResponse.getResponse().get(fieldName) != null) {
			return (String) qryResponse.getResponse().get(fieldName);
		}
		return null;
	}

	private QueryResponse getQueryResponse(String query, String opaPath)
			throws SolrServerException {

		/**********************************************************************************/
		// START: Using Solr Software Load Balancing
		solrServer = getLoadBalancedSolrServer.getServer();
		// END: Using Solr Software Load Balancing
		/**********************************************************************************/

		logger.info(" Content Detail Query: " + query);

		// Build the Solr parameters
		SolrUtils sUtils = new SolrUtils();
		SolrParams solrParams = sUtils.makeParams(opaPath, query, -1);

		// Execute the search
		return solrServer.query(solrParams);

	}

	// Set the hasAnnotation attribute for each objectId
	public AspireObject processObjectsXml(String naId, String objectsXml) {

		AspireObject objectsXmlObject = new AspireObject("objects");
		boolean hasObjects = false;

		try {
			if (objectsXml != null && !objectsXml.equals("")) {
				objectsXmlObject.loadXML(new StringReader(objectsXml));
				List<AspireObject> objectList = objectsXmlObject
						.getAll("objects").get(0).getAll("object");

				for (int i = 0; i < objectList.size(); i++) {
					hasObjects = true;
					boolean isNewObjectKeyFormat = false;
					AspireObject objectIdObject = objectList.get(i);
					String objectId = objectIdObject.getAttribute("id");

					// For each object in objects.xml - check if an annotation
					// exists
					boolean activeAnnotationExists = contentDetailDao
							.annotationExists(naId, objectId);

					List<AspireObject> listFiles = objectIdObject
							.getAll("file");
					if (listFiles != null && listFiles.size() > 0) {
						OpaStorage storage = opaStorageFactory.createOpaStorage();
						for (AspireObject file : listFiles) {
							String path = file.getAttribute("path");
							String mimeType = file.getAttribute("mime");
							String newObjectKey = path;
							String type = file.getAttribute("type");


							// remove the original jpeg2000 or bmp or TIFF files from list
							// they are causing problems with the UI.
							// their JPEG converted counterparts are still displayed
							if ("image/jp2".equalsIgnoreCase(mimeType)
						         || "image/bmp".equalsIgnoreCase(mimeType)
									|| "image/tiff".equalsIgnoreCase(mimeType)) {
								if ( "archival".equalsIgnoreCase(type)) {
									objectIdObject.removeChildren(file);
									continue;
								}
							}

							// remove leading forward slash
							if (newObjectKey.startsWith("/")) {
								newObjectKey = newObjectKey.substring(1);
							}

							// if the path starts with "lz" it's a post-TO4, new, format URL
							if (newObjectKey.startsWith("lz")) {
								isNewObjectKeyFormat = true;
							}

							if (isNewObjectKeyFormat) {
								logger.debug("found new object key: " + newObjectKey);
								String url = naraBaseUrl + "catalogmedia/" + newObjectKey;
								file.setAttribute("url", url);
                                if ("primary".equalsIgnoreCase(type)) {
                                    String renditionS3BaseUrl = storage.getURL(newObjectKey.replaceFirst("lz", "live"));
                                    String renditionBaseUrl = naraBaseUrl + "catalogmedia/" + newObjectKey.replaceFirst("lz", "live");
                                    objectIdObject.setAttribute("renditionBaseUrl", renditionBaseUrl);
                                    objectIdObject.setAttribute("renditionS3BaseUrl", renditionS3BaseUrl);
                                    logger.debug("setting renditionBaseUrl: " + renditionBaseUrl);
                                }
							} else {
								logger.debug("found legacy object key: " + newObjectKey);
								String url = naraBaseUrl + "OpaAPI/media/"+naId+"/"+path;
								file.setAttribute("url", url);
								if ("primary".equalsIgnoreCase(type)) {
									String renditionBaseUrl = naraBaseUrl + "OpaAPI/media/"+naId;
									String renditionS3BaseUrl = renditionBaseUrl;
									objectIdObject.setAttribute("renditionBaseUrl", renditionBaseUrl);
									objectIdObject.setAttribute("renditionS3BaseUrl", renditionS3BaseUrl);
									logger.debug("setting renditionBaseUrl: " + renditionBaseUrl);
								}
							}

                            if (mimeType.contains("audio")
                                    || mimeType.contains("video")) {
                                file.setAttribute("path", path);
                                file.setAttribute("stream", path);
                                if (storage instanceof S3OpaStorageImpl) {
                                    String newPath = storage
                                            .getCloudFrontURL(storage
                                                    .getFullPathInLive(
                                                            path,
                                                            Integer.parseInt(naId)));
                                    file.setAttribute("stream", newPath);
                                }
                            }
						}
					}
					if (isNewObjectKeyFormat) {
						AspireObject thumbnailObject = objectIdObject.get("thumbnail");
						if(thumbnailObject != null && objectIdObject.getAttribute("renditionBaseUrl") != null){
							thumbnailObject.setAttribute("url",objectIdObject.getAttribute("renditionBaseUrl") + "/" + thumbnailObject.getAttribute("path").replaceFirst("content","live"));
						}
						AspireObject imageTilesObject = objectIdObject.get("imageTiles");
						if(imageTilesObject != null && objectIdObject.getAttribute("renditionS3BaseUrl") != null){
							imageTilesObject.setAttribute("url",objectIdObject.getAttribute("renditionS3BaseUrl") + "/" + imageTilesObject.getAttribute("path").replaceFirst("content","live"));
						}
					} else {
						AspireObject thumbnailObject = objectIdObject.get("thumbnail");
						if(thumbnailObject != null && objectIdObject.getAttribute("renditionBaseUrl") != null){
							thumbnailObject.setAttribute("url",objectIdObject.getAttribute("renditionBaseUrl") + "/" + thumbnailObject.getAttribute("path").replaceFirst("content","live"));
						}
						AspireObject imageTilesObject = objectIdObject.get("imageTiles");
						if(imageTilesObject != null && objectIdObject.getAttribute("renditionBaseUrl") != null){
							imageTilesObject.setAttribute("url",objectIdObject.getAttribute("renditionBaseUrl") + "/" + imageTilesObject.getAttribute("path").replaceFirst("content","live"));
						}
					}


					// Set the hasAnnotation attribute for each objectId
					objectIdObject = objectIdObject.setAttribute(
							"hasAnnotation",
							String.valueOf(activeAnnotationExists));
				}
			}
		} catch (AspireException e) {
			logger.error(e.getMessage(), e);
			throw new OpaRuntimeException(e);
		} finally {
			try {
				objectsXmlObject.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new OpaRuntimeException(e);
			}
		}

		if (!hasObjects) {
			return null;
		}

		return objectsXmlObject;
	}

	// Transform XML to XHTML using the XSL specific to the record type
	public String createHtmlFromXml(String xml) {
		String html = "";
		try {
			System.setProperty("javax.xml.transform.TransformerFactory",
					"net.sf.saxon.TransformerFactoryImpl");

			javax.xml.transform.TransformerFactory transFact = javax.xml.transform.TransformerFactory
					.newInstance();
			javax.xml.transform.Source xsltSource = new javax.xml.transform.stream.StreamSource(
					new File(contentDetailsFilePath));
			javax.xml.transform.Transformer trans;
			trans = transFact.newTransformer(xsltSource);
			PTransformer ptransformer = new PTransformer(trans);

			// Description XML
			html = ptransformer.transform(xml);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new OpaRuntimeException(e);
		}
		return html;
	}

	private void setSequenceQuery(SequenceQueryValueObject sequenceQuery,
			String query, int offset, String accountType) {
		String result = query;

		sequenceQuery.setOriginalOffset(offset);

		// Set result fields
		result += SEQUENCE_RESULT_FIELDS;

		// Set rows and offset values
		if (offset == 0) {
			// If at the beginning, return only current and next
			result = result.replaceAll("rows=\\d+", "rows=2");
			sequenceQuery.setRows(2);
			sequenceQuery.setOffset(0);

		} else {
			// If not at the beginning decrease offset by one to return previous
			result = result.replaceAll("offset=\\d+", "offset=" + (--offset));
			sequenceQuery.setOffset(offset);

			int maxRows = configurationService
					.getSearchLimitForUser(accountType);

			if (offset != maxRows - 2) {
				// If not at search limit by user set rows to 3
				// (prev-current-next)
				result = result.replaceAll("rows=\\d+", "rows=3");
			} else {
				// If at search limit, set rows to 2 (prev-current)
				result = result.replaceAll("rows=\\d+", "rows=2");
			}
		}

		sequenceQuery.setSequenceQuery(result);
		// System.out.println(sequenceQuery.getSequenceQuery());

	}

	private String getOnlineAvailabilityHeader(String naId,
			String descriptionXml) throws ParserConfigurationException,
			SAXException, IOException {
		OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader = contentDetailsNotificationService
				.getOnlineAvailabilityHeaderByNaId(naId);

		String result = descriptionXml;
		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();

		InputSource inputSource = new InputSource(new StringReader(
				descriptionXml));
		Document doc = dBuilder.parse(inputSource);

		if (onlineAvailabilityHeader.getHeader() == null
				&& onlineAvailabilityHeader.getAvailabilityTS() == null) {
			Element notificationXml = doc.createElement("online-availability");
			notificationXml.setAttribute("enabled", "true");
			notificationXml.setAttribute("naId", naId);
			doc.getDocumentElement().appendChild(notificationXml);

		} else {
			Element notificationXml = doc.createElement("online-availability");
			notificationXml.setAttribute("enabled", onlineAvailabilityHeader
					.getStatus().toString());
			notificationXml.setAttribute("naId",
					onlineAvailabilityHeader.getNaId());
			notificationXml
					.setTextContent(onlineAvailabilityHeader.getHeader());
			doc.getDocumentElement().appendChild(notificationXml);
		}

		OutputFormat format = new OutputFormat(doc);
		StringWriter stringOut = new StringWriter();
		XMLSerializer serial = new XMLSerializer(stringOut, format);
		serial.serialize(doc);

		result = stringOut.toString();

		return result;
	}

}
