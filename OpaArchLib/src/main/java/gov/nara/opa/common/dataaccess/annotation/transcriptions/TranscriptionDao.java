package gov.nara.opa.common.dataaccess.annotation.transcriptions;

import gov.nara.opa.common.valueobject.annotation.transcriptions.TranscriptionValueObject;

import java.util.List;

public interface TranscriptionDao {

  List<TranscriptionValueObject> selectTranscriptionsByNaid(String naId);

}
