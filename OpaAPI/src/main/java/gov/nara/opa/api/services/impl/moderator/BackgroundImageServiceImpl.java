package gov.nara.opa.api.services.impl.moderator;

import gov.nara.opa.api.dataaccess.moderator.BackgroundImageDao;
import gov.nara.opa.api.services.moderator.BackgroundImageService;
import gov.nara.opa.api.services.search.GetLoadBalancedSolrServer;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.utils.OpaPair;
import gov.nara.opa.api.validation.moderator.CreateDeleteBackgroundImageRequestParameters;
import gov.nara.opa.api.validation.moderator.ViewBackgroundImageRequestParameters;
import gov.nara.opa.api.valueobject.moderator.BackgroundImageCollectionValueObject;
import gov.nara.opa.api.valueobject.moderator.BackgroundImageValueObject;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.SolrUtils;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;
import gov.nara.opa.common.services.solr.impl.LoadBalancedHttpSolrServer;
import gov.nara.opa.common.storage.OpaStorage;
import gov.nara.opa.common.storage.OpaStorageFactory;
import gov.nara.opa.common.storage.S3OpaStorageImpl;
import gov.nara.opa.common.storage.StorageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.io.Files;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;

@Component
@Transactional
public class BackgroundImageServiceImpl implements BackgroundImageService,
		CommonValueObjectConstants {

	private static OpaLogger logger = OpaLogger
			.getLogger(BackgroundImageServiceImpl.class);

	@Autowired
	private GetLoadBalancedSolrServer getLoadBalancedSolrServer;

	@Autowired
	OpaStorageFactory opaStorageFactory;

	@Autowired
	StorageUtils storageUtils;

	@Autowired
	private BackgroundImageDao backgroundImageDao;

	@Value(value = "${tinyfyAPIKey}")
	private String tinyfyAPIKey;

	@Value(value = "${export.nonbulk.output.location}")
	private String tempFolder;

	private static final String TINYFY_URL = "https://api.tinify.com/shrink";

	public BackgroundImageValueObject getRandomBackgroundImage(
			ViewBackgroundImageRequestParameters requestParameters) {

		if (requestParameters.getNaId() != null
				&& requestParameters.getObjectId() != null) {
			return backgroundImageDao.getRandomBackgroundImageWithException(
					requestParameters.getNaId(),
					requestParameters.getObjectId());
		}

		return backgroundImageDao.getRandomBackgroundImage();
	}

	public BackgroundImageCollectionValueObject getAllBackgroundImages() {
		BackgroundImageCollectionValueObject backgroundImages = null;
		List<BackgroundImageValueObject> images = backgroundImageDao
				.getAllBackgroundImages();
		if (images != null) {
			backgroundImages = new BackgroundImageCollectionValueObject(images);
		}
		return backgroundImages;
	}

	public BackgroundImageValueObject addBackgroundImage(
			CreateDeleteBackgroundImageRequestParameters requestParameters) {
		BackgroundImageValueObject backgroundImage = null;
		String url = null;
		String title = null;
		SolrDocument doc = queryDoc(requestParameters.getNaId(),
				requestParameters.getApiType());

		if (doc != null) {
			// Retrieve the title
			if (doc.getFieldValue("title") != null)
				title = StringUtils.removeMarkUps((String) doc
						.getFieldValue("title"));

			String objectsXml = "";
			if (doc.getFieldValue("objects") != null)
				objectsXml = (String) doc.getFieldValue("objects");

			if (objectsXml != null && !objectsXml.equals("")) {
				url = getUrl(requestParameters.getNaId(),
						requestParameters.getObjectId(), objectsXml);
			}
		}

		if (!StringUtils.isNullOrEmtpy(url)
				&& !StringUtils.isNullOrEmtpy(title)) {
			backgroundImage = backgroundImageDao.addBackgroundImage(
					requestParameters.getNaId(),
					requestParameters.getObjectId(), title, url, false);
		}

		return backgroundImage;
	}

	private SolrDocument queryDoc(String naId, String apiType) {

		SolrDocument doc = null;
		String query = "?action=search&q=naId:" + naId + "&apiType=iapi"
				+ "&resultFields=title,objects";
		String opaPath = apiType + "/" + Constants.API_VERS_NUM;

		LoadBalancedHttpSolrServer solrServer;
		try {

			/**********************************************************************************/
			// START: Using Solr Software Load Balancing
			solrServer = getLoadBalancedSolrServer.getServer();
			// END: Using Solr Software Load Balancing
			/**********************************************************************************/

			// Build the Solr parameters
			SolrUtils sUtils = new SolrUtils();
			SolrParams solrParams = sUtils.makeParams(opaPath, query, -1);

			// Execute the search
			QueryResponse qryResponse = solrServer.query(solrParams);

			// Extract the search results
			SolrDocumentList resultsList = qryResponse.getResults();

			// Process search results
			if (resultsList != null && resultsList.size() > 0) {
				doc = resultsList.get(0);
			}
		} catch (SolrServerException e) {
			logger.error(e.getMessage(), e);
			// throw new OpaRuntimeException(e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			// throw new OpaRuntimeException(e);
		}

		return doc;
	}

	private String getUrl(String naId, String objectId,
			String objectsXmlContents) {
		String url = null;
		AspireObject objectsXmlObject = new AspireObject("objects");
		if (naId != null && objectId != null) {
			OpaStorage storage = opaStorageFactory.createOpaStorage();
			try {
				if (objectsXmlContents != null
						&& !objectsXmlContents.equals("")) {
					objectsXmlObject.loadXML(new StringReader(
							objectsXmlContents));
					List<AspireObject> objectList = objectsXmlObject
							.getAll("objects").get(0).getAll("object");
					for (int i = 0; i < objectList.size(); i++) {
						AspireObject objectIdObject = objectList.get(i);
						String objectIdFromXml = objectIdObject
								.getAttribute("id");

						if (objectIdFromXml.equals(objectId)) {

							List<AspireObject> listFiles = objectIdObject
									.getAll("file");
							if (listFiles != null && listFiles.size() > 0) {
								for (AspireObject file : listFiles) {
									String path = file.getAttribute("path");
									String mimeType = file.getAttribute("mime");
									if (mimeType.contains("image")) {
										if (storage instanceof S3OpaStorageImpl) {
											String tinyfyPath = storageUtils
													.saveTinyfyImage(
															reduceImageUsingTinyfy(
																	storage,
																	path, naId),
															naId);
											if (tinyfyPath != null) {
												url = storage
														.getCloudFrontURL(tinyfyPath);
											} else {
												url = storage
														.getCloudFrontURL(storage
																.getFullPathInLive(
																		path,
																		Integer.parseInt(naId)));
											}
											break;
										} else {
											url = path;
											break;
										}
									}
								}
							}
						}
					}
				}
			} catch (AspireException e) {
				logger.error(e.getMessage(), e);
			} finally {
				try {
					objectsXmlObject.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		return url;
	}

	public File reduceImageUsingTinyfy(OpaStorage storage, String path,
			String naId) {
		File response = null;
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(TINYFY_URL)
					.openConnection();
			String auth = DatatypeConverter.printBase64Binary(String.format(
					"api:%1$s", tinyfyAPIKey).getBytes(UTF8_ENCODING));
			conn.setRequestProperty("Authorization",
					String.format("Basic %1$s", auth));
			conn.setDoOutput(true);

			OutputStream request = conn.getOutputStream();
			Files.copy(
					storage.getFile(storage.getFullPathInLive(path,
							Integer.parseInt(naId))), request);
			if (conn.getResponseCode() == HttpStatus.SC_CREATED) {
				int index = Math.max(path.lastIndexOf("/"),
						path.lastIndexOf("\\"));
				response = new File(String.format("%1$s/%2$s/%3$s", tempFolder,
						naId, path.substring(index + 1)));
				if (!response.exists()) {
					response.getParentFile().mkdirs();
					response.createNewFile();
				}
				final String url = conn.getHeaderFields().get("Location")
						.get(0);
				conn = (HttpURLConnection) new URL(url).openConnection();
				FileOutputStream outputStream = new FileOutputStream(
						response.getAbsoluteFile());
				outputStream.write(IOUtils.toByteArray(conn.getInputStream()));
				outputStream.close();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return response;
	}

	public BackgroundImageValueObject deleteBackgroundImage(
			CreateDeleteBackgroundImageRequestParameters requestParameters) {
		BackgroundImageValueObject result = null;
		if (requestParameters.getNaId() != null
				&& requestParameters.getObjectId() != null) {
			result = backgroundImageDao.deleteBackgroundImage(
					requestParameters.getNaId(),
					requestParameters.getObjectId(), false);
		}
		return result;
	}

	public void loadDefaultBackgroundImages(String images) {
		logger.info("Default Images: " + images);
		String[] defaultImages = images.split(";");
		List<OpaPair> values = new ArrayList<OpaPair>();
		for (String img : defaultImages) {
			String[] ids = img.split("/");
			if (ids != null && ids.length == 2) {
				String naId = ids[0];
				String objectId = ids[1];
				OpaPair pair = new OpaPair(naId, objectId);
				values.add(pair);
				BackgroundImageValueObject imgobj = backgroundImageDao
						.getBackgroundImage(naId, objectId, true);

				if (imgobj == null) {
					String url = null;
					String title = null;
					SolrDocument doc = queryDoc(naId, "iapi");

					if (doc != null) {
						if (doc.getFieldValue("title") != null)
							title = StringUtils.removeMarkUps((String) doc
									.getFieldValue("title"));

						String objectsXml = "";
						if (doc.getFieldValue("objects") != null)
							objectsXml = (String) doc.getFieldValue("objects");

						if (objectsXml != null && !objectsXml.equals("")) {
							url = getUrl(naId, objectId, objectsXml);
						}
					}

					if (!StringUtils.isNullOrEmtpy(url)
							&& !StringUtils.isNullOrEmtpy(title)) {
						backgroundImageDao.addBackgroundImage(naId, objectId,
								title, url, true);
					}
				}
			}
		}

		List<BackgroundImageValueObject> currentDefaultImages = backgroundImageDao
				.getAllDefaultBackgroundImages();

		if (currentDefaultImages != null) {
			for (BackgroundImageValueObject img : currentDefaultImages) {
				OpaPair pair = new OpaPair(img.getNaId(), img.getObjectId());
				if (!values.contains(pair)) {
					backgroundImageDao.deleteBackgroundImage(img.getNaId(),
							img.getObjectId(), true);
				}
			}
		}
	}
}
