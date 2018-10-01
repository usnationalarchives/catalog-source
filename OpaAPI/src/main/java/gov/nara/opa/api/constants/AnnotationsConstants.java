package gov.nara.opa.api.constants;

public final class AnnotationsConstants {

	public static final String TEXT_FIELD_NAME = "text";
	public static final String NAID_FIELD_NAME = "naId";
	public static final String OBJECTID_FIELD_NAME = "objectId";
	public static final String PAGE_NUMBER_FIELD_NAME = "pageNum";

	public static final String ACTION_FIELD_NAME = "action";

	/*
	 * Valid actions for lock operations
	 */
	public static final String LOCK_ACTION = "lock";
	public static final String UNLOCK_ACTION = "unlock";

	/*
	 * Valid query parameters for locks
	 */
	public static final String[] LOCK_VALID_PARAMETERS = { "action", "pageNum", "format", "pretty" };

	/*
	 * Valid actions for transcription and translation operations
	 */
	public static final String SAVE_AND_RELOCK_ACTION = "saveAndRelock";
	public static final String SAVE_AND_UNLOCK_ACTION = "saveAndUnlock";

	/*
	 * Valid query parameters for annotations
	 */
	public static final String[] ANNOTATION_VALID_PARAMETERS = { "action", "text", "pageNum", "format", "pretty" };
}