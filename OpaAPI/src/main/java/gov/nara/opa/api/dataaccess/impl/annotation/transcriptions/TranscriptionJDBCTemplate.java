package gov.nara.opa.api.dataaccess.impl.annotation.transcriptions;

import gov.nara.opa.api.annotation.TranscriptedOpaTitle;
import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.dataaccess.annotation.transcriptions.TranscriptionDao;
import gov.nara.opa.api.dataaccess.impl.annotation.tags.TranscriptedOpaTitleRowMapper;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.common.dataaccess.utils.GenericRowMapper;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class TranscriptionJDBCTemplate extends AbstractOpaDbJDBCTemplate
    implements TranscriptionDao {

  private static final String ORDER_BY_ID_DESC = " ORDER BY log_id DESC ";
  private static final String ORDER_BY_ID_ASC = " ORDER BY log_id ASC ";

  private String selectSql = "SELECT annotation_id, annotation, "
      + "saved_vers_num, annotation_md5, first_annotation_id, "
      + "status, na_id, object_id, page_num, opa_id, "
      + "account_id, annotation_ts FROM annotation_transcriptions ";
  
  
  private static final String TRANSCRIPTED_TITLES_SELECT = "SELECT CONVERT(ot.opa_title USING 'UTF8') opa_title, ot.opa_type, ot.total_pages, l.annotation_id, "
        + "l.first_annotation_id, tran.page_num, tran.annotation_ts, l.na_id, l.object_id, "
        + "l.account_id, l.log_ts, l.status, "
        + "authors.user_name author_user_name, authors.full_name author_full_name, "
        + "authors.is_nara_staff author_is_nara_staff, "
        + "creators.user_name creator_user_name, creators.full_name creator_full_name, "
        + "creators.is_nara_staff creator_is_nara_staff, original_tran.annotation_ts creation_ts ";
  
  private static final String TRANSCRIPTED_TITLES_JOINS = "JOIN opa_titles ot ON l.na_id = ot.na_id "
        + "JOIN accounts a ON l.account_id = a.account_id "
        + "JOIN annotation_transcriptions tran ON l.annotation_id = tran.annotation_id "
        + "JOIN accounts authors ON tran.account_id = authors.account_id "
        + "JOIN annotation_transcriptions original_tran ON l.first_annotation_id = original_tran.annotation_id "
        + "JOIN accounts creators ON original_tran.account_id = creators.account_id ";
  
  private static final String TRANSCRIPTED_TITLES_FIRST_ANNOTATION_IDS = "SELECT DISTINCT first_annotation_id "
        + "  FROM annotation_log "
        + "  WHERE annotation_type = 'TR' "
        + "  AND (status = 0 OR status = 1) "
        + "  AND action not in ('RESTORE','REMOVE')"
        + "  AND account_id = ( "
        + "    SELECT account_id "
        + "    FROM accounts "
        + "    WHERE user_name = ?)";

  private static final String TRANSCRIPTED_TITLES_FIRST_ANNOTATION_IDS_FOR_COUNT = "SELECT first_annotation_id "
        + "  FROM annotation_log "
        + "  WHERE annotation_type = 'TR' "
        + "  AND (status = 0 OR status = 1) "
        + "  AND account_id = ( "
        + "    SELECT account_id "
        + "    FROM accounts "
        + "    WHERE user_name = ?)";
  
  @Override
  public boolean insert(Transcription transcription)
      throws DataAccessException, UnsupportedEncodingException,
      BadSqlGrammarException {
    boolean result = false;

    String sql = "INSERT INTO annotation_transcriptions "
        + "(annotation, saved_vers_num, annotation_md5, "
        + "first_annotation_id, status, na_id, "
        + "object_id, page_num, opa_id, " + "account_id, annotation_ts) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())";

    Object[] parameters = new Object[] {
        transcription.getAnnotation().getBytes("UTF-8"),
        transcription.getSavedVersNum(),
        transcription.getAnnotationMD5(),
        transcription.getFirstAnnotationId(),
        (transcription.isStatus() ? 1 : 0),
        transcription.getNaId().getBytes("UTF-8"),
        transcription.getObjectId().getBytes("UTF-8"),
        transcription.getPageNum(),
        (transcription.getOpaId() != null ? transcription.getOpaId().getBytes(
            "UTF-8") : null), transcription.getAccountId() };

    result = (getJdbcTemplate().update(sql, parameters) > 0);

    // Update first annotation Id
    if (result && transcription.getFirstAnnotationId() == 0) {
      transcription = select(transcription.getNaId(),
          transcription.getObjectId(), 1);
      transcription.setFirstAnnotationId(transcription.getAnnotationId());
      update(transcription);
    }

    return result;
  }

  @Override
  public boolean update(Transcription transcription)
      throws DataAccessException, UnsupportedEncodingException,
      BadSqlGrammarException {
    boolean result = false;

    String sql = "UPDATE annotation_transcriptions SET "
        + "annotation = ?, saved_vers_num = ?, annotation_md5 = ?, "
        + "first_annotation_id = ?, status = ?, na_id = ?, "
        + "object_id = ?, page_num = ?, opa_id = ?, " + "account_id = ? "
        + "WHERE annotation_id = ?";

    Object[] parameters = new Object[] {
        transcription.getAnnotation().getBytes("UTF-8"),
        transcription.getSavedVersNum(),
        transcription.getAnnotationMD5(),
        transcription.getFirstAnnotationId(),
        (transcription.isStatus() ? 1 : 0),
        transcription.getNaId().getBytes("UTF-8"),
        transcription.getObjectId().getBytes("UTF-8"),
        transcription.getPageNum(),
        (transcription.getOpaId() != null ? transcription.getOpaId().getBytes(
            "UTF-8") : null), transcription.getAccountId(),
        transcription.getAnnotationId() };

    result = (getJdbcTemplate().update(sql, parameters) > 0);

    return result;
  }

  /**
   * Gets the last transcription update for a user and a first annotation Id
   * 
   * @param firstAnnotationid
   * @param userName
   * @return
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   * @throws BadSqlGrammarException
   */
  @Override
  public Transcription selectLastOwnerModifiedTranscription(
      int firstAnnotationId, String userName) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException {
    
    String sql = "SELECT at.annotation_id, at.account_id, at.annotation_ts, at.annotation, at.saved_vers_num, at.annotation_md5, "
        + "at.first_annotation_id, at.status, at.na_id, "
        + "at.object_id, at.page_num, at.opa_id "
        + "FROM opadb.annotation_transcriptions at JOIN accounts a ON at.account_id = a.account_id "
        + "WHERE first_annotation_id = ? "
        + "AND a.user_name = ? "
        + "AND (at.status = 0 OR at.status = 1) "
        + "ORDER BY annotation_id DESC LIMIT 1";

    Object[] paramArray = new Object[] { firstAnnotationId,
        userName.getBytes("UTF-8") };

    List<Transcription> transcriptions = getJdbcTemplate().query(sql,
        paramArray, new TranscriptionRowMapper());

    if (transcriptions.size() > 0) {
      return transcriptions.get(0);
    } else {
      return null;
    }
  }

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
  @Override
  public Transcription selectLastOtherUserModifiedTranscription(
      int firstAnnotationId, String userName) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException {

    String sql = "SELECT at.annotation_id, at.account_id, at.annotation_ts, at.annotation, "
        + "at.saved_vers_num, at.annotation_md5, at.first_annotation_id, at.status, at.na_id, "
        + "at.object_id, at.page_num, at.opa_id "
        + "FROM opadb.annotation_transcriptions at JOIN accounts a ON at.account_id = a.account_id "
        + "WHERE first_annotation_id = ? "
        + "AND a.user_name != ? "
        + "AND (at.status = 0 OR at.status = 1) "
        + "ORDER BY annotation_id DESC LIMIT 1;";
    
    Object[] paramArray = new Object[] { firstAnnotationId,
        userName.getBytes("UTF-8") };

    List<Transcription> transcriptions = getJdbcTemplate().query(sql,
        paramArray, new TranscriptionRowMapper());

    if (transcriptions.size() > 0) {
      return transcriptions.get(0);
    } else {
      return null;
    }
  }

  @Override
  public List<Transcription> select(String naId, String objectId)
      throws DataAccessException, UnsupportedEncodingException,
      BadSqlGrammarException {

    String whereClause = "WHERE (status = 0 OR status = 1) AND na_id = ? " 
        + "AND object_id = ? "
        + "ORDER BY saved_vers_num DESC ";

    Object[] paramArray = new Object[] { naId.getBytes("UTF-8"),
        objectId.getBytes("UTF-8") };

    return select(whereClause, paramArray);
  }

  @Override
  public Transcription select(int transcriptionId) throws DataAccessException,
      UnsupportedEncodingException, BadSqlGrammarException {

    Object[] paramArray = new Object[] { transcriptionId };

    List<Transcription> transcriptions = select("WHERE (status = 0 OR status = 1) AND annotation_id = ?",
        paramArray);

    return (transcriptions != null && transcriptions.size() > 0 ? transcriptions
        .get(0) : null);
  }

  @Override
  public Transcription select(String naId, String objectId, int status)
      throws DataAccessException, UnsupportedEncodingException,
      BadSqlGrammarException {

    Object[] paramArray = new Object[] { naId.getBytes("UTF-8"),
        objectId.getBytes("UTF-8"), status };

    List<Transcription> transcriptions = select(
        "WHERE na_id = ? AND object_id = ? AND status = ?", paramArray);

    return (transcriptions != null && transcriptions.size() > 0 ? transcriptions
        .get(0) : null);
  }

  @Override
  public List<Transcription> selectByNaIds(String[] naIds)
      throws DataAccessException, UnsupportedEncodingException,
      BadSqlGrammarException {

    String sql = "SELECT annotation_id, annotation, "
        + "saved_vers_num, annotation_md5, first_annotation_id, "
        + "status, na_id, object_id, page_num, opa_id, "
        + "account_id, annotation_ts "
        + "FROM annotation_transcriptions WHERE na_id in ( ";

    for (int i = 0; i < naIds.length; i++) {
      sql += "'" + naIds[i] + "'";
      if (i < naIds.length - 1)
        sql += ",";
    }

    sql += " ) AND status = 1 ";

    List<Transcription> transcriptions = getJdbcTemplate().query(sql,
        new Object[] {}, new TranscriptionRowMapper());

    return (transcriptions != null && transcriptions.size() > 0 ? transcriptions
        : null);

  }

  @Override
  public Transcription selectByVersion(String naId, String objectId, int version)
      throws DataAccessException, UnsupportedEncodingException,
      BadSqlGrammarException {

    Object[] paramArray = new Object[] { naId.getBytes("UTF-8"),
        objectId.getBytes("UTF-8"), version };

    List<Transcription> transcriptions = select(
        "WHERE (status = 0 OR status = 1) AND na_id = ? AND object_id = ? AND saved_vers_num = ?", paramArray);

    return (transcriptions != null && transcriptions.size() > 0 ? transcriptions
        .get(0) : null);
  }

  @Override
  public List<Transcription> select(String whereClause, Object[] paramArray) {

    String selectStatement = selectSql + whereClause;

    List<Transcription> transcriptions = getJdbcTemplate().query(
        selectStatement, paramArray, new GenericRowMapper<Transcription>(new TranscriptionExtractor()));

    return transcriptions;
  }

  @Override
  public List<TranscriptedOpaTitle> selectTranscriptedTitles(String title,
      String userName, int offset, int rows) throws DataAccessException,
      UnsupportedEncodingException {
    return selectTranscriptedTitles(title, userName, offset, rows, true);
  }
  
  
  @Override
  public List<TranscriptedOpaTitle> selectTranscriptedTitles(String title,
      String userName, int offset, int rows, boolean descOrder) throws DataAccessException,
      UnsupportedEncodingException {
   
    String sql = TRANSCRIPTED_TITLES_SELECT
        + "FROM annotation_log l "
        + TRANSCRIPTED_TITLES_JOINS
        + "WHERE l.annotation_type = 'TR' "
        + "AND l.status = 1 "
        + "AND l.first_annotation_id IN ( "
        + TRANSCRIPTED_TITLES_FIRST_ANNOTATION_IDS
        + ") ";

    if (!StringUtils.isNullOrEmtpy(title)) {
      sql += String.format("  AND (l.na_id = '%1$s' OR convert(ot.opa_title using UTF8) LIKE '%%%1$s%%') ", title);
    }

    // Order clause
    if(descOrder) {
      sql += ORDER_BY_ID_DESC;
    } else {
      sql += ORDER_BY_ID_ASC;
    }

    // Add the LIMIT configuration
    sql += " LIMIT ?, ? ";

    List<TranscriptedOpaTitle> opaTitles = getJdbcTemplate().query(sql,
        new Object[] { userName, offset, rows },
        new TranscriptedOpaTitleRowMapper());

    if (opaTitles != null && opaTitles.size() > 0) {
      return opaTitles;
    } else {
      return null;
    }
  }

  public int selectTranscriptedTitleCount(String title,
      String userName) throws DataAccessException,
      UnsupportedEncodingException {
    
    String sql = "select count(1) "
        + "FROM annotation_log l "
        + "JOIN opa_titles ot ON l.na_id = ot.na_id "
        + "JOIN accounts a ON l.account_id = a.account_id "
        + "JOIN annotation_transcriptions tran ON l.annotation_id = tran.annotation_id "
        + "WHERE l.annotation_type = 'TR' "
        + "AND l.status = 1 "
        + "AND l.first_annotation_id IN ( "
        + TRANSCRIPTED_TITLES_FIRST_ANNOTATION_IDS_FOR_COUNT
        + ") ";

    if (!StringUtils.isNullOrEmtpy(title)) {
      sql += String.format("  AND (l.na_id = '%1$s' OR convert(ot.opa_title using UTF8) LIKE '%%%1$s%%') ", title);
    }
    
    return getJdbcTemplate().queryForInt(sql, new Object[] { userName });
    
  }

  @Override
  public boolean isTranscriptionDuplicate(String naId, String objectId,
      String annotationMD5) throws DataAccessException,
      UnsupportedEncodingException {

    String sql = "SELECT count(1) FROM annotation_transcriptions WHERE na_id = ? AND object_id = ? AND annotation_md5 = ? "
        + "AND (status = 0 OR status = 1)";
    Object[] paramArray = new Object[] { naId, objectId, annotationMD5 };
    
    int duplicateCount = getJdbcTemplate().queryForInt(sql, paramArray);
    
    return (duplicateCount > 0);
  }
  
  
}
