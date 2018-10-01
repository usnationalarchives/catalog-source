/**
 * Copyright Search Technologies 2014
 * for NARA OPA
 */
package gov.nara.opa.ingestion;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import com.searchtechnologies.aspire.services.Component;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;

/**
 * @author Andrew Gullett
 * 
 */
public class ObjectsXml {

	public static final String OBJECTS_XML_TOP_LEVEL_ELEMENT = "objects";
	public static final String OBJECTS_XML_OBJECT_ELEMENT = "object";
	public static final String OBJECTS_XML_VERSION_ATTRIBUTE = "version";
	public static final String OBJECTS_XML_VERSION = "OPA-OBJECTS-1.0";
	public static final String OBJECTS_XML_CREATED_ATTRIBUTE = "created";

	/**
	 * Start a new objects.xml object.
	 * 
	 * @return An empty objects.xml object.
	 */
	public static AspireObject startNewObjects() {
		AspireObject objects = new AspireObject(OBJECTS_XML_TOP_LEVEL_ELEMENT);
		objects.setAttribute(OBJECTS_XML_VERSION_ATTRIBUTE, OBJECTS_XML_VERSION);

		SimpleDateFormat objectDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss'Z'");
		objectDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		Calendar calendar = Calendar.getInstance();
		calendar.set(2015, Calendar.JANUARY, 1, 0, 0, 0);

		objects.setAttribute(OBJECTS_XML_CREATED_ATTRIBUTE,
				objectDateFormat.format(calendar.getTime()));

		return objects;
	}

	/**
	 * Create an object entry.
	 * 
	 * @param record
	 *            The source description.
	 * @param digitalObject
	 *            The source digitalObject.
	 * @param sortNums
	 *            The sort numbers for the digitalObject.
	 * @return The object entry for the digitalObject.
	 * @throws AspireException
	 */
	public static AspireObject createObjectsXmlObject(Component component, AspireObject record,
			AspireObject digitalObject, Map<String, Integer> sortNums, AspireObject previousObjects)
			throws AspireException, IOException {

		AspireObject objectsXmlObject = new AspireObject(
				OBJECTS_XML_OBJECT_ELEMENT);
		String objectId = digitalObject.getText("objectIdentifier");
		objectsXmlObject.setAttribute("id", objectId);

		String accessFilename = digitalObject.getText("accessFilename").trim();

		String extension = FilenameUtils.getExtension(accessFilename);

		String mime = Objects.getMimeTypeFromExtension(extension, digitalObject
				.get("objectType").getText("termName"));

		// get the technical metadata from the previously generated objects xml
		if (previousObjects != null) {
			AspireObject previousObject = ObjectsXml.findObject(objectId, previousObjects);
			if (previousObject != null) {
				AspireObject technicalMetadata = previousObject.get("technicalMetadata");
				if (technicalMetadata != null) {
					objectsXmlObject.add(technicalMetadata);
				}
			}
		}

		if (sortNums != null && sortNums.containsKey(objectId)) {
			objectsXmlObject.setAttribute("objectSortNum",
					Integer.toString(sortNums.get(objectId)));
		}

		if (digitalObject.getText("objectDescription") != null) {
			objectsXmlObject.add("description",
					digitalObject.getText("objectDescription"));
		}

		if (digitalObject.getText("objectDesignator") != null) {
			objectsXmlObject.add("designator",
					digitalObject.getText("objectDesignator"));
		}

		// Only objects for DDI descriptions require the display flag
		if (record != null
				&& isVariantType(record.get("variantControlNumberArray"),
						"Download Display Identifier")) {
			if (digitalObject.getText("display") != null) {
				String displayText = digitalObject.getText("display");
				objectsXmlObject.add("display",
						(displayText.equalsIgnoreCase("true") ? "Y" : "N"));
			} else {
				objectsXmlObject.add("display", "N");
			}
		}

		String path;
		try {
			path = new URLCodec().decode(getPathAndFilename(accessFilename));
		}catch(DecoderException e){
			throw new AspireException("Error decoding accessFilename for object XML: ",e);
		}
		String bucketName = Components.getSettings(component).getS3StorageBucketName();
		// if the URL is of form: https://s3.amazonaws.com/<bucketName>/key
		// remove the bucketName from the path
		if ( path.toCharArray()[0] == '/') {
			if( path.substring(1).startsWith(bucketName) ) {
				path = path.substring(1).replaceFirst(bucketName, "");
			}
		} else {
			if( path.startsWith(bucketName) ) {
				path = path.replaceFirst(bucketName, "");
			}
		}
		if (!path.substring(1).startsWith("lz")) {
			path = "content" + path;
		}
		String filename = FilenameUtils.getName(path).trim();

		String type = "primary";

		if (mime.equals("image/jp2") || mime.equals("image/bmp") || mime.equals("image/tiff")) {
			String jpegName = FilenameUtils.getBaseName(filename) + ".jpg";
			String jpegPath = FilenameUtils.separatorsToUnix(FilenameUtils
					.concat(FilenameUtils.getFullPath(path), jpegName));
			String jpegMime = "image/jpeg";

			objectsXmlObject.add(createFileElement(jpegName, jpegPath,
					jpegMime, type));

			objectsXmlObject.add(createFileElement(filename, path, mime,
					"archival"));

			generatingDigitalObjectsEntries(objectsXmlObject, jpegName);

		} else {
			String pathValue = isSEIPObject(accessFilename) ? filename : path;
			objectsXmlObject.add(createFileElement(filename, pathValue, mime,
					type));

			if (isDigitalObject(mime, type)) {
				generatingDigitalObjectsEntries(objectsXmlObject, filename);
			}
		}

		if (isPdfObject(mime, type)) {
			// TODO: what if thumbnail is not generated
			// xml should be updated later?
			// same for other thumbnail elements?
			addThumbnailNode(objectsXmlObject, filename);
		}

		return objectsXmlObject;
	}

	private static AspireObject createFileElement(String name, String path,
			String mime, String type) {
		AspireObject element = new AspireObject("file");
		element.setAttribute("name", name);
		element.setAttribute("path", path);
		element.setAttribute("mime", mime);
		element.setAttribute("type", type);
		return element;
	}

	/**
	 * Go through a DAS digitalObjectArray and add an object for every
	 * digitalObject.
	 * 
	 * @param objects
	 *            Where to add the objects.
	 * @param record
	 *            The source DAS description.
	 * @param sortNums
	 *            SortNums for objects in the DAS description can be null before
	 *            sortNums are loaded into jobInfo.
	 * @param dasDigitalObjectArray
	 *            The source DAS digitalObject Array.
	 * @throws AspireException
	 */
	public static void addObjectArray(Component component, AspireObject objects,
			AspireObject record, AspireObject dasDigitalObjectArray,
			Map<String, Integer> sortNums) throws AspireException {
		for (AspireObject digitalObject : dasDigitalObjectArray.getChildren()) {
			if ("digitalObject".equals(digitalObject.getName())) {
				try {
					objects.add(createObjectsXmlObject(component, record, digitalObject,
							sortNums, null));
				} catch (Throwable e) {
				}
			}
		}
	}

	public static AspireObject addObjectFromObjectsXMLFile(
			AspireObject objects, AspireObject doc) throws AspireException {
		doc.set(objects);
		return doc;
	}

	public static AspireObject updateObjectFromObjectsXMLFile(
			AspireObject oldObjects, AspireObject newObjects, AspireObject doc)
			throws AspireException {
		if (!oldObjects.toXmlString().equals(newObjects.toXmlString())) {
			doc.removeChildren(oldObjects.getName());
			doc.add(newObjects);
		}
		return doc;
	}

	public static AspireObject addThumbnailNode(AspireObject object,
			String filename) {
		AspireObject imageThumbnail = new AspireObject(Objects.THUMBNAIL_NODE);

		String path = String.format("%s/%s/%s-thumb.jpg",
				OpaStorageArea.OPA_RENDITIONS, OpaStorageArea.THUMBNAILS,
				filename);

		imageThumbnail.setAttribute("path", path);
		imageThumbnail.setAttribute("mime", "image/jpeg");
		object.add(imageThumbnail);

		return imageThumbnail;
	}

	public static AspireObject updateThumbnailNode(AspireObject object,
			String thumbnailImagePath) throws AspireException {
		AspireObject imageThumbnail = object.get(Objects.THUMBNAIL_NODE);
		imageThumbnail.setAttribute("path", thumbnailImagePath);

		return imageThumbnail;
	}

	public static AspireObject addZoomImageNode(AspireObject object,
			String filename) {
		String path = String.format("%s/%s/%s.dzi",
				OpaStorageArea.OPA_RENDITIONS, OpaStorageArea.IMAGE_TILES,
				filename);

		AspireObject imageTiles = new AspireObject(Objects.ZOOM_IMAGES_NODE);

		imageTiles.setAttribute("path", path);
		object.add(imageTiles);

		return imageTiles;
	}

	public static AspireObject updateZoomImageNode(AspireObject object,
			String dziImagePath) throws AspireException {
		AspireObject imageTiles = object.get(Objects.ZOOM_IMAGES_NODE);
		imageTiles.setAttribute("path", dziImagePath);

		return imageTiles;
	}

	public static AspireObject findObject(String objectId, AspireObject objects) {
		for (AspireObject object : objects.getChildren()) {
			if (objectId.equals(object.getAttribute("id"))) {
				return object;
			}
		}
		return null;
	}

	public static boolean isDigitalObject(String mime, String type) {
		return mime.contains("image") && type.equals("primary");
	}

	public static boolean isJpeg2000Image(String mime) {
		return mime.contains("image") && mime.contains("jp2");
	}

	public static boolean isPdfObject(String mime, String type) {
		return mime.contains("pdf") && type.equals("primary");
	}

	public static boolean isSEIPObject(String accessFilename) {
		return FilenameUtils.getName(accessFilename).contains("SEIP");
	}

	public static AspireObject generatingDigitalObjectsEntries(
			AspireObject objectsXmlObject, String filenameWithoutExtension) {
		addThumbnailNode(objectsXmlObject, filenameWithoutExtension);
		addZoomImageNode(objectsXmlObject, filenameWithoutExtension);

		return objectsXmlObject;
	}

	public static String getPathAndFilename(String accessFilename)
			throws AspireException {
		return URIUtilities.getUrlWithPathEncoded(accessFilename).getPath();
	}

	public static String getSEIPFilename(String accessFilename)
			throws AspireException {
		try {
			URI accessUri = URIUtilities.encodeWhitespace(accessFilename);
			String path = accessUri.getPath().replace("%20", " ");
			return path.substring(path.lastIndexOf('/') + 1); // return filename
																// with leading
																// slash removed
		} catch (URISyntaxException ex) {
			throw new AspireException("invalid uri", ex);
		}
	}

	private static boolean isVariantType(
			AspireObject variantControlNumberArray, String type) {

		if (variantControlNumberArray != null) {
			for (AspireObject variantControlNumber : variantControlNumberArray
					.getChildren()) {
				try {
					if (type.equalsIgnoreCase(variantControlNumber.get("type")
							.getText("termName"))) {
						return true;
					}
				} catch (AspireException e) {
					// no action required, just continue to check other
					// variantControlNumber elements
				}
			}
		}
		return false;
	}
}
