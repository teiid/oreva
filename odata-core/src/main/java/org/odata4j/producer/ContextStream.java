package org.odata4j.producer;

import java.io.InputStream;

/**
 * The Class ContextStream.
 */
public class ContextStream {

  /** The input stream. */
  private InputStream inputStream;

  /** The content-type. */
  private String contentType;

  /** The content-disposition. */
  private String contentDisposition;

  /**
   * Instantiates a new context entity.
   * 
   * @param inputStream
   *            the input stream
   * @param contentType
   *            the content type
   * @param contentDisposition
   *            the content disposition
   */
  public ContextStream(InputStream inputStream, String contentType, String contentDisposition) {
    this.inputStream = inputStream;
    this.contentType = contentType;
    this.contentDisposition = contentDisposition;
  }

  /**
   * @return the inputStream
   */
  public InputStream getInputStream() {
    return inputStream;
  }

  /**
   * @param inputStream
   *            the inputStream to set
   */
  public void setInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  /**
   * @return the contentType
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * @param contentType
   *            the contentType to set
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  /**
   * @return the contentDisposition
   */
  public String getContentDisposition() {
    return contentDisposition;
  }

  /**
   * @param contentDisposition
   *            the contentDisposition to set
   */
  public void setContentDisposition(String contentDisposition) {
    this.contentDisposition = contentDisposition;
  }

}


