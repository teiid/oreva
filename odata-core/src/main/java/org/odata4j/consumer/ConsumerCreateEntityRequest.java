package org.odata4j.consumer;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.core4j.Enumerable;
import org.odata4j.core.OCreateRequest;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OPredicates;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.FormatType;
import org.odata4j.format.Settings;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamTokenizer.JsonTokenType;
import org.odata4j.format.json.JsonTypeConverter;
import org.odata4j.internal.FeedCustomizationMapping;
import org.odata4j.internal.InternalUtil;

/**
 * Create-request implementation.
 */
public class ConsumerCreateEntityRequest<T> extends AbstractConsumerEntityPayloadRequest implements OCreateRequest<T> {

  private static final String DUMMY_STRING_VALUE = "'dummy_string'";
  private static final short DUMMY_VALUE = 1;
  private final ODataClient client;
  private OEntity parent;
  private String navProperty;
  private OEntity entity;

  private final FeedCustomizationMapping fcMapping;

  public ConsumerCreateEntityRequest(ODataClient client, String serviceRootUri, EdmDataServices metadata, String entitySetName, FeedCustomizationMapping fcMapping) {
    super(entitySetName, serviceRootUri, metadata);
    this.client = client;
    this.fcMapping = fcMapping;
  }

  @Override
  public T execute() throws ODataProducerException {

    EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);
    // need call this first, otherwise the default value will not be sent to server.
    AppendDefaultValueForNullField(ees);
    ODataClientRequest request = getRequest();

    ODataClientResponse response = client.createEntity(request);

    // if this is new media link create request then send subsequent merge/get requests
    if (Boolean.TRUE.equals(ees.getType().getHasStream())) {
      String location = response.getHeaders().getFirst(ODataConstants.Headers.LOCATION);

      // send merge request to update rest of the properties
      ODataClientRequest mergeRequest = ODataClientRequest.merge(location, client.createRequestEntry(ees, null, props, links));
      client.updateEntity(mergeRequest);

      // send get request to return response as Oentity which is created.
      ODataClientRequest getRequest = ODataClientRequest.get(location);
      response = client.getEntity(getRequest);
    }
    return getResult(response);
  }

  private void AppendDefaultValueForNullField(EdmEntitySet ees) {
    Enumerable<OProperty<?>> propsEnum = Enumerable.create(props);
    EdmEntityType eType = ees.getType();
    Iterator<EdmProperty> mdProps = eType.getProperties().iterator();
    while (mdProps.hasNext()) {
      EdmProperty prop = mdProps.next();
      // this prop has default value, user does not set it in the request and it is simple type, populate with default value
      if (prop.getDefaultValue() != null && prop.getType().isSimple() && propsEnum.firstOrNull(OPredicates.propertyNameEquals(prop.getName())) == null) {
        OProperty<?> newProp = createPropWithDefaultValue(prop);
        super.properties(this, newProp);
      }
    }
  }

  private OProperty<?> createPropWithDefaultValue(EdmProperty prop) {
    String defaultValue = prop.getDefaultValue();
    EdmSimpleType<?> type = (EdmSimpleType<?>) prop.getType();

    // Trying to re-use the JsonTypeConverter that will convert string to 
    // corresponding types, the JsonTokenType is a required parameter, just
    // fake it here.
    JsonTokenType jsonTokenType = JsonTokenType.STRING;

    if (type == EdmSimpleType.BOOLEAN) {
      jsonTokenType = JsonTokenType.TRUE;
    }
    // The default value should match the data type, if it is a number, then it should not have () to enclose them.
    if (type != null && type == EdmSimpleType.INT16 || type == EdmSimpleType.INT32 || type == EdmSimpleType.INT64
        || type == EdmSimpleType.SINGLE || type == EdmSimpleType.DOUBLE || type == EdmSimpleType.DECIMAL) {
      if (defaultValue != null && defaultValue.length() > 0) {
        if (defaultValue.startsWith("(") && defaultValue.endsWith(")")) {
          defaultValue = defaultValue.replace("(", "").replace(")", "");
        }
      }
    }
    return JsonTypeConverter.parse(prop.getName(), type, defaultValue, jsonTokenType);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T get() {
    EdmEntitySet entitySet = metadata.getEdmEntitySet(entitySetName);
    return (T) OEntities.createRequest(entitySet, props, links);
  }

  @Override
  public OCreateRequest<T> properties(OProperty<?>... props) {
    return super.properties(this, props);
  }

  @Override
  public OCreateRequest<T> properties(Iterable<OProperty<?>> props) {
    return super.properties(this, props);
  }

  @Override
  public OCreateRequest<T> link(String navProperty, OEntity target) {
    return super.link(this, navProperty, target);
  }

  @Override
  public OCreateRequest<T> link(String navProperty, OEntityKey targetKey) {
    return super.link(this, navProperty, targetKey);
  }

  @Override
  public OCreateRequest<T> addToRelation(OEntity parent, String navProperty) {
    if (parent == null || navProperty == null) {
      throw new IllegalArgumentException("please provide the parent and the navProperty");
    }

    this.parent = parent;
    this.navProperty = navProperty;
    return this;
  }

  @Override
  public OCreateRequest<T> inline(String navProperty, OEntity... entities) {
    return super.inline(this, navProperty, entities);
  }

  @Override
  public OCreateRequest<T> inline(String navProperty, Iterable<OEntity> entities) {
    return super.inline(this, navProperty, Enumerable.create(entities).toArray(OEntity.class));
  }

  private ODataClientRequest getRequest() {
    EdmEntitySet ees = metadata.getEdmEntitySet(entitySetName);
    Entry entry = client.createRequestEntry(ees, null, props, links);
    entity = entry.getEntity();
    StringBuilder url = new StringBuilder(serviceRootUri);
    if (parent != null) {
      url.append(InternalUtil.getEntityRelId(parent))
          .append("/")
          .append(navProperty);
    } else {
      url.append(entitySetName);
    }
    ODataClientRequest request;

    if (Boolean.TRUE.equals(ees.getType().getHasStream())) {
      setMediaStream(entity);
      request = new ODataClientRequest("POST", url.toString(), prepareSlugHeaders(ees), null, entity.getMediaLinkStream());
    } else {
      request = ODataClientRequest.post(url.toString(), entry);
    }
    return request;
  }

  /**
   * Checks if media stream is set.
   * Iterates through Oproperties and set mediaLinkStream property in OEntity.
   *
   * @param entity the entity
   * @return the boolean
   */
  private void setMediaStream(OEntity entity) {
    // check whether the stream has been set using Oproperties
    for (OProperty<?> property : props) {
      if (property.getType().equals(EdmSimpleType.STREAM) && property.getValue() != null) {
        // media stream property is set, so set it in oentity.
        try
        {
          if (property.getValue() instanceof Blob) {
            entity.setMediaLinkStream(((Blob) property.getValue()).getBinaryStream());
          } else {
            entity.setMediaLinkStream((InputStream) property.getValue());
          }

        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
        // remove it from props to prevent sending it again for merge.
        props.remove(property);
        break;
      }
    }
  }

  /**
   * Prepare slug headers that needs to be sent along with the post request for media link creation.
   * We need to send the properties which are simple and can not be null.
   *
   * @param ees the ees
   * @return the map
   */
  private Map<String, String> prepareSlugHeaders(EdmEntitySet ees) {
    Map<String, String> headers = new HashMap<String, String>();
    StringBuilder sb = new StringBuilder();
    // Since we can't get the foreign keys from the Edm Entity Set, we set value for all simple properties that are not null
    for (OProperty<?> property : props) {
      if (Boolean.FALSE.equals(ees.getType().findProperty(property.getName()).isNullable()) && property.getValue() != null && property.getType().isSimple()) {
        sb.append(property.getName()).append("=").append(property.getValue().toString()).append(",");
      }
    }
    String str = null;
    if (sb.toString().endsWith(",")) {
      str = sb.substring(0, sb.length() - 1);
    }

    if (str == null) {
      List<String> keys = ees.getType().getKeys();
      for (String key : keys) {
        EdmProperty property = ees.getType().findProperty(key);
        sb.append(property.getName()).append("=").append(DUMMY_VALUE).append(",");
      }
      if (sb.toString().endsWith(",")) {
        str = sb.substring(0, sb.length() - 1);
      }
    }

    headers.put(ODataConstants.Headers.SLUG, str);
    return headers;
  }

  @SuppressWarnings("unchecked")
  private T getResult(ODataClientResponse response) {
    ODataVersion version = InternalUtil.getDataServiceVersion(response.getHeaders()
        .getFirst(ODataConstants.Headers.DATA_SERVICE_VERSION));

    Entry entry = getResult(version, client.getFeedReader(response));
    response.close();

    return (T) entry.getEntity();

  }

  private Entry getResult(ODataVersion version, Reader reader) {
    FormatParser<Entry> parser = FormatParserFactory.getParser(Entry.class,
        client.getFormatType(), new Settings(version, metadata, entitySetName, null, fcMapping));
    Entry entry = parser.parse(reader);

    return entry;
  }

  @Override
  public String formatRequest(FormatType formatType) {
    ODataClientRequest request = getRequest();
    return ConsumerBatchRequestHelper.formatSingleRequest(request, formatType);
  }

  @Override
  public Object getResult(ODataVersion version, Object payload, FormatType formatType) {
    Entry entry = getResult(version, client.getFeedReader((String) payload));
    return entry.getEntity();
  }

}
