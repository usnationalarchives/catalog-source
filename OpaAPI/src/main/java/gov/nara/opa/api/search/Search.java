package gov.nara.opa.api.search;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.solr.common.util.NamedList;

public class Search {

  // Document Number
  int docNumber;

  // Score
  Float score;

  // Identifiers
  String naId;
  int naIdSort;
  String personId;
  String orgId;
  String opaId;
  ArrayList<String> hmsEntryNumbers;
  String hmsEntryNumbersSort;
  String localId;
  ArrayList<String> containerId;
  String url;
  String accessPath;

  // Types
  String source;
  String type;
  String oldScope;
  String level;
  String parentLevel;
  String iconType;
  List<String> fileFormat;
  String originalMimeType;
  List<String> tabType;
  List<String> materialsType;

  // Display Fields
  String title;
  String titleSort;
  String parentTitle;
  List<String> allTitles;
  String webArea;
  String webAreaUrl;
  String content;
  List<String> creators;
  String teaser;
  boolean isOnline = false;
  boolean hasOnline = false;
  String thumbnailFile;
  String titleDate;
  List<String> location;
  List<String> locationKeywords;
  List<String> locationIds;
  List<String> dateRangeFacet;

  // Archival Hierachy Fields
  String parentNaId;
  List<String> ancestorNaIds;
  boolean hasChildren = false;

  // Digital Object Fields
  String objectId;
  int objectSortNum;
  String objectFile;
  String fileSize;

  // Brief Results
  LinkedHashMap<String, NamedList<String>> briefResults;

  // Annotation Fields
  List<String> allContributors;
  String allContributionsFirstDateTime;
  String allContributionsLatestDateTime;
  String tagsKeywords;
  String tagsExact;
  String tagsContributors;
  String tagFirstDateTime;
  String tagLatestDateTime;
  String commentFirstDateTime;
  String commentLatestDateTime;
  List<String> commentsContributors;
  String transcriptionFirstDateTime;
  String transcriptionLatestDateTime;
  List<String> transcriptionContributors;
  String translationFirstDateTime;
  String translationLatestDateTime;
  List<String> translationContributors;

  // XML Fields
  String description;
  String authority;
  String objects;

  // Authority Information
  String allAuthorityIds;
  String creatorIds;
  String subjectIds;
  String donorIds;
  String contributorIds;
  String personalReferenceIds;

  // Dates and Times
  String recordCreatedDateTime;
  String recordUpdatedDateTime;
  String firstIngestedDateTime;
  String ingestedDateTime;
  String productionDate;
  String productionDateString;
  String productionDateQualifier;
  String broadcastDate;
  String broadcastDateString;
  String broadcastDateQualifier;
  String releaseDate;
  String releaseDateString;
  String releaseDateQualifier;
  String coverageStartDate;
  String coverageStartDateString;
  String coverageStartDateQualifier;
  String coverageEndDate;
  String coverageEndDateString;
  String coverageEndDateQualifier;
  String inclusiveStartDate;
  String inclusiveStartDateString;
  String inclusiveStartDateQualifier;
  String inclusiveEndDate;
  String inclusiveEndDateString;
  String inclusiveEndDateQualifier;
  String authorityStartDate;
  String authorityStartDateString;
  String authorityStartDateQualifier;
  String authorityEndDate;
  String authorityEndDateString;
  String authorityEndDateQualifier;

  public Search() {

  }

  public Search(
      // Document Number
      int docNumber,

      // Score
      float score,

      // Identifiers
      String naId,
      int naIdSort,
      String personId,
      String orgId,
      String opaId,
      ArrayList<String> hmsEntryNumbers,
      String hmsEntryNumbersSort,
      String localId,
      ArrayList<String> containerId,
      String url,
      String accessPath,
      String source,
      String type,
      String oldScope,
      String level,
      String parentLevel,
      String iconType,
      List<String> fileFormat,
      String originalMimeType,
      List<String> tabType,
      List<String> materialsType,

      // Display Fields
      String title,
      String titleSort,
      String parentTitle,
      List<String> allTitles,
      String webArea,
      String webAreaUrl,
      String content,
      List<String> creators,
      String teaser,
      boolean isOnline,
      boolean hasOnline,
      String thumbnailFile,
      String titleDate,
      List<String> location,
      List<String> locationKeywords,
      List<String> locationIds,
      List<String> dateRangeFacet,

      // Archival Hierachy Fields
      String parentNaId,
      List<String> ancestorNaIds,
      boolean hasChildren,

      // Digital Object Fields
      String objectId,
      int objectSortNum,
      String objectFile,
      String fileSize,

      // Brief Results
      LinkedHashMap<String, NamedList<String>> briefResults,

      // Annotation Fields
      List<String> allContributors,
      String allContributionsFirstDateTime,
      String allContributionsLatestDateTime,
      String tagsKeywords,
      String tagsExact,
      String tagsContributors,
      String tagFirstDateTime,
      String tagLatestDateTime,
      String commentFirstDateTime,
      String commentLatestDateTime,
      List<String> commentsContributors,
      String transcriptionFirstDateTime,
      String transcriptionLatestDateTime,
      List<String> transcriptionContributors,
      String translationFirstDateTime,
      String translationLatestDateTime,
      List<String> translationContributors,

      // XML Fields
      String description,
      String authority,
      String objects,

      // Authority Information
      String allAuthorityIds,
      String creatorIds,
      String subjectIds,
      String donorIds,
      String contributorIds,
      String personalReferenceIds,

      // Dates and Times
      String recordCreatedDateTime, String recordUpdatedDateTime,
      String firstIngestedDateTime, String ingestedDateTime,
      String productionDate, String productionDateString,
      String productionDateQualifier, String broadcastDate,
      String broadcastDateString, String broadcastDateQualifier,
      String releaseDate, String releaseDateString,
      String releaseDateQualifier, String coverageStartDate,
      String coverageStartDateString, String coverageStartDateQualifier,
      String coverageEndDate, String coverageEndDateString,
      String coverageEndDateQualifier, String inclusiveStartDate,
      String inclusiveStartDateString, String inclusiveStartDateQualifier,
      String inclusiveEndDate, String inclusiveEndDateString,
      String inclusiveEndDateQualifier, String authorityStartDate,
      String authorityStartDateString, String authorityStartDateQualifier,
      String authorityEndDate, String authorityEndDateString,
      String authorityEndDateQualifier) {
    this.docNumber = docNumber;
    this.score = score;
    this.opaId = opaId;
    this.naId = naId;
    this.naIdSort = naIdSort;
    this.personId = personId;
    this.orgId = orgId;
    this.hmsEntryNumbers = hmsEntryNumbers;
    this.hmsEntryNumbersSort = hmsEntryNumbersSort;
    this.localId = localId;
    this.containerId = containerId;
    this.url = url;
    this.accessPath = accessPath;
    this.source = source;
    this.type = type;
    this.oldScope = oldScope;
    this.level = level;
    this.parentLevel = parentLevel;
    this.iconType = iconType;
    this.fileFormat = fileFormat;
    this.originalMimeType = originalMimeType;
    this.tabType = tabType;
    this.materialsType = materialsType;
    this.title = title;
    this.titleSort = titleSort;
    this.parentTitle = parentTitle;
    this.allTitles = allTitles;
    this.webArea = webArea;
    this.webAreaUrl = webAreaUrl;
    this.content = content;
    this.creators = creators;
    this.teaser = teaser;
    this.isOnline = isOnline;
    this.hasOnline = hasOnline;
    this.thumbnailFile = thumbnailFile;
    this.titleDate = titleDate;
    this.location = location;
    this.locationKeywords = locationKeywords;
    this.locationIds = locationIds;
    this.dateRangeFacet = dateRangeFacet;
    this.parentNaId = parentNaId;
    this.ancestorNaIds = ancestorNaIds;
    this.hasChildren = hasChildren;
    this.objectId = objectId;
    this.objectSortNum = objectSortNum;
    this.objectFile = objectFile;
    this.fileSize = fileSize;
    this.briefResults = briefResults;
  }

  public int getDocNumber() {
    return docNumber;
  }

  public void setDocNumber(int docNumber) {
    this.docNumber = docNumber;
  }

  public Float getScore() {
    return score;
  }

  public void setScore(Float score) {
    this.score = score;
  }

  public String getNaId() {
    return naId;
  }

  public void setNaId(String naId) {
    this.naId = naId;
  }

  public int getNaIdSort() {
    return naIdSort;
  }

  public void setNaIdSort(int naIdSort) {
    this.naIdSort = naIdSort;
  }

  public String getPersonId() {
    return personId;
  }

  public void setPersonId(String personId) {
    this.personId = personId;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public String getOpaId() {
    return opaId;
  }

  public void setOpaId(String opaId) {
    this.opaId = opaId;
  }

  public ArrayList<String> getHmsEntryNumbers() {
    return hmsEntryNumbers;
  }

  public void setHmsEntryNumbers(ArrayList<String> hmsEntryNumbers) {
    this.hmsEntryNumbers = hmsEntryNumbers;
  }

  public String getHmsEntryNumbersSort() {
    return hmsEntryNumbersSort;
  }

  public void setHmsEntryNumbersSort(String hmsEntryNumbersSort) {
    this.hmsEntryNumbersSort = hmsEntryNumbersSort;
  }

  public String getLocalId() {
    return localId;
  }

  public void setLocalId(String localId) {
    this.localId = localId;
  }

  public ArrayList<String> getContainerId() {
    return containerId;
  }

  public void setContainerId(ArrayList<String> containerId) {
    this.containerId = containerId;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getAccessPath() {
    return accessPath;
  }

  public void setAccessPath(String accessPath) {
    this.accessPath = accessPath;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getOldScope() {
    return oldScope;
  }

  public void setOldScope(String oldScope) {
    this.oldScope = oldScope;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public String getParentLevel() {
    return parentLevel;
  }

  public void setParentLevel(String parentLevel) {
    this.parentLevel = parentLevel;
  }

  public String getIconType() {
    return iconType;
  }

  public void setIconType(String iconType) {
    this.iconType = iconType;
  }

  public List<String> getFileFormat() {
    return fileFormat;
  }

  public void setFileFormat(List<String> fileFormat) {
    this.fileFormat = fileFormat;
  }

  public String getOriginalMimeType() {
    return originalMimeType;
  }

  public void setOriginalMimeType(String originalMimeType) {
    this.originalMimeType = originalMimeType;
  }

  public List<String> getTabType() {
    return tabType;
  }

  public void setTabType(List<String> tabType) {
    this.tabType = tabType;
  }

  public List<String> getMaterialsType() {
    return materialsType;
  }

  public void setMaterialsType(List<String> materialsType) {
    this.materialsType = materialsType;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitleSort() {
    return titleSort;
  }

  public void setTitleSort(String titleSort) {
    this.titleSort = titleSort;
  }

  public String getParentTitle() {
    return parentTitle;
  }

  public void setParentTitle(String parentTitle) {
    this.parentTitle = parentTitle;
  }

  public List<String> getAllTitles() {
    return allTitles;
  }

  public void setAllTitles(List<String> allTitles) {
    this.allTitles = allTitles;
  }

  public String getWebArea() {
    return webArea;
  }

  public void setWebArea(String webArea) {
    this.webArea = webArea;
  }

  public String getWebAreaUrl() {
    return webAreaUrl;
  }

  public void setWebAreaUrl(String webAreaUrl) {
    this.webAreaUrl = webAreaUrl;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public List<String> getCreators() {
    return creators;
  }

  public void setCreators(List<String> creators) {
    this.creators = creators;
  }

  public String getTeaser() {
    return teaser;
  }

  public void setTeaser(String teaser) {
    this.teaser = teaser;
  }

  public boolean getIsOnline() {
    return isOnline;
  }

  public void setIsOnline(boolean isOnline) {
    this.isOnline = isOnline;
  }

  public boolean getHasOnline() {
    return hasOnline;
  }

  public void setHasOnline(boolean hasOnline) {
    this.hasOnline = hasOnline;
  }

  public String getThumbnailFile() {
    return thumbnailFile;
  }

  public void setThumbnailFile(String thumbnailFile) {
    this.thumbnailFile = thumbnailFile;
  }

  public String getTitleDate() {
    return titleDate;
  }

  public void setTitleDate(String titleDate) {
    this.titleDate = titleDate;
  }

  public List<String> getLocation() {
    return location;
  }

  public void setLocation(List<String> location) {
    this.location = location;
  }

  public List<String> getLocationKeywords() {
    return locationKeywords;
  }

  public void setLocationKeywords(List<String> locationKeywords) {
    this.locationKeywords = locationKeywords;
  }

  public List<String> getLocationIds() {
    return locationIds;
  }

  public void setLocationIds(List<String> locationIds) {
    this.locationIds = locationIds;
  }

  public List<String> getDateRangeFacet() {
    return dateRangeFacet;
  }

  public void setDateRangeFacet(List<String> dateRangeFacet) {
    this.dateRangeFacet = dateRangeFacet;
  }

  public String getParentNaId() {
    return parentNaId;
  }

  public void setParentNaId(String parentNaId) {
    this.parentNaId = parentNaId;
  }

  public List<String> getAncestorNaIds() {
    return ancestorNaIds;
  }

  public void setAncestorNaIds(List<String> ancestorNaIds) {
    this.ancestorNaIds = ancestorNaIds;
  }

  public boolean getHasChildren() {
    return hasChildren;
  }

  public void setHasChildren(boolean hasChildren) {
    this.hasChildren = hasChildren;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public int getObjectSortNum() {
    return objectSortNum;
  }

  public void setObjectSortNum(int objectSortNum) {
    this.objectSortNum = objectSortNum;
  }

  public String getObjectFile() {
    return objectFile;
  }

  public void setObjectFile(String objectFile) {
    this.objectFile = objectFile;
  }

  public String getFileSize() {
    return fileSize;
  }

  public void setFileSize(String fileSize) {
    this.fileSize = fileSize;
  }

  public LinkedHashMap<String, NamedList<String>> getBriefResults() {
    return briefResults;
  }

  public void setBriefResults(
      LinkedHashMap<String, NamedList<String>> briefResults) {
    this.briefResults = briefResults;
  }

  public List<String> getAllContributors() {
    return allContributors;
  }

  public void setAllContributors(List<String> allContributors) {
    this.allContributors = allContributors;
  }

  public String getAllContributionsFirstDateTime() {
    return allContributionsFirstDateTime;
  }

  public void setAllContributionsFirstDateTime(
      String allContributionsFirstDateTime) {
    this.allContributionsFirstDateTime = allContributionsFirstDateTime;
  }

  public String getAllContributionsLatestDateTime() {
    return allContributionsLatestDateTime;
  }

  public void setAllContributionsLatestDateTime(
      String allContributionsLatestDateTime) {
    this.allContributionsLatestDateTime = allContributionsLatestDateTime;
  }

  public String getTagsKeywords() {
    return tagsKeywords;
  }

  public void setTagsKeywords(String tagsKeywords) {
    this.tagsKeywords = tagsKeywords;
  }

  public String getTagsExact() {
    return tagsExact;
  }

  public void setTagsExact(String tagsExact) {
    this.tagsExact = tagsExact;
  }

  public String getTagsContributors() {
    return tagsContributors;
  }

  public void setTagsContributors(String tagsContributors) {
    this.tagsContributors = tagsContributors;
  }

  public String getTagFirstDateTime() {
    return tagFirstDateTime;
  }

  public void setTagFirstDateTime(String tagFirstDateTime) {
    this.tagFirstDateTime = tagFirstDateTime;
  }

  public String getTagLatestDateTime() {
    return tagLatestDateTime;
  }

  public void setTagLatestDateTime(String tagLatestDateTime) {
    this.tagLatestDateTime = tagLatestDateTime;
  }

  public String getCommentFirstDateTime() {
    return commentFirstDateTime;
  }

  public void setCommentFirstDateTime(String commentFirstDateTime) {
    this.commentFirstDateTime = commentFirstDateTime;
  }

  public String getCommentLatestDateTime() {
    return commentLatestDateTime;
  }

  public void setCommentLatestDateTime(String commentLatestDateTime) {
    this.commentLatestDateTime = commentLatestDateTime;
  }

  public List<String> getCommentsContributors() {
    return commentsContributors;
  }

  public void setCommentsContributors(List<String> commentsContributors) {
    this.commentsContributors = commentsContributors;
  }

  public String getTranscriptionFirstDateTime() {
    return transcriptionFirstDateTime;
  }

  public void setTranscriptionFirstDateTime(String transcriptionFirstDateTime) {
    this.transcriptionFirstDateTime = transcriptionFirstDateTime;
  }

  public String getTranscriptionLatestDateTime() {
    return transcriptionLatestDateTime;
  }

  public void setTranscriptionLatestDateTime(String transcriptionLatestDateTime) {
    this.transcriptionLatestDateTime = transcriptionLatestDateTime;
  }

  public List<String> getTranscriptionContributors() {
    return transcriptionContributors;
  }

  public void setTranscriptionContributors(
      List<String> transcriptionContributors) {
    this.transcriptionContributors = transcriptionContributors;
  }

  public String getTranslationFirstDateTime() {
    return translationFirstDateTime;
  }

  public void setTranslationFirstDateTime(String translationFirstDateTime) {
    this.translationFirstDateTime = translationFirstDateTime;
  }

  public String getTranslationLatestDateTime() {
    return translationLatestDateTime;
  }

  public void setTranslationLatestDateTime(String translationLatestDateTime) {
    this.translationLatestDateTime = translationLatestDateTime;
  }

  public List<String> getTranslationContributors() {
    return translationContributors;
  }

  public void setTranslationContributors(List<String> translationContributors) {
    this.translationContributors = translationContributors;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAuthority() {
    return authority;
  }

  public void setAuthority(String authority) {
    this.authority = authority;
  }

  public String getObjects() {
    return objects;
  }

  public void setObjects(String objects) {
    this.objects = objects;
  }

  public String getAllAuthorityIds() {
    return allAuthorityIds;
  }

  public void setAllAuthorityIds(String allAuthorityIds) {
    this.allAuthorityIds = allAuthorityIds;
  }

  public String getCreatorIds() {
    return creatorIds;
  }

  public void setCreatorIds(String creatorIds) {
    this.creatorIds = creatorIds;
  }

  public String getSubjectIds() {
    return subjectIds;
  }

  public void setSubjectIds(String subjectIds) {
    this.subjectIds = subjectIds;
  }

  public String getDonorIds() {
    return donorIds;
  }

  public void setDonorIds(String donorIds) {
    this.donorIds = donorIds;
  }

  public String getContributorIds() {
    return contributorIds;
  }

  public void setContributorIds(String contributorIds) {
    this.contributorIds = contributorIds;
  }

  public String getPersonalReferenceIds() {
    return personalReferenceIds;
  }

  public void setPersonalReferenceIds(String personalReferenceIds) {
    this.personalReferenceIds = personalReferenceIds;
  }

  public String getRecordCreatedDateTime() {
    return recordCreatedDateTime;
  }

  public void setRecordCreatedDateTime(String recordCreatedDateTime) {
    this.recordCreatedDateTime = recordCreatedDateTime;
  }

  public String getRecordUpdatedDateTime() {
    return recordUpdatedDateTime;
  }

  public void setRecordUpdatedDateTime(String recordUpdatedDateTime) {
    this.recordUpdatedDateTime = recordUpdatedDateTime;
  }

  public String getFirstIngestedDateTime() {
    return firstIngestedDateTime;
  }

  public void setFirstIngestedDateTime(String firstIngestedDateTime) {
    this.firstIngestedDateTime = firstIngestedDateTime;
  }

  public String getIngestedDateTime() {
    return ingestedDateTime;
  }

  public void setIngestedDateTime(String ingestedDateTime) {
    this.ingestedDateTime = ingestedDateTime;
  }

  public String getProductionDate() {
    return productionDate;
  }

  public void setProductionDate(String productionDate) {
    this.productionDate = productionDate;
  }

  public String getProductionDateString() {
    return productionDateString;
  }

  public void setProductionDateString(String productionDateString) {
    this.productionDateString = productionDateString;
  }

  public String getProductionDateQualifier() {
    return productionDateQualifier;
  }

  public void setProductionDateQualifier(String productionDateQualifier) {
    this.productionDateQualifier = productionDateQualifier;
  }

  public String getBroadcastDate() {
    return broadcastDate;
  }

  public void setBroadcastDate(String broadcastDate) {
    this.broadcastDate = broadcastDate;
  }

  public String getBroadcastDateString() {
    return broadcastDateString;
  }

  public void setBroadcastDateString(String broadcastDateString) {
    this.broadcastDateString = broadcastDateString;
  }

  public String getBroadcastDateQualifier() {
    return broadcastDateQualifier;
  }

  public void setBroadcastDateQualifier(String broadcastDateQualifier) {
    this.broadcastDateQualifier = broadcastDateQualifier;
  }

  public String getReleaseDate() {
    return releaseDate;
  }

  public void setReleaseDate(String releaseDate) {
    this.releaseDate = releaseDate;
  }

  public String getReleaseDateString() {
    return releaseDateString;
  }

  public void setReleaseDateString(String releaseDateString) {
    this.releaseDateString = releaseDateString;
  }

  public String getReleaseDateQualifier() {
    return releaseDateQualifier;
  }

  public void setReleaseDateQualifier(String releaseDateQualifier) {
    this.releaseDateQualifier = releaseDateQualifier;
  }

  public String getCoverageStartDate() {
    return coverageStartDate;
  }

  public void setCoverageStartDate(String coverageStartDate) {
    this.coverageStartDate = coverageStartDate;
  }

  public String getCoverageStartDateString() {
    return coverageStartDateString;
  }

  public void setCoverageStartDateString(String coverageStartDateString) {
    this.coverageStartDateString = coverageStartDateString;
  }

  public String getCoverageStartDateQualifier() {
    return coverageStartDateQualifier;
  }

  public void setCoverageStartDateQualifier(String coverageStartDateQualifier) {
    this.coverageStartDateQualifier = coverageStartDateQualifier;
  }

  public String getCoverageEndDate() {
    return coverageEndDate;
  }

  public void setCoverageEndDate(String coverageEndDate) {
    this.coverageEndDate = coverageEndDate;
  }

  public String getCoverageEndDateString() {
    return coverageEndDateString;
  }

  public void setCoverageEndDateString(String coverageEndDateString) {
    this.coverageEndDateString = coverageEndDateString;
  }

  public String getCoverageEndDateQualifier() {
    return coverageEndDateQualifier;
  }

  public void setCoverageEndDateQualifier(String coverageEndDateQualifier) {
    this.coverageEndDateQualifier = coverageEndDateQualifier;
  }

  public String getInclusiveStartDate() {
    return inclusiveStartDate;
  }

  public void setInclusiveStartDate(String inclusiveStartDate) {
    this.inclusiveStartDate = inclusiveStartDate;
  }

  public String getInclusiveStartDateString() {
    return inclusiveStartDateString;
  }

  public void setInclusiveStartDateString(String inclusiveStartDateString) {
    this.inclusiveStartDateString = inclusiveStartDateString;
  }

  public String getInclusiveStartDateQualifier() {
    return inclusiveStartDateQualifier;
  }

  public void setInclusiveStartDateQualifier(String inclusiveStartDateQualifier) {
    this.inclusiveStartDateQualifier = inclusiveStartDateQualifier;
  }

  public String getInclusiveEndDate() {
    return inclusiveEndDate;
  }

  public void setInclusiveEndDate(String inclusiveEndDate) {
    this.inclusiveEndDate = inclusiveEndDate;
  }

  public String getInclusiveEndDateString() {
    return inclusiveEndDateString;
  }

  public void setInclusiveEndDateString(String inclusiveEndDateString) {
    this.inclusiveEndDateString = inclusiveEndDateString;
  }

  public String getInclusiveEndDateQualifier() {
    return inclusiveEndDateQualifier;
  }

  public void setInclusiveEndDateQualifier(String inclusiveEndDateQualifier) {
    this.inclusiveEndDateQualifier = inclusiveEndDateQualifier;
  }

  public String getAuthorityStartDate() {
    return authorityStartDate;
  }

  public void setAuthorityStartDate(String authorityStartDate) {
    this.authorityStartDate = authorityStartDate;
  }

  public String getAuthorityStartDateString() {
    return authorityStartDateString;
  }

  public void setAuthorityStartDateString(String authorityStartDateString) {
    this.authorityStartDateString = authorityStartDateString;
  }

  public String getAuthorityStartDateQualifier() {
    return authorityStartDateQualifier;
  }

  public void setAuthorityStartDateQualifier(String authorityStartDateQualifier) {
    this.authorityStartDateQualifier = authorityStartDateQualifier;
  }

  public String getAuthorityEndDate() {
    return authorityEndDate;
  }

  public void setAuthorityEndDate(String authorityEndDate) {
    this.authorityEndDate = authorityEndDate;
  }

  public String getAuthorityEndDateString() {
    return authorityEndDateString;
  }

  public void setAuthorityEndDateString(String authorityEndDateString) {
    this.authorityEndDateString = authorityEndDateString;
  }

  public String getAuthorityEndDateQualifier() {
    return authorityEndDateQualifier;
  }

  public void setAuthorityEndDateQualifier(String authorityEndDateQualifier) {
    this.authorityEndDateQualifier = authorityEndDateQualifier;
  }

}
