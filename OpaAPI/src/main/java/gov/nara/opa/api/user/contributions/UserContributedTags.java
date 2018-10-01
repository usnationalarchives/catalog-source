package gov.nara.opa.api.user.contributions;

/**
 * Class AnnotationReason with the required fields and columns as the table on
 * the database.
 */
public class UserContributedTags {
  private String text;
  private int status;
  private int account_id;
  private int frequency;

  /**
   * @return the text
   */
  public String getText() {
    return text;
  }

  /**
   * @param text
   *          the text to set
   */
  public void setText(String text) {
    this.text = text;
  }

  /**
   * @return the status
   */
  public int getStatus() {
    return status;
  }

  /**
   * @param status
   *          the status to set
   */
  public void setStatus(int status) {
    this.status = status;
  }

  /**
   * @return the account_id
   */
  public int getAccount_id() {
    return account_id;
  }

  /**
   * @param account_id
   *          the account_id to set
   */
  public void setAccount_id(int account_id) {
    this.account_id = account_id;
  }

  /**
   * @return the frequency
   */
  public int getFrequency() {
    return frequency;
  }

  /**
   * @param frequency
   *          the frequency to set
   */
  public void setFrequency(int frequency) {
    this.frequency = frequency;
  }

}
