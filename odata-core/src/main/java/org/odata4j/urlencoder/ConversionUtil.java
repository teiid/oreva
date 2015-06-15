package org.odata4j.urlencoder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedMap;

import org.odata4j.producer.resources.HeaderMap;

/**
 * 
 * This class uses URLEncoder to Encode the String and URLDecoder to decode the String.
 *<ol>
 *  <li>The alphanumeric characters "a" through "z", "A" through "Z" and "0" through "9" remain the same.</li>
 *  <li>The special characters ".", "-", "*", and "_" remain the same.</li>
 *  <li>The space character " " is converted into a plus sign "+".</li>
 *  <li>All other characters are unsafe and are first converted into one or more bytes using some encoding scheme.</li>
 *</ol>
 *Then each byte is represented by the 3-character string "%xy", where xy is the two-digit hexadecimal representation of the byte.<br>
 *The recommended encoding scheme to use is UTF-8. However, for compatibility reasons, if an encoding is not specified, then the default encoding of the platform is used.
 *
 * Copyright 2013 Halliburton
 * @author <a href="mailto:rajni.kumari@synerzip.com">Rajni Kumari</a>
 */
public class ConversionUtil {

  /** The Constant log. */
  private static final Logger log = Logger.getLogger(ConversionUtil.class.getName());

   /** The Constant encodingScheme. */
  private static final String encodingOrDecodingScheme = "UTF-8";

  /** The Constant UNSUPPORTED_CHARACTER. */
  private static final Map<String, String> UNSUPPORTED_CHARACTER = new HashMap<String, String>();
  static {
    UNSUPPORTED_CHARACTER.put("+", "%20");
    UNSUPPORTED_CHARACTER.put("%2F", "/");
    UNSUPPORTED_CHARACTER.put("%27", "'");
    UNSUPPORTED_CHARACTER.put("%3D", "=");
    UNSUPPORTED_CHARACTER.put("%2C", ",");
    UNSUPPORTED_CHARACTER.put("%28", "(");
    UNSUPPORTED_CHARACTER.put("%29", ")");
   }

  /**
   * Instantiates a new conversion util.
   */
  private ConversionUtil() {

  }

  /**
   * Encode string.
   *
   * @param stringToEncode the string to encode
   * @return the string
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  public static String encodeString(String stringToEncode) {
    String encodedString =  null;
    if (stringToEncode != null) {
      try {
        encodedString = ConversionUtil.removeUnsuportedCharacter(URLEncoder.encode(stringToEncode, encodingOrDecodingScheme));
      } catch (UnsupportedEncodingException e) {
        log.info(e.getMessage());
      }
    }
    return encodedString;
  }

  /**
   * Decode string.
   *
   * @param stringToDecode the string to decode
   * @return the string
   */
  public static String decodeString(String stringToDecode) {
    String decodedString = null;
    if (stringToDecode != null) {
      try {
        decodedString = URLDecoder.decode(stringToDecode, encodingOrDecodingScheme);
      } catch (UnsupportedEncodingException e) {
        log.warning("ConversionUtil failed to decode a string:"+stringToDecode+"\nthe error is"+e.getMessage());
      }
    }
    return decodedString;
  }

  /**
   * Removes the unsuported character.
   *
   * @param encodedString the encoded string
   * @return the string
   */
  private static String removeUnsuportedCharacter(String encodedString) {
    for (Map.Entry<String, String> e : UNSUPPORTED_CHARACTER.entrySet()) {
      encodedString = encodedString.replace(e.getKey(), e.getValue());
    }
    return encodedString;

  }
  
  public static MultivaluedMap<String, String> decodeQueryString(URI uri) {
    
    MultivaluedMap<String, String> params = new HeaderMap();
    
    if (uri == null) {
      return params;
    }
    String queryString = uri.getRawQuery();
    if (queryString == null) {
      return params;
    }
    queryString = queryString.trim();
    if (queryString.isEmpty()) {
      return params;
    }
    
    StringTokenizer st = new StringTokenizer(queryString, "&");
    while (st.hasMoreTokens()) {
      String param = st.nextToken();
      param = param.trim();
      if (param.isEmpty()) {
        continue;
      }
      
      int index = param.indexOf("=");
      
      try {
        if (index == -1) {
          params.add(URLDecoder.decode(param, "UTF-8"), "");
        } else if (index > 0) {
          String name = param.substring(0, index);
          String value = param.substring(index+1);
          params.add(URLDecoder.decode(name, "UTF-8"), URLDecoder.decode(value, "UTF-8"));
        }
      } catch (UnsupportedEncodingException e) {
        log.warning("ConversionUtil failed to decode a query string:"+param+"\nthe error is"+e.getMessage());
       
      }
    }
    
    return params;

  }
}
