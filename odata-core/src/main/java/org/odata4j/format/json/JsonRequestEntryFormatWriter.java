package org.odata4j.format.json;

import java.io.Writer;

import javax.ws.rs.core.UriInfo;

import org.odata4j.core.ODataConstants;
import org.odata4j.format.Entry;

public class JsonRequestEntryFormatWriter extends JsonFormatWriter<Entry> {

  public JsonRequestEntryFormatWriter(String jsonpCallback) {
    super(jsonpCallback);
  }

  @Override
  public String getContentType() {
    return ODataConstants.APPLICATION_JAVASCRIPT_VERBOSE_CHARSET_UTF8;
  }

  @Override
  public void write(UriInfo uriInfo, Writer w, Entry target) {

    JsonWriter jw = new JsonWriter(w);
    if (getJsonpCallback() != null) {
      jw.startCallback(getJsonpCallback());
    }

    writeContent(uriInfo, jw, target);
  }

  @Override
  protected void writeContent(UriInfo uriInfo, JsonWriter jw, Entry target) {
    writeOEntity(uriInfo, jw, target.getEntity(),
        target.getEntity().getEntitySet(), false);
  }

}
