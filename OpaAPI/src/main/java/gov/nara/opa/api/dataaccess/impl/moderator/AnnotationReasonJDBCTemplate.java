package gov.nara.opa.api.dataaccess.impl.moderator;

import gov.nara.opa.api.dataaccess.moderator.AnnotationReasonDao;
import gov.nara.opa.api.valueobject.moderator.AnnotationReasonValueObject;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.architecture.exception.OpaRuntimeException;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class AnnotationReasonJDBCTemplate extends AbstractOpaDbJDBCTemplate
    implements AnnotationReasonDao {

  public static final String SELECT_ANNOTATION_REASON_BY_ID_SQL = "SELECT * FROM annotation_reasons WHERE REASON_ID = ?";

  @Override
  public AnnotationReasonValueObject getAnnotationReasonById(Integer reasonId) {
    List<AnnotationReasonValueObject> annotations = getJdbcTemplate().query(
        SELECT_ANNOTATION_REASON_BY_ID_SQL, new Object[] { reasonId },
        new AnnotationReasonValueObjectRowMapper());
    if (annotations == null || annotations.size() == 0) {
      return null;
    } else if (annotations.size() == 1) {
      return annotations.get(0);
    }
    throw new OpaRuntimeException(
        "More than one annotation reason found for reason id: " + reasonId);
  }

}
