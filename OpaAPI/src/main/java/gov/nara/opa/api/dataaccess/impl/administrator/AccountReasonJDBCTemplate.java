package gov.nara.opa.api.dataaccess.impl.administrator;

import gov.nara.opa.api.dataaccess.administrator.AccountReasonDao;
import gov.nara.opa.api.valueobject.administrator.AccountReasonValueObject;
import gov.nara.opa.api.valueobject.administrator.AccountReasonValueObjectConstants;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class AccountReasonJDBCTemplate extends AbstractOpaDbJDBCTemplate
    implements AccountReasonDao, AccountReasonValueObjectConstants {

  @Override
  public void create(AccountReasonValueObject accountReason) {
	  Map<String, Object> inParamMap = new HashMap<String, Object>();
	  inParamMap.put("accountId", accountReason.getAccountId());
	  inParamMap.put("reasonDesc", accountReason.getReason());
	  inParamMap.put("reasonStatus", accountReason.getReasonStatus());
	  inParamMap.put("reasonAddedTs", accountReason.getReasonAddedTs());
	  int reasonId = StoredProcedureDataAccessUtils
				.executeWithIntResult(getJdbcTemplate(),
						"spCreateAccountReason", inParamMap, "reasonId");
	  accountReason.setReasonId(new Integer(reasonId));
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<AccountReasonValueObject> getAcountReasons(Integer accountId,
      String reason, Boolean reasonStatus) {
	  
	  Map<String, Object> inParamMap = new HashMap<String, Object>();
	  inParamMap.put("accountId", accountId);
	  inParamMap.put("reasonDesc", reason);
	  inParamMap.put("reasonStatus", reasonStatus);

	  return (List<AccountReasonValueObject>) StoredProcedureDataAccessUtils
			.execute(getJdbcTemplate(), "spSelectAccountReasons",
					new AccountReasonRowMapper(), inParamMap);
  }

  @SuppressWarnings("unchecked")
  @Override
  public AccountReasonValueObject getAcountReason(Integer reasonId) {
	  Map<String, Object> inParamMap = new HashMap<String, Object>();
	  inParamMap.put("reasonId", reasonId);
	  List<AccountReasonValueObject> accountReasons = (List<AccountReasonValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spSelectAccountReasonById",
						new AccountReasonRowMapper(), inParamMap);
	  if (accountReasons == null || accountReasons.size() == 0) {
		  return null;
	  } else if (accountReasons.size() > 1) {
		  throw new OpaRuntimeException(
				  "Too many account reasons found for reasonId: " + reasonId);
	  }
	  return accountReasons.get(0);
  }
}
