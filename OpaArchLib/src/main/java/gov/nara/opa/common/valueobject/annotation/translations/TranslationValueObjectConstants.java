package gov.nara.opa.common.valueobject.annotation.translations;

import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;

/*
 * Constants for the ANNOTATION_TRANSLATIONS table and store procedures
 */
public interface TranslationValueObjectConstants extends CommonValueObjectConstants {

	public static final String ANNOTATION_ID_DB = "annotation_id";
	public static final String ANNOTATION_DB = "annotation";
	public static final String LANGUAGE_DB = "language_iso";
	public static final String SAVED_VERS_NUM_DB = "saved_vers_num";
	public static final String FIRST_ANNOTATION_ID_DB = "first_annotation_id";
	public static final String ANNOTATION_MD5_DB = "annotation_md5";
	public static final String STATUS_DB = "status";
	public static final String NA_ID_DB = "na_id";
	public static final String OBJECT_ID_DB = "object_id";
	public static final String PAGE_NUM_DB = "page_num";
	public static final String OPA_ID_DB = "opa_id";
	public static final String ACCOUNT_ID_DB = "account_id";
	public static final String ANNOTATION_TS_DB = "annotation_ts";

	public static final String ANNOTATION_ID_PARAM = "annotationId";
	public static final String ANNOTATION_PARAM = "annotation";
	public static final String LANGUAGE_ISO_PARAM = "languageIso";
	public static final String SAVED_VERS_NUM_PARAM = "savedVersNum";
	public static final String ANNOTATION_MD5_PARAM = "annotationMD5";
	public static final String FIRST_ANNOTATION_ID_PARAM = "firstAnnotationId";
	public static final String STATUS_PARAM = "status";
	public static final String NAID_PARAM = "naId";
	public static final String OBJECT_ID_PARAM = "objectId";
	public static final String PAGE_NUM_PARAM = "pageNum";
	public static final String OPA_ID_PARAM = "opaId";
	public static final String ACCOUNT_ID_PARAM = "accountId";
	  
	public static final String ANNOTATION_ID_ASP = "@annotationId";
	public static final String ANNOTATION_TRANSLATION_ASP = "@text";
	public static final String LANGUAGE_ASP = "@language";
	public static final String VERSION_ASP = "@version";
	public static final String FIRST_ANNOTATION_ID_ASP = "@firstAnnotationId";
	public static final String STATUS_ASP = "@status";
	public static final String NA_ID_ASP = "@naId";
	public static final String OBJECT_ID_ASP = "@objectId";
	public static final String PAGE_NUM_ASP = "@pageNumber";
	public static final String OPA_ID_ASP = "@opaId";
	public static final String ACCOUNT_ID_ASP = "@accountId";
}