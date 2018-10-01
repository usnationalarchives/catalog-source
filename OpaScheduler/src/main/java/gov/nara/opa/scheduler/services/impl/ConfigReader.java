package gov.nara.opa.scheduler.services.impl;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.scheduler.valueobject.TaskValueObject;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConfigReader {

	private static OpaLogger logger = OpaLogger.getLogger(ConfigReader.class);

	private String filePath;

	public ConfigReader() {
	}

	public ConfigReader(String filePath) {
		this.filePath = filePath;
	}

	public String getFilePath() {
		return filePath;
	}

	public List<TaskValueObject> read(String apiURL) throws OpaRuntimeException {
		List<TaskValueObject> taskList = new ArrayList<TaskValueObject>();
		try {
			File xmlFile = new File(filePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(xmlFile);

			doc.getDocumentElement().normalize();

			logger.info(String.format("Configuration file read containing %1$s root element", 
					doc.getDocumentElement().getNodeName()));

			NodeList nodeList = doc.getElementsByTagName(TaskValueObject.TASK);

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					TaskValueObject task = new TaskValueObject(
							element.getAttribute(TaskValueObject.NAME),
							apiURL + element.getElementsByTagName(TaskValueObject.ENDPOINT).item(0).getTextContent(),
							Time.valueOf(element.getElementsByTagName(TaskValueObject.TIME).item(0).getTextContent()));
					taskList.add(task);
				}
			}
		} catch (NullPointerException e) {
			throw new OpaRuntimeException("Configuration file is missing", e);
		} catch (SAXException e) {
			throw new OpaRuntimeException(String.format("Cannot create parser for file %s", getFilePath()), e);
		} catch (IOException e) {
			logger.error("Configuration file is missing", e);
			throw new OpaRuntimeException(String.format("Cannot read configuration file %s", getFilePath()), e);
		} catch (ParserConfigurationException e) {
			logger.error("Configuration file is missing", e);
			throw new OpaRuntimeException(String.format("Cannot parse configuration file %s", getFilePath()), e);
		} catch (Exception e) {
			throw new OpaRuntimeException(e.getMessage(), e);
		}
		return taskList;
	}
}
