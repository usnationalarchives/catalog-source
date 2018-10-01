package gov.nara.opa.common.valueobject.annotation.comments;

public interface CommentValueObjectConstants {

	public static final String ANNOTATION_ID_DB = "ANNOTATION_ID";
	public static final String ANNOTATION_DB = "ANNOTATION";
	public static final String ANNOTATION_MD5_DB = "ANNOTATION_MD5";
	public static final String STATUS_DB = "STATUS";
	public static final String NA_ID_DB = "NA_ID";
	public static final String OBJECT_ID_DB = "OBJECT_ID";
	public static final String PAGE_NUM_DB = "PAGE_NUM";
	public static final String OPA_ID_DB = "OPA_ID";
	public static final String PARENT_ID_DB = "PARENT_ID";
	public static final String SEQUENCE_DB = "SEQUENCE";
	public static final String ACCOUNT_ID_DB = "ACCOUNT_ID";
	public static final String ANNOTATION_TS_DB = "ANNOTATION_TS";
	public static final String ANNOTATION_CREATED_TS_DB = "ANNOTATION_CREATED_TS";

	public static final String ANNOTATION_ID_ASP = "@id";
	public static final String ANNOTATION_COMMENT_ASP = "@text";
	public static final String STATUS_ASP = "@status";
	public static final String NA_ID_ASP = "@naId";
	public static final String OBJECT_ID_ASP = "@objectId";
	public static final String PAGE_NUM_ASP = "@pageNum";
	public static final String OPA_ID_ASP = "@opaId";
	public static final String ACCOUNT_ID_ASP = "@accountId";
	public static final String SEQUENCE_ASP = "@sequence";
	public static final String PARENT_ID_ASP = "@parentId";
	public static final String ANNOTATION_CREATED_TS_ASP = "@created";
	public static final String ANNOTATION_DELETED_TS_ASP = "@deleted";
	public static final String ANNOTATION_RESTORED_TS_ASP = "@restored";

	public static final String REPLY_ASP = "reply";
	public static final String REPLIES_ASP = "replies";
	public static final String USER_NAME_ASP = "@user";
	public static final String REMOVED_BY_MODERATOR_ASP = "@removedByModerator";
	public static final String ID_ASP = "@id";
	public static final String CREATED_ASP = "@created";
	public static final String LAST_MODIFIED_ASP = "@lastModified";
	public static final String FULL_NAME_ASP = "@fullName";
	public static final String DISPLAY_FULL_NAME = "@displayFullName";
	public static final String IS_NARA_STAFF_ASP = "@isNaraStaff";

	public static final String NA_ID_REQ_ASP = "naId";
	public static final String OBJECT_ID_REQ_ASP = "objectId";
	public static final String COMMENT_ID_REQ_ASP = "commentId";
	public static final String PAGE_NUM_REQ_ASP = "pageNum";
	public static final String COMMENT_TEXT_REQ_ASP = "commentText";

	public static final String CREATE_COMMENT_ACTION = "save";
	public static final String DELETE_COMMENT_ACTION = "deleteComment";
	public static final String VIEW_COMMENT_ACTION = "";
	public static final String REMOVE_COMMENT_ACTION = "removeComment";
}
