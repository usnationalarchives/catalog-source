package gov.nara.opa.api.dataaccess.user.lists;

import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;
import gov.nara.opa.api.dataaccess.impl.user.lists.UserListItemRowMapper;
import gov.nara.opa.api.dataaccess.impl.user.lists.UserListRowMapper;
import gov.nara.opa.api.user.lists.UserList;
import gov.nara.opa.api.user.lists.UserListItem;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class UserListJDBCTemplate extends AbstractOpaDbJDBCTemplate implements
		UserListDao {

	private final int CHUNK_SIZE = 2000;

	private static OpaLogger log = OpaLogger
			.getLogger(UserListJDBCTemplate.class);

	/**
	 * Insert a new list
	 * 
	 * @param userList
	 *            The list that we attempt to insert
	 * @return true if the list was inserted, otherwise false
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 */
	@Override
	public boolean create(UserList userList) throws DataAccessException,
			UnsupportedEncodingException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("listName", userList.getListName());
		inParamMap.put("accountId", userList.getAccountId());
		return StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spInsertAccountList", inParamMap);
	}

	/**
	 * Get a collection of lists filtering by listId.
	 * 
	 * @param listId
	 *            The account Id that will be used to filter
	 * @return Collection of lists filtered by listId
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<UserList> select(int listId) throws DataAccessException,
			UnsupportedEncodingException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("listId", listId);
		return (List<UserList>) StoredProcedureDataAccessUtils.execute(
				getJdbcTemplate(), "spSelectAccountListByListId",
				new UserListRowMapper(), inParamMap);
	}

	/**
	 * Get a collection of lists filtering by listName.
	 * 
	 * @param listName
	 *            The listName that will be used to filter
	 * @return Collection of lists with the specified name
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<UserList> select(String listName) throws DataAccessException,
			UnsupportedEncodingException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("listName", listName);
		return (List<UserList>) StoredProcedureDataAccessUtils.execute(
				getJdbcTemplate(), "spSelectAccountListByListName",
				new UserListRowMapper(), inParamMap);
	}

	/**
	 * Get a collection of lists filtering by listName and accountId.
	 * 
	 * @param listName
	 *            The listName that will be used to filter
	 * @param accountId
	 *            The account id that will be used to filter
	 * @return Collection of lists with the specified name
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<UserList> select(String listName, int accountId)
			throws DataAccessException, UnsupportedEncodingException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("listName", listName);
		inParamMap.put("accountId", accountId);
		return (List<UserList>) StoredProcedureDataAccessUtils.execute(
				getJdbcTemplate(), "spSelectAccountListByListNameAndAccountId",
				new UserListRowMapper(), inParamMap);
	}

	/**
	 * Get a collection of lists filtering by accountId.
	 * 
	 * @param accountId
	 *            The account id that will be used to filter
	 * @return Collection of lists with the specified name
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<UserList> selectByAccountId(int accountId, int offset, int rows)
			throws DataAccessException, UnsupportedEncodingException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", accountId);
		inParamMap.put("limOffset", offset);
		inParamMap.put("limRows", rows);
		return (List<UserList>) StoredProcedureDataAccessUtils.execute(
				getJdbcTemplate(),
				"spSelectAccountListByAccountIdWithLimitAndOffset",
				new UserListRowMapper(), inParamMap);
	}

	/**
	 * Get a collection of lists filtering by accountId.
	 * 
	 * @param accountId
	 *            The account id that will be used to filter
	 * @param offset
	 *            The offset specifies the offset of the first row to return
	 * @param rows
	 *            The count specifies maximum number of rows to return
	 * @return Collection of lists with the specified name
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<UserList> selectByAccountId(int accountId)
			throws DataAccessException, UnsupportedEncodingException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", accountId);
		return (List<UserList>) StoredProcedureDataAccessUtils.execute(
				getJdbcTemplate(), "spSelectAccountListByAccountId",
				new UserListRowMapper(), inParamMap);
	}

	/**
	 * Get a collection of lists filtering by accountId.
	 * 
	 * @param accountId
	 *            The account id that will be used to filter
	 * @return Collection of lists with the specified name
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<UserListItem> selectListItem(int listId, String opaId)
			throws DataAccessException, UnsupportedEncodingException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("opaId", opaId);
		inParamMap.put("listId", listId);
		return (List<UserListItem>) StoredProcedureDataAccessUtils.execute(
				getJdbcTemplate(), "spSelectAccountListItemsByOpaIdAndListId",
				new UserListItemRowMapper(), inParamMap);
	}

	/**
	 * Get a collection of lists items filtering by listId.
	 * 
	 * @param listId
	 *            The list id that will be used to filter
	 * @return Collection of items of the specified list
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<UserListItem> selectListItems(int listId) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("listId", listId);
		return (List<UserListItem>) StoredProcedureDataAccessUtils.execute(
				getJdbcTemplate(), "spSelectAccountListItemsByListId",
				new UserListItemRowMapper(), inParamMap);
	}

	/**
	 * Get a collection of lists items filtering by listId.
	 * 
	 * @param listId
	 *            The list id that will be used to filter
	 * @param offset
	 *            The offset specifies the offset of the first row to return
	 * @param rows
	 *            The count specifies maximum number of rows to return
	 * @return Collection of items of the specified list
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<UserListItem> selectListItems(int listId, int offset, int rows) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("listId", listId);
		inParamMap.put("limOffset", offset);
		inParamMap.put("limRows", rows);
		return (List<UserListItem>) StoredProcedureDataAccessUtils.execute(
				getJdbcTemplate(),
				"spSelectAccountListItemsWithLimitAndOffset",
				new UserListItemRowMapper(), inParamMap);
	}

	/**
	 * Update and existing list
	 * 
	 * @param userList
	 *            The list with the data that we want to update.
	 * @return true if the list was updated, otherwise false
	 * @throws DataAccessException
	 * @throws UnsupportedEncodingException
	 */
	@Override
	public boolean update(UserList userList)
			throws UnsupportedEncodingException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("listName", userList.getListName());
		inParamMap.put("listId", userList.getListId());
		return StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spUpdateAccountList", inParamMap);
	}

	/**
	 * Remove a opaId item from a specified list
	 * 
	 * @param opaId
	 *            The opaId that we need to delete
	 * @param listId
	 *            The list that we need to modify *
	 * @return True if the list was correctly updated, false otherwise.
	 */
	@Override
	public boolean removeFromList(UserList userList, String opaId)
			throws UnsupportedEncodingException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("listId", userList.getListId());
		inParamMap.put("opaId", opaId);
		return StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spDeleteAccountListItems", inParamMap);
	}

	/**
	 * Remove a opaId item from a specified list
	 * 
	 * @param opaId
	 *            The opaId that we need to delete
	 * @param listId
	 *            The list that we need to modify *
	 * @return True if the list was correctly updated, false otherwise.
	 */
	@Override
	public boolean removeAllFromList(UserList userList)
			throws UnsupportedEncodingException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("listId", userList.getListId());
		return StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spDeleteAllAccountListItemsFromList", inParamMap);
	}

	@Override
	public boolean delete(UserList userList) throws DataAccessException,
			UnsupportedEncodingException {
		removeAllFromList(userList);
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("listId", userList.getListId());
		return StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spDeleteAccountList", inParamMap);
	}

	@Override
	public int getUserListCount(int accountId) throws DataAccessException,
			UnsupportedEncodingException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", accountId);
		return StoredProcedureDataAccessUtils
				.executeWithIntResult(getJdbcTemplate(),
						"spGetAccountListCount", inParamMap, "count");
	}

	@Override
	public int getUserListItemCount(int listId) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("listId", listId);
		return StoredProcedureDataAccessUtils.executeWithIntResult(
				getJdbcTemplate(), "spGetAccountListItemsCount", inParamMap,
				"count");
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> checkListForOpaIds(int listId, String[] opaIds)
			throws DataAccessException, UnsupportedEncodingException {

		log.debug("checkListForOpaIds");
		List<String> opaIdsToAddToList = new ArrayList<String>();

		int times = opaIds.length / CHUNK_SIZE;
		if (times == 0) {
			times = 1;
		}

		for (int index = 0; index < times; ++index) {
			StringBuilder sql = new StringBuilder();
			int initValue = index * CHUNK_SIZE;
			int endValue = initValue + CHUNK_SIZE;
			for (int i = initValue; i < opaIds.length && i < endValue; i++) {
				opaIdsToAddToList.add(opaIds[i]);
				sql.append("'" + opaIds[i] + "'");
				if (i + 1 < opaIds.length && i + 1 < endValue) {
					sql.append(",");
				}
			}

			log.debug(String
					.format("checkListForOpaIds times: %1$d", index + 1));

			Map<String, Object> inParamMap = new HashMap<String, Object>();
			inParamMap.put("listId", listId);
			inParamMap.put("opaIds", sql.toString());
			List<UserListItem> temp = (List<UserListItem>) StoredProcedureDataAccessUtils
					.execute(getJdbcTemplate(),
							"spSelectAccountListItemsInBatch",
							new UserListItemRowMapper(), inParamMap);
			if (temp.size() > 0) {
				for (int i = 0; i < temp.size(); ++i) {
					opaIdsToAddToList.remove(temp.get(i).getOpaId());
				}
			}
		}

		log.debug("finish : checkListForOpaIds");
		return opaIdsToAddToList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserListItem> checkListForDuplicateOpaIds(int listId,
			String[] opaIds) throws DataAccessException,
			UnsupportedEncodingException {
		List<UserListItem> items = new ArrayList<UserListItem>();
		int times = opaIds.length / CHUNK_SIZE;
		if (times == 0) {
			times = 1;
		}

		for (int index = 0; index < times; ++index) {
			StringBuilder sql = new StringBuilder();
			int initValue = index * CHUNK_SIZE;
			int endValue = initValue + CHUNK_SIZE;
			for (int i = initValue; i < opaIds.length && i < endValue; i++) {
				sql.append("'" + opaIds[i] + "'");
				if (i + 1 < opaIds.length && i + 1 < endValue) {
					sql.append(",");
				}
			}

			log.debug(String
					.format("checkListForDuplicateOpaIds times: %1$d", index + 1));

			Map<String, Object> inParamMap = new HashMap<String, Object>();
			inParamMap.put("listId", listId);
			inParamMap.put("opaIds", sql.toString());
			List<UserListItem> temp = (List<UserListItem>) StoredProcedureDataAccessUtils
					.execute(getJdbcTemplate(),
							"spSelectAccountListItemsInBatch",
							new UserListItemRowMapper(), inParamMap);
			if (temp != null && temp.size() > 0) {
				items.addAll(temp);
			}
		}

		log.debug("finish : checkListForDuplicateOpaIds");
		return items;
	}

	@Override
	public int batchAddToUserList(int listId, List<String> opaIdsToAddToList) {

		log.debug("batchAddToUserList");

		int times = opaIdsToAddToList.size() / CHUNK_SIZE;
		if (times == 0) {
			times = 1;
		}

		int totalIdsAddedToList = 0;
		for (int index = 0; index < times; ++index) {
			StringBuilder sql = new StringBuilder();
			int initValue = index * CHUNK_SIZE;
			int endValue = initValue + CHUNK_SIZE;
			for (int i = initValue; i < opaIdsToAddToList.size()
					&& i < endValue; i++) {
				sql.append("(" + listId + ",'" + opaIdsToAddToList.get(i)
						+ "',now())");
				if (i + 1 < opaIdsToAddToList.size() && i + 1 < endValue) {
					sql.append(",");
				}
			}

			log.debug(String
					.format("batchAddToUserList times: %1$d", index + 1));

			Map<String, Object> inParamMap = new HashMap<String, Object>();
			inParamMap.put("insertValues", sql.toString());
			totalIdsAddedToList += StoredProcedureDataAccessUtils
					.executeWithNumberOfChanges(getJdbcTemplate(),
							"spInsertInBatchToAccountList", inParamMap);

		}

		return totalIdsAddedToList;
	}
}
