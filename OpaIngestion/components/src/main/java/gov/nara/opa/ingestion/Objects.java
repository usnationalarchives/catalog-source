/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nara.opa.ingestion;

import java.util.ArrayList;
import java.util.Map;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;

/**
 * 
 * @author pmart�nez
 */
public abstract class Objects {

	public static final String OPA_STORAGE_DIR = "opaStorageDir";
	public static final String OPA_STORAGE_OBJECTS_FILE = "objects.xml";
	public static final String LIVE_FOLDER = "live";
	public static final String THUMBNAIL_NODE = "thumbnail";
	public static final String ZOOM_IMAGES_NODE = "imageTiles";
	public static final String TECHNICAL_METADATA_NODE = "technicalMetadata";

	/**
	 * Map NARA object type IDs to MIME types.
	 * 
	 * @param extension
	 *            The file extension.
	 * @param objectType
	 *            The termName for digital object type in DAS XML.
	 * @return The MIME type string for the object type.
	 */
	public static String getMimeTypeFromExtension(String extension,
			String objectType) {
		switch (extension.toLowerCase()) {
		case "shp":
		case "shx":
		case "dbf":
			// Shapefile
			return "text/plain";
		case "tiff":
		case "tif":
			// Image (TIFF)
			return "image/tiff";
		case "wav":
			// Sound File (WAV)
			return "audio/wav";
		case "wmv":
			// Audio/Visual File (WMV)
			return "video/x-ms-wmv";
		case "doc":
		case "docx":
			// Microsoft Word Document
			return "application/msword";
		case "wri":
			// Microsoft Write Document *
			return "application/mswrite";
		case "htm":
			// World Wide Web Page *
			return "text/html";
		case "xml":
			// XML - Extensible Markup Language
			return "text/xml";
		case "zip":
			// ZIP
			return "application/zip";
		case "txt":
			// ASCII Text
			return "text/plain";
		case "avi":
			// Audio/Visual File (AVI)
			return "video/avi";
		case "csv":
			// Comma-separated values (CSV)
			return "text/csv";
		case "xlsx":
		case "xls":
			// MS Excel Spreadsheet
			return "application/excel";
		case "exe":
			// Executable program (exe file)
			return "application/octet-stream";
		case "gif":
			// Image (GIF)
			return "image/gif";
		case "html":
			// HTML
			return "text/html";
		case "jp2":
		case "jp2000":
			// Image (JP2)
			return "image/jp2";
		case "jpg":
		case "jpeg":
			// Image (JPG)
			return "image/jpeg";
		case "mov":
			// Audio/Visual File (MOV)
			return "video/quicktime";
		case "mp3":
			// Sound File (MP3)
			return "audio/mpeg3";
		case "mp4":
			// Audio/Visual File (MP4)
			return "video/mp4";
		case "pdf":
			// Portable Document File (PDF)
			return "application/pdf";
		case "bmp":
			// Image (BMP)
			return "image/bmp";
		case "ppt":
		case "pptx":
			// Microsoft PowerPoint Document
			return "application/mspowerpoint";
		case "rm":
		case "rmvb":
			// Audio/Visual (RealMedia Video Stream)
			return "application/vnd.rn-realmedia";
		case "rtf":
			// Rich Text Format (RTF)
			return "text/rtf";
		case "vsd":
		case "vss":
		case "vst":
		case "vsw":
		case "vdx":
		case "vsx":
		case "vtx":
		case "vsdx":
		case "vsdm":
		case "vssx":
		case "vssm":
		case "vstx":
		case "vstm":
		case "vsl":
			// Visio
			return "application/x-visio";
		default:
			return getMimeTypeFromTermName(objectType);
		}
	}

	/**
	 * Map NARA object type IDs to MIME types.
	 * 
	 * @param termName
	 *            The ID of the object type.
	 * @return The MIME type string for the object type.
	 */
	public static String getMimeTypeFromTermName(String termName) {
		switch (termName) {
		case "Shapefile":
			return "text/plain";
		case "Image (TIFF)":
			return "image/tiff";
		case "Visio":
			return "application/x-visio";
		case "Sound File (WAV)":
			return "audio/wav";
		case "Audio/Visual File (WMV)":
			return "video/x-ms-wmv";
		case "Microsoft Word Document":
			return "application/msword";
		case "Microsoft Write Document":
			return "application/mswrite";
		case "World Wide Web Page":
			return "text/html";
		case "XML - Extensible Markup Language":
			return "text/xml";
		case "ZIP":
			return "application/zip";
		case "ASCII Text":
			return "text/plain";
		case "Audio/Visual File (AVI)":
			return "video/avi";
		case "Comma-separated values (CSV)":
			return "text/csv";
		case "EBCDIC Text":
			return "text/plain";
		case "MS Excel Spreadsheet":
			return "application/excel";
		case "Executable program (exe file)":
			return "application/octet-stream";
		case "Image (GIF)":
			return "image/gif";
		case "HTML":
			return "text/html";
		case "Image (JP2)":
			return "image/jp2";
		case "Image (JPG)":
			return "image/jpeg";
		case "Audio/Visual File (MOV)":
			return "video/quicktime";
		case "Sound File (MP3)":
			return "audio/mpeg3";
		case "Audio/Visual File (MP4)":
			return "video/mp4";
		case "Portable Document File (PDF)":
			return "application/pdf";
		case "Image (BMP)":
			return "image/bmp";
		case "Microsoft PowerPoint Document":
			return "application/mspowerpoint";
		case "Audio/Visual (RealMedia Video Stream)":
			return "application/vnd.rn-realmedia";
		case "Rich Text Format (RTF)":
			return "text/rtf";
		default:
			return termName;
		}
	}

	public static void getSortNums(AspireObject objects,
			Map<String, Integer> sortNums) throws AspireException {
		if (objects == null) {
			return;
		}

		ArrayList<String> imageObjects = new ArrayList<String>();
		ArrayList<String> audioVideoObjects = new ArrayList<String>();
		ArrayList<String> pdfObjects = new ArrayList<String>();
		ArrayList<String> otherObjects = new ArrayList<String>();

		for (AspireObject object : objects.getChildren()) {
			if (isObject(object)) {
				boolean otherType = true;
				String objectId = object.getAttribute("id");
				AspireObject fileNode = object.get("file");
				if (fileNode != null) {
					String type = fileNode.getAttribute("type");
					String mime = fileNode.getAttribute("mime");
					if ("primary".equals(type) && mime != null) {
						// Image
						if (mime.contains("image")) {
							otherType = false;
							imageObjects.add(objectId);
						}
						// Audio/Video
						else if (mime.contains("audio")
								|| mime.contains("video")) {
							otherType = false;
							audioVideoObjects.add(objectId);
						}
						// PDF
						else if (mime.contains("pdf")) {
							otherType = false;
							pdfObjects.add(objectId);
						}
					}
				}
				// Other
				if (otherType) {
					otherObjects.add(objectId);
				}
			}
		}

		int sortNum = 1;
		// Image
		for (String objectId : imageObjects) {
			sortNums.put(objectId, sortNum++);
		}
		// Audio/Video
		for (String objectId : audioVideoObjects) {
			sortNums.put(objectId, sortNum++);
		}
		// PDF
		for (String objectId : pdfObjects) {
			sortNums.put(objectId, sortNum++);
		}
		// Other
		for (String objectId : otherObjects) {
			sortNums.put(objectId, sortNum++);
		}

	}

	public static boolean isObject(AspireObject node) {
		return ObjectsXml.OBJECTS_XML_OBJECT_ELEMENT.equals(node.getName());
	}

}
