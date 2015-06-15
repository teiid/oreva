package org.odata4j.test.integration.function;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.odata4j.core.OCollection;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OExtension;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.core.OSimpleObject;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmFunctionParameter;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.exceptions.NotFoundException;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.ContextStream;
import org.odata4j.producer.CountResponse;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityIdResponse;
import org.odata4j.producer.EntityQueryInfo;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.OBindableFunctionExtension;
import org.odata4j.producer.ODataContext;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.Responses;
import org.odata4j.producer.edm.MetadataProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionsActionsProducerMock implements ODataProducer {

  public static final String COLLECTION_STRING2 = "efg";
  public static final String COLLECTION_STRING1 = "abc";
  public static final double COLLECTION_DOUBLE2 = 1e12;
  public static final double COLLECTION_DOUBLE1 = -0.34;
  public static final String COMPLEY_TYPE_NAME_LOCATION = "RefScenario.c_Location";
  public static final String COMPLEY_TYPE_NAME_CITY = "RefScenario.c_City";
  public static final String COUNTRY = "Bavaria";
  public static final String CITY = "Munic";
  public static final String POSTAL_CODE = "12345";
  public static final String EMPLOYEE_NAME = "Hugo Hurtig";
  public static final String EMPLOYEE_ID = "abc123";

  public static final boolean BOOLEAN_VALUE = true;

  public static final String SOME_TEXT = "some text";

  private static final Logger LOGGER = LoggerFactory.getLogger(FunctionsActionsProducerMock.class);
  public static final short INT16_VALUE = 4711;

  private Map<String, OFunctionParameter> queryParameter;

  private QueryInfo queryInfo;

  private EdmDataServices metadata;

  @Override
  public EdmDataServices getMetadata() {
    if (this.metadata == null) {
      this.metadata = FunctionsActionsMetadataUtil.readFunctionMetadataServiceFromFile();
    }
    return this.metadata;
  }

  @Override
  public MetadataProducer getMetadataProducer() {
    return null;
  }

  @Override
  public EntitiesResponse getEntities(ODataContext context, String entitySetName, QueryInfo queryInfo) {
    EdmEntitySet entitySet = getMetadata().findEdmEntitySet(entitySetName);
    List<OEntity> entityList = new ArrayList<OEntity>();

    if (entitySetName.equals("Employees")) {
      entityList.add(createEmployeeEntity("123"));
      entityList.add(createEmployeeEntity("456"));
      EntitiesResponse response = Responses.entities(entityList, entitySet, null, null);
      return response;
    }

    return Responses.entities(entityList, entitySet, null, null);
  }

  @Override
  public CountResponse getEntitiesCount(ODataContext context, String entitySetName, QueryInfo queryInfo) {
    return null;
  }

  @Override
  public EntityResponse getEntity(ODataContext context, String entitySetName, OEntityKey entityKey, EntityQueryInfo queryInfo) {
    String key = entityKey.asSingleValue().toString();
    if (entitySetName.equals("Employees")) {
      OEntity entity = createEmployeeEntity(key);
      EntityResponse response = Responses.entity(entity);
      return response;
    } else if (entitySetName.equals("Companies")) {
      OEntity entity = createCompanyEntity(key);
      EntityResponse response = Responses.entity(entity);
      return response;
    }

    throw new NotFoundException("No employee found with key : " + key);

  }

  @Override
  public BaseResponse getNavProperty(ODataContext context, String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
    return null;
  }

  @Override
  public CountResponse getNavPropertyCount(ODataContext context, String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
    return null;
  }

  @Override
  public void close() {}

  @Override
  public EntityResponse createEntity(ODataContext context, String entitySetName, OEntity entity) {
    return null;
  }

  @Override
  public EntityResponse createEntity(ODataContext context, String entitySetName, OEntityKey entityKey, String navProp, OEntity entity) {
    return null;
  }

  @Override
  public void deleteEntity(ODataContext context, String entitySetName, OEntityKey entityKey) {}

  @Override
  public void mergeEntity(ODataContext context, String entitySetName, OEntity entity) {}

  @Override
  public void updateEntity(ODataContext context, String entitySetName, OEntity entity) {}

  @Override
  public EntityIdResponse getLinks(ODataContext context, OEntityId sourceEntity, String targetNavProp) {
    return null;
  }

  @Override
  public void createLink(ODataContext context, OEntityId sourceEntity, String targetNavProp, OEntityId targetEntity) {}

  @Override
  public void updateLink(ODataContext context, OEntityId sourceEntity, String targetNavProp, OEntityKey oldTargetEntityKey, OEntityId newTargetEntity) {}

  @Override
  public void deleteLink(ODataContext context, OEntityId sourceEntity, String targetNavProp, OEntityKey targetEntityKey) {}

  @SuppressWarnings("unchecked")
  @Override
  public BaseResponse callFunction(ODataContext context, EdmFunctionImport function, Map<String, OFunctionParameter> params, QueryInfo queryInfo) {
    BaseResponse response;

    FunctionsActionsProducerMock.LOGGER.debug("EdmFunctionImport Object:    " + function.getName());
    FunctionsActionsProducerMock.LOGGER.debug("EdmFunctionImport Parameter: " + params);
    FunctionsActionsProducerMock.LOGGER.debug("EdmFunctionImport QueryInfo: " + queryInfo);

    this.queryParameter = params;
    this.queryInfo = queryInfo;

    if (FunctionsActionsMetadataUtil.TEST_BOUND_FUNCTION.equals(function.getName())) {
      OFunctionParameter employeeParam = params.get("employee");
      OEntity employee = (OEntity) employeeParam.getValue();
      OFunctionParameter p2 = params.get("p2");
      String p2Value = ((OSimpleObject<String>) p2.getValue()).getValue();
      String result = (String) employee.getProperty("EmployeeId").getValue() + "-" + p2Value;
      response = Responses.simple(EdmSimpleType.STRING, function.getName(), result);

    } else if (FunctionsActionsMetadataUtil.TEST_BOUND_ACTION.equals(function.getName())) {
      OFunctionParameter employeeParam = params.get("employee");
      OEntity employee = (OEntity) employeeParam.getValue();
      OFunctionParameter p2 = params.get("p2");
      String p2Value = ((OSimpleObject<String>) p2.getValue()).getValue();
      String result = (String) employee.getProperty("EmployeeId").getValue() + "-" + p2Value;
      response = Responses.simple(EdmSimpleType.STRING, function.getName(), result);

    } else if (FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_ACTION.equals(function.getName())
        || FunctionsActionsMetadataUtil.TEST_OVERLOADED_BOUND_FUNCTION.equals(function.getName())) {
      OFunctionParameter entityParam = params.get(function.getBoundParameter().getName());
      OEntity entity = (OEntity) entityParam.getValue();
      OFunctionParameter p2 = params.get("p2");
      String p2Value = ((OSimpleObject<String>) p2.getValue()).getValue();
      String result = function.getName() + "-" + entity.getEntitySetName() + entity.getEntityKey().toKeyString() + "-" + p2Value;
      response = Responses.simple(EdmSimpleType.STRING, function.getName(), result);

    } else if (FunctionsActionsMetadataUtil.TEST_COLLECTION_BOUND_FUNCTION.equals(function.getName())) {
      EdmFunctionParameter boundParameter = function.getBoundParameter();
      OFunctionParameter entityParam = params.get(boundParameter.getName());
      OCollection<?> collection = (OCollection<?>) entityParam.getValue();

      String result = Integer.toString(collection.size());
      response = Responses.simple(EdmSimpleType.STRING, function.getName(), result);

    } else {
      throw new RuntimeException("Unsupported Test Case for FunctionImport: " + function.getName());
    }

    return response;
  }

  private OEntity createEmployeeEntity(String id) {
    EdmEntitySet entitySet = this.getMetadata().findEdmEntitySet("Employees");
    OEntityKey entityKey = OEntityKey.parse("EmployeeId='" + id + "'");
    ArrayList<OProperty<?>> properties = new ArrayList<OProperty<?>>();
    properties.add(OProperties.string("EmployeeName", FunctionsActionsProducerMock.EMPLOYEE_NAME));
    properties.add(OProperties.string("EmployeeId", FunctionsActionsProducerMock.EMPLOYEE_ID));
    OEntity entity = OEntities.create(entitySet, entityKey, properties, null);
    return entity;
  }

  private OEntity createCompanyEntity(String id) {
    EdmEntitySet entitySet = this.getMetadata().findEdmEntitySet("Companies");
    OEntityKey entityKey = OEntityKey.parse("CompanyId='" + id + "'");
    ArrayList<OProperty<?>> properties = new ArrayList<OProperty<?>>();
    properties.add(OProperties.string("CompanyId", id));
    properties.add(OProperties.string("CompanyName", "Name-" + id));
    OEntity entity = OEntities.create(entitySet, entityKey, properties, null);
    return entity;
  }

  public Map<String, OFunctionParameter> getQueryParameter() {
    return this.queryParameter;
  }

  public QueryInfo getQueryInfo() {
    return this.queryInfo;
  }

  @Override
  public <TExtension extends OExtension<ODataProducer>> TExtension findExtension(Class<TExtension> clazz) {
    if (clazz.equals(OBindableFunctionExtension.class)) {
      return clazz.cast(new BindableExtension());
    }
    return null;
  }

  static class BindableExtension implements OBindableFunctionExtension {
    @Override
    public boolean isFunctionBindable(EdmFunctionImport function, OEntity entity) {
      LOGGER.debug("Checking entity for bindability : "
          + entity.getEntitySetName() + entity.getEntityKey().toKeyString() + " to function "
          + function.getName());
      boolean result = !entity.getEntityKey().asSingleValue().equals("NotBinded");
      return result;
    }

  }

  @Override
  public void beginChangeSetBoundary() {
    // TODO Auto-generated method stub

  }

  @Override
  public void commitChangeSetBoundary() {
    // TODO Auto-generated method stub

  }

  @Override
  public void rollbackChangeSetBoundary() {
    // TODO Auto-generated method stub

  }

  @Override
  public EntityResponse createResponseForBatchPostOperation(String entitySetName, OEntity entity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InputStream getInputStreamForMediaLink(String entitySetName, OEntityKey entityKey, EntityQueryInfo queryInfo) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateEntityWithStream(String entitySetName, OEntity entity) {
    // TODO Auto-generated method stub

  }

  @Override
  public ContextStream getInputStreamForNamedStream(String entitySetName, OEntityKey entityKey, String columnName, QueryInfo queryInfo) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateEntityWithNamedStream(String entitySetName, OEntityKey entityKey, String columnName, ContextStream streamContext) {
    // TODO Auto-generated method stub
    
  }
}
