package gov.nara.opa.api.controller.moderator;

import gov.nara.opa.api.services.annotation.comments.CommentService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.moderator.CommentsDeleteModeratorValidator;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.validation.moderator.CommentsModeratorRequestParameters;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class CommentModeratorController extends AbstractBaseController {

  @Autowired
  private CommentService commentService;

  @Autowired
  private CommentsDeleteModeratorValidator commentsModeratorValidator;

  public static final String RESTORE_COMMENT_ACTION = "restoreComment";
  public static final String REMOVE_COMMENT_ACTION = "removeComment";

  public static final String COMMENT_PARENT_ENTITY_NAME = "comment";

  @RequestMapping(value = {
      "/{apiType}/" + Constants.API_VERS_NUM
          + "/moderator/comments/id/{naId}/{annotationId}",
      "/{apiType}/"
          + Constants.API_VERS_NUM
          + "/moderator/comments/id/{naId}/objects/{objectId:.+}/{annotationId}" }, method = RequestMethod.DELETE)
  public ResponseEntity<String> removeComment(
      @Valid CommentsModeratorRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request)
      throws BadSqlGrammarException, DataAccessException,
      UnsupportedEncodingException {

    ValidationResult validationResult = commentsModeratorValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          REMOVE_COMMENT_ACTION);
    }

    CommentValueObject comment = (CommentValueObject) validationResult
        .getContextObjects().get(
            CommentsDeleteModeratorValidator.COMMENT_VALUE_OBJECT_KEY);
    if (comment != null) {
      commentService.removeComment(requestParameters, comment, request
          .getSession().getId());
      return createSuccessResponseEntity(COMMENT_PARENT_ENTITY_NAME,
          requestParameters, comment, request, REMOVE_COMMENT_ACTION);
    } else {
      ValidationUtils.setValidationError(validationResult,
          ArchitectureErrorCodeConstants.NOT_FOUND,
          String.format(ArchitectureErrorMessageConstants.NO_RECORDS_FOUND, COMMENT_PARENT_ENTITY_NAME),
          REMOVE_COMMENT_ACTION, HttpStatus.NOT_FOUND);
      return createErrorResponseEntity(validationResult, request,
          REMOVE_COMMENT_ACTION);
    }
  }

  @RequestMapping(value = {
      "/{apiType}/" + Constants.API_VERS_NUM
          + "/moderator/comments/id/{naId}/{annotationId}",
      "/{apiType}/"
          + Constants.API_VERS_NUM
          + "/moderator/comments/id/{naId}/objects/{objectId:.+}/{annotationId}" }, method = RequestMethod.PUT)
  public ResponseEntity<String> restoreComment(
      @Valid CommentsModeratorRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request)
      throws BadSqlGrammarException, DataAccessException,
      UnsupportedEncodingException {

    ValidationResult validationResult = commentsModeratorValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          RESTORE_COMMENT_ACTION);
    }

    CommentValueObject comment = (CommentValueObject) validationResult
        .getContextObjects().get(
            CommentsDeleteModeratorValidator.COMMENT_VALUE_OBJECT_KEY);
    commentService.restoreComment(requestParameters, comment, request
        .getSession().getId());
    return createSuccessResponseEntity(COMMENT_PARENT_ENTITY_NAME,
        requestParameters, comment, request, RESTORE_COMMENT_ACTION);
  }
}
