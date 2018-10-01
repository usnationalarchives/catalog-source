package gov.nara.opa.api.services.impl.moderator;

import gov.nara.opa.api.dataaccess.annotation.logs.AnnotationLogDao;
import gov.nara.opa.api.dataaccess.annotation.transcriptions.TranscriptionDao;
import gov.nara.opa.api.dataaccess.moderator.ModeratorStreamDao;
import gov.nara.opa.api.moderator.contributionsStream.ContributionsStreamErrorCode;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.moderator.ViewModeratorStreamService;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ViewModeratorStreamServiceImpl implements
		ViewModeratorStreamService {

	private static OpaLogger logger = OpaLogger
			.getLogger(ViewModeratorStreamServiceImpl.class);

	@Autowired
	private TranscriptionDao transcriptionDao;

	@Autowired
	private AnnotationLogDao annotationLogDao;

	@Autowired
	private ModeratorStreamDao moderatorStreamDao;

	@Autowired
	private ConfigurationService configService;

	@Override
	public ServiceResponseObject viewModeratorStream(int offset, int rows,
			String filterType, String naId) {
		ContributionsStreamErrorCode errorCode = ContributionsStreamErrorCode.NONE;
		HashMap<String, Object> resultHashMap = new HashMap<String, Object>();
		int displayTime;

		try {
			List<Map<String, Object>> stream = null;

			switch (filterType) {
			case "TG":
				logger.debug("getting tag stream");
				displayTime = configService.getConfig().getTagsDisplayTime();

				stream = moderatorStreamDao.getTagStream(offset, rows, naId,
						displayTime, true);
				break;
			case "TR":
				logger.debug("getting transcription stream");
				displayTime = configService.getConfig()
						.getTranscriptionsDisplayTime();

				stream = moderatorStreamDao.getTranscriptionStream(offset,
						rows, naId, displayTime, true);
				break;
			case "CM":
				logger.debug("getting comments stream");
				displayTime = configService.getConfig()
						.getCommentsDisplayTime();

				stream = moderatorStreamDao.getCommentsStream(offset, rows,
						naId, displayTime);
				break;
			case "Moderator":
				logger.debug("getting moderator stream");
				int tagDisplayTime = configService.getConfig()
						.getTagsDisplayTime();
				int transcriptionDisplayTime = configService.getConfig()
						.getTranscriptionsDisplayTime();
				int commentDisplayTime = configService.getConfig()
						.getCommentsDisplayTime();

				stream = moderatorStreamDao.getModeratorStream(offset, rows,
						naId, tagDisplayTime, transcriptionDisplayTime,
						commentDisplayTime, true);
				break;
			default:
				logger.debug("unknown action");
				break;
			}

			if (stream != null) {
				logger.debug(String.format("Size of stream: %1$d",
						stream.size()));

				resultHashMap.put("Stream", stream);
			} else {
				logger.debug("Stream is empty");
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			errorCode = ContributionsStreamErrorCode.INTERNAL_ERROR;
			errorCode.setErrorMessage(e.getMessage());
		}

		return new ServiceResponseObject(errorCode, resultHashMap);
	}

	@Override
	public ServiceResponseObject viewContributionTotals(String naId) {
		ContributionsStreamErrorCode errorCode = ContributionsStreamErrorCode.NONE;
		HashMap<String, Object> resultHashMap = new HashMap<String, Object>();
		int tagDisplayTime = configService.getConfig().getTagsDisplayTime();
		int transcriptionDisplayTime = configService.getConfig()
				.getTranscriptionsDisplayTime();
		int commentDisplayTime = configService.getConfig()
				.getCommentsDisplayTime();

		try {

			// Get transcription total
			int transcriptionTotal = moderatorStreamDao.getTranscriptionTotals(
					naId, transcriptionDisplayTime, true);
			resultHashMap.put("TranscriptionTotal", transcriptionTotal);

			// Get tag total
			int tagTotal = moderatorStreamDao.getTagTotals(naId,
					tagDisplayTime, true);
			resultHashMap.put("TagTotal", tagTotal);

			// Get comment total
			int commentTotal = moderatorStreamDao.getCommentTotals(naId,
					commentDisplayTime);
			resultHashMap.put("CommentTotal", commentTotal);

			// Get moderator total
			int modTotal = moderatorStreamDao.getModeratorTotals(naId,
					tagDisplayTime, transcriptionDisplayTime,
					commentDisplayTime, true);
			resultHashMap.put("ModeratorTotal", modTotal);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			errorCode = ContributionsStreamErrorCode.INTERNAL_ERROR;
			errorCode.setErrorMessage(e.getMessage());
		}

		return new ServiceResponseObject(errorCode, resultHashMap);
	}

}
