package org.odata4j.format.jsonlite;

import java.io.Writer;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.odata4j.core.Guid;
import org.odata4j.core.OAtomStreamEntity;
import org.odata4j.core.OBindableEntity;
import org.odata4j.core.OCollection;
import org.odata4j.core.OComplexObject;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntity;
import org.odata4j.core.OLink;
import org.odata4j.core.ONamedStreamLink;
import org.odata4j.core.OObject;
import org.odata4j.core.OProperty;
import org.odata4j.core.ORelatedEntitiesLinkInline;
import org.odata4j.core.ORelatedEntityLinkInline;
import org.odata4j.core.OSimpleObject;
import org.odata4j.core.UnsignedByte;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.json.JsonWriter;
import org.odata4j.internal.InternalUtil;
import org.odata4j.repack.org.apache.commons.codec.binary.Base64;

/**
 * Write content to an output stream in JSON Lite format.
 *
 * <p>This class is abstract because it delegates the strategy pattern of writing
 * actual content elements to its (various) subclasses.
 *
 * <p>Each element in the array to be written can be wrapped in a function call
 * on the JavaScript side by specifying the name of a function to call to the
 * constructor.
 *
 * @param <T> the type of the content elements to be written to the stream.
 * @author <a href="mailto:rajni.kumari@synerzip.com">Rajni Kumari</a>
 */
public abstract class JsonLiteFormatWriter<T> implements FormatWriter<T> {

  private final String jsonpCallback;

  private final String metadataType;

  /**
   * Creates a new JSON writer.
   *
   * @param jsonpCallback a function to call on the javascript side to act
   * on the data provided in the content.
   * @param metadataType 
   */
  public JsonLiteFormatWriter(String jsonpCallback, String metadataType) {
    this.jsonpCallback = jsonpCallback;
    this.metadataType = metadataType;
  }

  /**
   * A strategy method to actually write content objects
   *
   * @param uriInfo the base URI that indicates where in the schema we are
   * @param jw the JSON writer object
   * @param target the content value to be written
   */
  abstract protected void writeContent(UriInfo uriInfo, JsonWriter jw, T target);

  @Override
  public String getContentType() {
    if (jsonpCallback == null) {
      if (metadataType == null) {
        return OdataJsonLiteConstant.APPLICATION_JAVASCRIPT_JSONLITE_CHARSET_UTF8;
      }
      else if (metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_FULLMETADATA)) {
        return OdataJsonLiteConstant.APPLICATION_JAVASCRIPT_JSONLITE_FULLMETADATA_CHARSET_UTF8;
      }
      else if (metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_NOMETADATA)) {
        return OdataJsonLiteConstant.APPLICATION_JAVASCRIPT_JSONLITE_NOMETADATA_CHARSET_UTF8;
      }
      else {
        return OdataJsonLiteConstant.APPLICATION_JAVASCRIPT_JSONLITE_CHARSET_UTF8;
      }
    }
    else {
      return ODataConstants.TEXT_JAVASCRIPT_CHARSET_UTF8;
    }
  }

  /**
   * Gets the jsonp callback.
   *
   * @return the jsonp callback
   */
  protected String getJsonpCallback() {
    return jsonpCallback;
  }

  @Override
  public void write(UriInfo uriInfo, Writer w, T target) {

    JsonWriter jw = new JsonWriter(w);
    if (getJsonpCallback() != null) {
      jw.startCallback(getJsonpCallback());
    }

    jw.startObject();
    {
      writeContent(uriInfo, jw, target);
    }
    jw.endObject();

    if (getJsonpCallback() != null) {
      jw.endCallback();
    }

  }

  /**
   * Write value.
   *
   * @param jw the jw
   * @param type the type
   * @param pvalue the pvalue
   * @param isResponse check for server response or request from client
   */
  @SuppressWarnings("unchecked")
  protected void writeValue(JsonWriter jw, EdmType type, Object pvalue, boolean isResponse) {
    if (pvalue == null) {
      jw.writeNull();
    } else if (type.equals(EdmSimpleType.BINARY)) {
      jw.writeString(Base64.encodeBase64String((byte[]) pvalue));
    } else if (type.equals(EdmSimpleType.BOOLEAN)) {
      jw.writeBoolean((Boolean) pvalue);
    } else if (type.equals(EdmSimpleType.BYTE)) {
      jw.writeNumber(((UnsignedByte) pvalue).intValue());
    } else if (type.equals(EdmSimpleType.SBYTE)) {
      jw.writeNumber((Byte) pvalue);
    } else if (type.equals(EdmSimpleType.DATETIME)) {
      jw.writeString(pvalue.toString());
    } else if (type.equals(EdmSimpleType.DECIMAL)) {
      jw.writeString(((BigDecimal) pvalue).toPlainString());
    } else if (type.equals(EdmSimpleType.DOUBLE)) {
      jw.writeString(pvalue.toString());
    } else if (type.equals(EdmSimpleType.GUID)) {
      jw.writeString(((Guid) pvalue).toString());
    } else if (type.equals(EdmSimpleType.INT16)) {
      jw.writeNumber((Short) pvalue);
    } else if (type.equals(EdmSimpleType.INT32)) {
      jw.writeNumber((Integer) pvalue);
    } else if (type.equals(EdmSimpleType.INT64)) {
      jw.writeString(pvalue.toString());
    } else if (type.equals(EdmSimpleType.SINGLE)) {
      jw.writeNumber((Float) pvalue);
    } else if (type.equals(EdmSimpleType.TIME)) {
      jw.writeRaw(InternalUtil.formatTimeForJson((LocalTime) pvalue));
    } else if (type.equals(EdmSimpleType.DATETIMEOFFSET)) {
      jw.writeRaw(InternalUtil.formatDateTimeOffsetForJson((DateTime) pvalue));
    } else if (type instanceof EdmComplexType || (type instanceof EdmSimpleType && (!((EdmSimpleType<?>) type).isSimple()))) {
      // the OComplexObject value type is not in use everywhere yet, fix TODO
      if (pvalue instanceof OComplexObject) {
        pvalue = ((OComplexObject) pvalue).getProperties();
      }
      writeComplexObject(jw, (List<OProperty<?>>) pvalue, isResponse, type);
    } else if (type instanceof EdmCollectionType) {
      writeCollection(jw, (EdmCollectionType) type, (OCollection<? extends OObject>) pvalue, isResponse);
    } else {
      String value = pvalue.toString();
      jw.writeString(value);
    }
  }

  /**
   * Write collection.
   *
   * @param jw the jw
   * @param type the type
   * @param coll the coll
   * @param isResponse check for server response or request from client
   */
  @SuppressWarnings("rawtypes")
  protected void writeCollection(JsonWriter jw, EdmCollectionType type, OCollection<? extends OObject> coll, boolean isResponse) {
    jw.startArray();
    {
      boolean isFirst = true;
      Iterator<? extends OObject> iter = coll.iterator();
      while (iter.hasNext()) {
        OObject obj = iter.next();
        if (isFirst) {
          isFirst = false;
        } else {
          jw.writeSeparator();
        }
        if (obj instanceof OComplexObject) {
          writeComplexObject(jw, ((OComplexObject) obj).getProperties(), isResponse, obj.getType());
        } else if (obj instanceof OSimpleObject) {
          writeValue(jw, obj.getType(), ((OSimpleObject) obj).getValue(), isResponse);
        }
      }

    }
    jw.endArray();
  }

  /**
   * Write a single complex type in ODATA JSON format. it can be from an entity, or from an collection, it can also be
   * a single complex type response.
   * for single complex type response, the complecObjectName should not be null, in other cases it should be null.
   *
   * @param jw the jw
   * @param props the props
   * @param isResponse check for server response or request from client
   * @param type 
   */
  protected void writeComplexObject(JsonWriter jw, List<OProperty<?>> props, boolean isResponse, EdmType type) {
    jw.startObject();
    {
      if (!isResponse) {
        jw.writeName(OdataJsonLiteConstant.TYPE_PROPERTY);
        jw.writeString(type.getFullyQualifiedTypeName());
        jw.writeSeparator();
      }
      writeOProperties(jw, props, isResponse);
    }
    jw.endObject();
  }

  /**
   * Write o entity.
   *
   * @param uriInfo the uri info
   * @param jw the jw
   * @param oe the oe
   * @param ees the ees
   * @param isResponse the is response
   */
  protected void writeOEntity(UriInfo uriInfo, JsonWriter jw, OEntity oe, EdmEntitySet ees, boolean isResponse) {

    String baseUri = null;

    if (isResponse && oe.getEntityType() != null) {

      baseUri = uriInfo != null ? uriInfo.getBaseUri().toString() : "";
      String absId = baseUri + InternalUtil.getEntityRelId(oe);

      OAtomStreamEntity stream = oe.findExtension(OAtomStreamEntity.class);
      if (metadataType != null && metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_FULLMETADATA)) {
        jw.writeName(OdataJsonLiteConstant.TYPE_PROPERTY);
        jw.writeString(ees.getType().getFullyQualifiedTypeName());
        jw.writeSeparator();
        jw.writeName(OdataJsonLiteConstant.ID_PROPERTY);
        jw.writeString(absId);
        jw.writeSeparator();
        jw.writeName(OdataJsonLiteConstant.EDIT_LINK_PROPERTY);
        jw.writeString(InternalUtil.getEntityRelId(oe));
        jw.writeSeparator();
      }

      // Adding additional metadata per Entry that describes the Media Resource (MR) associated with the Entry
      if (stream != null && ees.getType().getHasStream() != null && ees.getType().getHasStream()) {
        if (metadataType != null && !metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_NOMETADATA)) {
          jw.writeName(OdataJsonLiteConstant.MEDIA_READ_LINK_PROPERTY);
          jw.writeString(InternalUtil.getEntityRelId(oe) + stream.getAtomEntitySource());
          jw.writeSeparator();
          jw.writeName(OdataJsonLiteConstant.MEDIA_EDIT_LINK_PROPERTY);
          jw.writeString(InternalUtil.getEntityRelId(oe));
          jw.writeSeparator();
          jw.writeName(OdataJsonLiteConstant.MEDIA_CONTENT_TYPE_PROPERTY);
          jw.writeString(stream.getAtomEntityType());
          jw.writeSeparator();
        }
      }

      // Exposed bound functions if any
      if (oe != null) {
        OBindableEntity bindableEntity = oe.findExtension(OBindableEntity.class);
        if (bindableEntity != null) {

          if (bindableEntity.getBindableActions().size() > 0) {
            jw.writeName(OdataJsonLiteConstant.ACTIONS_PROPERTY);
            jw.startArray();
            boolean first = true;
            for (Map.Entry<String, EdmFunctionImport> entry : bindableEntity.getBindableActions().entrySet()) {
              if (!first) {
                jw.writeSeparator();
              } else {
                first = false;
              }
              jw.startObject();
              writeFunction(jw, absId, entry.getKey(), entry.getValue());
              jw.endObject();
            }
            jw.endArray();
            jw.writeSeparator();
          }
          if (bindableEntity.getBindableFunctions().size() > 0) {
            jw.writeName(OdataJsonLiteConstant.FUNCTIONS_PROPERTY);
            jw.startArray();
            boolean first = true;
            for (Map.Entry<String, EdmFunctionImport> entry : bindableEntity.getBindableFunctions().entrySet()) {
              if (!first) {
                jw.writeSeparator();
              } else {
                first = false;
              }
              jw.startObject();
              writeFunction(jw, absId, entry.getKey(), entry.getValue());
              jw.endObject();
            }
            jw.endArray();
            jw.writeSeparator();
          }

        }
      }
    }
    //Writes links
    writeLinks(jw, oe, uriInfo, isResponse);

    writeOProperties(jw, oe.getProperties(), isResponse);

  }

  /**
   * Write request or response links.
   *
   * @param jw the jw
   * @param oe the oe
   * @param uriInfo the uri info
   * @param isResponse the is response
   */
  private void writeLinks(JsonWriter jw, OEntity oe, UriInfo uriInfo, boolean isResponse) {
    if (oe.getLinks() != null) {
      for (OLink link : oe.getLinks()) {
        if (isResponse) {
          writeResponseLink(jw, link, oe, uriInfo);
        } else {
          writeReqestLink(jw, link, oe, uriInfo);
        }
      }
    }
  }

  /**
   * Write reqest link.
   *
   * @param jw the jw
   * @param link the link
   * @param oe the oe
   * @param uriInfo the uri info
   */
  private void writeReqestLink(JsonWriter jw, OLink link, OEntity oe, UriInfo uriInfo) {
    if (link.isInline()) {
      jw.writeName(link.getTitle());
      if (link.isCollection()) {
        jw.startArray();
        {
          if (((ORelatedEntitiesLinkInline) link).getRelatedEntities() != null) {
            boolean isFirstInlinedEntity = true;
            for (OEntity re : ((ORelatedEntitiesLinkInline) link).getRelatedEntities()) {

              if (isFirstInlinedEntity) {
                isFirstInlinedEntity = false;
              } else {
                jw.writeSeparator();
              }
              jw.startObject();
              jw.writeName(OdataJsonLiteConstant.TYPE_PROPERTY);
              jw.writeString(re.getEntityType().getFullyQualifiedTypeName());
              jw.writeSeparator();
              writeOEntity(uriInfo, jw, re, re.getEntitySet(), false);
              jw.endObject();
            }

          }
        }
        jw.endArray();
        jw.writeSeparator();
      } else {
        OEntity re = ((ORelatedEntityLinkInline) link).getRelatedEntity();
        if (re == null) {
          jw.writeNull();
        } else {
          jw.startObject();
          jw.writeName(OdataJsonLiteConstant.TYPE_PROPERTY);
          jw.writeString(re.getEntityType().getFullyQualifiedTypeName());
          jw.writeSeparator();
          writeOEntity(uriInfo, jw, re, re.getEntitySet(), false);
          jw.endObject();
          jw.writeSeparator();
        }
      }
    } else {
      jw.writeName(link.getTitle());
      jw.startObject();
      jw.writeName("url");
      jw.writeString(link.getHref());
      jw.endObject();
      jw.writeSeparator();

    }
  }

  /**
   * Write response link.
   *
   * @param jw the jw
   * @param link the link
   * @param oe the oe
   * @param uriInfo the uri info
   */
  private void writeResponseLink(JsonWriter jw, OLink link, OEntity oe, UriInfo uriInfo) {
    if (metadataType != null && metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_FULLMETADATA)) {
      jw.writeName(link.getTitle() + OdataJsonLiteConstant.NAVIGATION_LINK_URL_PROPERTY);
      jw.writeString(InternalUtil.getEntityRelId(oe) + "/" + link.getTitle());
      jw.writeSeparator();
      jw.writeName(link.getTitle() + OdataJsonLiteConstant.ASSOCIATION_LINK_URL_PROPERTY);
      jw.writeString(InternalUtil.getEntityRelId(oe) + "/" + OdataJsonLiteConstant.DOLLAR_LINKS_PROPERTY + "/" + link.getTitle());
      jw.writeSeparator();
    }
    if (link instanceof ONamedStreamLink) {
      // write the named stream link here and return
      // this is like CD_ATTACHMENT('DWS39')/ATTACHMENT , ref will contain customer query string like project
      String relId = InternalUtil.getEntityRelId(oe)+"/"+link.getHref();

      jw.writeName(link.getTitle() + "@odata.mediaEditLink");
      jw.writeString(relId);
      
      jw.writeSeparator();
      
      jw.writeName(link.getTitle() + "@odata.mediaReadLink");
      jw.writeString(relId);
      
      jw.writeSeparator();

      jw.writeName(link.getTitle() + "@odata.mediaContentType");
      jw.writeString(link.getType());
      
      jw.writeSeparator();
      
      return;
    }
    if (link.isInline()) {

      jw.writeName(link.getTitle());
      if (link.isCollection()) {
        jw.startArray();
        {
          if (((ORelatedEntitiesLinkInline) link).getRelatedEntities() != null) {
            boolean isFirstInlinedEntity = true;
            for (OEntity re : ((ORelatedEntitiesLinkInline) link).getRelatedEntities()) {

              if (isFirstInlinedEntity) {
                isFirstInlinedEntity = false;
              } else {
                jw.writeSeparator();
              }
              jw.startObject();
              writeOEntity(uriInfo, jw, re, re.getEntitySet(), true);
              jw.endObject();
            }

          }
        }
        jw.endArray();
        jw.writeSeparator();
      } else {
        OEntity re = ((ORelatedEntityLinkInline) link).getRelatedEntity();
        if (re == null) {
          jw.writeNull();
          jw.writeSeparator();
        } else {
          jw.startObject();
          writeOEntity(uriInfo, jw, re, re.getEntitySet(), true);
          jw.endObject();
          jw.writeSeparator();
        }
      }
    }

  }

  /**
   * Write function.
   *
   * @param jw the jw
   * @param entityUri the entity uri
   * @param fqFunctionName the fq function name
   * @param function the function
   */
  private void writeFunction(JsonWriter jw, String entityUri, String fqFunctionName, EdmFunctionImport function) {
    jw.writeName("#" + function.getName());
    jw.startObject();
    if (metadataType != null && metadataType.equalsIgnoreCase(OdataJsonLiteConstant.FORMAT_TYPE_JSONLITE_FULLMETADATA)) {
      jw.writeName(OdataJsonLiteConstant.TITLE_VALUE);
      jw.writeString(fqFunctionName);
      jw.writeSeparator();
    }
    jw.writeName(OdataJsonLiteConstant.TARGET_PROPERTY);
    jw.writeString(entityUri + "/" + fqFunctionName);
    jw.endObject();

  }

  /**
   * Write o properties.
   *
   * @param jw the jw
   * @param properties the properties
   * @param isResponse check for server response or request from client
   */
  protected void writeOProperties(JsonWriter jw, List<OProperty<?>> properties, boolean isResponse) {
    boolean isFirst = true;
    for (OProperty<?> prop : properties) {
      if (isFirst) {
        isFirst = false;
      } else {
        jw.writeSeparator();
      }
      if (!isResponse && (prop.getType() instanceof EdmCollectionType)) {
        jw.writeName(prop.getName() + "@" + OdataJsonLiteConstant.TYPE_PROPERTY);
        jw.writeString(prop.getType().getFullyQualifiedTypeName());
        jw.writeSeparator();
      }
      //"DateTimeProperty@odata.type":"Edm.DateTime",
      if (isResponse && metadataType != null && metadataType.equalsIgnoreCase(OdataJsonLiteConstant.METADATA_TYPE_FULLMETADATA)) {
        writeMetadataInfo(jw, prop.getType(), prop.getName());
      }
      jw.writeName(prop.getName());
      writeValue(jw, prop.getType(), prop.getValue(), isResponse);
    }
  }

  private void writeMetadataInfo(JsonWriter jw, EdmType type, String propertyName) {
    if (type.equals(EdmSimpleType.BINARY)) {
      jw.writeName(propertyName + "@" + OdataJsonLiteConstant.TYPE_PROPERTY);
      jw.writeString(type.getFullyQualifiedTypeName());
      jw.writeSeparator();
    } else if (type.equals(EdmSimpleType.DATETIME)) {
      jw.writeName(propertyName + "@" + OdataJsonLiteConstant.TYPE_PROPERTY);
      jw.writeString(type.getFullyQualifiedTypeName());
      jw.writeSeparator();
    } else if (type.equals(EdmSimpleType.DECIMAL)) {
      jw.writeName(propertyName + "@" + OdataJsonLiteConstant.TYPE_PROPERTY);
      jw.writeString(type.getFullyQualifiedTypeName());
      jw.writeSeparator();
    } else if (type.equals(EdmSimpleType.GUID)) {
      jw.writeName(propertyName + "@" + OdataJsonLiteConstant.TYPE_PROPERTY);
      jw.writeString(type.getFullyQualifiedTypeName());
      jw.writeSeparator();
    } else if (type.equals(EdmSimpleType.INT64)) {
      jw.writeName(propertyName + "@" + OdataJsonLiteConstant.TYPE_PROPERTY);
      jw.writeString(type.getFullyQualifiedTypeName());
      jw.writeSeparator();
    } else if (type.equals(EdmSimpleType.INT16)) {
      jw.writeName(propertyName + "@" + OdataJsonLiteConstant.TYPE_PROPERTY);
      jw.writeString(type.getFullyQualifiedTypeName());
      jw.writeSeparator();

    } else if (type.equals(EdmSimpleType.TIME)) {
      jw.writeName(propertyName + "@" + OdataJsonLiteConstant.TYPE_PROPERTY);
      jw.writeString(type.getFullyQualifiedTypeName());
      jw.writeSeparator();
    } else if (type.equals(EdmSimpleType.DATETIMEOFFSET)) {
      jw.writeName(propertyName + "@" + OdataJsonLiteConstant.TYPE_PROPERTY);
      jw.writeString(type.getFullyQualifiedTypeName());
      jw.writeSeparator();
    } else if (type.equals(EdmSimpleType.BYTE)) {
      jw.writeName(propertyName + "@" + OdataJsonLiteConstant.TYPE_PROPERTY);
      jw.writeString(type.getFullyQualifiedTypeName());
      jw.writeSeparator();
    } else if (type.equals(EdmSimpleType.SBYTE)) {
      jw.writeName(propertyName + "@" + OdataJsonLiteConstant.TYPE_PROPERTY);
      jw.writeString(type.getFullyQualifiedTypeName());
      jw.writeSeparator();
    }

  }

}
