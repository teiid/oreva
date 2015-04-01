package org.odata4j.core;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmFunctionImport.FunctionKind;
import org.odata4j.edm.EdmType;
import org.odata4j.exceptions.NotImplementedException;


/**
 * This is a helper class to build OBindableEntity instances.
 *
 */
public class OBindableEntities {

  public static OBindableEntity createBindableExtension(Map<String, EdmFunctionImport> actions, Map<String, EdmFunctionImport> functions) {
    return new OBindableEntityImpl(functions, actions);
  }
  
  public static OEntity createBindableEntity(final OEntity entity, Map<String, EdmFunctionImport> bindableFunctions){
    final Map<String, EdmFunctionImport> functions = new HashMap<String, EdmFunctionImport>();
    final Map<String, EdmFunctionImport> actions = new HashMap<String, EdmFunctionImport>();
    for (Map.Entry<String, EdmFunctionImport> entry: bindableFunctions.entrySet()){
      String fqFunctionName = entry.getKey();
      EdmFunctionImport f = entry.getValue();
      if (f.getFunctionKind() == FunctionKind.Action){
        actions.put(fqFunctionName, f);
      } else if (f.getFunctionKind() == FunctionKind.Function){
        functions.put(fqFunctionName, f);
      }
    }
    
    return new OExtensibleEntity(entity, 
        new OBindableEntityImpl(Collections.unmodifiableMap(functions), Collections.unmodifiableMap(actions)));
  }
  
  
  private static class OBindableEntityImpl implements OBindableEntity {

    private final Map<String, EdmFunctionImport> functions;
    private final Map<String, EdmFunctionImport> actions;
    
    public OBindableEntityImpl(Map<String, EdmFunctionImport> functions, Map<String, EdmFunctionImport> actions) {
      this.functions = functions;
      this.actions = actions;
    }

    @Override
    public Map<String, EdmFunctionImport> getBindableActions() {
      return actions;
    }

    @Override
    public Map<String, EdmFunctionImport> getBindableFunctions() {
      return functions;
    }
    
  }  
  

  /**
   * Class used to add extensions over an existing OEntity.
   *
   */
  private static class OExtensibleEntity implements OEntity{
    private final OEntity delegate;
    private final Collection<Object> extensions;
	private String mediaStreamContentType;
	private InputStream mediaInputStream;

    public OExtensibleEntity(OEntity delegate, Object... extensions) {
      super();
      this.delegate = delegate;
      this.extensions = Arrays.asList(extensions);
    }

    @Override
    public String getEntitySetName() {
      return delegate.getEntitySetName();
    }

    @Override
    public OEntityKey getEntityKey() {
      return delegate.getEntityKey();
    }

    @Override
    public List<OProperty<?>> getProperties() {
      return delegate.getProperties();
    }

    @Override
    public OProperty<?> getProperty(String propName) {
      return delegate.getProperty(propName);
    }

    @Override
    public <T> OProperty<T> getProperty(String propName, Class<T> propClass) {
      return delegate.getProperty(propName, propClass);
    }

    @Override
    public <TExtension extends OExtension<OEntity>> TExtension findExtension(Class<TExtension> clazz) {
      for (Object extension : extensions) {
        if (clazz.isInstance(extension)) {
          return clazz.cast(extension);
        }
      }
      return delegate.findExtension(clazz);
    }

    @Override
    public EdmType getType() {
      return delegate.getType();
    }

    @Override
    public EdmEntitySet getEntitySet() {
      return delegate.getEntitySet();
    }

    @Override
    public EdmEntityType getEntityType() {
      return delegate.getEntityType();
    }

    @Override
    public String getEntityTag() {
      return delegate.getEntityTag();
    }

    @Override
    public List<OLink> getLinks() {
      return delegate.getLinks();
    }

    @Override
    public <T extends OLink> T getLink(String title, Class<T> linkClass) {
      return delegate.getLink(title, linkClass);
    }
    
    /*************************************************************
     * the following are LGC added method, we should remove it.
     *************************************************************/
    @Override
    public String getMediaTypeForStream() {
      return this.mediaStreamContentType;
    }

    @Override
    public InputStream getMediaLinkStream() {
      return this.mediaInputStream;
    }

    @Override
    public void setMediaLinkStream(InputStream inStream) {
      this.mediaInputStream = inStream;
    }

    @Override
    public void setMediaTypeForStream(String mediaTypeForStream) {
      this.mediaStreamContentType = mediaTypeForStream;
    }


  }  
}
