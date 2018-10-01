package gov.nara.opa.api.dataaccess.annotation.transcriptions;

import gov.nara.opa.api.annotation.TranscriptedOpaTitle;
import gov.nara.opa.api.annotation.transcriptions.Transcription;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface TranscriptionDao {

  /**
   * Inserts a new transcription in the database
   * 
   * @param transcription
   *          The transcription object instance
   * @return True if successfuly inserted
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  boolean insert(Transcription transcription) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException;

  /**
   * Updates an existing transcription
   * 
   * @param transcription
   *          The transcription object instance
   * @return True if succesfuly updated
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  boolean update(Transcription transcription) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException;

  /**
   * Gets all transcriptions for an naId and object Id
   * 
   * @param naId
   * @param objectId
   * @return
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  Transcription selectLastOtherUserModifiedTranscription(int firstAnnotationId,
      String userName) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException;

  /**
   * Gets all transcriptions for an naId and object Id
   * 
   * @param naId
   * @param objectId
   * @return
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  Transcription selectLastOwnerModifiedTranscription(int firstAnnotationId,
      String userName) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException;

  /**
   * Gets all transcriptions for an naId and object Id
   * 
   * @param naId
   * @param objectId
   * @return
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  List<Transcription> select(String naId, String objectId)
      throws DataAccessException, UnsupportedEncodingException,
      BadSqlGrammarException;

  /**
   * Gets a transcription for the provided Id
   * 
   * @param trasncriptionId
   * @return
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  Transcription select(int transcriptionId) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException;

  /**
   * Gets the current active transcription for an object and a user
   * 
   * @param naId
   * @param objectId
   * @param status
   * @return
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  Transcription select(String naId, String objectId, int status)
      throws DataAccessException, UnsupportedEncodingException,
      BadSqlGrammarException;

  /**
   * Gets the current active transcription for an object and a user
   * 
   * @param naId
   * @return
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  public List<Transcription> selectByNaIds(String[] naIds)
      throws DataAccessException, UnsupportedEncodingException,
      BadSqlGrammarException;

  /**
   * Gets the transcription for the na Id and object Id specified by the version
   * 
   * @param naId
   * @param objectId
   * @param version
   * @return
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  Transcription selectByVersion(String naId, String objectId, int version)
      throws DataAccessException, UnsupportedEncodingException,
      BadSqlGrammarException;

  /**
   * Generic select operation
   * 
   * @param whereClause
   * @param paramArray
   * @return The list of transcriptions that match the where clause. Must
   *         include the WHERE keyword.
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  public List<Transcription> select(String whereClause, Object[] paramArray)
      throws DataAccessException, UnsupportedEncodingException,
      BadSqlGrammarException;

  /**
   * Returns the titles information transcripted
   * 
   * @param trasncriptionText
   * @param title
   * @param userName
   * @param offset
   * @param rows
   * @return
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  public List<TranscriptedOpaTitle> selectTranscriptedTitles(String title,
      String userName, int offset, int rows) throws DataAccessException,
      UnsupportedEncodingException;
  
  public List<TranscriptedOpaTitle> selectTranscriptedTitles(String title,
      String userName, int offset, int rows, boolean descOrder) throws DataAccessException,
      UnsupportedEncodingException;  

  public int selectTranscriptedTitleCount(String title, String userName)
      throws DataAccessException, UnsupportedEncodingException;

  public boolean isTranscriptionDuplicate(String naId, String objectId,
      String annotationMD5) throws DataAccessException,
      UnsupportedEncodingException;

}
