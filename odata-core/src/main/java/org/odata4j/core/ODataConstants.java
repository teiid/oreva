package org.odata4j.core;

/**
 * Useful constants.
 */
public class ODataConstants {

  private ODataConstants() {}

  public static final String TEXT_PLAIN = "text/plain";
  public static final String TEXT_PLAIN_CHARSET_UTF8 = TEXT_PLAIN
      + ";charset=" + Charsets.Lower.UTF_8;

  public static final String APPLICATION_ATOM_XML = "application/atom+xml";
  public static final String APPLICATION_ATOM_XML_CHARSET_UTF8 = APPLICATION_ATOM_XML
      + ";charset=" + Charsets.Lower.UTF_8;

  public static final String APPLICATION_ATOMSVC_XML = "application/atomsvc+xml";
  public static final String APPLICATION_ATOMSVC_XML_CHARSET_UTF8 = APPLICATION_ATOMSVC_XML
      + ";charset=" + Charsets.Lower.UTF_8;

  public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
  public static final String APPLICATION_XML = "application/xml";
  public static final String APPLICATION_XML_CHARSET_UTF8 = "application/xml;charset="
      + Charsets.Lower.UTF_8;
  public static final String TEXT_JAVASCRIPT_CHARSET_UTF8 = "text/javascript;charset="
      + Charsets.Lower.UTF_8;
  public static final String APPLICATION_JAVASCRIPT = "application/json";
  public static final String APPLICATION_JAVASCRIPT_VERBOSE = "application/json;odata=verbose";
  public static final String APPLICATION_JAVASCRIPT_CHARSET_UTF8 = APPLICATION_JAVASCRIPT
      + ";charset=" + Charsets.Lower.UTF_8;
  public static final String APPLICATION_JAVASCRIPT_VERBOSE_CHARSET_UTF8 = APPLICATION_JAVASCRIPT_VERBOSE
      + ";charset=" + Charsets.Lower.UTF_8;
  public static final String APPLICATION_HTTP = "application/http";
  public static final String APPLICATION_BINARY = "application/binary";

  public static final ODataVersion DATA_SERVICE_VERSION = ODataVersion.V3;
  public static final String DATA_SERVICE_VERSION_HEADER = DATA_SERVICE_VERSION.asString;

  public static final String BINARY = "binary";

  public static final String CDATA_TAG_START = "<![CDATA[";
  public static final String CDATA_TAG_END = "]]>";
  public static final String ERROR_TEXT = " @*#*@ Error While Reading Data @*#*@ : ";

  public static final String NaN_value = "NaN";

  /** Common http header names. */
  public static class Headers {
    public static final String X_HTTP_METHOD = "X-HTTP-METHOD";
    public static final String DATA_SERVICE_VERSION = "DataServiceVersion";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String USER_AGENT = "User-Agent";
    public static final String LOCATION = "Location";
    public static final String SLUG = "Slug";
    public static final String IF_MATCH = "If-Match";
    public static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    public static final String CONTENT_LENGTH = "Content-Length";
  }

  public static final int COPY_BUFFER_SIZE = 8 * 1024;
  /** 
   * The default buffer size after which we push the contents to file while creating the response
   */
  public static final Integer DEFAULT_BUFFER_THRESHOLD_LIMIT = 64 * 1024 * 1024;
  public static final String ODATA_WRITER_THRESHOLD_SIZE = "odata4j.writer.buffer.threshold";
  public static final String JERSEY_CLIENT_CHUNKED_ENCODING_SIZE = "jersey.config.client.chunkedEncodingSize";
  public static final String ODATA_STREAM_BUFFER_SIZE = "odata4j.stream.buffer.size";

  /** Common character sets. */
  public static class Charsets {
    /** Common character sets. (UPPER-CASE) */
    public static class Upper {
      public static final String UTF_8 = "UTF-8";
      public static final String ISO_8859_1 = "ISO-8859-1"; // latin1
      public static final String ISO_8859_15 = "ISO-8859-15"; // latin9
    }

    /** Common character sets. (lower-case) */
    public static class Lower {
      public static final String UTF_8 = "utf-8";
      public static final String ISO_8859_1 = "iso-8859-1";
      public static final String ISO_8859_15 = "iso-8859-15";
    }
  }
}
