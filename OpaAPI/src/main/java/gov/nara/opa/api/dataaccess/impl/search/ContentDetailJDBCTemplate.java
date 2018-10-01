package gov.nara.opa.api.dataaccess.impl.search;

import gov.nara.opa.api.dataaccess.search.ContentDetailDao;
import gov.nara.opa.api.valueobject.search.ContentDetailObjectConstants;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ContentDetailJDBCTemplate extends AbstractOpaDbJDBCTemplate
    implements ContentDetailDao, ContentDetailObjectConstants {

  // private static final String SELECT_OPA_TITLE = "SELECT na_id, opa_title, "
  // + "opa_type, total_pages FROM opa_titles WHERE na_id = ?";

  public static final String ACTIVE_ANNOTATION_EXISTS = "SELECT log_id FROM annotation_log "
      + "WHERE na_id = ? AND object_id = ? AND status = ? LIMIT 1";

  // @Override
  // public List<OpaTitle> selectOpaType(int naId) throws DataAccessException {
  //
  // List<OpaTitle> opaTitles = getJdbcTemplate().query(SELECT_OPA_TITLE,
  // new Object[] { naId }, new OpaTitleRowMapper());

  // if (opaTitles != null && opaTitles.size() > 0) {
  // return opaTitles;
  // } else {
  // return null;
  // }
  // }

  @Override
  public boolean annotationExists(String naId, String objectId) {
    String sql = ACTIVE_ANNOTATION_EXISTS;
    List<Object> params = new ArrayList<Object>();
    params.add(naId);
    params.add(objectId);
    params.add("1");

    if (getJdbcTemplate().query(sql, params.toArray(),
        new AnnotationExistsRowMapper()).size() > 0) {
      return true;
    }

    return false;
  }
}
