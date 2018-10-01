package gov.nara.opa.common.valueobject.search;

import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;
import gov.nara.opa.common.valueobject.export.DigitalObjectValueObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.saxon.s9api.XdmNode;

import org.apache.solr.common.SolrDocument;

public class SearchRecordValueObject {

  private SolrDocument solrDocument;
  private String opaId;
  private String naId;
  private String parentDescriptionNaId;
  private String resultType;
  private XdmNode compiledOpaXml;
  private String descriptionXml;
  private XdmNode compiledDescriptionXml;
  private String authorityXml;
  private XdmNode compiledAuthorityXml;
  private String objectsXml;
  private XdmNode compiledObjectsXml;
  private String publicContributionsXml;
  private XdmNode compiledPublicContributionsXml;
  private String objectVersion;
  private Map<String, DigitalObjectValueObject> objects = new LinkedHashMap<String, DigitalObjectValueObject>();
  private List<TagValueObject> tags;
  private String objectId;
  private String extractedText;

  // private String compiledOpaXml

  public String getExtractedText() {
	return extractedText;
}

public void setExtractedText(String extractedText) {
	this.extractedText = extractedText;
}

public SolrDocument getSolrDocument() {
    return solrDocument;
  }

  public void setSolrDocument(SolrDocument solrDocument) {
    this.solrDocument = solrDocument;
  }

  public String getOpaId() {
    return opaId;
  }

  public void setOpaId(String opaId) {
    this.opaId = opaId;
  }

  public String getResultType() {
    return resultType;
  }

  public void setResultType(String resultType) {
    this.resultType = resultType;
  }

  public XdmNode getCompiledOpaXml() {
    return compiledOpaXml;
  }

  public void setCompiledOpaXml(XdmNode compiledOpaXml) {
    this.compiledOpaXml = compiledOpaXml;
  }

  public String getNaId() {
    return naId;
  }

  public void setNaId(String naId) {
    this.naId = naId;
  }

  public String getParentDescriptionNaId() {
    return parentDescriptionNaId;
  }

  public void setParentDescriptionNaId(String parentDescriptionNaId) {
    this.parentDescriptionNaId = parentDescriptionNaId;
  }

  public String getObjectsXml() {
    return objectsXml;
  }

  public void setObjectsXml(String objectsXml) {
    this.objectsXml = objectsXml;
  }

  public XdmNode getCompiledObjectsXml() {
    return compiledObjectsXml;
  }

  public void setCompiledObjectsXml(XdmNode compiledObjectsXml) {
    this.compiledObjectsXml = compiledObjectsXml;
  }

  public String getDescriptionXml() {
    return descriptionXml;
  }

  public void setDescriptionXml(String descriptionXml) {
    this.descriptionXml = descriptionXml;
  }

  public XdmNode getCompiledDescriptionXml() {
    return compiledDescriptionXml;
  }

  public void setCompiledDescriptionXml(XdmNode compiledDescriptionXml) {
    this.compiledDescriptionXml = compiledDescriptionXml;
    if (compiledDescriptionXml != null) {
      setCompiledOpaXml(compiledDescriptionXml);
    }
  }

  public String getAuthorityXml() {
    return authorityXml;
  }

  public void setAuthorityXml(String authorityXml) {
    this.authorityXml = authorityXml;
  }

  public XdmNode getCompiledAuthorityXml() {
    return compiledAuthorityXml;
  }

  public void setCompiledAuthorityXml(XdmNode compiledAuthorityXml) {
    this.compiledAuthorityXml = compiledAuthorityXml;
    if (compiledAuthorityXml != null) {
      setCompiledOpaXml(compiledAuthorityXml);
    }
  }

  public Map<String, DigitalObjectValueObject> getObjects() {
    return objects;
  }

  public List<TagValueObject> getTags() {
    if (tags == null) {
      tags = new ArrayList<TagValueObject>();
    }
    return tags;
  }

  public String getObjectVersion() {
    return objectVersion;
  }

  public void setObjectVersion(String objectVersion) {
    this.objectVersion = objectVersion;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public String getPublicContributionsXml() {
    return publicContributionsXml;
  }

  public void setPublicContributionsXml(String publicContributionsXml) {
    this.publicContributionsXml = publicContributionsXml;
  }

  public XdmNode getCompiledPublicContributionsXml() {
    return compiledPublicContributionsXml;
  }

  public void setCompiledPublicContributionsXml(
      XdmNode compiledPublicContributionsXml) {
    this.compiledPublicContributionsXml = compiledPublicContributionsXml;
  }

}
