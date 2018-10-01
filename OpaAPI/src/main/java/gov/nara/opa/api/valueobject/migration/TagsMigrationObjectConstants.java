package gov.nara.opa.api.valueobject.migration;

import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;

public interface TagsMigrationObjectConstants extends
    CommonValueObjectConstants {
  public static final String TAGS_READ = "@tagsRead";
  public static final String TAGS_WRITTEN = "@tagsWritten";
  public static final String TOTAL_ORPHANED_TAGS = "@totalOrphanedTags";
  public static final String TOTAL_MIGRATED_TAGS = "@totalMigratedTags";
  public static final String TOTAL_DUPLICATE_TAGS = "@totalDuplicateTags";
  public static final String ORPHANED_TAGS = "orphanedTags";
  public static final String DUPLICATE_TAGS = "duplicateTags";
  
}
