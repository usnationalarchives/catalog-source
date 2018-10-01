package gov.nara.opa.common.valueobject.annotation.tags;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;

public class TagValueObjectHelper {

  public static TagValueObject createTagForInsert(Integer accountId,
      String naId, String objectId, String text, Integer pageNum) {
    TagValueObject tag = new TagValueObject();
    tag.setAccountId(accountId);
    tag.setAnnotation(text);
    tag.setAnnotationMD5(DigestUtils.md5Hex(text));
    // tag.setAnnotationTS(TimestampUtils.getCurrentTimeInUtc());
    tag.setAnnotationTS(new Timestamp(new Date().getTime()));
    tag.setNaId(naId);
    tag.setObjectId(objectId);
    tag.setStatus(true);
    tag.setPageNum(pageNum);
    // TODO what should we do about Opa ID?
    return tag;
  }
}
