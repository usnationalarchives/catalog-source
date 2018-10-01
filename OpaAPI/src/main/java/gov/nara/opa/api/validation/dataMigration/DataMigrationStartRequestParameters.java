package gov.nara.opa.api.validation.dataMigration;

import java.util.LinkedHashMap;

import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaPattern;

public class DataMigrationStartRequestParameters extends
    AbstractRequestParameters {
  
  @OpaNotNullAndNotEmpty
  @OpaPattern(regexp = "(^standard$)|(^power$)", message = ErrorConstants.INVALID_USER_TYPE)
  private String migrationType;
  
  @OpaNotNullAndNotEmpty
  private String localDataEntity;

  public String getMigrationType() {
    return migrationType;
  }

  public void setMigrationType(String migrationType) {
    this.migrationType = migrationType;
  }

  public String getLocalDataEntity() {
    return localDataEntity;
  }

  public void setLocalDataEntity(String localDataEntity) {
    this.localDataEntity = localDataEntity;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    // TODO Auto-generated method stub
    return null;
  }

}
