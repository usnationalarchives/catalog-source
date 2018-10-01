package gov.nara.opa.api.services.export;

import gov.nara.opa.common.valueobject.export.AccountExportValueObject;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface NonBulkExportService {

  void executeExport(AccountExportValueObject accountExport,
      HttpServletResponse response, HttpServletRequest request)
      throws IOException, TimeoutException;

}