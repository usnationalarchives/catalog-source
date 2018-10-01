package gov.nara.opa.api.dataaccess.impl.user.contributions;

import gov.nara.opa.api.user.contributions.UserContributedTags;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class UserContributedTagsExtractor implements
    ResultSetExtractor<UserContributedTags> {

  @Override
  public UserContributedTags extractData(ResultSet resultSet)
      throws SQLException, DataAccessException {

    UserContributedTags userContributedTags = new UserContributedTags();

    userContributedTags.setAccount_id(resultSet.getInt("account_id"));
    userContributedTags.setFrequency(resultSet.getInt("frequency"));
    userContributedTags.setText(resultSet.getString("annotation"));
    userContributedTags.setStatus(resultSet.getInt("status"));

    return userContributedTags;
  }

}
