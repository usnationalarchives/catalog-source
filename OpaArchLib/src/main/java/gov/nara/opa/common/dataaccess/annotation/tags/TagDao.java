package gov.nara.opa.common.dataaccess.annotation.tags;

import gov.nara.opa.common.valueobject.annotation.tags.OpaTitle;
import gov.nara.opa.common.valueobject.annotation.tags.Tag;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.dao.DataAccessException;

public interface TagDao {

  /**
   * Returns the tag that has the provided annotation Id
   * 
   * @param annotationId
   * @return The tag instance
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  Tag select(int annotationId) throws DataAccessException,
      UnsupportedEncodingException;

  /**
   * Returns the titles information tagged with the specified tagId
   * 
   * @param tagId
   * @return The titles Collection
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  List<OpaTitle> selectTaggedTitles(String tagText, String title,
      String userName, int offset, int rows) throws DataAccessException,
      UnsupportedEncodingException;
  
  List<OpaTitle> selectTaggedTitles(String tagText, String title,
      String userName, int offset, int rows, boolean descOrder) throws DataAccessException,
      UnsupportedEncodingException;
  
  
  int selectTaggedTitleCount(String tagText, String title,
      String userName) throws DataAccessException, UnsupportedEncodingException;

  /**
   * Returns the titles information tagged with the specified tagId
   * 
   * @param tagId
   * @return The titles Collection
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  List<OpaTitle> getTitlesByNaIds(String[] naIdsList)
      throws DataAccessException, UnsupportedEncodingException;

  /**
   * Retrieve tag info for a given naId and tag annotation value
   * 
   * @param naId
   *          NARA ID
   * @param objectId
   *          Object ID
   * @param tagText
   *          tag annotation text
   * @param md5Flag
   *          search on tag text / MD5 encrypted tag text (true / false)
   * @return List containing Tag object
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  List<Tag> select(String naId, String objectId, String tagText, int status,
      boolean md5Flag) throws DataAccessException, UnsupportedEncodingException;

  List<String> selectTagValuesByNaIdAndObjectId(String naId, String objectId);

  List<String> selectTagValuesByNaIdAndObjectId(String naId, String objectId,
      Boolean status);

  void createTag(TagValueObject tag);

  /**
   * Updates a tag object
   * 
   * @param tagObj
   * @param sessionId
   * @return
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  Tag update(Tag tagObj) throws DataAccessException,
      UnsupportedEncodingException;

  List<TagValueObject> selectAllTags(String naId, String objectId, String text,
      Boolean status, String action, boolean descendingOrder);

  List<TagValueObject> selectAllTags(String naId, String objectId, String text,
      Boolean status, String action);

  List<TagValueObject> selectAllTags(String naId, String objectId, String text,
      Boolean status);

  List<TagValueObject> selectAllTagsByNaIds(String[] naIdsList)
      throws DataAccessException, UnsupportedEncodingException;

  List<TagValueObject> selectAllTagsByAnnotationId(String annotationId);

  void updateTagStatus(Integer annotationId, Boolean status);

}
