package org.odata4j.test.integration.function;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ws.rs.core.MediaType;

import org.core4j.Enumerable;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OBindableEntity;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityGetRequest;
import org.odata4j.core.OObject;
import org.odata4j.core.OSimpleObject;
import org.odata4j.format.FormatType;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.producer.server.ODataServer;
import org.odata4j.test.integration.AbstractRuntimeTest;
import org.odata4j.test.integration.ResponseData;
import org.xml.sax.SAXException;


@RunWith(JUnit4.class)
public class FunctionsActionsTest extends AbstractRuntimeTest {

  public FunctionsActionsTest() {
    super(RuntimeFacadeType.JERSEY);
  }

  /*
  public FunctionImportTest(RuntimeFacadeType type) {
    super(type);
  }*/

  private static ArrayList<FormatType> formats;
  static {
    FunctionsActionsTest.formats = new ArrayList<FormatType>();
    FunctionsActionsTest.formats.add(FormatType.JSONVERBOSE);
    FunctionsActionsTest.formats.add(FormatType.ATOM);
    FunctionsActionsTest.formats.add(FormatType.JSON);
    FunctionsActionsTest.formats.add(FormatType.JSONLITEFULLMETADATA);
    FunctionsActionsTest.formats.add(FormatType.JSONLITENOMETADATA);
  }

  private static ODataServer server;

  private final static String endpointUri = "http://localhost:8810/FunctionImportScenario.svc/";

  private static FunctionsActionsProducerMock mockProducer;

  private String formatQuery(FormatType type) {
    String query;

    switch (type) {
    case ATOM:
      query = "$format=atom";
      break;
    case JSONVERBOSE:
      query = "$format=jsonverbose";
      break;
    case JSON:
      query = "$format=json";
      break;
    case JSONLITEFULLMETADATA:
      query = "$format=json;odata=fullmetadata";
      break;
    case JSONLITENOMETADATA:
      query = "$format=json;odata=nometadata";
      break;
    default:
      throw new RuntimeException("Unknown Format Type: " + type);
    }

    return query;
  }

  private static void initializeXmlUnit() {
    HashMap<String, String> m = new HashMap<String, String>();
    m.put("m", "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");
    m.put("d", "http://schemas.microsoft.com/ado/2007/08/dataservices");
    m.put("edmx", "http://schemas.microsoft.com/ado/2007/06/edmx");
    m.put("g", "http://www.w3.org/2005/Atom"); // 'g' is a dummy for global namespace

    NamespaceContext ctx = new SimpleNamespaceContext(m);
    XMLUnit.setXpathNamespaceContext(ctx);
    XpathEngine engine = XMLUnit.newXpathEngine();
    engine.setNamespaceContext(ctx);
  }

  @BeforeClass
  public static  void before() throws Exception {

    initializeXmlUnit();

    mockProducer = new FunctionsActionsProducerMock();

    DefaultODataProducerProvider.setInstance(mockProducer);
    
    server = new FunctionsActionsTest().rtFacade.startODataServer(FunctionsActionsTest.endpointUri);
  }

  @AfterClass
  public static void after() throws Exception {
    if (server != null) {
      server.stop();
      server = null;
    }
  }

  @Test
  public void testCallFunctionAsServiceOperation() throws XpathException, SAXException, IOException {
    String query = "?employee=Employees('mykey')&p2='value'";

    for (FormatType format : FunctionsActionsTest.formats) {
      ResponseData responseData = this.rtFacade.getWebResource(endpointUri + FunctionsActionsMetadataUtil.TEST_BOUND_FUNCTION + query + "&" + this.formatQuery(format));
      String resource = responseData.getEntity();

      assertEquals(format.toString(), 200, responseData.getStatusCode());
      assertNotNull(format.toString(), this.mockProducer.getQueryParameter());

      switch (format) {
      case ATOM:
        assertXpathExists("/d:" + FunctionsActionsMetadataUtil.TEST_BOUND_FUNCTION, resource);
        assertXpathEvaluatesTo("mykey-value", "/d:" + FunctionsActionsMetadataUtil.TEST_BOUND_FUNCTION + "/text()", resource);
        break;
      case JSONVERBOSE:
        assertTrue(format.toString(), resource.contains(FunctionsActionsMetadataUtil.TEST_BOUND_FUNCTION));
        assertTrue(format.toString(), resource.contains("mykey-value"));
        break;
      case JSON:
        assertTrue(format.toString(), resource.contains("mykey-value"));
        break;
      case JSONLITENOMETADATA:
        assertTrue(format.toString(), resource.contains("mykey-value"));
        break;
      case JSONLITEFULLMETADATA:
        assertTrue(format.toString(), resource.contains("mykey-value"));
        break;
      default:
        throw new RuntimeException("Unknown Format Type: " + format);
      }

    }
  }

  @Test
  public void testCallBoundFunction() throws XpathException, SAXException, IOException {
    String query = endpointUri + "Employees('mykey')" + "/"
        + FunctionsActionsMetadataUtil.CONTAINER_NAME + "." + FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_FUNCTION
        + "?p2='value'";

    for (FormatType format : FunctionsActionsTest.formats) {
      ResponseData responseData = this.rtFacade.getWebResource(query + "&" + this.formatQuery(format));
      String resource = responseData.getEntity();

      assertEquals(format.toString(), 200, responseData.getStatusCode());
      assertNotNull(format.toString(), this.mockProducer.getQueryParameter());

      String expected = FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_FUNCTION + "-" + "Employees('mykey')" + "-" + "value";

      switch (format) {
      case ATOM:
        assertXpathExists("/d:" + FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_FUNCTION, resource);
        assertXpathEvaluatesTo(expected, "/d:" + FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_FUNCTION + "/text()", resource);
        break;
      case JSONVERBOSE:
        assertTrue("JSON " + resource, resource.contains(FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_FUNCTION));
        assertTrue("JSON " + resource, resource.contains(expected));
        break;
      case JSON:
        assertTrue("JSON " + resource, resource.contains(FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_FUNCTION));
        assertTrue("JSON " + resource, resource.contains(expected));
        break;
      case JSONLITEFULLMETADATA:
        assertTrue("JSON " + resource, resource.contains(FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_FUNCTION));
        assertTrue("JSON " + resource, resource.contains(expected));
        break;
      case JSONLITENOMETADATA:
        assertTrue("JSON " + resource, resource.contains(FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_FUNCTION));
        assertTrue("JSON " + resource, resource.contains(expected));
        break;
      default:
        throw new RuntimeException("Unknown Format Type: " + format);
      }
    }
  }

  @Test
  public void testCallBoundAction() throws XpathException, SAXException, IOException {

    /**
     * There appears to be a strange race condition or something in the test environment:
     * if the first request to the server has a payload, the server can
     * timeout waiting for data from the client.  Not sure why or if it is a client
     * or server issue. Workaround:  issue a GET first to prime things.
     */
    rtFacade.getWebResource(endpointUri);

    String query = endpointUri + "Employees('mykey')" + "/" + FunctionsActionsMetadataUtil.CONTAINER_NAME + "." + FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_ACTION;
    String payload = "{\n \"p2\" : \"value\"\n}";
    InputStream payloadStream = new ByteArrayInputStream(payload.getBytes());

    ResponseData responseData = this.rtFacade.postWebResource(query, payloadStream, MediaType.APPLICATION_JSON_TYPE, null);

    String resource = responseData.getEntity();

    String expected = FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_ACTION + "-" + "Employees('mykey')" + "-" + "value";

    assertEquals("JSON", 200, responseData.getStatusCode());
    assertNotNull("JSON", this.mockProducer.getQueryParameter());

    assertTrue("JSON", resource.contains(FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_ACTION));
    assertTrue("JSON", resource.contains(expected));

  }

  @Test
  public void testCallFunctionFromConsumer() throws Exception {
    for (FormatType format : FunctionsActionsTest.formats) {
      ODataConsumer c = rtFacade.createODataConsumer(endpointUri, format, null);

      OEntityGetRequest<OEntity> getEntityRrequest = c.getEntity("Employees", "abc123");
      ODataConsumer.dump.all(true);
      OEntity employee = getEntityRrequest.execute();

      Enumerable<OObject> result = c.callFunction(FunctionsActionsMetadataUtil.TEST_BOUND_FUNCTION)
          .parameter("employee", employee)
          .pString("p2", "value")
          .execute();
      ODataConsumer.dump.all(true);

      assertNotNull(result);

      OObject resultEntity = result.first();
      assertTrue(resultEntity instanceof OSimpleObject<?>);
      assertEquals("abc123-value", ((OSimpleObject<?>) resultEntity).getValue());
    }
  }

  @Test
  public void testCallBoundFunctionFromConsumer() throws Exception {
    for (FormatType format : FunctionsActionsTest.formats) {
      ODataConsumer c = rtFacade.createODataConsumer(endpointUri, format, null);
      OEntity employee = c.getEntity("Employees", "abc123").execute();

      Enumerable<OObject> result = c.callFunction(FunctionsActionsMetadataUtil.CONTAINER_NAME + "." + FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_FUNCTION)
          .bind("Employees", employee.getEntityKey())
          .pString("p2", "value")
          .execute();

      assertNotNull(result);

      OObject resultEntity = result.first();
      assertTrue(resultEntity instanceof OSimpleObject<?>);

      String expected = FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_FUNCTION + "-" + "Employees('abc123')" + "-" + "value";

      assertEquals(expected, ((OSimpleObject<?>) resultEntity).getValue());
    }
  }

  @Test
  public void testCallOverloadedBoundFunctionFromConsumer() throws Exception {
    for (FormatType format : FunctionsActionsTest.formats) {
      ODataConsumer c = rtFacade.createODataConsumer(endpointUri, format, null);
      OEntity company = c.getEntity("Companies", "abc123").execute();

      Enumerable<OObject> result = c.callFunction(FunctionsActionsMetadataUtil.CONTAINER_NAME + "." + FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_FUNCTION)
          .bind("Companies", company.getEntityKey())
          .pString("p2", "value")
          .execute();

      assertNotNull(result);

      OObject resultEntity = result.first();
      assertTrue(resultEntity instanceof OSimpleObject<?>);

      String expected = FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_FUNCTION + "-" + "Companies('abc123')" + "-" + "value";

      assertEquals(expected, ((OSimpleObject<?>) resultEntity).getValue());
    }
  }

  @Test
  public void testCallBoundActionFromConsumer() throws Exception {
    for (FormatType format : FunctionsActionsTest.formats) {
      ODataConsumer c = rtFacade.createODataConsumer(endpointUri, format, null);
      OEntity employee = c.getEntity("Employees", "abc123").execute();

      Enumerable<OObject> result = c.callFunction(FunctionsActionsMetadataUtil.CONTAINER_NAME + "." + FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_ACTION)
          .bind("Employees", employee.getEntityKey())
          .pString("p2", "value")
          .execute();

      assertNotNull(result);
      OObject resultEntity = result.first();
      assertTrue(resultEntity instanceof OSimpleObject<?>);

      String expected = FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_ACTION + "-" + "Employees('abc123')" + "-" + "value";

      assertEquals(expected, ((OSimpleObject<?>) resultEntity).getValue());
    }
  }

  @Test
  public void testCallOverloadedBoundActionFromConsumer() throws Exception {
    for (FormatType format : FunctionsActionsTest.formats) {
      ODataConsumer c = rtFacade.createODataConsumer(endpointUri, format, null);
      OEntity company = c.getEntity("Companies", "abc123").execute();

      Enumerable<OObject> result = c.callFunction(FunctionsActionsMetadataUtil.CONTAINER_NAME + "." + FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_ACTION)
          .bind("Companies", company.getEntityKey())
          .pString("p2", "value")
          .execute();

      assertNotNull(result);
      OObject resultEntity = result.first();
      assertTrue(resultEntity instanceof OSimpleObject<?>);

      String expected = FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_ACTION + "-" + "Companies('abc123')" + "-" + "value";

      assertEquals(expected, ((OSimpleObject<?>) resultEntity).getValue());
    }
  }

  @Test
  public void testConditionalFunctionBinding() throws Exception {
    for (FormatType format : FunctionsActionsTest.formats) {
      ODataConsumer c = rtFacade.createODataConsumer(endpointUri, format, null);
      ODataConsumer.dump.all(true);
      OEntity employee = c.getEntity("Employees", "abc123").execute();
      ODataConsumer.dump.all(false);

      OBindableEntity bindableEntity = employee.findExtension(OBindableEntity.class);
      assertNotNull(format.toString(), bindableEntity);
      assertEquals(format.toString(), 2, bindableEntity.getBindableActions().size());
      assertEquals(format.toString(), 2, bindableEntity.getBindableFunctions().size());

      employee = c.getEntity("Employees", "NotBinded").execute();

      bindableEntity = employee.findExtension(OBindableEntity.class);
      assertNull(format.toString(), bindableEntity);

    }
  }

  @Test
  public void testCollectionFunctionBinding() throws Exception {
    for (FormatType format : FunctionsActionsTest.formats) {
      ODataConsumer c = rtFacade.createODataConsumer(endpointUri, format, null);
      Enumerable<OObject> result =
          c.callFunction(FunctionsActionsMetadataUtil.CONTAINER_NAME + "." + FunctionsActionsMetadataUtil.TEST_COLLECTION_BOUND_FUNCTION)
              .bind("Employees")
              .execute();

      assertNotNull(result);
      OObject resultEntity = result.first();
      assertTrue(resultEntity instanceof OSimpleObject<?>);

      String expected = "2";

      assertEquals(expected, ((OSimpleObject<?>) resultEntity).getValue());
    }
  }
}
