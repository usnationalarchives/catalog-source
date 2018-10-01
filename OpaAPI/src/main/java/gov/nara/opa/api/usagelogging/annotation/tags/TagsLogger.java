package gov.nara.opa.api.usagelogging.annotation.tags;

import gov.nara.opa.architecture.logging.ApiTypeLoggingEnum;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.logging.usage.UsageLogCode;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.common.AnnotationConstants;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.TagsCollectionValueObject;

public class TagsLogger {
  public static final String DELETE_TAG_ACTION = "deleteTag";
  
  static OpaLogger log = OpaLogger.getLogger(TagsLogger.class);

  public static final String TAGS_NO_OF_USER_CONTRIBUTIONS_VIA_API_MESSGE = "Action=%1$s, AnnotationType=%2$s, "
      + "Naid=%3$s, Object=%4$s";
  public static final String INFO_TAG_MESSAGE = "naId=%1$s,objectId=%2$s,action=%3$s,tagText=%4$s";

  public static void logTags(TagsCollectionValueObject tags,
      Class<? extends AbstractBaseController> controller, String action,
      String apiType) {

    for (TagValueObject tag : tags.getTags()) {
      logTag(tag, controller, action, apiType);
    }
    
  }

  public static void logTag(TagValueObject tag, Class<?> controller,
      String action, String apiType) {
    String naId = tag.getNaId();
    String objectId = tag.getObjectId() == null ? "" : tag.getObjectId();

    if(!action.equals(DELETE_TAG_ACTION)) {
      log.usage(controller, ApiTypeLoggingEnum.toApiTypeLoggingEnum(apiType),
          UsageLogCode.TAG, String.format(
              TAGS_NO_OF_USER_CONTRIBUTIONS_VIA_API_MESSGE, action,
              AnnotationConstants.ANNOTATION_TYPE_TAG_LOGS, naId,
              objectId));
    }
    log.info(String.format(INFO_TAG_MESSAGE, naId, objectId, action, tag.getAnnotation()));
  }

}
