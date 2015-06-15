package org.odata4j.format;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.odata4j.edm.EdmDataServices;
import org.odata4j.exceptions.NotImplementedException;
import org.odata4j.format.json.JsonCollectionFormatWriter;
import org.odata4j.format.json.JsonComplexObjectFormatWriter;
import org.odata4j.format.json.JsonEntryFormatWriter;
import org.odata4j.format.json.JsonErrorFormatWriter;
import org.odata4j.format.json.JsonFeedFormatWriter;
import org.odata4j.format.json.JsonParametersFormatWriter;
import org.odata4j.format.json.JsonPropertyFormatWriter;
import org.odata4j.format.json.JsonRequestEntryFormatWriter;
import org.odata4j.format.json.JsonServiceDocumentFormatWriter;
import org.odata4j.format.json.JsonSimpleFormatWriter;
import org.odata4j.format.json.JsonSingleLinkFormatWriter;
import org.odata4j.format.json.JsonSingleLinksFormatWriter;
import org.odata4j.format.jsonlite.JsonLiteCollectionFormatWriter;
import org.odata4j.format.jsonlite.JsonLiteComplexObjectFormatWriter;
import org.odata4j.format.jsonlite.JsonLiteEntryFormatWriter;
import org.odata4j.format.jsonlite.JsonLiteErrorFormatWriter;
import org.odata4j.format.jsonlite.JsonLiteFeedFormatWriter;
import org.odata4j.format.jsonlite.JsonLiteParametersFormatWriter;
import org.odata4j.format.jsonlite.JsonLitePropertyFormatWriter;
import org.odata4j.format.jsonlite.JsonLiteRequestEntryFormatWriter;
import org.odata4j.format.jsonlite.JsonLiteServiceDocumentFormatWriter;
import org.odata4j.format.jsonlite.JsonLiteSimpleFormatWriter;
import org.odata4j.format.jsonlite.JsonLiteSingleLinkFormatWriter;
import org.odata4j.format.jsonlite.JsonLiteSingleLinksFormatWriter;
import org.odata4j.format.jsonlite.OdataJsonLiteConstant;
import org.odata4j.format.xml.AtomCollectionFormatWriter;
import org.odata4j.format.xml.AtomComplexFormatWriter;
import org.odata4j.format.xml.AtomEntryFormatWriter;
import org.odata4j.format.xml.AtomErrorFormatWriter;
import org.odata4j.format.xml.AtomFeedFormatWriter;
import org.odata4j.format.xml.AtomRequestEntryFormatWriter;
import org.odata4j.format.xml.AtomServiceDocumentFormatWriter;
import org.odata4j.format.xml.AtomSimpleFormatWriter;
import org.odata4j.format.xml.AtomSingleLinkFormatWriter;
import org.odata4j.format.xml.AtomSingleLinksFormatWriter;
import org.odata4j.format.xml.XmlPropertyFormatWriter;
import org.odata4j.producer.CollectionResponse;
import org.odata4j.producer.ComplexObjectResponse;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ErrorResponse;
import org.odata4j.producer.PropertyResponse;
import org.odata4j.producer.SimpleResponse;

public class FormatWriterFactory {

  private static interface FormatWriters {

    FormatWriter<EdmDataServices> getServiceDocumentFormatWriter();

    FormatWriter<EntitiesResponse> getFeedFormatWriter();

    FormatWriter<EntityResponse> getEntryFormatWriter();

    FormatWriter<PropertyResponse> getPropertyFormatWriter();

    FormatWriter<SimpleResponse> getSimpleFormatWriter();

    FormatWriter<Entry> getRequestEntryFormatWriter();

    FormatWriter<Parameters> getRequestParametersFormatWriter();

    FormatWriter<SingleLink> getSingleLinkFormatWriter();

    FormatWriter<SingleLinks> getSingleLinksFormatWriter();

    FormatWriter<ComplexObjectResponse> getComplexObjectFormatWriter();

    FormatWriter<CollectionResponse<?>> getCollectionFormatWriter();

    FormatWriter<ErrorResponse> getErrorFormatWriter();
  }

  @SuppressWarnings("unchecked")
  public static <T> FormatWriter<T> getFormatWriter(Class<T> targetType, List<MediaType> acceptTypes, String format, String callback) {

    FormatType type = null;

    // if format is explicitly specified, use that
    if (format != null)
      type = FormatType.parse(format);

    // if header accepts json, use that
    if (type == null && acceptTypes != null) {
      for (MediaType acceptType : acceptTypes) {
        if (acceptType.getType().equals(MediaType.APPLICATION_JSON_TYPE.getType()) &&
            acceptType.getSubtype().equals(MediaType.APPLICATION_JSON_TYPE.getSubtype())) {
          Map<String, String> parameters = acceptType.getParameters();
          if (parameters.containsValue(OdataJsonLiteConstant.VERBOSE_VALUE)) {
            type = FormatType.JSONVERBOSE;
            break;
          }
          else if (parameters.containsValue(OdataJsonLiteConstant.METADATA_TYPE_FULLMETADATA)) {
            type = FormatType.JSONLITEFULLMETADATA;
            break;
          }
          else if (parameters.containsValue(OdataJsonLiteConstant.METADATA_TYPE_NOMETADATA)) {
            type = FormatType.JSONLITENOMETADATA;
            break;
          }
          else {
            type = FormatType.JSON;
            break;
          }
        }
      }
    }

    // else default to atom
    if (type == null)
      type = FormatType.ATOM;

    // Function calls must use JSON verbose for atom
    if (targetType.equals(Parameters.class)) {
      if (type.equals(FormatType.ATOM))
        type = FormatType.JSONVERBOSE;
    }
    // We will be treating json-lite as default format type which will return minimal metadata, $format=json or jsonlite 
    // Also we are supporting json-verbose format which can be accessed using $format=jsonverbose or verbosejson
    FormatWriters formatWriters;
    if (type.equals(FormatType.JSON)) {
      formatWriters = new JsonLiteWriters(callback, OdataJsonLiteConstant.METADATA_TYPE_MINIMALMETADATA);
    }
    else if (type.equals(FormatType.JSONLITEFULLMETADATA)) {
      formatWriters = new JsonLiteWriters(callback, OdataJsonLiteConstant.METADATA_TYPE_FULLMETADATA);
    }
    else if (type.equals(FormatType.JSONLITENOMETADATA)) {
      formatWriters = new JsonLiteWriters(callback, OdataJsonLiteConstant.METADATA_TYPE_NOMETADATA);
    }
    else if (type.equals(FormatType.JSONVERBOSE)) {
      formatWriters = new JsonVerboseWriters(callback);
    } else {
      formatWriters = new AtomWriters();
    }

    if (targetType.equals(EdmDataServices.class))
      return (FormatWriter<T>) formatWriters.getServiceDocumentFormatWriter();

    if (targetType.equals(EntitiesResponse.class))
      return (FormatWriter<T>) formatWriters.getFeedFormatWriter();

    if (targetType.equals(EntityResponse.class))
      return (FormatWriter<T>) formatWriters.getEntryFormatWriter();

    if (targetType.equals(PropertyResponse.class))
      return (FormatWriter<T>) formatWriters.getPropertyFormatWriter();

    if (targetType.equals(SimpleResponse.class))
      return (FormatWriter<T>) formatWriters.getSimpleFormatWriter();

    if (Entry.class.isAssignableFrom(targetType))
      return (FormatWriter<T>) formatWriters.getRequestEntryFormatWriter();

    if (SingleLink.class.isAssignableFrom(targetType))
      return (FormatWriter<T>) formatWriters.getSingleLinkFormatWriter();

    if (SingleLinks.class.isAssignableFrom(targetType))
      return (FormatWriter<T>) formatWriters.getSingleLinksFormatWriter();

    if (targetType.equals(ComplexObjectResponse.class))
      return (FormatWriter<T>) formatWriters.getComplexObjectFormatWriter();

    if (targetType.equals(CollectionResponse.class))
      return (FormatWriter<T>) formatWriters.getCollectionFormatWriter();

    if (targetType.equals(ErrorResponse.class))
      return (FormatWriter<T>) formatWriters.getErrorFormatWriter();

    if (targetType.equals(Parameters.class))
      return (FormatWriter<T>) formatWriters.getRequestParametersFormatWriter();

    throw new IllegalArgumentException("Unable to locate format writer for " + targetType.getName() + " and format " + type);

  }

  public static class JsonVerboseWriters implements FormatWriters {

    private final String callback;

    public JsonVerboseWriters(String callback) {
      this.callback = callback;
    }

    //TODO: check the metadata type and then provide appropriate writter
    @Override
    public FormatWriter<EdmDataServices> getServiceDocumentFormatWriter() {
      return new JsonServiceDocumentFormatWriter(callback);
    }

    @Override
    public FormatWriter<EntitiesResponse> getFeedFormatWriter() {
      return new JsonFeedFormatWriter(callback);
    }

    @Override
    public FormatWriter<EntityResponse> getEntryFormatWriter() {
      return new JsonEntryFormatWriter(callback);
    }

    @Override
    public FormatWriter<PropertyResponse> getPropertyFormatWriter() {
      return new JsonPropertyFormatWriter(callback);
    }

    @Override
    public FormatWriter<SimpleResponse> getSimpleFormatWriter() {
      return new JsonSimpleFormatWriter(callback);
    }

    @Override
    public FormatWriter<Entry> getRequestEntryFormatWriter() {
      return new JsonRequestEntryFormatWriter(callback);
    }

    @Override
    public FormatWriter<SingleLink> getSingleLinkFormatWriter() {
      return new JsonSingleLinkFormatWriter(callback);
    }

    @Override
    public FormatWriter<SingleLinks> getSingleLinksFormatWriter() {
      return new JsonSingleLinksFormatWriter(callback);
    }

    @Override
    public FormatWriter<ComplexObjectResponse> getComplexObjectFormatWriter() {
      return new JsonComplexObjectFormatWriter(callback);
    }

    @Override
    public FormatWriter<CollectionResponse<?>> getCollectionFormatWriter() {
      return new JsonCollectionFormatWriter(callback);
    }

    @Override
    public FormatWriter<ErrorResponse> getErrorFormatWriter() {
      return new JsonErrorFormatWriter(callback);
    }

    @Override
    public FormatWriter<Parameters> getRequestParametersFormatWriter() {
      return new JsonParametersFormatWriter(callback);
    }
  }

  public static class AtomWriters implements FormatWriters {

    @Override
    public FormatWriter<EdmDataServices> getServiceDocumentFormatWriter() {
      return new AtomServiceDocumentFormatWriter();
    }

    @Override
    public FormatWriter<EntitiesResponse> getFeedFormatWriter() {
      return new AtomFeedFormatWriter();
    }

    @Override
    public FormatWriter<EntityResponse> getEntryFormatWriter() {
      return new AtomEntryFormatWriter();
    }

    @Override
    public FormatWriter<PropertyResponse> getPropertyFormatWriter() {
      return new XmlPropertyFormatWriter();
    }

    @Override
    public FormatWriter<Entry> getRequestEntryFormatWriter() {
      return new AtomRequestEntryFormatWriter();
    }

    @Override
    public FormatWriter<SingleLink> getSingleLinkFormatWriter() {
      return new AtomSingleLinkFormatWriter();
    }

    @Override
    public FormatWriter<SingleLinks> getSingleLinksFormatWriter() {
      return new AtomSingleLinksFormatWriter();
    }

    @Override
    public FormatWriter<ComplexObjectResponse> getComplexObjectFormatWriter() {
      return new AtomComplexFormatWriter();
    }

    @Override
    public FormatWriter<CollectionResponse<?>> getCollectionFormatWriter() {
      return new AtomCollectionFormatWriter();
    }

    @Override
    public FormatWriter<SimpleResponse> getSimpleFormatWriter() {
      return new AtomSimpleFormatWriter();
    }

    @Override
    public FormatWriter<ErrorResponse> getErrorFormatWriter() {
      return new AtomErrorFormatWriter();
    }

    @Override
    public FormatWriter<Parameters> getRequestParametersFormatWriter() {
      throw new NotImplementedException("The ATOM format for function payload is not implemented");
    }
  }

  public static class JsonLiteWriters implements FormatWriters {
    private final String callback;
    private String metadataType;

    public JsonLiteWriters(String callback) {
      this.callback = callback;
    }

    public JsonLiteWriters(String callback, String metadataType) {
      this.callback = callback;
      this.metadataType = metadataType;
    }

    @Override
    public FormatWriter<EdmDataServices> getServiceDocumentFormatWriter() {
      return new JsonLiteServiceDocumentFormatWriter(callback, metadataType);
    }

    @Override
    public FormatWriter<EntitiesResponse> getFeedFormatWriter() {
      return new JsonLiteFeedFormatWriter(callback, metadataType);
    }

    @Override
    public FormatWriter<EntityResponse> getEntryFormatWriter() {
      return new JsonLiteEntryFormatWriter(callback, metadataType);
    }

    @Override
    public FormatWriter<PropertyResponse> getPropertyFormatWriter() {
      return new JsonLitePropertyFormatWriter(callback, metadataType);
    }

    @Override
    public FormatWriter<SimpleResponse> getSimpleFormatWriter() {
      return new JsonLiteSimpleFormatWriter(callback, metadataType);
    }

    @Override
    public FormatWriter<Entry> getRequestEntryFormatWriter() {
      return new JsonLiteRequestEntryFormatWriter(callback, metadataType);
    }

    @Override
    public FormatWriter<SingleLink> getSingleLinkFormatWriter() {
      return new JsonLiteSingleLinkFormatWriter(callback, metadataType);
    }

    @Override
    public FormatWriter<SingleLinks> getSingleLinksFormatWriter() {
      return new JsonLiteSingleLinksFormatWriter(callback, metadataType);
    }

    @Override
    public FormatWriter<ComplexObjectResponse> getComplexObjectFormatWriter() {
      return new JsonLiteComplexObjectFormatWriter(callback, metadataType);
    }

    @Override
    public FormatWriter<CollectionResponse<?>> getCollectionFormatWriter() {
      return new JsonLiteCollectionFormatWriter(callback, metadataType);
    }

    @Override
    public FormatWriter<ErrorResponse> getErrorFormatWriter() {
      return new JsonLiteErrorFormatWriter(callback, metadataType);
    }

    @Override
    public FormatWriter<Parameters> getRequestParametersFormatWriter() {
      return new JsonLiteParametersFormatWriter(callback, metadataType);
    }
  }

}
