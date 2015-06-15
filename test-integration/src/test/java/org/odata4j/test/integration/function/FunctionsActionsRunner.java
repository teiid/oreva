package org.odata4j.test.integration.function;

import org.odata4j.examples.JaxRsImplementation;
import org.odata4j.examples.ODataServerFactory;
import org.odata4j.producer.resources.DefaultODataProducerProvider;

public class FunctionsActionsRunner {

  private final static String endpointUri = "http://localhost:8811/FunctionImportScenario.svc/";

  public static void main(String[] args) {
    DefaultODataProducerProvider.setInstance(new FunctionsActionsProducerMock());
    new ODataServerFactory(JaxRsImplementation.JERSEY).hostODataServer(FunctionsActionsRunner.endpointUri);
  }

}
