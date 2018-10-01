package gov.nara.opa.api.dataaccess.authentication;

import gov.nara.opa.api.dataaccess.impl.authentication.AuthenticationRowMapper;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class AuthenticationJDBCTemplate extends AbstractOpaDbJDBCTemplate
		implements AuthenticationDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<UserAccount> select(String userName)
			throws DataAccessException, UnsupportedEncodingException {
		userName.getBytes("UTF-8");
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("username", userName);
		inParamMap.put("accountStatus", 1);
		return (List<UserAccount>) StoredProcedureDataAccessUtils.execute(
				getJdbcTemplate(), "spSelectUserAccount",
				new AuthenticationRowMapper(), inParamMap);
	}
}
