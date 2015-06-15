package org.odata4j.consumer;

import java.io.Reader;

import org.core4j.Enumerable;
import org.odata4j.core.OAtomStreamEntity;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityGetRequest;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.format.Entry;
import org.odata4j.format.Feed;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.FormatType;
import org.odata4j.format.Settings;
import org.odata4j.internal.EntitySegment;
import org.odata4j.internal.FeedCustomizationMapping;
import org.odata4j.internal.InternalUtil;

/**
 * Get-entity-request implementation.
 */
public class ConsumerGetEntityRequest<T> extends AbstractConsumerEntityRequest<T> implements OEntityGetRequest<T> {

  private final Class<T> entityType;
  private final FeedCustomizationMapping fcMapping;

  private String select;
  private String expand;

  public ConsumerGetEntityRequest(ODataClient client, Class<T> entityType, String serviceRootUri,
      EdmDataServices metadata, String entitySetName, OEntityKey key, FeedCustomizationMapping fcMapping) {
    super(client, serviceRootUri, metadata, entitySetName, key);
    this.entityType = entityType;
    this.fcMapping = fcMapping;
  }

  @Override
  public ConsumerGetEntityRequest<T> select(String select) {
    this.select = select;
    return this;
  }

  @Override
  public ConsumerGetEntityRequest<T> expand(String expand) {
    this.expand = expand;
    return this;
  }

  @Override
  public T execute() throws ODataProducerException {

    ODataClientRequest request = getRequest();

    ODataClientResponse response = getClient().getEntity(request);

    T resultEntity = getResult(response);

    if (resultEntity instanceof OEntity) {
      //  the first segment contains the entitySetName we start from
      EdmEntitySet entitySet = getMetadata().getEdmEntitySet(getSegments().get(0).segment);
      if (Boolean.TRUE.equals(entitySet.getType().getHasStream())) { // getHasStream can return null
        // If we have the stream, then we get the response and store the reference to the stream but we don't read it yet
        // kchen: if a producer does not implement ours, the client will never get the entity back.
    	  
    	OEntity resultOEntity = (OEntity) resultEntity;
    	OAtomStreamEntity stream = resultOEntity.findExtension(OAtomStreamEntity.class);
    	
    	// found stream entity 
    	if(stream != null) {
	        request = getStreamRequest(stream);
	        ODataClientResponse responseStream = getClient().getEntity(request);        
	        resultOEntity.setMediaLinkStream(responseStream.getEntityInputStream());
	        resultOEntity.setMediaTypeForStream(responseStream.getMediaType().toString());
    	}
      }
    }

    return resultEntity;
  }

  private ODataClientRequest getRequest() {
    String path = Enumerable.create(getSegments()).join("/");

    ODataClientRequest request = ODataClientRequest.get(getServiceRootUri() + path);

    if (select != null) {
      request = request.queryParam("$select", select);
    }

    if (expand != null) {
      request = request.queryParam("$expand", expand);
    }

    return request;
  }

  private T getResult(ODataClientResponse response) {
    if (response == null)
      return null;

    ODataVersion version = InternalUtil.getDataServiceVersion(response.getHeaders()
        .getFirst(ODataConstants.Headers.DATA_SERVICE_VERSION));

    T result = getResult(version, getClient().getFeedReader(response), getClient().getFormatType());
    response.close();

    return result;
  }

  private T getResult(ODataVersion version, Reader reader, FormatType formatType) {
    //  the first segment contains the entitySetName we start from
    EdmEntitySet entitySet = getMetadata().getEdmEntitySet(getSegments().get(0).segment);
    for (EntitySegment segment : getSegments().subList(1, getSegments().size())) {
      EdmNavigationProperty navProperty = entitySet.getType().findNavigationProperty(segment.segment);
      entitySet = getMetadata().getEdmEntitySet(navProperty.getToRole().getType());
    }

    OEntityKey key = Enumerable.create(getSegments()).last().key;

    FormatParser<Feed> parser = FormatParserFactory
        .getParser(Feed.class, formatType, new Settings(version, getMetadata(), entitySet.getName(), key, fcMapping));

    Entry entry = Enumerable.create(parser.parse(reader).getEntries())
        .firstOrNull();

    return (T) InternalUtil.toEntity(entityType, entry.getEntity());

  }

  @Override
  public String formatRequest(FormatType formatType) {
    ODataClientRequest request = getRequest();
    return ConsumerBatchRequestHelper.formatSingleRequest(request, formatType);
  }

  @Override
  public Object getResult(ODataVersion version, Object payload, FormatType formatType) {
    Reader reader = getClient().getFeedReader((String) payload);
    return getResult(version, reader, formatType);
  }

  private ODataClientRequest getStreamRequest(OAtomStreamEntity stream) {
     ODataClientRequest request = ODataClientRequest.get(getServiceRootUri() + stream.getAtomEntitySource());

    return request;
  }
}
