package org.odata4j.format.json;

import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.odata4j.core.OCollection;
import org.odata4j.core.OCollections;
import org.odata4j.core.OComplexObject;
import org.odata4j.core.OEntity;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OFunctionParameters;
import org.odata4j.core.OObject;
import org.odata4j.core.OSimpleObject;
import org.odata4j.core.OSimpleObjects;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmFunctionParameter;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;
import org.odata4j.exceptions.NotImplementedException;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.FormatType;
import org.odata4j.format.Parameters;
import org.odata4j.format.Settings;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader.JsonEvent;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader.JsonValueEvent;

public class JsonParametersFormatParser extends JsonFormatParser implements FormatParser<Parameters> {

  public JsonParametersFormatParser(Settings settings) {
    super(settings);
  }

  @Override
  public Parameters parse(Reader reader) {
    JsonStreamReader jsr = JsonStreamReaderFactory.createJsonStreamReader(reader);
    try {
      ParametersImpl parameters = new ParametersImpl();

      if (jsr.hasNext()){

        // skip the StartObject event
        ensureStartObject(jsr.nextEvent());
  
        while (jsr.hasNext()) {
          JsonEvent event = jsr.nextEvent();
          if (event.isStartProperty()) {
            String parameterName = event.asStartProperty().getName();
            EdmFunctionParameter efp = parseFunction.getParameter(parameterName);
            if (efp != null) {
              OFunctionParameter param = readParameter(efp, parameterName, jsr);
              if (param != null) {
                parameters.addParameter(param);
              }
            }
          } else if (event.isEndObject()) {
            break;
          }
        }
      }
      return parameters;

      // no interest in the closing events
    } finally {
      jsr.close();
    }
  }

  protected OFunctionParameter readParameter(EdmFunctionParameter efp, String paramName, JsonStreamReader jsr) {

    EdmType type = efp.getType();
    if (type.isSimple()) {
      JsonEvent event = jsr.nextEvent();
      OSimpleObject<?> object =  OSimpleObjects.create((EdmSimpleType<?>)type, event.asEndProperty().getValue());
      OFunctionParameter functionParameter = OFunctionParameters.create(paramName, object);
      return functionParameter;
    } else if (type instanceof EdmCollectionType) {
      JsonCollectionFormatParser jsonColParser = createJsonCollectionParser((EdmCollectionType)type);
      OCollection<? extends OObject> o = jsonColParser.parseCollection(jsr);
      OFunctionParameter functionParameter = OFunctionParameters.create(paramName, o);
      return functionParameter;
    } else if (type instanceof EdmComplexType) {
      JsonComplexObjectFormatParser jsonCTParser = createJsonCTParser((EdmComplexType) type);
      OComplexObject o = jsonCTParser.parseSingleObject(jsr);
      OFunctionParameter functionParameter = OFunctionParameters.create(paramName, o);
      return functionParameter;
    } else if (type instanceof EdmEntityType) {
      JsonEntityFormatParser jsonETParser = createJsonETParser((EdmEntityType)type);
      OEntity o = jsonETParser.parseSingleEntity(jsr);
      OFunctionParameter functionParameter = OFunctionParameters.create(paramName, o);
      return functionParameter;
    }

    throw new NotImplementedException("Using type " + type.getClass().getName() + " as parameter in a function/action is not supported");

  }
  
  protected JsonEntityFormatParser createJsonETParser(EdmEntityType edmType) {
    Settings s = new Settings(
        this.version,
        this.metadata,
        this.entitySetName,
        this.entityKey,
        null, // FeedCustomizationMapping fcMapping,
        false, // boolean isResponse);
        edmType); // expected type
    
    return new JsonEntityFormatParser(s);
  }

  /**
   * Create a Json Collection Parser to parse the payload.
   * @param edmType the Collection Type.
   * @return a new Json Collection Format Parser.
   */
  protected JsonCollectionFormatParser createJsonCollectionParser(EdmCollectionType edmType) {
    Settings s = new Settings(
        this.version,
        this.metadata,
        this.entitySetName,
        this.entityKey,
        null, // FeedCustomizationMapping fcMapping,
        false, // boolean isResponse);
        edmType); // expected type

    return new JsonCollectionFormatParser(s);
  }

  /**
   * Create a Json Complex type parser.
   * @param edmType
   * @return
   */
  protected JsonComplexObjectFormatParser createJsonCTParser(EdmComplexType edmType) {
    Settings s = new Settings(
        this.version,
        this.metadata,
        this.entitySetName,
        this.entityKey,
        null, // FeedCustomizationMapping fcMapping,
        false, // boolean isResponse);
        edmType); // expected type

    return new JsonComplexObjectFormatParser(s);
  }


  static class ParametersImpl implements Parameters {

    private Map<String, OFunctionParameter> parameters = new HashMap<String, OFunctionParameter>();
    
    @Override
    public Collection<OFunctionParameter> getParameters() {
      return parameters.values();
    }
    
    public void addParameter(OFunctionParameter parameter) {
      parameters.put(parameter.getName(), parameter);
    }
  }
}
