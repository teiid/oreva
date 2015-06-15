package org.odata4j.core;

import java.io.StringReader;

import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;
import org.odata4j.exceptions.NotImplementedException;
import org.odata4j.expression.CommonExpression;
import org.odata4j.expression.Expression;
import org.odata4j.expression.ExpressionParser;
import org.odata4j.expression.LiteralExpression;
import org.odata4j.format.Settings;
import org.odata4j.format.json.JsonCollectionFormatParser;
import org.odata4j.format.json.JsonComplexObjectFormatParser;
import org.odata4j.format.json.JsonEntityFormatParser;

/**
 * A static factory to create immutable {@link OFunctionParameter} instances.
 */
public class OFunctionParameters {

  private OFunctionParameters() {}

  /**
   * Creates a new OFunctionParameter, inferring the edm-type from the value provided, which cannot be null.
   *
   * @param <T>  the property value's java-type
   * @param name  the property name
   * @param value  the property value
   * @return a new OData property instance
   */
  public static <T> OFunctionParameter create(String name, T value) {
    if (value == null)
      throw new IllegalArgumentException("Cannot infer EdmType if value is null");

    if (value instanceof OObject) {
      return new FunctionParameterImpl(name, (OObject) value);
    }

    EdmSimpleType<?> type = EdmSimpleType.forJavaType(value.getClass());
    return new FunctionParameterImpl(name, OSimpleObjects.create(type, value));
  }

  /** Creates a new OFunctionParameter by parsing a string value 
   * @param edmDataServices */
  public static OFunctionParameter parse(EdmDataServices edmDataServices, String name, EdmType type, String value) {
    if (type instanceof EdmSimpleType) {
      CommonExpression ce = ExpressionParser.parse(value);
      if (ce instanceof LiteralExpression) { 
        // may have to case the literalValue based on type...
        Object val = convert(Expression.literalValue((LiteralExpression) ce), (EdmSimpleType<?>) type);
        return new FunctionParameterImpl(name, OSimpleObjects.create((EdmSimpleType<?>) type, val));
      }
    } else if (type instanceof EdmCollectionType) {
      JsonCollectionFormatParser jsonColParser = new JsonCollectionFormatParser((EdmCollectionType) type, edmDataServices);
      OCollection<? extends OObject> o = jsonColParser.parse(new StringReader(value));
      OFunctionParameter functionParameter = OFunctionParameters.create(name, o);
      return functionParameter;     
    }else if (type instanceof EdmComplexType) {
      JsonComplexObjectFormatParser jsonCTParser = new JsonComplexObjectFormatParser((EdmComplexType)type);
      OComplexObject o = jsonCTParser.parse(new StringReader(value));
      OFunctionParameter functionParameter = OFunctionParameters.create(name, o);
      return functionParameter;
    } else if (type instanceof EdmEntityType) {
      Settings s = new Settings(
          // TODO: version should coming from client
          ODataConstants.DATA_SERVICE_VERSION, 
          edmDataServices,
          null,
          null,
          null, // FeedCustomizationMapping fcMapping,
          false, // boolean isResponse);
          type); // expected type

      JsonEntityFormatParser jsonETParser = new JsonEntityFormatParser(s);
      OEntity o = jsonETParser.parse(new StringReader(value));
      OFunctionParameter functionParameter = OFunctionParameters.create(name, o);
      return functionParameter;
    }
    // TODO for other types
    throw new NotImplementedException();
  }

  private static Object convert(Object val, EdmSimpleType<?> type) {
    Object v = val;
    if (type.equals(EdmSimpleType.INT16) && (!(val instanceof Short))) {
      // parser gave us an Integer
      v = Short.valueOf(((Number) val).shortValue());
    } else if (type.equals(EdmSimpleType.SINGLE) && (!(val instanceof Float))) {
      // parser gave us a Single
      v = new Float(((Number) val).floatValue());
    } else if (type.equals(EdmSimpleType.BYTE) && (!(val instanceof UnsignedByte))) {
      // parser gave us an Edm.Byte
      v = UnsignedByte.valueOf(((Number) val).intValue());
    } else if (type.equals(EdmSimpleType.SBYTE) && (!(val instanceof Byte))) {
      // parser gave us a SByte
      v = Byte.valueOf(((Number) val).byteValue());
    }
    return v;
  }

  private static class FunctionParameterImpl implements OFunctionParameter {

    private final String name;
    private final OObject obj;

    public FunctionParameterImpl(String name, OObject obj) {
      this.name = name;
      this.obj = obj;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public EdmType getType() {
      return obj.getType();
    }

    @Override
    public OObject getValue() {
      return obj;
    }

    @Override
    public String toString() {
      return String.format("OFunctionParameter[%s,%s,%s]", getName(), getType(), getValue());
    }
  }

}
