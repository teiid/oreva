/**
 * 
 */
package org.odata4j.format.jsonlite;

/**
 * @author RDXSZ0008
 *
 */
public class OdataJsonLiteConstant {

  public static final String FORMAT_TYPE_JSONLITE = "application/json;odata=minimalmetadata";
  public static final String FORMAT_TYPE_JSONLITE_FULLMETADATA = "application/json;odata=fullmetadata";
  public static final String FORMAT_TYPE_JSONLITE_NOMETADATA = "application/json;odata=nometadata";
  public static final String APPLICATION_JAVASCRIPT_JSONLITE_CHARSET_UTF8 = FORMAT_TYPE_JSONLITE
      + ";charset=" + Charsets.Lower.UTF_8;
  public static final String APPLICATION_JAVASCRIPT_JSONLITE_FULLMETADATA_CHARSET_UTF8 = FORMAT_TYPE_JSONLITE_FULLMETADATA
      + ";charset=" + Charsets.Lower.UTF_8;
  public static final String APPLICATION_JAVASCRIPT_JSONLITE_NOMETADATA_CHARSET_UTF8 = FORMAT_TYPE_JSONLITE_NOMETADATA
      + ";charset=" + Charsets.Lower.UTF_8;

  public static final String METADATA_PROPERTY_WITH_HASH_DOLLAR = "$metadata#";
  public static final String METADATA_PROPERTY = "odata.metadata";
  public static final String NEXT_PROPERTY = "odata.nextLink";
  public static final String COUNT_PROPERTY = "odata.count";

  public static final String TYPE_PROPERTY = "odata.type";
  public static final String ETAG_PROPERTY = "odata.etag";
  public static final String ACTIONS_PROPERTY = "odata.actions";
  public static final String FUNCTIONS_PROPERTY = "odata.functions";
  public static final String VALUE_PROPERTY = "value";
  public static final String VERBOSE_VALUE = "verbose";
  public static final String TARGET_PROPERTY = "target";

  public static final String MEDIA_READ_LINK_PROPERTY = "odata.mediaReadLink";
  public static final String MEDIA_EDIT_LINK_PROPERTY = "odata.mediaEditLink";
  public static final String MEDIA_CONTENT_TYPE_PROPERTY = "odata.mediaContentType";
  public static final String NAV_LINK_URL_PROPERTY = "odata.navigationLinkUrl";
  public static final String START_URI = "(";

  public static final String METADATA_TYPE_FULLMETADATA = "fullmetadata";
  public static final String METADATA_TYPE_NOMETADATA = "nometadata";
  public static final String ID_PROPERTY = "odata.id";
  public static final String EDIT_LINK_PROPERTY = "odata.editLink";

  public static final String ERROR_PROPERTY = "odata.error";
  public static final String CODE_VALUE = "code";
  public static final String MESSAGE_VALUE = "message";
  public static final String LANG_VALUE = "lang";
  public static final String EN_US_VALUE = "en-US";

  public static final String NAVIGATION_LINK_URL_PROPERTY = "@odata.navigationLinkUrl";
  public static final String ASSOCIATION_LINK_URL_PROPERTY = "@odata.associationLinkUrl";
  public static final String DOLLAR_LINKS_PROPERTY = "$links";
  public static final String TITLE_VALUE = "title";
  public static final String METADATA_TYPE_MINIMALMETADATA = "minimalmetadata";

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
