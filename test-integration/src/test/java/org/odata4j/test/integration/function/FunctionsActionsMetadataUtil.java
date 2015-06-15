package org.odata4j.test.integration.function;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.xml.EdmxFormatParser;
import org.odata4j.stax2.XMLInputFactory2;
import org.odata4j.stax2.staximpl.StaxXMLFactoryProvider2;

public class FunctionsActionsMetadataUtil {

  public static final String CONTAINER_NAME = "RefScenario";
  public static final String TEST_BOUND_FUNCTION = "TestBoundFunction";
  public static final String TEST_BOUND_ACTION = "TestBoundAction";
  public static final String TEST_COLLECTION_BOUND_FUNCTION = "TestCollectionBoundFunction";
  public static final String TEST_OVERLOADED_BOUND_FUNCTION = "TestOverloadedBoundFunction";
  public static final String TEST_OVERLOADED_BOUND_ACTION = "TestOverloadedBoundAction";

  private static final String REF_SCENARIO_EDMX = "/META-INF/FunctionImportV3Scenario.edmx.xml";
  private static final String REF_SCENARIO_EDMX_NO_STREAM = "/META-INF/FunctionImportV3ScenarioNoStream.edmx.xml";

  public static EdmDataServices readMetadataServiceFromFile() {
    InputStream inputStream = FunctionsActionsProducerMock.class.getResourceAsStream(FunctionsActionsMetadataUtil.REF_SCENARIO_EDMX);
    Reader reader = new InputStreamReader(inputStream);

    XMLInputFactory2 inputFactory = StaxXMLFactoryProvider2.getInstance().newXMLInputFactory2();
    EdmxFormatParser parser = new EdmxFormatParser();
    EdmDataServices edmDataService = parser.parseMetadata(inputFactory.createXMLEventReader(reader));

    return edmDataService;
  }

  public static String readMetadataFromFile() {
    try {
      InputStream inputStream = FunctionsActionsMetadataUtil.class.getResourceAsStream(FunctionsActionsMetadataUtil.REF_SCENARIO_EDMX);

      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      String line = null;
      StringBuilder stringBuilder = new StringBuilder();
      String ls = System.getProperty("line.separator");
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
        stringBuilder.append(ls);
      }
      return stringBuilder.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static EdmDataServices readFunctionMetadataServiceFromFile() {
    InputStream inputStream = FunctionsActionsProducerMock.class.getResourceAsStream(FunctionsActionsMetadataUtil.REF_SCENARIO_EDMX_NO_STREAM);
    Reader reader = new InputStreamReader(inputStream);

    XMLInputFactory2 inputFactory = StaxXMLFactoryProvider2.getInstance().newXMLInputFactory2();
    EdmxFormatParser parser = new EdmxFormatParser();
    EdmDataServices edmDataService = parser.parseMetadata(inputFactory.createXMLEventReader(reader));

    return edmDataService;
  }

}
