package gov.nara.opa.api.valueobject.migration;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TagsMigrationValueObject extends AbstractWebEntityValueObject
    implements TagsMigrationObjectConstants {

  private Integer tagsRead;
  private Integer tagsWritten;
  private Integer totalOrphanedTags;
  private Integer totalMigratedTags;
  private Integer totalDuplicateTags;
  private List<String> orphanedTags;
  private List<Map<String, Object>> duplicateTags;
  private Boolean fullDetail = false;
  
  public Boolean getFullDetail() {
    return fullDetail;
  }

  public void setFullDetail(Boolean fullDetail) {
    this.fullDetail = fullDetail;
  }
  
  public Integer getTotalOrphanedTags() {
    return totalOrphanedTags;
  }

  public void setTotalOrphanedTags(Integer totalOrphanedTags) {
    this.totalOrphanedTags = totalOrphanedTags;
  }

  public Integer getTotalMigratedTags() {
    return totalMigratedTags;
  }

  public void setTotalMigratedTags(Integer totalMigratedTags) {
    this.totalMigratedTags = totalMigratedTags;
  }

  public List<String> getOrphanedTags() {
    return orphanedTags;
  }

  public void setOrphanedTags(List<String> orphanedTags) {
    this.orphanedTags = orphanedTags;
  }

  public Integer getTagsRead() {
    return tagsRead;
  }

  public void setTagsRead(Integer tagsRead) {
    this.tagsRead = tagsRead;
  }

  public Integer getTagsWritten() {
    return tagsWritten;
  }

  public void setTagsWritten(Integer tagsWritten) {
    this.tagsWritten = tagsWritten;
  }

  public Integer getTotalDuplicateTags() {
    return totalDuplicateTags;
  }

  public void setTotalDuplicateTags(Integer totalDuplicateTags) {
    this.totalDuplicateTags = totalDuplicateTags;
  }

  public List<Map<String, Object>> getDuplicateTags() {
    return duplicateTags;
  }

  public void setDuplicateTags(List<Map<String, Object>> duplicateTags) {
    this.duplicateTags = duplicateTags;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
    
    result.put("@action", action);
    result.put(TAGS_READ, getTagsRead());
    result.put(TAGS_WRITTEN, getTagsWritten());
    result.put(TOTAL_ORPHANED_TAGS, getTotalOrphanedTags());
    result.put(TOTAL_MIGRATED_TAGS, getTotalMigratedTags());
    result.put(TOTAL_DUPLICATE_TAGS, getTotalDuplicateTags());
    if(fullDetail) {
      result.put(ORPHANED_TAGS, getOrphanedTags());
      result.put(DUPLICATE_TAGS, getDuplicateTags());
    }
    
    return result;
  }

}
