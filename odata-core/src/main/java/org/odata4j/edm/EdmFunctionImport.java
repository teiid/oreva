package org.odata4j.edm;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.HttpMethod;

import org.odata4j.core.ImmutableList;

/**
 * A CSDL FunctionImport element.
 *
 * <p>The FunctionImport element in conceptual schema definition language (CSDL) represents a function that is
 * defined in the data source but available to objects through the conceptual model. For example, a Function element
 * in the storage model can be used to represent a stored procedure in a database.
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/cc716710.aspx">[msdn] FunctionImport Element (CSDL)</a>
 */
public class EdmFunctionImport extends EdmItem {

  public enum FunctionKind {
    ServiceOperation, 
    Function, 
    Action
  }

  private final String name;
  private final EdmEntitySet entitySet;
  private final EdmType returnType;
  private final String httpMethod;
  private final boolean bindable;
  private final boolean sideEffecting;
  private final boolean alwaysBindable;
  private final ImmutableList<EdmFunctionParameter> parameters;

  private EdmFunctionImport(String name, EdmEntitySet entitySet, EdmType returnType,
      String httpMethod, boolean bindable, boolean sideEffecting, boolean alwaysBindable,
      ImmutableList<EdmFunctionParameter> parameters, EdmDocumentation doc,
      ImmutableList<EdmAnnotation<?>> annots, ImmutableList<EdmAnnotation<?>> annotElements) {
    super(doc, annots, annotElements);
    this.name = name;
    this.entitySet = entitySet;
    this.returnType = returnType;
    this.httpMethod = httpMethod;
    this.bindable = bindable;
    this.sideEffecting = sideEffecting;
    this.alwaysBindable = alwaysBindable;
    this.parameters = parameters;
  }

  public String getName() {
    return name;
  }

  public EdmEntitySet getEntitySet() {
    return entitySet;
  }

  public EdmType getReturnType() {
    return returnType;
  }

  public String getHttpMethod() {
    FunctionKind kind = getFunctionKind();
    if (kind == FunctionKind.ServiceOperation){
      return httpMethod;
    } else if (kind == FunctionKind.Function){
      return HttpMethod.GET;
    } else {
      return HttpMethod.POST;
    }
  }

  public boolean isBindable() {
    return bindable && parameters != null && parameters.size() > 0;
  }

  public EdmFunctionParameter getBoundParameter(){
    if (bindable && parameters != null){
      for (EdmFunctionParameter param : parameters){
        if (param.isBound()){
          return param;
        }
      }
    }
    return null;
  }
  
  public EdmFunctionParameter getParameter(String name){
    if (bindable && parameters != null){
      for (EdmFunctionParameter param : parameters){
        if (param.getName().equals(name)){
          return param;
        }
      }
    }
    return null;
  }
  
  public boolean isSideEffecting() {
    return sideEffecting;
  }

  public boolean isAlwaysBindable() {
    return alwaysBindable;
  }

  public List<EdmFunctionParameter> getParameters() {
    return parameters;
  }

  public FunctionKind getFunctionKind() {
    if (httpMethod != null){
      return FunctionKind.ServiceOperation;
    } else {
      if (sideEffecting){
        return FunctionKind.Action;
      } else {
        return FunctionKind.Function;
      }
    }
  }
  
  public static Builder newBuilder() {
    return new Builder();
  }

  static Builder newBuilder(EdmFunctionImport functionImport, BuilderContext context) {
    return context.newBuilder(functionImport, new Builder());
  }

  /** Mutable builder for {@link EdmFunctionImport} objects. */
  public static class Builder extends EdmItem.Builder<EdmFunctionImport, Builder> {
    private String name;
    private EdmEntitySet.Builder entitySet;
    private String entitySetName;
    private EdmType returnType;
    private EdmType.Builder<?, ?> returnTypeBuilder;
    private String returnTypeName;
    private String httpMethod;
    private boolean bindable;
    private boolean sideEffecting;
    private boolean alwaysBindable;
    private final List<EdmFunctionParameter.Builder> parameters = new ArrayList<EdmFunctionParameter.Builder>();
    private boolean isCollection;

    @Override
    Builder newBuilder(EdmFunctionImport functionImport, BuilderContext context) {
      List<EdmFunctionParameter.Builder> functionParameters = new ArrayList<EdmFunctionParameter.Builder>();
      for (EdmFunctionParameter functionParameter : functionImport.parameters)
        functionParameters.add(EdmFunctionParameter.newBuilder(functionParameter, context));
      return new Builder().setName(functionImport.name).setEntitySet(functionImport.entitySet != null ? EdmEntitySet.newBuilder(functionImport.entitySet, context) : null)
          .setReturnType(functionImport.returnType)
          .setHttpMethod(functionImport.httpMethod)
          .setBindable(functionImport.bindable)
          .setSideEffecting(functionImport.sideEffecting)
          .setAlwaysBindable(functionImport.alwaysBindable)
          .addParameters(functionParameters);
    }

    public EdmFunctionImport build() {
      List<EdmFunctionParameter> builtParameters = new ArrayList<EdmFunctionParameter>();
      for (EdmFunctionParameter.Builder parameter : this.parameters){
        builtParameters.add(parameter.build());
      }
      EdmType returnType =
          this.returnType != null ? this.returnType
              : returnTypeBuilder != null ? returnTypeBuilder.build() : null;
      return new EdmFunctionImport(name, entitySet == null ? null : entitySet.build(), returnType, httpMethod,
          bindable, sideEffecting, alwaysBindable,
          ImmutableList.copyOf(builtParameters), getDocumentation(), ImmutableList.copyOf(getAnnotations()),
          ImmutableList.copyOf(getAnnotationElements()));
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setEntitySet(EdmEntitySet.Builder entitySet) {
      this.entitySet = entitySet;
      return this;
    }

    public Builder setReturnType(EdmType returnType) {
      this.returnType = returnType;
      return this;
    }

    public Builder setReturnType(EdmType.Builder<?, ?> returnType) {
      this.returnTypeBuilder = returnType;
      return this;
    }

    public Builder setHttpMethod(String httpMethod) {
      this.httpMethod = httpMethod;
      return this;
    }

    public Builder setBindable(boolean bindable) {
      this.bindable = bindable;
      return this;
    }

    public Builder setSideEffecting(boolean sideEffecting) {
      this.sideEffecting = sideEffecting;
      return this;
    }

    public Builder setAlwaysBindable(boolean alwaysBindable) {
      this.alwaysBindable = alwaysBindable;
      return this;
    }

    public Builder addParameters(EdmFunctionParameter.Builder... parameters) {
      for (EdmFunctionParameter.Builder parameter : parameters) {
        this.parameters.add(parameter);
      }
      return this;
    }

    public Builder addParameters(List<EdmFunctionParameter.Builder> parameters) {
      this.parameters.addAll(parameters);
      return this;
    }

    public String getEntitySetName() {
      return entitySetName;
    }

    public Builder setEntitySetName(String entitySetName) {
      this.entitySetName = entitySetName;
      return this;
    }

    public String getReturnTypeName() {
      return returnTypeName;
    }

    public boolean isCollection() {
      return isCollection;
    }

    public String getName() {
      return name;
    }

    public String getHttpMethod() {
      return httpMethod;
    }

    public boolean isBindable() {
      return bindable;
    }

    public boolean isSideEffecting() {
      return sideEffecting;
    }

    public boolean isAlwaysBindable() {
      return alwaysBindable;
    }

    public List<EdmFunctionParameter.Builder> getParameters() {
      return parameters;
    }

    public Builder setReturnTypeName(String returnTypeName) {
      this.returnTypeName = returnTypeName;
      return this;
    }

    public Builder setIsCollection(boolean isCollection) {
      this.isCollection = isCollection;
      return this;
    }

  }

}
