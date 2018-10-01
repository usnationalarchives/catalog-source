package gov.nara.opa.architecture.web.validation;

import gov.nara.opa.architecture.web.validation.constraint.messageinterpolator.MessageInterpolator;
import gov.nara.opa.architecture.web.validation.constraint.messageinterpolator.OpaConstrainedStringListMessageInterpolator;
import gov.nara.opa.architecture.web.validation.constraint.messageinterpolator.OpaEmailMessageInterpolator;
import gov.nara.opa.architecture.web.validation.constraint.messageinterpolator.OpaIntegerTypeMistmatchMessageInterpolator;
import gov.nara.opa.architecture.web.validation.constraint.messageinterpolator.OpaNotNullAndNotEmptyMessageInterpolator;
import gov.nara.opa.architecture.web.validation.constraint.messageinterpolator.OpaPatternMessageInterpolator;
import gov.nara.opa.architecture.web.validation.constraint.messageinterpolator.OpaSizeMessageInterpolator;
import gov.nara.opa.architecture.web.validation.constraint.messageinterpolator.OpaUserPasswordMessageInterpolator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.validation.FieldError;

/**
 * @author aolaru
 * @date Jun 4, 2014 Provides a mechanism to look up the specific interpolator
 *       associated with individual validations and use it to interpolate error
 *       messages/codes
 */
public class DefaultErrorMessagesInterpolator implements MessageInterpolator {

  static Logger log = Logger.getLogger(DefaultErrorMessagesInterpolator.class);

  protected static final Map<String, MessageInterpolator> messageInterpolators = new ConcurrentHashMap<String, MessageInterpolator>();

  // the keys in this map are changed against the codes in this method to find
  // the appropriate interpolator to use: @see <a
  // href="http://docs.spring.io/spring/docs/3.0.x/api/org/springframework/context/support/DefaultMessageSourceResolvable.html#getCodes()">DefaultMessageSourceResolvable.getCodes()</a>
  static {
    messageInterpolators.put("OpaSize", new OpaSizeMessageInterpolator());
    messageInterpolators.put("OpaPattern", new OpaPatternMessageInterpolator());
    messageInterpolators.put("OpaUserPassword",
        new OpaUserPasswordMessageInterpolator());
    messageInterpolators.put("OpaNotNullAndNotEmpty",
        new OpaNotNullAndNotEmptyMessageInterpolator());
    messageInterpolators.put("OpaEmail", new OpaEmailMessageInterpolator());
    messageInterpolators.put("java.lang.Integer.typeMismatch",
        new OpaIntegerTypeMistmatchMessageInterpolator());
    messageInterpolators.put("OpaConstrainedStringList",
        new OpaConstrainedStringListMessageInterpolator());
  }

  @Override
  public void interpolate(FieldError springError, ValidationError opaError) {
    MessageInterpolator messageInterpolator = findMessageInterpolator(springError);
    if (messageInterpolator == null) {
      log.error("Message interpolator not found to create error messages for field: "
          + springError.getField());
      return;
    }
    messageInterpolator.interpolate(springError, opaError);
  }

  /**
   * Find the appropriate Opa Message interpolator to be used to convert
   * messages/codes associated with this spring validation error
   * 
   * @param springError
   *          Spring error whose codes is interrogated to find out the message
   *          interpolator
   * @return The Opa Message interpolator
   */
  protected MessageInterpolator findMessageInterpolator(FieldError springError) {
    String[] codes = springError.getCodes();
    for (String code : codes) {
      MessageInterpolator messageInterpolator = getDefaultMessageInterpolatorsGroup()
          .get(code);
      if (messageInterpolator != null) {
        return messageInterpolator;
      }
    }
    return null;
  }

  /**
   * Provides the default map of codes -> message interpolators. Subclasses can
   * override this method to provide their own map.
   * 
   * @return The map of Message Interpolators
   */
  protected Map<String, MessageInterpolator> getDefaultMessageInterpolatorsGroup() {
    return messageInterpolators;
  }

}
