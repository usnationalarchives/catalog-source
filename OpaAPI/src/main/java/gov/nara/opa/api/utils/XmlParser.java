package gov.nara.opa.api.utils;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlParser {
  
  private static OpaLogger logger = OpaLogger.getLogger(XmlParser.class);

  /**
   * Utility class to read an XML string into a Map
   * 
   * @param xmlString
   * @return XML Map
   */
  public Map<Object, Object> parseXML(String xmlString) {
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(new ByteArrayInputStream(xmlString
          .getBytes()));
      doc.getDocumentElement().normalize();
      NodeList resultNode = doc.getChildNodes();
      HashMap<Object, Object> result = new HashMap<Object, Object>();
      XmlParser.MyNodeList tempNodeList = new XmlParser.MyNodeList();
      String emptyNodeName = null, emptyNodeValue = null;

      for (int index = 0; index < resultNode.getLength(); index++) {
        Node tempNode = resultNode.item(index);
        if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
          tempNodeList.addNode(tempNode);
        }
        emptyNodeName = tempNode.getNodeName();
        emptyNodeValue = tempNode.getNodeValue();
      }

      if (tempNodeList.getLength() == 0 && emptyNodeName != null
          && emptyNodeValue != null) {
        result.put(emptyNodeName, emptyNodeValue);
        return result;
      }

      this.parseXMLNode(tempNodeList, result);
      return result;
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private void parseXMLNode(NodeList nList, HashMap<Object, Object> result) {
    for (int temp = 0; temp < nList.getLength(); temp++) {
      Node nNode = nList.item(temp);
      if (nNode.getNodeType() == Node.ELEMENT_NODE
          && nNode.hasChildNodes()
          && nNode.getFirstChild() != null
          && (nNode.getFirstChild().getNextSibling() != null || nNode
              .getFirstChild().hasChildNodes())) {
        NodeList childNodes = nNode.getChildNodes();
        XmlParser.MyNodeList tempNodeList = new XmlParser.MyNodeList();
        for (int index = 0; index < childNodes.getLength(); index++) {
          Node tempNode = childNodes.item(index);
          if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
            tempNodeList.addNode(tempNode);
          }
        }
        HashMap<Object, Object> counterHashMap = new HashMap<Object, Object>();
        HashMap<Object, Object> dataHashMap = new HashMap<Object, Object>();
        if (result.containsKey(nNode.getNodeName())
            && ((HashMap<Object, Object>) result.get(nNode.getNodeName()))
                .containsKey(0)) {
          Map<Object, Object> mapExisting = (Map<Object, Object>) result
              .get(nNode.getNodeName());
          Integer index = 0;
          if (mapExisting.containsKey(0)) {
            while (true) {
              if (mapExisting.containsKey(index)) {
                counterHashMap.put(index, mapExisting.get(index));
                index++;
              } else {
                break;
              }
            }
          } else {
            result.put(nNode.getNodeName(), counterHashMap);
            counterHashMap.put("0", mapExisting);
            index = 1;
          }
          result.put(nNode.getNodeName(), counterHashMap);
          counterHashMap.put(index, dataHashMap);
        } else if (result.containsKey(nNode.getNodeName())) {
          counterHashMap.put(0, result.get(nNode.getNodeName()));
          result.put(nNode.getNodeName(), counterHashMap);
          counterHashMap.put(1, dataHashMap);
        } else {
          result.put(nNode.getNodeName(), dataHashMap);
        }
        if (nNode.getAttributes().getLength() > 0) {
          Map<Object, Object> attributeMap = new HashMap<Object, Object>();
          for (int attributeCounter = 0; attributeCounter < nNode
              .getAttributes().getLength(); attributeCounter++) {
            attributeMap.put(nNode.getAttributes().item(attributeCounter)
                .getNodeName(), nNode.getAttributes().item(attributeCounter)
                .getNodeValue());
          }
          dataHashMap.put("__attributes", attributeMap);
        }
        this.parseXMLNode(tempNodeList, dataHashMap);
      } else if (nNode.getNodeType() == Node.ELEMENT_NODE
          && nNode.hasChildNodes() && nNode.getFirstChild() != null
          && nNode.getFirstChild().getNextSibling() == null) {
        this.putValue(result, nNode);
      } else if (nNode.getNodeType() == Node.ELEMENT_NODE) {
        this.putValue(result, nNode);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void putValue(HashMap<Object, Object> result, Node nNode) {
    HashMap<Object, Object> attributeMap = new HashMap<Object, Object>();
    Object nodeValue = null;
    if (nNode.getFirstChild() != null) {
      nodeValue = nNode.getFirstChild().getNodeValue();
      if (nodeValue != null) {
        nodeValue = nodeValue.toString().trim();
      }
    }
    HashMap<Object, Object> nodeMap = new HashMap<Object, Object>();
    nodeMap.put("value", nodeValue);
    Object putNode = nodeValue;
    if (nNode.getAttributes().getLength() > 0) {
      for (int attributeCounter = 0; attributeCounter < nNode.getAttributes()
          .getLength(); attributeCounter++) {
        attributeMap.put(nNode.getAttributes().item(attributeCounter)
            .getNodeName(), nNode.getAttributes().item(attributeCounter)
            .getNodeValue());
      }
      nodeMap.put("__attributes", attributeMap);
      putNode = nodeMap;
    }
    HashMap<Object, Object> counterHashMap = new HashMap<Object, Object>();
    HashMap<Object, Object> dataHashMap = new HashMap<Object, Object>();
    if (result.containsKey(nNode.getNodeName())
        && result.get(nNode.getNodeName()) instanceof HashMap
        && ((HashMap<Object, Object>) result.get(nNode.getNodeName()))
            .containsKey(0)) {
      Map<Object, Object> mapExisting = (Map<Object, Object>) result.get(nNode
          .getNodeName());
      Integer index = 0;
      if (mapExisting.containsKey(0)) {
        while (true) {
          if (mapExisting.containsKey(index)) {
            counterHashMap.put(index, mapExisting.get(index));
            index++;
          } else {
            break;
          }
        }
      } else {
        index = 1;
      }
      counterHashMap.put(index, putNode);
      result.put(nNode.getNodeName(), counterHashMap);
    } else if (result.containsKey(nNode.getNodeName())) {
      Object existingObject = result.get(nNode.getNodeName());
      result.put(nNode.getNodeName(), dataHashMap);
      dataHashMap.put(0, existingObject);
      dataHashMap.put(1, putNode);
    } else {
      result.put(nNode.getNodeName(), putNode);
    }
  }

  class MyNodeList implements NodeList {
    List<Node> nodes = new ArrayList<Node>();
    int length = 0;

    public MyNodeList() {
    }

    public void addNode(Node node) {
      nodes.add(node);
      length++;
    }

    @Override
    public Node item(int index) {
      try {
        return nodes.get(index);
      } catch (Exception ex) {
        logger.error(ex.getMessage(), ex);
      }
      return null;
    }

    @Override
    public int getLength() {
      return length;
    }
  }

  public static void main(String[] args) {
    XmlParser xmlParser = new XmlParser();
    xmlParser.parseXML("");
  }

}
