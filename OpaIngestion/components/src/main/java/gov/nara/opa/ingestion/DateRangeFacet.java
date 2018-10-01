package gov.nara.opa.ingestion;

import java.util.HashSet;
import java.util.Set;

/**
 * Return date range facet strings given a year range.
 */

/**
 * @author agullett
 *
 */
public class DateRangeFacet {

  private final static int CENTURY_MAX = 1699;  // the year up to which we want century date ranges
  private final static int HALF_CENTURY_MAX = 1799;  // the last year where we want to return half-century ranges

  public static Set<String> dateRangeFacets(YearRange yearRange) {
    Set<String> dateRanges = new HashSet<String>();

    for (int year = yearRange.getStartYear(); year <= yearRange.getEndYear(); year++) {
      if (year <= CENTURY_MAX) {
        dateRanges.add(dateRangeCentury(year).toString());
      } else if (year <= HALF_CENTURY_MAX) {
        dateRanges.add(dateRangeHalfCentury(year).toString());
      }
      else {
        dateRanges.add(dateRangeDecade(year).toString());
      }
    }

    return dateRanges;
}

  private static YearRange dateRangeCentury(int year) {
    int centuryStart = (year / 100) * 100;
    int centuryEnd = centuryStart + 99;
    return new YearRange(centuryStart, centuryEnd);
  }

  private static YearRange dateRangeHalfCentury(int year) {
    int halfCenturyStart;
    int halfCenturyEnd;

    if ((year % 100) < 50) {
      halfCenturyStart = ((year / 100) * 100);
    } else {
      halfCenturyStart = (((year / 100) * 100) + 50);
    }

    halfCenturyEnd = (halfCenturyStart + 49);

    return new YearRange(halfCenturyStart, halfCenturyEnd);
  }

  private static YearRange dateRangeDecade(int year) {
    int decadeStart = (year / 10) * 10;
    int decadeEnd = decadeStart + 9;

    return new YearRange(decadeStart, decadeEnd);
  }

}
