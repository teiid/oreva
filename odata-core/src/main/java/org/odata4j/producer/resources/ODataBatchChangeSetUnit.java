package org.odata4j.producer.resources;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ContextResolver;

import org.odata4j.core.Guid;
import org.odata4j.core.ODataConstants;
import org.odata4j.producer.ODataProducer;

/**
 * The ODataBatchChangeSetUnit will handle the a change set within the
 * batch request, it will contain list of CUD operation but no GET request
 * nor another change set.
 * 
 * It will be set the transaction boundary if the producer implements 
 * the ODataChangeSetBoundaryProducer interface.
 * 
 * Copyright 2013 Halliburton
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 *
 */
public class ODataBatchChangeSetUnit extends ODataBatchUnit {
  private static Logger logger = Logger.getLogger(ODataBatchChangeSetUnit.class.getName());

  private List<ODataBatchSingleUnit> parts = new ArrayList<ODataBatchSingleUnit>();
  private String changesetBoundary = "changesetresponse_" + Guid.randomGuid().toString();
  private List<Response> responses = null;

  public List<ODataBatchSingleUnit> getParts() {
    return parts;
  }

  public void addPart(ODataBatchSingleUnit unit) {
    if (unit instanceof ODataBatchGetUnit) {
      throw new UnsupportedOperationException("GET operation cannot be part of ChangeSet request");
    }
    parts.add(unit);
  }

  @Override
  public Response execute(HttpHeaders httpHeaders, ContextResolver<ODataProducer> producerResolver, URI baseUrI) throws Exception {
    if (parts.size() <= 0) {
      return Response.ok().build();
    }

    responses = new ArrayList<Response>(parts.size());
    logger.log(Level.INFO, "the number of operations in the changeset is " + parts.size());

    ODataProducer producer = producerResolver.getContext(ODataProducer.class);
    producer.beginChangeSetBoundary();

    for (ODataBatchSingleUnit unit : parts) {
      Response response = null;
      try {
        response = unit.execute(httpHeaders, producerResolver, baseUrI);
        unit.setIntermediateResponse(response);
      } catch (Exception e) {
        logger.log(Level.SEVERE, e.getMessage());
        producer.rollbackChangeSetBoundary();
        // throw Exception and catch it in parent method to create a changeset failure response.
        throw e;
      }
    }

    try {
      producer.commitChangeSetBoundary();
      for (ODataBatchSingleUnit unit : parts) {
        Response response = unit.getIntermediateResponse();
        try {
          //build the response only in case of create.
          if (response.getStatus() == Status.CREATED.getStatusCode() && response.getEntity() != null && !response.getEntity().equals("")) {
            response = unit.createResponseForBatch(httpHeaders, producerResolver, baseUrI, (String) response.getEntity());

          }
          responses.add(response);
        } catch (Exception e) {
          //Exception occurred only while building response after commit, so don't throw it back; instead just return the cashed response.
          responses.add(response);
          logger.log(Level.SEVERE, "Error in building changeset response");
          logger.log(Level.SEVERE, e.getMessage());
        }
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "ChangeSet ending the transaction encountered the following error.");
      logger.log(Level.SEVERE, e.getMessage());
      // throw Exception and catch it in parent method to create a changeset failure response.
      throw e;
    }
    return buildChangeSetResponse();
  }

  @Override
  public String getBatchUnitContentType() {
    StringBuilder ctBld = new StringBuilder();

    ctBld.append("\n").append(ODataConstants.Headers.CONTENT_TYPE).append(": multipart/mixed; boundary=").append(changesetBoundary).append("\n");

    return ctBld.toString();
  }

  /**
   * Builds the change set response.
   *
   * @return the response
   */
  private Response buildChangeSetResponse() {
    StringBuilder changeResponse = new StringBuilder();

    int i = 0;
    for (ODataBatchSingleUnit unit : parts) {
      Response response = responses.get(i++);
      changeResponse.append("\n--").append(changesetBoundary);
      changeResponse.append(BatchRequestResource.createResponseBodyPart(unit, response));
    }
    changeResponse.append("\n--").append(changesetBoundary).append("--\n");

    return Response.ok(changeResponse).build();
  }

}
