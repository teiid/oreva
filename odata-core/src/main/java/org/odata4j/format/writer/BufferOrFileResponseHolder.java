package org.odata4j.format.writer;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.odata4j.core.ODataConstants;
import org.odata4j.internal.InternalUtil;

/**
 * The BufferOrFileResponseHolder class holds the response received from the producer. Since the response can be fairly large, it can store the response 
 * in the buffer if the size of the response is less than the threshold. If the size of the response overshoots the threshold then we start writing the 
 * contents to file.<br><br>
 * 
 * Copyright 2013 Halliburton
 * 
 * @author <a href="mailto:Anil.Allewar2@halliburton.com">Anil Allewar</a>
 */
public class BufferOrFileResponseHolder {

  /** The Constant TEMP_FILE_PREFIX. */
  public static final String TEMP_FILE_PREFIX = "odataresp_";

  /** The buffer threshold size. */
  private Integer bufferThresholdSize;

  /** 
   * The buffer. We use a StringBuilder as one thread servicing the HTTP request will initialize instance of this class and use it. 
   * Instantiate the StringBuffer always with initial capacity of 8KB 
   */
  private StringBuilder buffer = new StringBuilder(ODataConstants.COPY_BUFFER_SIZE);

  /** The temp file. */
  private File responseFile;

  /** The file used for storage boolean. */
  private boolean fileUsedForStorage;

  /** The error while writing to file boolean. */
  private boolean errorWhileWritingToFile;

  /**
   * Instantiates a new buffer or file response holder and sets the buffer threshold size.
   */
  public BufferOrFileResponseHolder() {
    this.setBufferThresholdSize();
  }

  /**
  * @return the buffer
  */
  public StringBuilder getBuffer() {
    return buffer;
  }

  /**
   * @param buffer the buffer to set
   */
  public void setBuffer(StringBuilder buffer) {
    this.buffer = buffer;
  }

  /**
   * @return the responseFile
   */
  public File getResponseFile() {
    return responseFile;
  }

  /**
   * @param responseFile the responseFile to set
   */
  public void setResponseFile(File responseFile) {
    this.responseFile = responseFile;
  }

  /**
   * @return the fileUsedForStorage
   */
  public boolean isFileUsedForStorage() {
    return fileUsedForStorage;
  }

  /**
   * @return the bufferThresholdSize
   */
  public Integer getBufferThresholdSize() {
    return bufferThresholdSize;
  }

  /**
   * @return the errorWhileWritingToFile
   */
  public boolean isErrorWhileWritingToFile() {
    return errorWhileWritingToFile;
  }

  /**
   * @param errorWhileWritingToFile the errorWhileWritingToFile to set
   */
  public void setErrorWhileWritingToFile(boolean errorWhileWritingToFile) {
    this.errorWhileWritingToFile = errorWhileWritingToFile;
  }

  /**
   * Sets the buffer threshold size.
   */
  private void setBufferThresholdSize() {
    Integer bufferThresholdSize = ODataConstants.DEFAULT_BUFFER_THRESHOLD_LIMIT;
    String thresholdSizeSysVarValue = InternalUtil.getSystemPropertyValue(ODataConstants.ODATA_WRITER_THRESHOLD_SIZE);
    if (thresholdSizeSysVarValue != null && !thresholdSizeSysVarValue.isEmpty()) {
      try {
        int convertedSysVarValue = Integer.parseInt(thresholdSizeSysVarValue);
        //The value passed on the system variable is in MB and we need to convert it to bytes
        bufferThresholdSize = convertedSysVarValue * 1024 * 1024;
      } catch (NumberFormatException numFormatException) {
        // We ignore the exception and use default;
      }
    }
    //Set the threshold size
    this.bufferThresholdSize = bufferThresholdSize;
  }

  /**
   * Transfer buffer contents to file.
   *
   * @param str the content to be written out 
   * @param writer the writer which contains reference to the temp file contained in the BufferOrFileResponseHolder
   */
  public synchronized void transferBufferContentsToFile(String str, Writer writer) {
    if ((this.buffer.length() + str.length()) > this.bufferThresholdSize) {
      if (!this.fileUsedForStorage) {
        try {
          // Add the current content to the buffer and write it out to the file
          this.buffer.append(str);
          // We write out character by character so as to not load another copy of the StringBuilder in memory
          for (int i = 0; i < this.buffer.length(); i++) {
            writer.write(this.buffer.charAt(i));
          }
          //Set the boolean to indicate that we now use the file for storing the response
          this.fileUsedForStorage = true;
        } catch (IOException ioException) {
          // We gobble the exception and keep the fileUsedForStorage to false
        }
      }
    }
  }
}
