package org.odata4j.format.jsonlite;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.odata4j.core.OEntity;
import org.odata4j.format.json.JsonWriter;
import org.odata4j.producer.EntitiesResponse;

/**
 * The Class JsonLiteFeedFormatWriter.
 * 
 * @author <a href="mailto:rajni.kumari@synerzip.com">Rajni Kumari</a>
 */
public class JsonLiteFeedFormatWriter extends JsonLiteFormatWriter<EntitiesResponse> {
  private final String metadataType;

  public JsonLiteFeedFormatWriter(String jsonpCallback, String metadataType) {
    super(jsonpCallback, metadataType);
    this.metadataType = metadataType;
  }

  @Override
  protected void writeContent(UriInfo uriInfo, JsonWriter jw, EntitiesResponse target) {
    if (metadataType != null && !metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_NOMETADATA)) {
      jw.writeName(OdataJsonLiteConstant.METADATA_PROPERTY);
      String metadataInfo = uriInfo.getBaseUri() + OdataJsonLiteConstant.METADATA_PROPERTY_WITH_HASH_DOLLAR + target.getEntitySet().getName();
      MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
      List<String> list = queryParameters.get("$select");
      if (list != null) {
        String select = "&$select=";
        //read the first value as it contains the property name selected with $select query option
        select = select + list.get(0);
        metadataInfo = metadataInfo + select;
      }
      jw.writeString(metadataInfo);
      jw.writeSeparator();
    }
    if (target.getInlineCount() != null) {
      jw.writeName(OdataJsonLiteConstant.COUNT_PROPERTY);
      jw.writeString(target.getInlineCount().toString());
      jw.writeSeparator();
    }
    jw.writeName(OdataJsonLiteConstant.VALUE_PROPERTY);
    jw.startArray();
    {
      boolean isFirst = true;
      for (OEntity oe : target.getEntities()) {

        if (isFirst) {
          isFirst = false;
        } else {
          jw.writeSeparator();
        }
        jw.startObject();
        writeOEntity(uriInfo, jw, oe, target.getEntitySet(), true);
        jw.endObject();
      }

    }
    jw.endArray();

    if (target.getSkipToken() != null) {

      // $skip only applies to the first page of results.
      // if $top was given, we have to reduce it by the number of entities
      // we are returning now.
      String tops = uriInfo.getQueryParameters().getFirst("$top");
      int top = -1;
      if (tops != null) {
        // query param value already validated
        top = Integer.parseInt(tops);
        top -= target.getEntities().size();
      }
      UriBuilder uri = uriInfo.getRequestUriBuilder();
      if (top > 0) {
        uri.replaceQueryParam("$top", top);
      } else {
        uri.replaceQueryParam("$top");
      }
      String nextHref = uri
          .replaceQueryParam("$skiptoken", target.getSkipToken())
          .replaceQueryParam("$skip").build().toString();
      if (!metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_NOMETADATA)) {
        nextHref = nextHref.replaceFirst(uriInfo.getBaseUri().toString(), "").replace("/", "");
      }
      jw.writeSeparator();
      jw.writeName(OdataJsonLiteConstant.NEXT_PROPERTY);
      jw.writeString(nextHref);
    }

  }
}
