package gov.nara.opa.api.dataaccess.moderator;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

public interface ModeratorStreamDao {
	/**
	 * Gets a segment of tags and annotation logs sorted by timestamp and
	 * filtered by naId if any
	 * 
	 * @param offset
	 * @param rows
	 * @param naId
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	public List<Map<String, Object>> getTagStream(int offset, int rows,
			String naId, int displayTime) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException;

	public List<Map<String, Object>> getTagStream(int offset, int rows,
			String naId, int displayTime, boolean useSp)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException;

	/**
	 * @return The totals for tags for the past 6 monts as needed by
	 *         transcription moderator stream
	 */
	public int getTagTotals(int displayTime);

	/**
	 * @param naId
	 *            The naId for the tag stream filter
	 * @return The filtered totals for tags for the past 6 monts as needed by
	 *         transcription moderator stream
	 */
	public int getTagTotals(String naId, int displayTime);

	/**
	 * @param naId
	 *            The naId for the tag stream filter
	 * @param displayTime
	 * @param useSp
	 * @return The filtered totals for tags for the past 6 monts as needed by
	 *         transcription moderator stream
	 */
	public int getTagTotals(String naId, int displayTime, boolean useSp);

	/**
	 * Gets a segment of tags and annotation logs sorted by timestamp and
	 * filtered by naId if any
	 * 
	 * @param offset
	 * @param rows
	 * @param naId
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	public List<Map<String, Object>> getTranscriptionStream(int offset,
			int rows, String naId, int displayTime) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException;

	/**
	 * Gets a segment of tags and annotation logs sorted by timestamp and
	 * filtered by naId if any
	 * 
	 * @param offset
	 * @param rows
	 * @param naId
	 * @param displayTime
	 * @param useSp
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	public List<Map<String, Object>> getTranscriptionStream(int offset,
			int rows, String naId, int displayTime, boolean useSp)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException;

	/**
	 * Gets a segment of comments and annotation logs sorted by timestamp and
	 * filtered by naId if any
	 * 
	 * @param offset
	 * @param rows
	 * @param naId
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	public List<Map<String, Object>> getCommentsStream(int offset, int rows,
			String naId, int displayTime) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException;

	/**
	 * @return The totals for transcriptions for the past 6 monts as needed by
	 *         transcription moderator stream
	 */
	public int getTranscriptionTotals(int displayTime);

	/**
	 * @return The totals for transcriptions for the past 6 monts as needed by
	 *         transcription moderator stream
	 */
	public int getTranscriptionTotals(String naId, int displayTime);

	/**
	 * @param naId
	 * @param displayTime
	 * @param useSp
	 * @return The totals for transcriptions for the past 6 monts as needed by
	 *         transcription moderator stream
	 */
	public int getTranscriptionTotals(String naId, int displayTime,
			boolean useSp);

	/**
	 * Gets a segment of tags and annotation logs sorted by timestamp and
	 * filtered by naId if any
	 * 
	 * @param offset
	 * @param rows
	 * @param naId
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	public List<Map<String, Object>> getModeratorStream(int offset, int rows,
			String naId, int tagDisplayTime, int transcriptionDisplayTime,
			int commentDisplayTime) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException;

	/**
	 * Gets a segment of tags and annotation logs sorted by timestamp and
	 * filtered by naId if any
	 * 
	 * @param offset
	 * @param rows
	 * @param naId
	 * @param tagDisplayTime
	 * @param transcriptionDisplayTime
	 * @param useSp
	 * @return
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 * @throws BadSqlGrammarException
	 */
	public List<Map<String, Object>> getModeratorStream(int offset, int rows,
			String naId, int tagDisplayTime, int transcriptionDisplayTime,
			int commentDisplayTime, boolean useSp) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException;

	/**
	 * @return The totals for transcriptions for the past 6 monts as needed by
	 *         transcription moderator stream
	 */
	public int getModeratorTotals(int tagDisplayTime,
			int transcriptionDisplayTime, int commentDisplayTime);

	/**
	 * @return The totals for transcriptions for the past 6 monts as needed by
	 *         transcription moderator stream
	 */
	public int getModeratorTotals(String naId, int tagDisplayTime,
			int transcriptionDisplayTime, int commentDisplayTime);

	/**
	 * @return The totals for transcriptions for the past 6 monts as needed by
	 *         transcription moderator stream
	 */
	public int getModeratorTotals(String naId, int tagDisplayTime,
			int transcriptionDisplayTime, int commentDisplayTime, boolean useSp);

	/**
	 * @return The totals for comments for the past 6 months as needed by
	 *         transcription moderator stream
	 */
	public int getCommentTotals(int displayTime);

	/**
	 * @param naId
	 *            The naId for the comment stream filter
	 * @return The filtered totals for comments for the past 6 months as needed
	 *         by transcription moderator stream
	 */
	public int getCommentTotals(String naId, int displayTime);
}
