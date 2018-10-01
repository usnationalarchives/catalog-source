
package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

public class URIUtilities {

    private static final URLCodec URL_CODEC = new URLCodec();

    public static URI encodeWhitespace(String uri) throws URISyntaxException{
    return new URI(uri.replace(" ", "%20"));
  }

    public static URL getUrlWithPathEncoded(String urlString) throws AspireException {
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new AspireException("malformed url", e, urlString);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(url.getProtocol());
        sb.append("://");
        sb.append(url.getHost());

        String[] segments = url.getPath().split("/");

        ArrayList<String> segmentsEncoded = new ArrayList<>();

        for (String segment : segments){
            try {
                segment = URL_CODEC.decode(segment);
                segmentsEncoded.add(
                        StringUtils.replace(
                                URL_CODEC.encode(segment), "+", "%20"
                        )
                );
            } catch (EncoderException e) {
                throw new AspireException("url encode", e);
            } catch (DecoderException e) {
                throw new AspireException("url decode", e);
            }
        }

        sb.append(StringUtils.join(segmentsEncoded, '/'));

        String urlWithPathEncoded = sb.toString();

        try {
            return new URL(urlWithPathEncoded);
        } catch (MalformedURLException e) {
            throw new AspireException("malformed url", e, urlWithPathEncoded);
        }
    }
}
