/**
 * Copyright Search Technologies 2014
 * for NARA OPA
 */
package gov.nara.opa.ingestion;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;

import com.searchtechnologies.aspire.framework.*;
import com.searchtechnologies.aspire.services.*;

/**
 * Populate dateRangeFacet for an Archival Description.
 * @author OPA Ingestion Team
 */
public class DateRangeFacetsForArchivalDescriptionStage extends StageImpl {

  String currentYear;

  final static String dateRangeFacetFieldName = "dateRangeFacet";
  final static String dateRangeElementName = "dateRange";

  final AXPath productionDateXPath = AXPathFactory.newInstance("doc/*/productionDateArray/proposableQualifiableDate/year");
  final AXPath releaseDateXPath = AXPathFactory.newInstance("doc/*/releaseDateArray/proposableQualifiableDate/year");
  final AXPath copyrightDateXPath = AXPathFactory.newInstance("doc/*/copyrightDateArray/proposableQualifiableDate/year");
  final AXPath broadcastDateXPath = AXPathFactory.newInstance("doc/*/broadcastDateArray/proposableQualifiableDate/year");
  /*
  final AXPath itemProductionDateXPath = AXPathFactory.newInstance("doc/item/productionDateArray/proposableQualifiableDate/year");
  final AXPath itemCopyrightDateXPath = AXPathFactory.newInstance("doc/item/copyrightDateArray/proposableQualifiableDate/year");

  final AXPath itemAvProductionDateXPath = AXPathFactory.newInstance("doc/itemAv/productionDateArray/proposableQualifiableDate/year");
  final AXPath itemAvCopyrightDateXPath = AXPathFactory.newInstance("doc/itemAv/copyrightDateArray/proposableQualifiableDate/year");
   */

  static final AXPath coverageStartYearXPath = AXPathFactory.newInstance("doc/*/coverageDates/coverageStartDate/year");
  static final AXPath coverageEndYearXPath = AXPathFactory.newInstance("doc/*/coverageDates/coverageEndDate/year");

  static final AXPath inclusiveStartYearXPath = AXPathFactory.newInstance("doc/*/inclusiveDates/inclusiveStartDate/year");
  static final AXPath inclusiveEndYearXPath = AXPathFactory.newInstance("doc/*/inclusiveDates/inclusiveEndDate/year");

  static final AXPath parentInclusiveStartYearXPath = AXPathFactory.newInstance("doc/*/*[starts-with(name(),'parent')]/inclusiveDates/inclusiveStartDate/year");
  static final AXPath parentInclusiveEndYearXPath = AXPathFactory.newInstance("doc/*/*[starts-with(name(),'parent')]/inclusiveDates/inclusiveEndDate/year");

  static final AXPath descriptionTypeXPath = AXPathFactory.newInstance("name(/doc/*[contains('collection|recordGroup|series|fileUnit|item|itemAv|geographicPlaceName|organization|person|specificRecordsType|topicalSubject', local-name())][1])");
  /**
   * Populate dateRangeFacet for an Archival Description.
   * @param j  The job to process.
   */
  @Override
  public void process(Job j) throws AspireException {
    Set<String> dateRangeFacet = new HashSet<String>();

    AspireObject doc = j.get();
    String adType = descriptionTypeXPath.getString(doc);
    String logoutput = "";

    // TFS 84176 - Introduce more specific logic for description types
    /* Records Groups and Collections
      1. Coverage dates, if they exist
      2. Inclusive dates next, if Coverage Dates don't exist
      Series
      1. Coverage dates, if they exist
      2. Inclusive dates next, if Coverage Dates don't exist
      File Units
      1. Coverage dates, if they exist
      2. Inclusive dates next, if Coverage Dates don't exist (inherited from Series)
      Items/ItemAV
      1. Production Dates
      2. Copyright Dates, if Production Dates don't exist
      3. Coverage Dates, if either Production Dates or Copyright Dates don't exist
      4. Inclusive Dates, if either Production Dates, Copyright Dates, or Coverage Dates don't exist
    */

    logoutput += "coverage: "+getRangeString(coverageStartYearXPath.getElement(doc), coverageEndYearXPath.getElement(doc))+" ";
    logoutput += "inclusive: "+getRangeString(inclusiveStartYearXPath.getElement(doc), inclusiveEndYearXPath.getElement(doc))+" ";
    logoutput += "parentInclusive: "+getRangeString(parentInclusiveStartYearXPath.getElement(doc), parentInclusiveEndYearXPath.getElement(doc))+" ";

    // for item/itemav, try getting the production date.  if no production date, use copyright date
    if ("item".equalsIgnoreCase(adType)
            || "itemAv".equalsIgnoreCase(adType)
            ) {

      AspireObject productionDateElement = productionDateXPath.getElement(doc);
      AspireObject copyrightDateElement = copyrightDateXPath.getElement(doc);

      if (productionDateElement != null) {
        String productionDate = productionDateElement.getText();
        logoutput += "production: " + productionDate + " ";
        dateRangeFacet.addAll(DateRangeFacet.dateRangeFacets(new YearRange(productionDate, productionDate)));
      } else if (copyrightDateElement != null) {
        String copyrightDate = copyrightDateElement.getText();
        logoutput += "copyright: " + copyrightDate + " ";
        dateRangeFacet.addAll(DateRangeFacet.dateRangeFacets(new YearRange(copyrightDate, copyrightDate)));
      }
    }

    // if there is no production or copyright date for item/itemav, or the description is a collection, rg, series or fileunit
    // then try getting the coverage date.  if no coverage, use immediate inclusive.  if no immediate inclusive, use parent inclusive
    if (dateRangeFacet.isEmpty()
            || "collection".equalsIgnoreCase(adType)
            || "recordGroup".equalsIgnoreCase(adType)
            || "series".equalsIgnoreCase(adType)
            || "fileUnit".equalsIgnoreCase(adType)
            ) {

      AspireObject coverageStartYearElement = coverageStartYearXPath.getElement(doc);
      AspireObject coverageEndYearElement = coverageEndYearXPath.getElement(doc);

      AspireObject inclusiveStartYearElement = inclusiveStartYearXPath.getElement(doc);
      AspireObject inclusiveEndYearElement = inclusiveEndYearXPath.getElement(doc);

      if (coverageStartYearElement != null || coverageEndYearElement != null) {
        dateRangeFacet.addAll(getFacetsUsingCurrentYearIfNoEnd(coverageStartYearElement, coverageEndYearElement));
      } else if (inclusiveStartYearElement != null || inclusiveEndYearElement != null) {
        dateRangeFacet.addAll(getFacetsUsingCurrentYearIfNoEnd(inclusiveStartYearElement, inclusiveEndYearElement));
      } else {
        dateRangeFacet.addAll(getFacetsUsingCurrentYearIfNoEnd(parentInclusiveStartYearXPath.getElement(doc), parentInclusiveEndYearXPath.getElement(doc)));
      }
    }

    // Put the date range facets in the doc.
    if (!dateRangeFacet.isEmpty()) {
      debug(adType + "---\ndates: "+logoutput+"\n date facets: "+dateRangeFacet);
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

  private Set<String> getFacetsUsingCurrentYearIfNoEnd(AspireObject startYearElement, AspireObject endYearElement) {
    Set<String> dateRangeFacets = new HashSet<String>();

    int startYear;
    int endYear;
    try {
      if (startYearElement != null) {
        startYear = Integer.parseInt(startYearElement.getText());
        int currentYear = Integer.parseInt(this.currentYear);
        if (endYearElement != null) {
          endYear = Integer.parseInt(endYearElement.getText());
          if (endYear > currentYear) {
            endYear = currentYear;
          }
        } else {
          // If no end year is given, use the current year.
          endYear = currentYear;
        }
        dateRangeFacets.addAll(DateRangeFacet.dateRangeFacets(new YearRange(startYear, endYear)));
      }
    } catch (NumberFormatException e) {
      // Do nothing.
      // If any of the years could not be used, don't create a date range facet.
    }
    return dateRangeFacets;
  }

  private String getRangeString(AspireObject startYearElement, AspireObject endYearElement) {
    String range = "";
    String startYear = "";
    String endYear = "";
    try {
      if (startYearElement != null) {
        startYear = startYearElement.getText();
        int currentYear = Integer.parseInt(this.currentYear);
        if (endYearElement != null) {
          endYear = endYearElement.getText();
          if (Integer.parseInt(endYear) > currentYear) {
            endYear = currentYear+"";
          }
        } else {
          // If no end year is given, use the current year.
          endYear = currentYear+"";
        }
      }
    } catch (NumberFormatException e) {
      // Do nothing.
      // If any of the years could not be used, don't create a date range facet.
    }
    return startYear +" - "+endYear;
  }

}
