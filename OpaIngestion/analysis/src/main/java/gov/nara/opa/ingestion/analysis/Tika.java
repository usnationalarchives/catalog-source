package gov.nara.opa.ingestion.analysis;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.searchtechnologies.aspire.services.AspireException;

/**
 * Extracts text and technical metadata from files using Apache Tika.
 */
public abstract class Tika {

	public static void extractText(File file, Writer writer)
			throws AspireException {
		try (InputStream inputStream = org.apache.tika.io.TikaInputStream
				.get(file)) {
			Parser parser = new AutoDetectParser();
			ContentHandler contentHandler = new BodyContentHandler(writer);
			Metadata metadata = new Metadata();
			metadata.set(Metadata.CONTENT_TYPE, "text/html");
			metadata.set(Metadata.CONTENT_ENCODING, "UTF-8");
			
			parser.parse(inputStream, contentHandler, metadata,
					new ParseContext());

		} catch (SAXException | TikaException | IOException ex) {
			throw new AspireException("Extract text", ex);
		}
	}

	public static Metadata extractTechnicalMetaData(File file)
			throws AspireException {
		try (InputStream inputStream = org.apache.tika.io.TikaInputStream
				.get(file)) {
			Parser parser = new AutoDetectParser();
			ContentHandler contentHandler = new DefaultHandler();
			Metadata metadata = new Metadata();
			metadata.set(Metadata.CONTENT_TYPE, "text/html");
			metadata.set(Metadata.CONTENT_ENCODING, "UTF-8");

			parser.parse(inputStream, contentHandler, metadata,
					new ParseContext());
			return metadata;
		} catch (IOException | SAXException | TikaException ex) {
			throw new AspireException("Extract technical metadata", ex);
		}
	}

}
