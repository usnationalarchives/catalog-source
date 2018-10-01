package gov.nara.opa.api.services.user.contributions;

import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.common.valueobject.annotation.tags.OpaTitle;

import java.util.List;

public interface ViewOpaTitleService {

  /**
   * Get the Collection of titles that met the specified filters
   * 
   * @param tagText
   *          Required, tag text to look
   * @param title
   *          Name of the title we look
   * @param userName
   *          Required, username owner of the tag
   * @param offset
   *          offset for pagination
   * @param rows
   *          rows to be returned
   * @return Collection of titles that met the specified filters
   */
  public ServiceResponseObject viewTaggedTitles(String tagText, String title,
      String userName, int offset, int rows);
  
  public ServiceResponseObject viewTaggedTitles(String tagText, String title,
      String userName, int offset, int rows, boolean descOrder);

  /**
   * Get the Collection of titles that met the specified filters
   * 
   * @param tagText
   *          Required, tag text to look
   * @param title
   *          Name of the title we look
   * @param userName
   *          Required, username owner of the tag
   * @param offset
   *          offset for pagination
   * @param rows
   *          rows to be returned
   * @return Collection of titles that met the specified filters
   */
  public List<OpaTitle> getTitlesByNaIds(String[] naIdsList);

  /**
   * Get the Collection of titles that met the specified filters
   * 
   * @param transcriptionText
   *          Required, transcription Text to look
   * @param title
   *          Name of the title we look
   * @param userName
   *          Required, username owner of the tag
   * @param offset
   *          offset for pagination
   * @param rows
   *          rows to be returned
   * @return Collection of titles that met the specified filters
   */
  public ServiceResponseObject viewTranscriptedTitles(String title,
      String userName, int offset, int rows);

  public ServiceResponseObject viewTranscriptedTitles(String title,
      String userName, int offset, int rows, boolean descOrder);  
  
  /**
   * Validate username exists
   * 
   * @param userName
   *          Username to be valdiated
   * @return true/false user exists
   */
  public boolean isValidUserName(String userName);
}
