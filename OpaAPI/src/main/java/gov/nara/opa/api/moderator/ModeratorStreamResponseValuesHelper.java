package gov.nara.opa.api.moderator;

import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.system.logging.APILogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.stereotype.Component;

@Component
public class ModeratorStreamResponseValuesHelper {
	private static APILogger log = APILogger
			.getLogger(ModeratorStreamResponseValuesHelper.class);

	private List<HashMap<String, Object>> stream;
	private int offset;
	private int rows;

	public ModeratorStreamResponseValuesHelper() {

	}

	/**
	 * Sets all internal fields to null. This is necessary because Spring will
	 * instantiate this as a singleton class.
	 */
	@SuppressWarnings("unchecked")
	public void Init(ServiceResponseObject responseObject, int offset, int rows) {
		HashMap<String, Object> contentMap = null;
		contentMap = (responseObject != null ? responseObject.getContentMap()
				: null);

		stream = null;
		if (contentMap != null) {

			if (contentMap.containsKey("Stream")) {
				stream = (List<HashMap<String, Object>>) contentMap
						.get("Stream");
			}

			this.offset = offset;
			this.rows = rows;
		}
	}

	/**
	 * Builds a LinkedHashMap that will be passed to the APIResponse instance to
	 * process the output.
	 * 
	 * @return A LinkedHashMap instance with the requested output from the
	 *         objects that were set.
	 */
	public LinkedHashMap<String, Object> getResponseValues() {
		return getResponseValues(false);
	}

	/**
	 * Builds a LinkedHashMap that will be passed to the APIResponse instance to
	 * process the output.
	 * 
	 * @return A LinkedHashMap instance with the requested output from the
	 *         objects that were set.
	 */
	@SuppressWarnings("unchecked")
	public LinkedHashMap<String, Object> getResponseValues(
			boolean isModeratorAction) {
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

		if (stream == null) {
			result.put("results", "stream is empty");
		} else {

			// Transaction dictionary
			LinkedHashMap<String, LinkedHashMap<String, Object>> transactions = new LinkedHashMap<String, LinkedHashMap<String, Object>>();

			for (HashMap<String, Object> entry : stream) {
				LinkedHashMap<String, Object> contributionMap = new LinkedHashMap<String, Object>();

				Integer annotationId = (Integer) entry.get("annotationId");
				String annotationType = (String) entry.get("annotationType");
				String key = getContributionKey(entry, annotationId,
						annotationType);
				String action = entry.get("action").toString();

				contributionMap.put("@transId", annotationId);
				contributionMap.put("@type", getContributionType((String) entry
						.get("annotationType")));
				contributionMap.put("@on", entry.get("on"));
				contributionMap.put("@when", TimestampUtils
						.getUtcString((Timestamp) entry.get("createTS")));
				contributionMap.put("@logTS", TimestampUtils
						.getUtcString((Timestamp) entry.get("TS")));
				contributionMap.put("@userId", entry.get("userName"));
				contributionMap.put("@fullName", entry.get("fullName"));
				contributionMap.put("@displayFullName", ((boolean) entry
						.get("displayFullName") ? "true" : "false"));
				contributionMap
						.put("@isNaraStaff", ((boolean) entry
								.get("isNaraStaff") ? "true" : "false"));
				contributionMap.put("@authorUserId",
						entry.get("authorUserName"));
				contributionMap.put("@authorFullName",
						entry.get("authorFullName"));
				contributionMap.put("@authorDisplayFullName", ((boolean) entry
						.get("authorDisplayFullName") ? "true" : "false"));
				contributionMap.put("@authorIsNaraStaff", ((boolean) entry
						.get("authorIsNaraStaff") ? "true" : "false"));
				contributionMap.put("@hasNote",
						((boolean) entry.get("hasNote") ? "true" : "false"));
				contributionMap.put("@notes", entry.get("notes"));
				contributionMap.put("@action", entry.get("action"));
				contributionMap.put("@reason", entry.get("reason"));
				contributionMap.put("@createTS", TimestampUtils
						.getUtcString((Timestamp) entry.get("createTS")));

				// Get particular item
				setContributionContent(contributionMap, annotationType, entry);

				String title = entry.get("title") != null ? entry.get("title")
						.toString() : "";

				title = StringUtils.removeMarkUps(title);

				contributionMap.put("title", title);
				contributionMap.put("accessPath", "TBD");

				// Get object/description
				setReference(contributionMap, annotationType, entry);

				// Add to history it's a remove/restore
				if (transactions.containsKey(key)) {
					log.debug("getResponseValues", "adding to history");
					log.debug("getResponseValues",
							transactions.get(key).get("history").toString());

					if (action.equals("REMOVE") || action.equals("RESTORE")) {
						((ArrayList<LinkedHashMap<String, Object>>) transactions
								.get(key).get("history")).add(contributionMap);
					}

				} else {
					log.debug("getResponseValues", "creating to history");
					ArrayList<LinkedHashMap<String, Object>> historyArray = new ArrayList<LinkedHashMap<String, Object>>();

					if (action.equals("REMOVE") || action.equals("RESTORE")) {
						historyArray
								.add((LinkedHashMap<String, Object>) contributionMap
										.clone());
					}

					contributionMap.put("history", historyArray);

					transactions.put(key, contributionMap);
				}
			}

			result.put("offset", offset);
			result.put("rows", rows);
			result.put("total", transactions.size());

			if (transactions.size() == 0) {
				log.debug("getResponseValues", "no transactions to add");
			}

			ArrayList<LinkedHashMap<String, Object>> contributionList = new ArrayList<LinkedHashMap<String, Object>>();
			if (!isModeratorAction) {
				SortedMap<String, LinkedHashMap<String, Object>> sortedTransactions = new TreeMap<String, LinkedHashMap<String, Object>>(
						EntryInverseComparator);
				for (LinkedHashMap<String, Object> mapItem : transactions
						.values()) {
					sortedTransactions.put(
							mapItem.get("@createTS").toString()
									+ mapItem.get("@transId").toString()
									+ mapItem.get("@type").toString(), mapItem);
				}
				contributionList.addAll(sortedTransactions.values());
			} else {
				contributionList.addAll(transactions.values());
			}
			result.put("contribution", contributionList);
		}

		return result;
	}

	public static Comparator<String> EntryInverseComparator = new Comparator<String>() {
		public int compare(String item1, String item2) {
			return item2.compareToIgnoreCase(item1);
		}
	};

	private String getContributionKey(HashMap<String, Object> entry,
			Integer annotationId, String annotationType) {

		switch (annotationType) {
		case "TR":
			String naId = entry.get("naId").toString();
			String objectId = entry.get("objectId").toString();
			return annotationType + naId + objectId;
		case "TG":
			return annotationType + annotationId.toString();
		case "CM":
			return annotationType + annotationId.toString();
		case "AH":
			return naId = entry.get("naId").toString();
		}

		return "unknown";
	}

	private String getContributionType(String typeAcronym) {
		switch (typeAcronym) {
		case "TR":
			return "transcription";
		case "TG":
			return "tag";
		case "CM":
			return "comment";
		case "AH":
			return "announcement";
		}
		return "unknown";
	}

	private void setContributionContent(
			LinkedHashMap<String, Object> contributionMap, String typeAcronym,
			HashMap<String, Object> entry) {
		LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();

		switch (typeAcronym) {
		case "TR":
			values.put("@version", entry.get("version"));
			values.put("teaser", entry.get("teaser"));
			contributionMap.put("transcription", values);
			break;
		case "TG":
			values.put("@text", entry.get("text"));
			contributionMap.put("tag", values);
			break;
		case "CM":
			values.put("@text", entry.get("text"));
			contributionMap.put("comment", values);
			break;
		case "AH":
			values.put("@text", String.format("announcement: %s",
					entry.get("naId").toString()));
			contributionMap.put("announcement", values);
			break;
		}
	}

	private void setReference(LinkedHashMap<String, Object> contributionMap,
			String typeAcronym, HashMap<String, Object> entry) {
		LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();

		switch (typeAcronym) {
		case "TR":
			values.put("@naid", entry.get("naId"));
			values.put("@id", entry.get("objectId"));
			values.put("@pageNum", entry.get("pageNum"));
			if (entry.get("totalPages") != null) {
				values.put("@totalPages", entry.get("totalPages"));
			} else {
				values.put("@totalPages", 0);
			}

			contributionMap.put("object", values);
			break;
		case "TG":
			values.put("@naid", entry.get("naId"));
			if (entry.get("objectId") != null) {
				values.put("@id", entry.get("objectId"));
				values.put("@pageNum", entry.get("pageNum"));
				if (entry.get("totalPages") != null) {
					values.put("@totalPages", entry.get("totalPages"));
				} else {
					values.put("@totalPages", 0);
				}

			}

			contributionMap.put("description", values);
			break;
		case "CM":
			values.put("@naid", entry.get("naId"));
			if (entry.get("objectId") != null) {
				values.put("@id", entry.get("objectId"));
				values.put("@pageNum", entry.get("pageNum"));
				if (entry.get("totalPages") != null) {
					values.put("@totalPages", entry.get("totalPages"));
				} else {
					values.put("@totalPages", 0);
				}

			}

			contributionMap.put("description", values);
			break;
		case "AH":
			values.put("@naid", entry.get("naId"));
			contributionMap.put("description", values);
			break;
		}
	}
}
