package gov.nara.opa.ingestion;

/**
 * Store a start year and end year together and provide
 * a way to display them consistent with dateRangeFormat.
 */

/**
 * @author agullett
 *
 */
public class YearRange {

  public static final String dateDivider = " - ";

  private int startYear;
  private int endYear;

  /**
   * @param startYear
   * @param endYear
   */
  public YearRange(int startYear, int endYear) {
    this.startYear = startYear;
    this.endYear = endYear;
  }

  /**
   * @param startYear
   * @param endYear
   */
  public YearRange(String startYear, String endYear) {
    this.startYear = Integer.parseInt(startYear);
    this.endYear = Integer.parseInt(endYear);
  }

  /**
   * @return the startYear
   */
  public int getStartYear() {
    return startYear;
  }

  /**
   * @param startYear the startYear to set
   */
  public void setStartYear(int startYear) {
    this.startYear = startYear;
  }

  /**
   * @return the endYear
   */
  public int getEndYear() {
    return endYear;
  }

  /**
   * @param endYear the endYear to set
   */
  public void setEndYear(int endYear) {
    this.endYear = endYear;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return startYear + dateDivider + endYear;
  }

}
