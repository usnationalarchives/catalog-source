package gov.nara.opa.common.services.docstransforms.impl.normalize.valueextractor;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.common.services.docstransforms.impl.normalize.AbstractValueExtractor;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class LinkValueExtractor extends AbstractValueExtractor {

	Pattern regexPattern = Pattern.compile("$\\{(.*?)\\}");
	Map<String, String> mapReplaceTokens = new ConcurrentHashMap<String, String>();

	private String naraBaseUrl;

	@Override
	protected Object getValue(String valueGenerationInstruction, SearchRecordValueObject document, String type,
			AccountExportValueObject accountExport, String section) {

		try {
			String returnValue = valueGenerationInstruction;
			for (String token : mapReplaceTokens.keySet()) {
				String replacement = null;
				if (token.equals("naId")) {
					replacement = document.getNaId();
					if(gov.nara.opa.architecture.utils.StringUtils.isNullOrEmtpy(replacement)) {
						replacement = document.getParentDescriptionNaId();
					}
				}
				if(!gov.nara.opa.architecture.utils.StringUtils.isNullOrEmtpy(returnValue)) {
					if(!gov.nara.opa.architecture.utils.StringUtils.isNullOrEmtpy(replacement)) {
						returnValue = returnValue.replace(mapReplaceTokens.get(token), replacement);
					} else {
						throw new OpaRuntimeException("replacement value is null");
					}
				} else {
					throw new OpaRuntimeException("return value is null");
				}
			}
			return naraBaseUrl + returnValue;
		} catch(Exception e) {
			throw new OpaRuntimeException(e);
		}
	}

	public LinkValueExtractor(String linkPattern, String naraBaseUrl) {
		this.naraBaseUrl = naraBaseUrl;
		String[] extractedTokens = StringUtils.substringsBetween(linkPattern, "${", "}");
		for (String extractedToken : extractedTokens) {
			mapReplaceTokens.put(extractedToken, "${" + extractedToken + "}");
		}
	}
}
