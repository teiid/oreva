package org.odata4j.producer.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;

import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.exceptions.NotFoundException;
import org.odata4j.exceptions.NotImplementedException;
import org.odata4j.internal.InternalUtil;
import org.odata4j.producer.EntityQueryInfo;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataContext;
import org.odata4j.producer.ODataContextImpl;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.OMediaLinkExtension;

public class ValueRequestResource extends BaseResource {	

  @GET
  public Response get(
      @Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo,
      @Context ContextResolver<ODataProducer> producerResolver,
      @Context SecurityContext securityContext,
      @PathParam("entitySetName") String entitySetName,
      @PathParam("id") String id,
      @QueryParam("$expand") String expand,
      @QueryParam("$select") String select) {
    ODataProducer producer = producerResolver.getContext(ODataProducer.class);
    EdmEntitySet entitySet = producer.getMetadata().findEdmEntitySet(entitySetName);

    if (entitySet != null && entitySet.getType().getHasStream()) {
      ODataContext odataContext = ODataContextImpl.builder()
          .aspect(httpHeaders)
          .aspect(securityContext)
          .aspect(producer)
          .aspect(entitySet)
          .aspect(uriInfo)
          .build();

      return getStreamResponse(httpHeaders, uriInfo, producer, entitySet, id, new EntityQueryInfo(
          null,
          OptionsQueryParser.parseCustomOptions(uriInfo),
          OptionsQueryParser.parseExpand(expand),
          OptionsQueryParser.parseSelect(select)),
          securityContext,
          odataContext);
    }
    throw new NotFoundException();
  }

  protected Response getStreamResponse(HttpHeaders httpHeaders, UriInfo uriInfo, ODataProducer producer, EdmEntitySet entitySet, String entityId, EntityQueryInfo queryInfo,
      SecurityContext securityContext, ODataContext odataContext) {

    // this is from new odata4j 0.8, 
    // OMediaLinkExtension mediaLinkExtension = this.getMediaLinkExtension(httpHeaders, uriInfo, entitySet, producer, odataContext);
    OMediaLinkExtension mediaLinkExtension = producer.findExtension(OMediaLinkExtension.class);
    if (mediaLinkExtension == null)
      throw new NotImplementedException();

    EntityResponse entityResponse = producer.getEntity(odataContext,
        entitySet.getName(), OEntityKey.parse(entityId), queryInfo);
    InputStream entityStream = mediaLinkExtension.getInputStreamForMediaLinkEntry(odataContext, entityResponse.getEntity(), null, queryInfo);
    StreamingOutput outputStream = getOutputStreamFromInputStream(entityStream);
    String contentType = mediaLinkExtension.getMediaLinkContentType(odataContext, entityResponse.getEntity());
    String contentDisposition = mediaLinkExtension.getMediaLinkContentDisposition(odataContext, entityResponse.getEntity());

    // this is from latest odata4j code, why we choose outputStream?
    //return Response.ok(entityStream, contentType).header("Content-Disposition", contentDisposition).build();

    return Response.ok(outputStream, contentType).header("Content-Disposition", contentDisposition).build();
  }

  /**
   * Gets the output stream from input stream which will only be called when the client starts reading the stream.
   *
   * @param inputStream the input stream
   * @return the output stream from input stream
   */
  protected static StreamingOutput getOutputStreamFromInputStream(
      final InputStream inputStream) {
    final StreamingOutput outputStream = new StreamingOutput() {
      public void write(OutputStream out) throws IOException,
          WebApplicationException {
        try {
          copyInputToOutput(inputStream, out);
        } catch (IOException e) {
          // do nothing
        } finally {
          // close output stream which was flushed earlier
          out.close();
          // close the input stream for media column, fix NPE
          if (inputStream != null) {
            inputStream.close();
          }
        }
      }
    };
    return outputStream;
  }

   /**
   * Gets the output stream from input stream which will only be called when the client starts reading the stream.
   *
   * @param inputStream the input stream
   * @return the output stream from input stream
   */
  private static void copyInputToOutput(InputStream inStream, OutputStream outStream) throws IOException{
    // fix NPE
    if (inStream == null) {
      return;
    }
	  byte[] buf = new byte[setStreamBufferSize()];
	    int n;
	    try {
			while ((n = inStream.read(buf)) != -1) {
			  outStream.write(buf, 0, n);
			}
		} catch (IOException e) {
			throw e;
		}
  }
  
  /**
   * Gets the  stream buffer size for reading input stream which will only be called when the client starts reading the stream.
   *   
   * @return the output stream from input stream
   */
  private static Integer setStreamBufferSize(){
	  Integer defaultStreamBuffsize = ODataConstants.COPY_BUFFER_SIZE;
	  //Property is read from the dsdsAppConfig.properties.
	  String streamBufferSize = InternalUtil.getSystemPropertyValue(ODataConstants.ODATA_STREAM_BUFFER_SIZE);
	  if (streamBufferSize != null && !streamBufferSize.isEmpty()) {
	    try {
	      int convertedSysVarValue = Integer.parseInt(streamBufferSize);
	      //The value passed on the system variable is in MB and we need to convert it to bytes
	      defaultStreamBuffsize = convertedSysVarValue * 1024 * 1024;
	    } catch (NumberFormatException numFormatException) {
	      // We ignore the exception and use default;
	    }
	  }
	  
	  return defaultStreamBuffsize;
  }

}