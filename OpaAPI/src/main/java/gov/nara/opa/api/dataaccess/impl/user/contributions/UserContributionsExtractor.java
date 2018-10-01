package gov.nara.opa.api.dataaccess.impl.user.contributions;

import gov.nara.opa.api.valueobject.user.contributions.UserContributionValueObject;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class UserContributionsExtractor implements
		ResultSetExtractor<UserContributionValueObject> {

	@Override
	public UserContributionValueObject extractData(ResultSet resultSet)
			throws SQLException, DataAccessException {

		UserContributionValueObject userContributions = new UserContributionValueObject();

		userContributions
				.setTotalTags(columnExists(resultSet, "totaltags") ? resultSet
						.getInt("totaltags") : 0);
		userContributions.setTotalTagsMonth(columnExists(resultSet,
				"totaltagsmonth") ? resultSet.getInt("totaltagsmonth") : 0);
		userContributions.setTotalTagsYear(columnExists(resultSet,
				"totaltagsyear") ? resultSet.getInt("totaltagsyear") : 0);

		userContributions.setTotalTranscriptions(columnExists(resultSet,
				"totaltranscriptions") ? resultSet
				.getInt("totaltranscriptions") : 0);
		userContributions.setTotalTranscriptionsMonth(columnExists(resultSet,
				"totaltranscriptionsmonth") ? resultSet
				.getInt("totaltranscriptionsmonth") : 0);
		userContributions.setTotalTranscriptionsYear(columnExists(resultSet,
				"totaltranscriptionsyear") ? resultSet
				.getInt("totaltranscriptionsyear") : 0);

		userContributions.setTotalComments(columnExists(resultSet,
				"totalcomments") ? resultSet.getInt("totalcomments") : 0);
		userContributions.setTotalCommentsMonth(columnExists(resultSet,
				"totalcommentsmonth") ? resultSet.getInt("totalcommentsmonth")
				: 0);
		userContributions.setTotalCommentsYear(columnExists(resultSet,
				"totalcommentsyear") ? resultSet.getInt("totalcommentsyear")
				: 0);

		userContributions.setTotalContributions(columnExists(resultSet,
				"totalcontributions") ? resultSet.getInt("totalcontributions")
				: 0);
		userContributions.setTotalContributionsMonth(columnExists(resultSet,
				"totalcontributionsmonth") ? resultSet
				.getInt("totalcontributionsmonth") : 0);
		userContributions.setTotalContributionsYear(columnExists(resultSet,
				"totalcontributionsyear") ? resultSet
				.getInt("totalcontributionsyear") : 0);

		userContributions
				.setTotalTags(columnExists(resultSet, "totaltags") ? resultSet
						.getInt("totaltags") : 0);
		userContributions.setTotalTranscriptions(columnExists(resultSet,
				"totaltranscriptions") ? resultSet
				.getInt("totaltranscriptions") : 0);
		userContributions.setTotalComments(columnExists(resultSet,
				"totalcomments") ? resultSet.getInt("totalcomments") : 0);
		userContributions.setTotalContributions(columnExists(resultSet,
				"totalcontributions") ? resultSet.getInt("totalcontributions")
				: 0);

		return userContributions;
	}

	private boolean columnExists(ResultSet rs, String column) {
		try {
			rs.findColumn(column);
			return true;
		} catch (SQLException sqlex) {
		}
		return false;
	}
}
