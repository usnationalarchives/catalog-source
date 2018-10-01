package gov.nara.opa.api.controller.annotation.comments;

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

import gov.nara.opa.api.services.annotation.comments.CommentService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.usagelogging.annotation.comments.CommentsLogger;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.annotation.AnnotationsByIdRequestParameters;
import gov.nara.opa.api.validation.annotation.AnnotationsModifyByIdRequestParameters;
import gov.nara.opa.api.validation.annotation.comments.CommentsAndRepliesRequestParameters;
import gov.nara.opa.api.validation.annotation.comments.CommentsAndRepliesViewRequestParameters;
import gov.nara.opa.api.validation.annotation.comments.CommentsCreateRequestParameters;
import gov.nara.opa.api.validation.annotation.comments.CommentsCreateValidator;
import gov.nara.opa.api.validation.annotation.comments.CommentsDeleteByIdValidator;
import gov.nara.opa.api.validation.annotation.comments.CommentsDeleteValidator;
import gov.nara.opa.api.validation.annotation.comments.CommentsModifyByIdValidator;
import gov.nara.opa.api.validation.annotation.comments.CommentsModifyValidator;
import gov.nara.opa.api.validation.annotation.comments.CommentsSearchRequestParameters;
import gov.nara.opa.api.validation.annotation.comments.CommentsSearchValidator;
import gov.nara.opa.api.validation.annotation.comments.CommentsViewRequestParameters;
import gov.nara.opa.api.validation.annotation.comments.CommentsViewValidator;
import gov.nara.opa.api.validation.search.SolrParamsValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;
import gov.nara.opa.common.valueobject.annotation.comments.CommentsCollectionValueObject;

@Controller
public class CommentController extends AbstractBaseController {

	private static OpaLogger log = OpaLogger.getLogger(CommentController.class);

	@Autowired
	private CommentsCreateValidator createCommentsValidator;

	@Autowired
	private CommentsModifyValidator modifyCommentsValidator;
	
	@Autowired
	private CommentsModifyByIdValidator modifyCommentsByIdValidator;

	@Autowired
	private CommentsDeleteValidator deleteCommentsValidator;
	
	@Autowired
	private CommentsDeleteByIdValidator deleteCommentsByIdValidator;

	@Autowired
	private CommentsViewValidator viewCommentsValidator;

	@Autowired
	private CommentsSearchValidator commentsSearchValidator;

	@Autowired
	private CommentService commentService;

	@Autowired
	SolrParamsValidator solrParamsValidator;

	public static final String CREATE_COMMENT_ACTION = "create";
	public static final String CREATE_COMMENT_SEARCH_ACTION = "saveSearch";
	public static final String MODIFY_COMMENT_ACTION = "modifyComment";
	public static final String MODIFY_REPLY_ACTION = "modifyReply";
	public static final String COMMENT_PARENT_ENTITY_NAME = "comment";
	public static final String DELETE_COMMENT_ACTION = "deleteComment";

	public static final String INFO_COMMENT_MESSAGE = "naId=%1$s,objectId=%2$s,action=%3$s,commentText=%4$s";
	public static final String GET_COMMENT_ACTION = "getComment";
	public static final String VIEW_COMMENT_ACTION = "";
	public static final String TAGS_PARENT_ENTITY_NAME_COMMENTS = "comments";
	public static final String TAGS_PARENT_ENTITY_NAME_COMMENT = "comment";

	@RequestMapping(value = {
			"/{apiType}/" + Constants.API_VERS_NUM + "/id/{naId}/comments",
			"/{apiType}/" + Constants.API_VERS_NUM
			+ "/id/{naId}/objects/{objectId:.+}/comments" }, method = RequestMethod.POST)
	public ResponseEntity<String> createComment(
			@Valid CommentsCreateRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request)
					throws BadSqlGrammarException, DataAccessException,
					UnsupportedEncodingException {

		ValidationResult validationResult = createCommentsValidator.validate(
				bindingResult, request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					CREATE_COMMENT_ACTION);
		}

		requestParameters.setHttpSessionId(request.getSession().getId());
		CommentValueObject comment = commentService
				.createComment(requestParameters);

		return createSuccessResponseEntity(COMMENT_PARENT_ENTITY_NAME,
				requestParameters, comment, request, CREATE_COMMENT_ACTION);
	}

	@RequestMapping(value = {
			"/{apiType}/" + Constants.API_VERS_NUM
			+ "/id/{naId}/comments/{annotationId}",
			"/{apiType}/"
					+ Constants.API_VERS_NUM
					+ "/id/{naId}/objects/{objectId:.+}/comments/{annotationId}" }, method = RequestMethod.POST)
	public ResponseEntity<String> replyComment(
			@Valid CommentsCreateRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request)
					throws BadSqlGrammarException, DataAccessException,
					UnsupportedEncodingException {

		ValidationResult validationResult = createCommentsValidator.validate(
				bindingResult, request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					CREATE_COMMENT_ACTION);
		}

		requestParameters.setHttpSessionId(request.getSession().getId());
		CommentValueObject reply = commentService
				.replyComment(requestParameters);

		CommentsLogger.logComment(reply, this.getClass(),
				CREATE_COMMENT_ACTION, requestParameters.getApiType());

		return createSuccessResponseEntity(COMMENT_PARENT_ENTITY_NAME,
				requestParameters, reply, request, CREATE_COMMENT_ACTION);
	}

	@RequestMapping(value = {
			"/{apiType}/" + Constants.API_VERS_NUM
			+ "/id/{naId}/comments/{annotationId}",
			"/{apiType}/"
					+ Constants.API_VERS_NUM
					+ "/id/{naId}/objects/{objectId:.+}/comments/{annotationId}" }, method = RequestMethod.PUT)
	public ResponseEntity<String> modifyComment(
			@Valid CommentsAndRepliesRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request)
					throws BadSqlGrammarException, DataAccessException,
					UnsupportedEncodingException {

		ValidationResult validationResult = modifyCommentsValidator.validate(
				bindingResult, request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					MODIFY_COMMENT_ACTION);
		}

		CommentValueObject comment = (CommentValueObject) validationResult
				.getContextObjects().get(
						CommentsModifyValidator.COMMENT_VALUE_OBJECT_KEY);
		commentService.modifyComment(comment, requestParameters.getText(), request
				.getSession().getId());

		comment = commentService.viewCommentById(
				requestParameters.getAnnotationId(),
				requestParameters.getNaId(), requestParameters.getObjectId());

		CommentsLogger.logComment(comment, this.getClass(),
				MODIFY_COMMENT_ACTION, requestParameters.getApiType());

		return createSuccessResponseEntity(COMMENT_PARENT_ENTITY_NAME,
				requestParameters, comment, request, MODIFY_COMMENT_ACTION);
	}

	@RequestMapping(value = {"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM +
			"/comments/{annotationId}"}, method = RequestMethod.PUT)
	public ResponseEntity<String> modifyCommentById(
			@Valid AnnotationsModifyByIdRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request)
					throws BadSqlGrammarException, DataAccessException,
					UnsupportedEncodingException {

		ValidationResult validationResult = modifyCommentsByIdValidator.validate(
				bindingResult, request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					MODIFY_COMMENT_ACTION);
		}

		CommentValueObject comment = (CommentValueObject) validationResult
				.getContextObjects().get(
						CommentsModifyValidator.COMMENT_VALUE_OBJECT_KEY);
		commentService.modifyComment(comment, requestParameters.getText(), request
				.getSession().getId());

		comment = commentService.viewCommentById(
				requestParameters.getAnnotationId(),
				comment.getNaId(), comment.getObjectId());

		CommentsLogger.logComment(comment, this.getClass(),
				MODIFY_COMMENT_ACTION, requestParameters.getApiType());

		return createSuccessResponseEntity(COMMENT_PARENT_ENTITY_NAME,
				requestParameters, comment, request, MODIFY_COMMENT_ACTION);
	}

	@RequestMapping(value = {
			"/{apiType}/" + Constants.API_VERS_NUM
			+ "/id/{naId}/comments/{annotationId}/{replyId}",
			"/{apiType}/"
					+ Constants.API_VERS_NUM
					+ "/id/{naId}/objects/{objectId:.+}/comments/{annotationId}/{replyId}" }, method = RequestMethod.PUT)
	public ResponseEntity<String> modifyReply(
			@Valid CommentsAndRepliesRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request)
					throws BadSqlGrammarException, DataAccessException,
					UnsupportedEncodingException {

		ValidationResult validationResult = modifyCommentsValidator.validate(
				bindingResult, request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					MODIFY_COMMENT_ACTION);
		}

		CommentValueObject comment = (CommentValueObject) validationResult
				.getContextObjects().get(
						CommentsModifyValidator.COMMENT_VALUE_OBJECT_KEY);
		commentService.modifyReply(comment, requestParameters.getReplyId(),
				requestParameters, request.getSession().getId());

		comment = commentService.viewCommentById(
				requestParameters.getAnnotationId(),
				requestParameters.getNaId(), requestParameters.getObjectId());

		CommentsLogger.logComment(comment, this.getClass(),
				MODIFY_REPLY_ACTION, requestParameters.getApiType());

		return createSuccessResponseEntity(COMMENT_PARENT_ENTITY_NAME,
				requestParameters, comment, request, MODIFY_COMMENT_ACTION);
	}

	@RequestMapping(value = {
			"/{apiType}/" + Constants.API_VERS_NUM
			+ "/id/{naId}/comments/{annotationId}",
			"/{apiType}/"
					+ Constants.API_VERS_NUM
					+ "/id/{naId}/objects/{objectId:.+}/comments/{annotationId}" }, method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteComment(
			@Valid CommentsAndRepliesViewRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request)
					throws BadSqlGrammarException, DataAccessException,
					UnsupportedEncodingException {

		ValidationResult validationResult = deleteCommentsValidator.validate(
				bindingResult, request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					DELETE_COMMENT_ACTION);
		}

		CommentValueObject comment = (CommentValueObject) validationResult
				.getContextObjects().get(
						CommentsDeleteValidator.COMMENT_VALUE_OBJECT_KEY);
		commentService.deleteComment(comment, request.getSession().getId());

		comment = commentService.viewCommentById(
				requestParameters.getAnnotationId(),
				requestParameters.getNaId(), requestParameters.getObjectId());

		CommentsLogger.logComment(comment, this.getClass(),
				DELETE_COMMENT_ACTION, requestParameters.getApiType());

		return createSuccessResponseEntity(COMMENT_PARENT_ENTITY_NAME,
				requestParameters, comment, request, DELETE_COMMENT_ACTION);
	}

	@RequestMapping(value = {"/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM +
	"/comments/{annotationId}"}, method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteCommentById(
			@Valid AnnotationsByIdRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request)
					throws BadSqlGrammarException, DataAccessException,
					UnsupportedEncodingException {

		ValidationResult validationResult = deleteCommentsByIdValidator.validate(
				bindingResult, request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					DELETE_COMMENT_ACTION);
		}

		CommentValueObject comment = (CommentValueObject) validationResult
				.getContextObjects().get(
						CommentsDeleteValidator.COMMENT_VALUE_OBJECT_KEY);
		commentService.deleteComment(comment, request.getSession().getId());

		CommentsLogger.logComment(comment, this.getClass(),
				DELETE_COMMENT_ACTION, requestParameters.getApiType());

		return createSuccessResponseEntity(COMMENT_PARENT_ENTITY_NAME,
				requestParameters, comment, request, DELETE_COMMENT_ACTION);
	}

	@RequestMapping(value = {
			"/{apiType}/" + Constants.API_VERS_NUM
			+ "/id/{naId}/comments/{annotationId}/{replyId}",
			"/{apiType}/"
					+ Constants.API_VERS_NUM
					+ "/id/{naId}/objects/{objectId:.+}/comments/{annotationId}/{replyId}" }, method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteReply(
			@Valid CommentsAndRepliesViewRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request)
					throws BadSqlGrammarException, DataAccessException,
					UnsupportedEncodingException {

		ValidationResult validationResult = deleteCommentsValidator.validate(
				bindingResult, request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					DELETE_COMMENT_ACTION);
		}

		CommentValueObject comment = (CommentValueObject) validationResult
				.getContextObjects().get(
						CommentsDeleteValidator.COMMENT_VALUE_OBJECT_KEY);
		commentService.deleteReply(comment, requestParameters.getReplyId(),
				request.getSession().getId());

		comment = commentService.viewCommentById(
				requestParameters.getAnnotationId(),
				requestParameters.getNaId(), requestParameters.getObjectId());

		CommentsLogger.logComment(comment, this.getClass(),
				DELETE_COMMENT_ACTION, requestParameters.getApiType());

		return createSuccessResponseEntity(COMMENT_PARENT_ENTITY_NAME,
				requestParameters, comment, request, DELETE_COMMENT_ACTION);
	}

	@RequestMapping(value = {
			"/{apiType}/" + Constants.API_VERS_NUM + "/id/{naId}/comments",
			"/{apiType}/" + Constants.API_VERS_NUM
			+ "/id/{naId}/objects/{objectId:.+}/comments" }, method = RequestMethod.GET)
	public ResponseEntity<String> viewComments(
			@Valid CommentsViewRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request) {

		ValidationResult validationResult = viewCommentsValidator.validate(
				bindingResult, request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					VIEW_COMMENT_ACTION);
		}

		CommentsCollectionValueObject comments = commentService
				.getComments(requestParameters);

		AbstractWebEntityValueObject responseObject = null;
		String entityName = null;

		Integer totalComments = comments.getComments().size();
		if (totalComments.intValue() == 0) {
			addNoRecordsFoundError(validationResult);
			return createErrorResponseEntity(validationResult, request,
					VIEW_COMMENT_ACTION);
		} else if (totalComments.intValue() == 1
				&& requestParameters.getText() != null) {
			responseObject = comments.getComments().get(0);
			entityName = TAGS_PARENT_ENTITY_NAME_COMMENT;
		} else {
			responseObject = comments;
			entityName = TAGS_PARENT_ENTITY_NAME_COMMENTS;
		}

		for (CommentValueObject comment : comments.getComments()) {
			log.debug(String.format(INFO_COMMENT_MESSAGE, comment.getNaId(),
					comment.getObjectId(), GET_COMMENT_ACTION,
					comment.getAnnotation()));
		}

		return createSuccessResponseEntity(entityName, requestParameters,
				responseObject, request, VIEW_COMMENT_ACTION);
	}

	@RequestMapping(value = {
			"/{apiType}/" + Constants.API_VERS_NUM
			+ "/id/{naId}/comments/{annotationId}",
			"/{apiType}/"
					+ Constants.API_VERS_NUM
					+ "/id/{naId}/objects/{objectId:.+}/comments/{annotationId}" }, method = RequestMethod.GET)
	public ResponseEntity<String> viewComment(
			@Valid CommentsViewRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request) {

		ValidationResult validationResult = viewCommentsValidator.validate(
				bindingResult, request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					VIEW_COMMENT_ACTION);
		}

		CommentValueObject comment = commentService.viewCommentById(
				requestParameters.getAnnotationId(),
				requestParameters.getNaId(), requestParameters.getObjectId());

		if (comment == null) {
			addNoRecordsFoundError(validationResult);
			return createErrorResponseEntity(validationResult, request,
					VIEW_COMMENT_ACTION);
		}

		log.info(String.format(INFO_COMMENT_MESSAGE, comment.getNaId(),
				comment.getObjectId(), GET_COMMENT_ACTION,
				comment.getAnnotation()));

		return createSuccessResponseEntity(TAGS_PARENT_ENTITY_NAME_COMMENT,
				requestParameters, comment, request, VIEW_COMMENT_ACTION);
	}

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM }, method = RequestMethod.POST, params = "comment")
	public ResponseEntity<String> createSearchComment(
			@Valid CommentsSearchRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request)
					throws BadSqlGrammarException, DataAccessException,
					UnsupportedEncodingException {

		ValidationResult validationResult = commentsSearchValidator.validate(
				bindingResult, request,
				AbstractRequestParameters.PUBLIC_API_TYPE);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					CREATE_COMMENT_SEARCH_ACTION);
		}
		requestParameters.setQueryParameters(request.getParameterMap());
		solrParamsValidator.validate(validationResult,
				requestParameters.getQueryParameters());
		CommentsCreateRequestParameters baseRequestParameters = commentsSearchValidator
				.createAddBaseRequestParameters(request, requestParameters,
						validationResult);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					CREATE_COMMENT_SEARCH_ACTION);
		}

		validationResult.setValidatedRequest(baseRequestParameters);
		createCommentsValidator.performCustomValidation(validationResult,
				request);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					CREATE_COMMENT_SEARCH_ACTION);
		}

		log.debug(String.format("Page number:%1$d",
				baseRequestParameters.getPageNum()));

		baseRequestParameters.setHttpSessionId(request.getSession().getId());
		CommentValueObject comment = commentService
				.createComment(baseRequestParameters);

		CommentsLogger.logComment(comment, this.getClass(),
				CREATE_COMMENT_SEARCH_ACTION,
				baseRequestParameters.getApiType());

		return createSuccessResponseEntity(COMMENT_PARENT_ENTITY_NAME,
				baseRequestParameters, comment, request,
				CREATE_COMMENT_SEARCH_ACTION);
	}

	private void addNoRecordsFoundError(ValidationResult validationResult) {
		ValidationError error = new ValidationError();
		error.setErrorCode(ErrorCodeConstants.COMMENT_NOT_FOUND);
		error.setErrorMessage(ErrorConstants.COMMENT_NOT_FOUND);
		validationResult.addCustomValidationError(error);
		validationResult.setHttpStatus(HttpStatus.NOT_FOUND);
	}
}