/**
 * Copyright Search Technologies 2014
 * for NARA OPA
 */
package gov.nara.opa.ingestion;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import com.searchtechnologies.aspire.framework.*;
import com.searchtechnologies.aspire.services.*;

/**
 * Populate dateRangeFacet for an Authority Record.
 * @author OPA Ingestion Team
 */
public class DateRangeFacetsForAuthorityRecordStage extends StageImpl {

  String currentYear;

  final static String dateRangeFacetFieldName = "dateRangeFacet";
  final static String dateRangeElementName = "dateRange";

  static final AXPath establishYearXPath = AXPathFactory.newInstance("doc/organization/organizationNameArray/organizationName/establishDate/year");
  static final AXPath abolishYearXPath = AXPathFactory.newInstance("doc/organization/organizationNameArray/organizationName/abolishDate/year");

  static final AXPath birthYearXPath = AXPathFactory.newInstance("doc/person/birthDate/year");
  static final AXPath deathYearXPath = AXPathFactory.newInstance("doc/person/deathDate/year");

  /**
   * Populate dateRangeFacet for an Authority Record.
   * @param j  The job to process.
   */
  @Override
  public void process(Job j) throws AspireException {
    Set<String> dateRangeFacet = new HashSet<String>();

    AspireObject doc = j.get();

    // Get dateRangeFacets for establish and abolish dates.
    List<AspireObject> establishYearElements = establishYearXPath.getElementList(doc);
    List<AspireObject> abolishYearElements = abolishYearXPath.getElementList(doc);
    if ( (establishYearElements != null) && (abolishYearElements != null) ) {
      if ( (establishYearElements.size() > 0) && (abolishYearElements.size() > 0) ) {
        if ( establishYearElements.size() == abolishYearElements.size() ) {
          for (int i = 0; i < establishYearElements.size(); i++) {
            String start = establishYearElements.get(i).getText();
            String end = abolishYearElements.get(i).getText();
            if (end.compareTo(this.currentYear) > 0) {
              // Assume years in the future really mean "until now".
              end = this.currentYear;
            }
            dateRangeFacet.addAll(DateRangeFacet.dateRangeFacets(new YearRange(start, end)));
          }
        }
      }
    }

    // Get dateRangeFacets for birth and death dates.
    AspireObject birthYearElement = birthYearXPath.getElement(doc);
    AspireObject deathYearElement = deathYearXPath.getElement(doc);
    int birthYear;
    int deathYear;
    try {
      if (birthYearElement != null) {
        birthYear = Integer.parseInt(birthYearElement.getText());
        if (deathYearElement != null) {
          deathYear = Integer.parseInt(deathYearElement.getText());
        } else {
          // We're not saying they actually died in this year. We're just
          //   limiting the date range facets up to the current year.
          deathYear = Integer.parseInt(this.currentYear);
        }
        dateRangeFacet.addAll(DateRangeFacet.dateRangeFacets(new YearRange(birthYear, deathYear)));
      }
    } catch (NumberFormatException e) {
      // Do nothing.
      // If any of the years could not be used, don't create a date range facet.
    }

    // Put the date range facets in the doc.
    if (!dateRangeFacet.isEmpty()) {
      if (doc.get(dateRangeFacetFieldName) == null) {
        doc.add(dateRangeFacetFieldName);
      }
      for (String dateRange : dateRangeFacet) {
        doc.get(dateRangeFacetFieldName).add(dateRangeElementName).setContent(dateRange);
      }
    }
  }


  /**
   * Release any resources that need to be released.
   */
  @Override
  public void close() {
    // bh.close();
  }


  /**
   * Initialize this component with the configuration data from the component manager
   * configuration. NOTE:  This method is *always* called, even if the component
   * manager configuration is empty (in this situation, "config" will be null).
   *
   * @param config The XML &lt;config&gt; DOM element which holds the custom configuration
   * for this component from the component manager configuration file.
   * @throws AspireException
   */
  @Override
  public void initialize(Element config) throws AspireException {
    this.currentYear = new SimpleDateFormat("yyyy").format(new Date());
  }
}
