package gov.nara.opa.common.storage;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;

import gov.nara.opa.architecture.exception.OpaRuntimeException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

public class URIUtilities {

	private static final URLCodec URL_CODEC = new URLCodec();

	/**
	 * Encodes the white spaces in the URI
	 * 
	 * @param uri URI to be encoded
	 * @return URI with encoded white spaces
	 * @throws URISyntaxException
	 */
	public static URI encodeWhitespace(String uri) throws URISyntaxException {
		return new URI(uri.replace(" ", "%20"));
	}

	/**
	 * Gets a URL with the path encoded for S3
	 * 
	 * @param urlString The URL path
	 * @return Encoded URL for S3
	 * @throws OpaRuntimeException
	 */
	public static URL getUrlWithPathEncoded(String urlString)
			throws OpaRuntimeException {
		URL url;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			throw new OpaRuntimeException(String.format("Malformed url: %s",
					urlString), e);
		}

		StringBuilder sb = new StringBuilder();
		sb.append(url.getProtocol());
		sb.append("://");
		sb.append(url.getHost());

		String[] segments = url.getPath().split("/");

		ArrayList<String> segmentsEncoded = new ArrayList<>();

		for (String segment : segments) {
			try {
				segmentsEncoded.add(StringUtils.replace(
						URL_CODEC.encode(segment), "+", "%20"));
			} catch (EncoderException e) {
				throw new OpaRuntimeException("url encode", e);
			}
		}

		sb.append(StringUtils.join(segmentsEncoded, '/'));

		String urlWithPathEncoded = sb.toString();

		try {
			return new URL(urlWithPathEncoded);
		} catch (MalformedURLException e) {
			throw new OpaRuntimeException(String.format("Malformed url: %s",
					urlWithPathEncoded), e);
		}
	}
}
