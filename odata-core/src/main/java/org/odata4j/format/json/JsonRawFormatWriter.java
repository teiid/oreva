package org.odata4j.format.json;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;

import javax.ws.rs.core.UriInfo;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.Throwables;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;
import org.odata4j.format.FormatWriter;
import org.odata4j.internal.InternalUtil;
import org.odata4j.producer.RawResponse;
import org.odata4j.repack.org.apache.commons.codec.binary.Base64;

/**
 * write a single value that has an EdmSimpleType type
 */
public class JsonRawFormatWriter implements FormatWriter<RawResponse> {

  @Override
  public void write(UriInfo uriInfo, Writer w, RawResponse target) {
	 EdmType type = target.getType();
	Object value = target.getValue();
	String sValue = null;
    if (type.isSimple()) {
    	// simple
        // now write the value
        if (type == EdmSimpleType.INT32) {
          if (value != null) {
            sValue = value.toString();
          }
        } else if (type == EdmSimpleType.INT16) {
          if (value != null) {
            sValue = value.toString();
          }
        } else if (type == EdmSimpleType.INT64) {
          if (value != null) {
            sValue = value.toString();
          }
        } else if (type == EdmSimpleType.BOOLEAN) {
          if (value != null) {
            sValue = value.toString();
          }
        } else if (type == EdmSimpleType.BYTE) {
          if (value != null) {
            sValue = value.toString();
          }
        } else if (type == EdmSimpleType.SBYTE) {
          if (value != null) {
            sValue = value.toString();
          }
        } else if (type == EdmSimpleType.DECIMAL) {
          if (value != null) {
            sValue = ((BigDecimal) value).toPlainString();
          }
        } else if (type == EdmSimpleType.SINGLE) {
          if (value != null) {
            sValue = value.toString();
          }
        } else if (type == EdmSimpleType.DOUBLE) {
          if (value != null) {
            sValue = value.toString();
          }
        } else if (type == EdmSimpleType.STRING) {
          if (value != null) {
            sValue = value.toString();
          }
        } else if (type == EdmSimpleType.DATETIME) {
          if (value != null)
            sValue = InternalUtil.formatDateTimeForXml(
                (LocalDateTime) value);
        } else if (type == EdmSimpleType.BINARY) {
          byte[] bValue = (byte[]) value;
          if (value != null) {
            sValue = Base64.encodeBase64String(bValue);
          }
        } else if (type == EdmSimpleType.GUID) {
          if (value != null) {
            sValue = value.toString();
          }
        } else if (type == EdmSimpleType.TIME) {
          if (value != null) {
            sValue = InternalUtil.formatTimeForXml((LocalTime) value);
          }
        } else if (type == EdmSimpleType.DATETIMEOFFSET) {
          // Edm.DateTimeOffset '-'? yyyy '-' mm '-' dd 'T' hh ':' mm
          // ':' ss ('.' s+)? (zzzzzz)?
          if (value != null) {
            sValue = InternalUtil.formatDateTimeOffsetForXml((DateTime) value);
          }
        } else {
          throw new UnsupportedOperationException("Implement " + target.getType());
        }
        try {
			if (sValue == null) {
				w.write("null");
			} else {
				w.write(sValue);
			}
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
    }
  }  

  @Override
  public String getContentType() {
    return ODataConstants.TEXT_PLAIN;
  }
}
