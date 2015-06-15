package org.odata4j.format.writer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.ws.rs.core.MediaType;

import org.odata4j.core.ODataConstants;
import org.odata4j.internal.InternalUtil;

import com.sun.jersey.core.util.ReaderWriter;

public class BufferOrFileResponseWriter extends Writer {

  private BufferOrFileResponseHolder responseHolder = new BufferOrFileResponseHolder();
  private Writer responseWriter = null;
  private MediaType mediaType;
  private final String JBOSS_TEMP_LOC = "jboss.server.temp.dir";

  public BufferOrFileResponseWriter(String contentType) {
    if (contentType != null && MediaType.valueOf(contentType) != null) {
      this.mediaType = MediaType.valueOf(contentType);
    }
  }

  /**
   * Write response data to holder.
   *
   * @param content the content
   */
  private void writeResponseDataToHolder(String content) {
    if (this.responseHolder.isFileUsedForStorage() && !this.responseHolder.isErrorWhileWritingToFile()) {
      this.writeDataToFile(content);
    } else {
      this.writeDataToBuffer(content);
    }
  }

  /**
   * Write data to file.
   *
   * @param content the content
   */
  private void writeDataToFile(String content) {
    try {

      if (this.responseWriter == null) {
        File tempFile = File.createTempFile(BufferOrFileResponseHolder.TEMP_FILE_PREFIX, null, this.getDirectoryLocation());
        // Mark the file to delete on VM shutdown if it wasn't deleted already
        tempFile.deleteOnExit();

        this.responseHolder.setResponseFile(tempFile);
        this.responseWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.responseHolder.getResponseFile()),
            ReaderWriter.getCharset(this.mediaType)));
        this.responseHolder.transferBufferContentsToFile(content, this.responseWriter);
      }
      this.responseWriter.write(content);

    } catch (IOException ioException) {
      // Transfer contents of file to buffer if there was a midway error
      this.transferFileContentsToBufferOnError();
      // Let the holder know that there was a error writing to file so that we don't write to file any longer
      this.responseHolder.setErrorWhileWritingToFile(true);
      // If we get an exception writing to file we continue appending the data to the buffer 
      this.responseHolder.getBuffer().append(content);
    }
  }

  /**
   * Write data to buffer.
   *
   * @param content the content
   */
  private void writeDataToBuffer(String content) {

    // If we are past the buffer threshold then we transfer the contents of the buffer to the file and start writing to the file
    if ((this.responseHolder.getBuffer().length() + content.length()) > this.responseHolder.getBufferThresholdSize()) {
      try {
        //Create a temporary file to hold the response data
        File tempFile = File.createTempFile(BufferOrFileResponseHolder.TEMP_FILE_PREFIX, null, this.getDirectoryLocation());
        // Mark the file to delete on VM shutdown if it wasn't deleted already
        tempFile.deleteOnExit();

        this.responseHolder.setResponseFile(tempFile);
        this.responseWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.responseHolder.getResponseFile()),
            ReaderWriter.getCharset(this.mediaType)));
        this.responseHolder.transferBufferContentsToFile(content, this.responseWriter);
      } catch (IOException e) {
        // Transfer contents of file to buffer if there was a midway error
        this.transferFileContentsToBufferOnError();
        // Let the holder know that there was a error writing to file so that we don't write to file any longer
        this.responseHolder.setErrorWhileWritingToFile(true);
        // If we get an exception writing to file we continue appending the data to the buffer 
        this.responseHolder.getBuffer().append(content);
      }
    } else {
      // Means that the buffer is still not exhausted
      this.responseHolder.getBuffer().append(content);
    }
  }

  /**
   * Transfer file contents to buffer if there was an error writing to the file.
   */
  private void transferFileContentsToBufferOnError() {
    BufferedReader bufferedReader = null;
    int i;
    if (this.responseHolder.getResponseFile().length() != this.responseHolder.getBuffer().length()) {
      try {
        this.responseHolder.setBuffer(new StringBuilder(ODataConstants.COPY_BUFFER_SIZE));
        bufferedReader = new BufferedReader(new FileReader(this.responseHolder.getResponseFile()));
        while ((i = bufferedReader.read()) != -1) {
          this.responseHolder.getBuffer().append(i);
        }
      } catch (FileNotFoundException e) {

      } catch (IOException e) {

      }
    }
  }

  /*
   * (non-Javadoc)
   * @see java.io.Writer#write(char[], int, int)
   */
  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    if ((off < 0) || (off > cbuf.length) || (len < 0) ||
        ((off + len) > cbuf.length) || ((off + len) < 0)) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return;
    }
    String content = new String(cbuf);
    this.writeResponseDataToHolder(content.substring(off, off + len));
  }

  /* (non-Javadoc)
   * @see java.io.Writer#write(int)
   */
  @Override
  public void write(int c) throws IOException {
    char[] cbuf = { (char) c };
    this.writeResponseDataToHolder(new String(cbuf));
  }

  /* (non-Javadoc)
   * @see java.io.Writer#write(char[])
   */
  @Override
  public void write(char[] cbuf) throws IOException {
    this.writeResponseDataToHolder(new String(cbuf));
  }

  /* (non-Javadoc)
   * @see java.io.Writer#write(java.lang.String)
   */
  @Override
  public void write(String str) throws IOException {
    this.writeResponseDataToHolder(str);
  }

  /* (non-Javadoc)
   * @see java.io.Writer#write(java.lang.String, int, int)
   */
  @Override
  public void write(String str, int off, int len) throws IOException {
    this.writeResponseDataToHolder(str.substring(off, off + len));
  }

  /* (non-Javadoc)
   * @see java.io.Writer#append(java.lang.CharSequence)
   */
  @Override
  public Writer append(CharSequence csq) throws IOException {
    CharSequence cs = (csq == null ? "null" : csq);
    this.write(cs.toString());
    return this;
  }

  /* (non-Javadoc)
   * @see java.io.Writer#append(java.lang.CharSequence, int, int)
   */
  @Override
  public Writer append(CharSequence csq, int start, int end) throws IOException {
    CharSequence cs = (csq == null ? "null" : csq);
    this.write(cs.subSequence(start, end).toString());
    return this;
  }

  /* (non-Javadoc)
   * @see java.io.Writer#append(char)
   */
  @Override
  public Writer append(char c) throws IOException {
    this.write(c);
    return this;
  }

  /**
   * If we were writing to a file, then calling the flush() method will flush out any data held in the buffered writer to the file.
   */
  @Override
  public void flush() throws IOException {
    if (this.responseWriter != null) {
      this.responseWriter.flush();
    }
  }

  /**
   * If we were writing to a file, then calling the close method will flush and close the writer.<br><br>
   * 
   * Subsequent invocations on the {@link BufferOrFileResponseHolder} object should get the file and open a {@link FileReader} to navigate the contents of the file.
   */
  @Override
  public void close() throws IOException {
    if (this.responseWriter != null) {
      this.responseWriter.flush();
      this.responseWriter.close();
      this.responseWriter = null;
    }
  }

  /**
   * @return the responseHolder
   */
  public BufferOrFileResponseHolder getResponseHolder() {
    return responseHolder;
  }

  /**
   * Gets the directory location.
   *
   * @return the directory location
   */
  private File getDirectoryLocation() {
    String jbossTempLocation = InternalUtil.getSystemPropertyValue(JBOSS_TEMP_LOC);
    File directory = null;
    if (jbossTempLocation != null) {
      directory = new File(jbossTempLocation + File.separator + "dsds");
      if (!directory.exists()) {
        //create directory
        directory.mkdir();
      }
    }
    return directory;
  }
}
