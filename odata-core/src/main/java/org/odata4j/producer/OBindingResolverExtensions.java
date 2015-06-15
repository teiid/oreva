package org.odata4j.producer;

import java.util.ArrayList;
import java.util.List;

import org.odata4j.core.OCollection;
import org.odata4j.core.OCollections;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OEntityKey.KeyType;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OFunctionParameters;
import org.odata4j.core.OLink;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmFunctionParameter;
import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.expression.Expression;


public class OBindingResolverExtensions {

  public static OBindingResolverExtension getPartialBindingResolver(){
    return new KeyOnlyBindingResolverExtension();
  }
  
  public static OBindingResolverExtension getFullBindingResolver(){
    return new PreloadBindingResolverExtension();
  }
  
  /**
   * An OBindingResolverExtension that will only resolve entity keys.
   *
   */
  static class KeyOnlyBindingResolverExtension implements OBindingResolverExtension {

    @Override
    public OFunctionParameter resolveBindingParameter(
        ODataContext context, 
        EdmFunctionImport function, 
        String entitySetName, 
        OEntityKey entityKey, 
        QueryInfo queryInfo) {
      EdmFunctionParameter parameter = function.getBoundParameter();
      ODataProducer producer = context.getContextAspect(ODataProducer.class);
      if (parameter.getType() instanceof EdmCollectionType){
        // Collection binding
        EdmEntitySet entitySet = producer.getMetadata().findEdmEntitySet(entitySetName);
        EdmEntityType itemType = entitySet.getType();
        // We try to select only key attributes
        List<String> keyNames = itemType.getKeys();
        List<EntitySimpleProperty> selectedKeys = new ArrayList<EntitySimpleProperty>();
        for (String keyName: keyNames){
          selectedKeys.add(Expression.simpleProperty(keyName));
        }
        QueryInfo qi = new QueryInfo(
            queryInfo.inlineCount, 
            queryInfo.top, 
            queryInfo.skip, 
            queryInfo.filter, 
            queryInfo.orderBy, 
            queryInfo.skipToken, 
            queryInfo.customOptions, 
            queryInfo.expand, 
            selectedKeys);
        EntitiesResponse response = producer.getEntities(context, entitySetName, qi);
        List<OEntity> entities = response.getEntities();
        OCollection.Builder<OEntity> builder = OCollections.newBuilder(itemType);
        for (OEntity entity : entities){
          builder = builder.add(entity);
        }
        OCollection<?> collection = builder.build();
        return OFunctionParameters.create(parameter.getName(), collection);        
      } else {
        // Entity binding
        EdmEntitySet entitySet = producer.getMetadata().findEdmEntitySet(entitySetName);
        EdmEntityType entityType = entitySet.getType();
        List<OProperty<?>> properties = new ArrayList<OProperty<?>>();
        if (entityKey.getKeyType() == KeyType.SINGLE){
          OProperty<?> key = OProperties.simple(entityType.getKeys().get(0), entityKey.asSingleValue());
          properties.add(key);
        } else {
          properties.addAll(entityKey.asComplexProperties());
        }
        OEntity oEntity = OEntities.create(
            entitySet, 
            entityType, 
            entityKey, 
            properties, 
            new ArrayList<OLink>());
        return OFunctionParameters.create(parameter.getName(), oEntity);
      }
    }
  }
  
  /**
   * An OBindingResolverExtension that will resolve the full entity by loading
   * them from the producer.
   *
   */
  static class PreloadBindingResolverExtension implements OBindingResolverExtension {

    @Override
    public OFunctionParameter resolveBindingParameter(ODataContext context, EdmFunctionImport function, String entitySetName, OEntityKey entityKey, QueryInfo queryInfo) {
      EdmFunctionParameter parameter = function.getBoundParameter();
      ODataProducer producer = context.getContextAspect(ODataProducer.class);
      if (parameter.getType() instanceof EdmCollectionType){
        
        // Collection binding
        EdmEntitySet entitySet = producer.getMetadata().findEdmEntitySet(entitySetName);
        EdmEntityType itemType = entitySet.getType();
        
        // We use the queryInfo use to prefilter arguments
        // No way to filter the function result ?!
        EntitiesResponse response = producer.getEntities(context, entitySetName, queryInfo);
        List<OEntity> entities = response.getEntities();
        OCollection.Builder<OEntity> builder = OCollections.newBuilder(itemType);
        for (OEntity entity : entities){
          builder = builder.add(entity);
        }
        OCollection<?> collection = builder.build();
        return OFunctionParameters.create(parameter.getName(), collection);
      } else {
        
        // Entity binding
        // queryInfo passed is null as we use it to filter resulting data
        EntityResponse response = producer.getEntity(context, entitySetName, entityKey, null);
        OEntity entity = response.getEntity();
        return OFunctionParameters.create(parameter.getName(), entity);
      }
    }
    
  }
}
