package gov.nara.opa.common.services.docstransforms.impl.doccreators;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.common.services.docstransforms.Constants;
import gov.nara.opa.common.storage.OpaStorage;
import gov.nara.opa.common.storage.OpaStorageFactory;
import gov.nara.opa.common.valueobject.export.AccountExportStatusEnum;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.DigitalObjectValueObject;
import gov.nara.opa.common.valueobject.export.ValueHolderValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;
import gov.nara.opa.common.valueobject.search.SolrQueryResponseValueObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Component
@Scope("prototype")
public class PdfDocumentCreator extends AbstractAspireObjectDocumentCreator
		implements DocumentCreator, Constants {

	public static final int MAX_LINE_LENGTH = 82;

	private static final float THUMBNAIL_HEIGHT = 80f;
	private static final float THUMBNAIL_WIDTH = 64f;
	private static final int THUMBNAIL_TABLE_COL_NO = 8;
	
	private static long LONG_TIMED_DOCUMENT_MILLIS = 10000;

	@Value(value = "${opaStorage.baseLocation}")
	String opaStorageBaseLocation;

	@Autowired
	OpaStorageFactory opaStorageFactory;

	private static OpaLogger logger = OpaLogger
			.getLogger(PdfDocumentCreator.class);

	@Override
	protected int getMaxLineLength() {
		return MAX_LINE_LENGTH;
	}

	@Override
	public void createDocument(LinkedList<ValueHolderValueObject> records,
			OutputStream outputStream, int documentIndex, int totalDocuments,
			boolean flush, String resultType,
			AccountExportValueObject accountExport, Object writer,
			SearchRecordValueObject document,
			SolrQueryResponseValueObject queryResponse) throws IOException {

		Document pdfDocument = (Document) ((Object[]) writer)[0];
		PdfWriter pdfWriter = (PdfWriter) ((Object[]) writer)[1];
		try {
			long startTime = new Date().getTime();
			
			if(records != null) {
				logger.debug(String.format("Adding %1$d records to document", records.size()));
			} else {
				logger.debug("No records to add");
			}
			
			addContentToPdf(pdfDocument, pdfWriter, records, documentIndex,
					totalDocuments, accountExport, document);
			long endTime = new Date().getTime();
			if(endTime - startTime > LONG_TIMED_DOCUMENT_MILLIS) {
				logger.info(String.format("ExportId:[%1$d] Document with index [%2$d] took more than [%3$s] millisecs: [%4$d] - opaId: %5$s", 
						accountExport.getExportId(), documentIndex, LONG_TIMED_DOCUMENT_MILLIS, endTime - startTime, document.getOpaId()));
			}
		} catch (DocumentException e) {
			throw new OpaRuntimeException(e);
		}
	}

	private void addContentToPdf(Document pdfDocument, PdfWriter pdfWriter,
			LinkedList<ValueHolderValueObject> records, int documentIndex,
			int totalDocuments, AccountExportValueObject accountExport,
			SearchRecordValueObject document) throws DocumentException,
			MalformedURLException, IOException {
		if (documentIndex == 1) {
			
			logger.debug("Adding header to document");
			
			addDocumentHeader(pdfDocument);
		}
		if (records != null) {
			records = flattenValue(records);
			populatePublicContributions(records, document, accountExport);
			// populateThumbnails(records, document, accountExport);
			addRecord(pdfDocument, records, documentIndex);
			// multiplyThumbnails(document.getObjects());
			addThumbnails(document, pdfWriter, accountExport, pdfDocument);
		} else {
			
			logger.debug("Records are null");
		}

		if (documentIndex == totalDocuments) {
			
			logger.debug("Adding footer");
			
			addDocumentFooter(pdfDocument);
		}
	}

	private void addThumbnails(SearchRecordValueObject document,
			PdfWriter pdfWriter, AccountExportValueObject accountExport,
			Document pdfDocument) throws MalformedURLException, IOException,
			DocumentException {

		long startTime = 0;
		long endTime = 0;
		long LONG_PROCESS_MILLIS = 5000;
		
		startTime = new Date().getTime();
		
		if (!accountExport.getIncludeThumbnails()
				|| document.getObjects() == null
				|| document.getObjects().size() == 0) {
			return;
		}
		int thumbnailIndex = 0;
		Paragraph thumbnailsParagraph = null;
		PdfPTable thumbnailTable = null;
		Collection<DigitalObjectValueObject> digitalObjects = document
				.getObjects().values();
		
		OpaStorage storage = opaStorageFactory.createOpaStorage();

		for (DigitalObjectValueObject digitalObject : digitalObjects) {
			//Check for timeout
			if(accountExport.getRequestStatus() == AccountExportStatusEnum.TIMEDOUT) {
				return;
			}
			
			
			String thumbnailPath = digitalObject.getThumbnailPath();
			if (thumbnailPath != null) {
				if (thumbnailsParagraph == null) {

					// Add page break if the table doesn't fit the page
					if (pdfWriter != null
							&& pdfWriter.getVerticalPosition(true)
									- (1 + Math
											.floor(THUMBNAIL_HEIGHT
													* (digitalObjects.size() / THUMBNAIL_TABLE_COL_NO))) < pdfDocument
										.bottom()) {
						pdfDocument.newPage();
					}

					thumbnailsParagraph = new Paragraph();
					thumbnailsParagraph.add("Thumbnails:");
					pdfDocument.add(thumbnailsParagraph);
					pdfDocument.add(new Paragraph(" "));
					thumbnailTable = getNewThumbnailTable();
				}

				String naId = document.getNaId();
				if(StringUtils.isNullOrEmtpy(naId)) {
					naId = document.getParentDescriptionNaId();
				}
				if(StringUtils.isNullOrEmtpy(naId)) {
					throw new OpaRuntimeException("Document nas no naId");
				}
				
				String path = storage.getFullPathInLive(thumbnailPath, Integer
            .valueOf(naId));
				
				thumbnailIndex++;
				String caption = null;
				if (digitalObject.getDesignator() != null &&
					digitalObject.getDescription() != null) {
					caption = digitalObject.getDesignator() + ", " + digitalObject.getDescription();
				} else {
					caption = (digitalObject.getDesignator() != null) ? digitalObject.getDesignator() : digitalObject.getDescription();
				}
				addImage(path, thumbnailTable, caption, storage);
			}
		}

		if (thumbnailIndex % THUMBNAIL_TABLE_COL_NO == 0
				&& thumbnailTable != null) {
			pdfDocument.add(thumbnailTable);
		}
		if (thumbnailIndex % THUMBNAIL_TABLE_COL_NO != 0
				&& thumbnailTable != null) {
			int emptyCellsToAdd = THUMBNAIL_TABLE_COL_NO - thumbnailIndex
					% THUMBNAIL_TABLE_COL_NO;
			for (int i = 1; i <= emptyCellsToAdd; i++) {
				thumbnailTable.addCell("");
			}

			pdfDocument.add(thumbnailTable);
		}
		
		endTime = new Date().getTime();
		if(endTime - startTime > LONG_PROCESS_MILLIS) {
			logger.info(String.format("ExportId:[%1$d] Thumbnail inclusion took more than [%2$s] millisecs: [%3$d]", accountExport.getExportId(), LONG_PROCESS_MILLIS, endTime - startTime));
		}

	}

	/**
	 * Creates a PdfPTable object that with contain the thumbnails
	 * 
	 * @return
	 */
	private PdfPTable getNewThumbnailTable() {
		PdfPTable thumbnailTable = new PdfPTable(THUMBNAIL_TABLE_COL_NO);
		thumbnailTable.getDefaultCell().setBorder(0);
		thumbnailTable.getDefaultCell().setMinimumHeight(THUMBNAIL_HEIGHT);
		thumbnailTable.setWidthPercentage(100f);
		return thumbnailTable;
	}

	/**
	 * Attempts to add an image to the provided table
	 * 
	 * @param imagePath
	 *            The path to the image file
	 * @param table
	 *            The table object where the image will be loaded
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws DocumentException
	 */
	private void addImage(String imagePath, PdfPTable table,
			String caption, OpaStorage opaStorage) throws MalformedURLException, 
			IOException, DocumentException {
		if (!opaStorage.exists(imagePath)) {
			if (logger.isTraceEnabled()) {
				logger.trace("Cannot find thumbnail path: " + imagePath);
			}
			table.addCell(getMissingFileParagraph(caption));
			return;
		}
		byte[] data = null;

		try {
			data = opaStorage.getFileContent(imagePath);
		} catch(IOException e) {
			logger.error("Cannot retrieve file contents from key: " + imagePath);
			return;
		}
		Image image = Image.getInstance(data);
		image.scaleToFit(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
		PdfPCell cell = new PdfPCell();
		if (caption != null) {
			Paragraph p = new Paragraph();
			Phrase phrase = new Phrase();
			phrase.add(new Chunk(image, 0, 0, true));
			phrase.add("\n" + caption + "\n\n");
			p.setFont(new Font(Font.FontFamily.HELVETICA, 8));
			p.setAlignment(Element.ALIGN_CENTER);
			p.add(phrase);
			cell.addElement(p);
		} else {
			cell.setFixedHeight(THUMBNAIL_HEIGHT + 2);
			cell.addElement(image);
		}
		table.addCell(cell);
	}

	private Paragraph getMissingFileParagraph(String caption) {
		if (caption != null) {
			return new Paragraph("Missing image file \n\n\n\n\n\n\n" + caption, new Font(
					Font.FontFamily.HELVETICA, 10));
		}
		return new Paragraph("Missing image file", new Font(
				Font.FontFamily.HELVETICA, 10));
	}

	private void addDocumentHeader(Document document) throws DocumentException {
		Paragraph header = new Paragraph();
		addEmptyLine(header, 1);
		header.add("National Archives Catalog\n");
		header.add("U.S. National Archives and Records Administration\n\n");
		document.add(header);
	}

	private void addRecord(Document document,
			LinkedList<ValueHolderValueObject> records, int documentIndex)
			throws DocumentException {

		
		logger.debug(String.format("Adding record information to %1$d", documentIndex));

		Paragraph separator = new Paragraph();
		separator.add(getSeparator(documentIndex));
		document.add(separator);

		for (ValueHolderValueObject record : records) {
			Paragraph p = new Paragraph();
			StringBuilder sb = new StringBuilder();
			appendRecord(record, sb);
			int shortLabelIndex = sb.indexOf(":   ");
			if (shortLabelIndex > 0 && shortLabelIndex < CHARS_PADDING_TO_LEFT) {
				sb.delete(shortLabelIndex + 1, shortLabelIndex + 5);
			}
			p.add(sb.toString());
			document.add(p);
		}

	}

	private void addDocumentFooter(Document document) throws DocumentException {
		Paragraph footer = new Paragraph();
		addEmptyLine(footer, 3);
		footer.add("National Archives Catalog\n");
		footer.add("U.S. National Archives and Records Administration\n\n");
		footer.add("8601 Adelphi Road\n");
		footer.add("College Park, MD 20740\n");
		footer.add("Email:  search@nara.gov\n");
		footer.add("On the web:   http://www.archives.gov/research/search/\n\n");
		footer.add("Disclaimer:\n");
		footer.add("The user contributed portion of this description has been contributed "
				+ "by a Citizen Archivist. NARA has not reviewed these contributions and cannot "
				+ "guarantee the information is complete, accurate or authoritative.\n");
		document.add(footer);
	}

	private static void addEmptyLine(Paragraph paragraph, int number) {

		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}
}
