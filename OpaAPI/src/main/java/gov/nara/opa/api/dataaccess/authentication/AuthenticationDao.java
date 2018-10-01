package gov.nara.opa.api.dataaccess.authentication;

import gov.nara.opa.api.user.accounts.UserAccount;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.springframework.dao.DataAccessException;

public interface AuthenticationDao {

  /**
   * @param userName
   *          login username
   * @param pwd
   *          login password
   * @return List containing UserAccount object
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  List<UserAccount> select(String userName) throws DataAccessException,
      UnsupportedEncodingException;
}
