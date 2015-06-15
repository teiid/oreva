package org.odata4j.producer.resources;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.odata4j.core.ODataConstants;
import org.odata4j.format.writer.BufferOrFileResponseHolder;

import com.sun.jersey.core.util.ReaderWriter;

@Provider
@Produces({ ODataConstants.APPLICATION_ATOM_XML_CHARSET_UTF8, ODataConstants.APPLICATION_XML_CHARSET_UTF8, ODataConstants.APPLICATION_JAVASCRIPT_VERBOSE_CHARSET_UTF8, ODataConstants.TEXT_JAVASCRIPT_CHARSET_UTF8 })
public class ODataWriteResponseProvider implements MessageBodyWriter<BufferOrFileResponseHolder> {

  /**
   * Called before writeTo to ascertain the length in bytes of the serialized form of responseHolder.
   * A non-negative return value is used in a HTTP Content-Length header<br><br>
   * 
   * We always return the value of -1 so that it is not set in the content header.
   *
   * @param responseHolder - the response holder which is the instance to write
   * @param type - the class of object that is to be written.
   * @param genericType - the type of object to be written, obtained either by reflection of a resource method return type or by inspection of the returned instance. 
   * @param annotations - an array of the annotations on the resource method that returns the object.
   * @param mediaType - the media type of the HTTP entity. 
   * @return the size
   */
  @Override
  public long getSize(BufferOrFileResponseHolder responseHolder, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  /**
   * Ascertain if the MessageBodyWriter supports a particular type. 
   *
   * @param type - the class of object that is to be written.
   * @param genericType - the type of object to be written, obtained either by reflection of a resource method return type or by inspection of the returned instance. 
   * @param annotations - an array of the annotations on the resource method that returns the object.
   * @param mediaType - the media type of the HTTP entity. 
   * @return true, if is writeable
   */
  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return BufferOrFileResponseHolder.class.isAssignableFrom(type);
  }

  /**
   * Write a type to an HTTP response. The response header map is mutable but any changes must be made before writing to the output stream 
   * since the headers will be flushed prior to writing the response body. 
   *
   * @param responseHolder - the response holder which is the instance to write
   * @param type - the class of object that is to be written.
   * @param genericType - the type of object to be written, obtained either by reflection of a resource method return type or by inspection of the returned instance. 
   * @param annotations - an array of the annotations on the resource method that returns the object.
   * @param mediaType - the media type of the HTTP entity. 
   * @param valueMap - a mutable map of the HTTP response headers
   * @param outputStream - the OutputStream for the HTTP entity. The implementation should not close the output stream. 
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws WebApplicationException the web application exception
   */
  @Override
  public void writeTo(BufferOrFileResponseHolder responseHolder, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> valueMap, OutputStream outputStream) throws IOException, WebApplicationException {
    if (responseHolder.isFileUsedForStorage()) {
      FileInputStream fileInputStream = null;
      try {
        fileInputStream = new FileInputStream(responseHolder.getResponseFile());
        //The data is written to the output stream provided by the Jersey hook into MessageBodyWriter
        ReaderWriter.writeTo(fileInputStream, outputStream);
        //Flush any remaining data held in the writer
        outputStream.flush();
      } finally {
        //Close the input stream to the file
        if (fileInputStream != null) {
          fileInputStream.close();
          fileInputStream = null;
        }
        // Delete the temp file that was created
        if (responseHolder.getResponseFile() != null && responseHolder.getResponseFile().exists()) {
          responseHolder.getResponseFile().delete();
        }
      }
    } else {
      //Write contents of buffer to the outputstream
      ReaderWriter.writeToAsString(responseHolder.getBuffer().toString(), outputStream, mediaType);
    }
  }
}
