package gov.nara.opa.common.services.docstransforms.impl.normalize;

import gov.nara.opa.common.services.docstransforms.Constants;
import gov.nara.opa.common.services.docstransforms.impl.normalize.valueextractor.FieldsListXslValueExtractor;
import gov.nara.opa.common.services.docstransforms.impl.normalize.valueextractor.HardcodedValueExtractor;
import gov.nara.opa.common.services.docstransforms.impl.normalize.valueextractor.LinkValueExtractor;
import gov.nara.opa.common.services.docstransforms.impl.normalize.valueextractor.ListXslValueExtractor;
import gov.nara.opa.common.services.docstransforms.impl.normalize.valueextractor.SimpleXpathValueExtractor;
import gov.nara.opa.common.services.docstransforms.impl.normalize.valueextractor.SingleValueXslValueExtractor;
import gov.nara.opa.common.services.docstransforms.impl.normalize.valueextractor.SolrDocValueExtractor;
import gov.nara.opa.common.services.docstransforms.impl.normalize.valueextractor.TagsValueExtractor;
import gov.nara.opa.common.services.docstransforms.impl.normalize.valueextractor.ThumbnailsValueExtractor;
import gov.nara.opa.common.valueobject.export.FieldDefinitionValueObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DocumentValuesExtractorHelper implements Constants {

  @Value("${naraBaseUrl}")
  String naraBaseUrl;

  public AbstractValueExtractor getValueExtractor(
      FieldDefinitionValueObject fieldDefinition) {
    if (fieldDefinition.getSource().equals(FIELD_SOURCE_HARDCODED)) {
      return new HardcodedValueExtractor();
    } else if (fieldDefinition.getSource().equals(FIELD_SOURCE_SOLR_DOC)) {
      return new SolrDocValueExtractor();
    } else if (fieldDefinition.getSource().equals(
        FIELD_SOURCE_OPA_XML_SIMPLE_XPATH)) {
      return new SimpleXpathValueExtractor(
          fieldDefinition.getValueGenerationInstruction());
    } else if (fieldDefinition.getSource()
        .equals(FIELD_SOURCE_OPA_XML_LIST_XSL)) {
      return new ListXslValueExtractor(
          fieldDefinition.getValueGenerationInstruction());
    } else if (fieldDefinition.getSource().equals(
        FIELD_SOURCE_OPA_XML_SINGLE_VALUE_XSL)) {
      return new SingleValueXslValueExtractor(
          fieldDefinition.getValueGenerationInstruction());
    } else if (fieldDefinition.getSource().equals(FIELD_SOURCE_LINK)) {
      return new LinkValueExtractor(
          fieldDefinition.getValueGenerationInstruction(), naraBaseUrl);
    } else if (fieldDefinition.getSource().equals(
        FIELD_SOURCE_OPA_XML_FIELDS_LIST_XSL)) {
      return new FieldsListXslValueExtractor(
          fieldDefinition.getValueGenerationInstruction());
    } else if (fieldDefinition.getSource().equals(FIELD_SOURCE_TAGS)) {
      return new TagsValueExtractor();
    } else if (fieldDefinition.getSource().equals(FIELD_SOURCE_THUMBNAILS)) {
      return new ThumbnailsValueExtractor();
    }
    return null;
  }

}
