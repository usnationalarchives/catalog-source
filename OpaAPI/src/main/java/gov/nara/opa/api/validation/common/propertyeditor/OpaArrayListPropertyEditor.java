package gov.nara.opa.api.validation.common.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpaArrayListPropertyEditor extends PropertyEditorSupport {

  @Override
  public void setAsText(String text) {
    setValue(getTokens(text));
  }

  /**
   * Gets a list of tokens delimited by commas. If a token is surrounded by
   * double quotes and includes comma(s), those comma(s) will not be considered
   * as separators. Double quotes in tokens surrounded by quotes will be removed
   * when those tokens are added to the return list.
   * 
   * @param value
   *          String with comma delimited tokens
   * @return A List with of parsed tokens. Any double quoted tokens will be
   *         added at the beginning of the return list
   */
  public static ArrayList<String> getTokens(String value) {
    ArrayList<String> tokens = new ArrayList<String>();
    // regex to extract all quoted tokens first
    Pattern pattern = Pattern.compile("(?:^|,)\\s*?\"(.*?)\"");
    Matcher matcher = pattern.matcher(value.trim());
    while (matcher.find()) {
      tokens.add(matcher.group(1));
    }
    // take out the quoted tokens from the input string. This will allow for the
    // rest of the tokens to be extracted
    String valueWihoutQuotedTokens = matcher.replaceAll("");
    String[] regularTokens = valueWihoutQuotedTokens.split(",");
    for (String regularToken : regularTokens) {
      if (!regularToken.trim().equals("")) {
        tokens.add(regularToken);
      }
    }
    return tokens;
  }
}
