/**
 * 
 */
package org.odata4j.producer.resources;

/**
 * 
 * Thread Local to hold the isBatchProcess variable value. "true" in case of batch process request ($batch).
 * 
 * Copyright 2013 Halliburton
 * @author <a href="mailto:amit.jahagirdar@synerzip.com">amit.jahagirdar</a>
 * 
 */
public class BatchProcessThreadLocal {

  /** The Constant BATCH_PROCESS_DIFFERENTIATOR. */
  private static final ThreadLocal<Boolean> BATCH_PROCESS_DIFFERENTIATOR = new ThreadLocal<Boolean>();

  /**
   * Checks if is batch process.
   * 
   * @return the boolean
   */
  public static Boolean isBatchProcess() {
    return BATCH_PROCESS_DIFFERENTIATOR.get();
  }

  /**
   * Sets the batch process flag.
   * 
   * @param flag
   *            the new batch process flag
   */
  public static void setBatchProcessFlag(Boolean flag) {
    BATCH_PROCESS_DIFFERENTIATOR.set(flag);
  }

}
