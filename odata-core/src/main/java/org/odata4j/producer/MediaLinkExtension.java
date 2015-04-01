package org.odata4j.producer;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;
import javax.ws.rs.core.HttpHeaders;

import org.core4j.Enumerable;
import org.odata4j.core.OAtomStreamEntity;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OLink;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.core.OSimpleObject;
import org.odata4j.core.OSimpleObjects;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;

/**
 * Implementation of OMediaLinkExtension which actually exposes the API's from producer to work with Media Link Entries.
 *
 */
public class MediaLinkExtension implements OMediaLinkExtension {

  private ODataProducer producer = null;
  private static Map<String, Object> defaultValuesMap = new HashMap<String, Object>();

  static {
    defaultValuesMap.put(EdmSimpleType.STRING.getFullyQualifiedTypeName(), "");
    defaultValuesMap.put(EdmSimpleType.BOOLEAN.getFullyQualifiedTypeName(), Boolean.FALSE);
    defaultValuesMap.put(EdmSimpleType.DOUBLE.getFullyQualifiedTypeName(), 0.0D);
    defaultValuesMap.put(EdmSimpleType.INT16.getFullyQualifiedTypeName(), 0);
    defaultValuesMap.put(EdmSimpleType.INT32.getFullyQualifiedTypeName(), 0);
    defaultValuesMap.put(EdmSimpleType.INT64.getFullyQualifiedTypeName(), 0);
    defaultValuesMap.put(EdmSimpleType.DECIMAL.getFullyQualifiedTypeName(), 0.0);
    defaultValuesMap.put(EdmSimpleType.SINGLE.getFullyQualifiedTypeName(), 0.0);
    defaultValuesMap.put(EdmSimpleType.BINARY.getFullyQualifiedTypeName(), new byte[] {});
    try {
      defaultValuesMap.put(EdmSimpleType.STREAM.getFullyQualifiedTypeName(), new SerialBlob(new byte[]{}));
    } catch (SerialException e) {
      // handle exception 
    } catch (SQLException e) {
      // handle exception
    }
  }

  /**
   * Default constructor 
   */
  public MediaLinkExtension() {}

  /**
   * Constructor to have the producer reference 
   * 
   * @param producer
   */
  public MediaLinkExtension(ODataProducer producer) {
    this.producer = producer;
  }

  @Override
  public InputStream getInputStreamForMediaLinkEntry(ODataContext odataContext, OEntity mle, String etag, EntityQueryInfo query) {
    InputStream mediaStream = null;
    if (this.producer != null) {
      // This API from producer is supposed to return the stream for a BLOB
      mediaStream = producer.getInputStreamForMediaLink(mle.getEntitySet().getName(), OEntityKey.parse(mle.getEntityKey().toKeyString()), query);
    }

    return mediaStream;
  }

  @Override
  public String getMediaLinkContentType(ODataContext odataContext, OEntity mle) {
    OAtomStreamEntity stream = mle.findExtension(OAtomStreamEntity.class);
    if(stream!=null){
      return stream.getAtomEntityType();
    }
    return null;
  }

  @Override
  public OutputStream getOutputStreamForMediaLinkEntryCreate(ODataContext odataContext, OEntity mle, String etag, QueryInfo query) {
    return null;
  }

  @Override
  public OutputStream getOutputStreamForMediaLinkEntryUpdate(ODataContext odataContext, OEntity mle, String etag, QueryInfo query) {
    return null;
  }

  @Override
  public OEntity getMediaLinkEntryForUpdateOrDelete(ODataContext odataContext, EdmEntitySet entitySet, OEntityKey key, HttpHeaders httpHeaders) {
    ArrayList<OProperty<?>> props = new ArrayList<OProperty<?>>();
    Enumerable<EdmProperty> properties = entitySet.getType().getProperties();
    // ignore fields from slug 
    for (EdmProperty edmProperty : properties) {
      EdmType type = edmProperty.getType();
      // check if the property is not null-able, if so set the default value while persisting the entity
      if (type.equals(EdmSimpleType.STREAM)) {
        props.add(OProperties.simple(edmProperty.getName(), defaultValuesMap.get(type.getFullyQualifiedTypeName())));
      }
    }

    return OEntities.create(entitySet, key, props, Collections.<OLink> emptyList());
  }

  @Override
  public void deleteStream(ODataContext odataContext, OEntity mle, QueryInfo query) {
    /*String id = mle.getEntityKey().asSingleValue().toString();
    if (mediaResources.containsKey(id)) {
      mediaResources.remove(id);
    } else {
      throw new NotFoundException("MLE with id: " + id + " not found");
    }*/
  }

  /*
   * (non-Javadoc)
   * @see org.odata4j.producer.OMediaLinkExtension#createMediaLinkEntry(org.odata4j.edm.EdmEntitySet, javax.ws.rs.core.HttpHeaders)
   */
  @Override
  public OEntity createMediaLinkEntry(ODataContext odataContext, EdmEntitySet entitySet, HttpHeaders httpHeaders) {
    /* 
     * We will get all the properties which are part of composite/primary key in the slug header other than the system generated 
     * columns which are part of composite/primary key. Adding key references coming in as part of slug to the properties.
     */
    ArrayList<OProperty<?>> props = new ArrayList<OProperty<?>>();
    Map<String, Object> keyValueMap = new HashMap<String, Object>();
    String slugHeader = httpHeaders.getRequestHeader("Slug") != null ? httpHeaders.getRequestHeader("Slug").get(0) : null;

    if (slugHeader != null && !slugHeader.equals("")) {
      String[] split = slugHeader.split(",");
      for (String slug : split) {
        String[] keyValueArray = slug.split("=");
        if (keyValueArray.length != 0) {
          EdmProperty property = entitySet.getType().findProperty(keyValueArray[0]);
          if (property != null) {
            EdmType type = property.getType();
            OSimpleObject<?> parse = OSimpleObjects.parse((EdmSimpleType<?>) type, keyValueArray[1].trim());
            props.add(OProperties.simple(keyValueArray[0], parse.getType(), parse.getValue()));
            keyValueMap.put(keyValueArray[0], keyValueArray[1]);
          }
        }
      }
    }

    /*
     * Setting "IsMedia"=true property to empty byte[] so that we have a property whose value will be replaced with the content from
     * stream. Also we are setting dummy values for the non-nullable columns for which we do not have real values as it may cause an
     * issue while persisting an entity.
     */
    Enumerable<EdmProperty> properties = entitySet.getType().getProperties();
    for (EdmProperty edmProperty : properties) {
      EdmType type = edmProperty.getType();
      if (type.equals(EdmSimpleType.STREAM)) {
        props.add(OProperties.simple(edmProperty.getName(), defaultValuesMap.get(type.getFullyQualifiedTypeName())));
      }
      else if (!edmProperty.isNullable()) {
        /*
         * Depending on the EdmType get the default value for the property, which are stored in a static map add property to the 
         * list of properties 
         */
        if (type != null && defaultValuesMap.get(type.getFullyQualifiedTypeName()) != null
            && keyValueMap.get(edmProperty.getName()) == null) {
          props.add(OProperties.simple(edmProperty.getName(), defaultValuesMap.get(type.getFullyQualifiedTypeName())));
        }
      }
    }

    OEntity entity = OEntities.createRequest(entitySet, props, null);
    return entity;
  }

  @Override
  public OEntity updateMediaLinkEntry(ODataContext odataContext, OEntity mle, OutputStream outStream) {
    return mle;
  }

  @Override
  public String getMediaLinkContentDisposition(ODataContext odataContext, OEntity mle) {
    return null;
  }

}
