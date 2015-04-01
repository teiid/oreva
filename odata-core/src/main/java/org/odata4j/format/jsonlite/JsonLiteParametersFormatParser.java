package org.odata4j.format.jsonlite;

import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.odata4j.core.OCollection;
import org.odata4j.core.OComplexObject;
import org.odata4j.core.OEntity;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OFunctionParameters;
import org.odata4j.core.OObject;
import org.odata4j.core.OSimpleObject;
import org.odata4j.core.OSimpleObjects;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmFunctionParameter;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;
import org.odata4j.exceptions.NotImplementedException;
import org.odata4j.format.FormatParser;
import org.odata4j.format.Parameters;
import org.odata4j.format.Settings;
import org.odata4j.format.json.JsonStreamReaderFactory;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader.JsonEvent;

/**
 * The Class JsonLiteParametersFormatParser.
 * 
 * @author <a href="mailto:shantanu@synerzip.com">Shantanu Dindokar</a>
 */
public class JsonLiteParametersFormatParser extends JsonLiteFormatParser implements FormatParser<Parameters> {

  public JsonLiteParametersFormatParser(Settings settings) {
    super(settings);
  }

  /* (non-Javadoc)
   * @see org.odata4j.format.FormatParser#parse(java.io.Reader)
   */
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
      JsonEvent event = jsr.nextEvent();
      JsonLiteCollectionFormatParser jsonColParser = createJsonLiteCollectionParser((EdmCollectionType)type);
      OCollection<? extends OObject> o = jsonColParser.parseCollection(jsr, event);
      OFunctionParameter functionParameter = OFunctionParameters.create(paramName, o);
      return functionParameter;
    } else if (type instanceof EdmComplexType) {
      JsonEvent event = jsr.nextEvent();
      JsonLiteComplexObjectFormatParser jsonCTParser = createJsonLiteCTParser((EdmComplexType) type);
      OComplexObject o = jsonCTParser.parseSingleObject(jsr, event);
      OFunctionParameter functionParameter = OFunctionParameters.create(paramName, o);
      return functionParameter;
    } else if (type instanceof EdmEntityType) {
      JsonLiteEntityFormatParser jsonETParser = createJsonETParser((EdmEntityType)type);
      OEntity o = jsonETParser.parseSingleEntity(jsr);
      OFunctionParameter functionParameter = OFunctionParameters.create(paramName, o);
      return functionParameter;
    }

    throw new NotImplementedException("Using type " + type.getClass().getName() + " as parameter in a function/action is not supported");

  }
  
  protected JsonLiteEntityFormatParser createJsonETParser(EdmEntityType edmType) {
    Settings s = new Settings(
        this.version,
        this.metadata,
        this.entitySetName,
        this.entityKey,
        null, // FeedCustomizationMapping fcMapping,
        false, // boolean isResponse);
        edmType); // expected type
    
    return new JsonLiteEntityFormatParser(s);
  }

  /**
   * Create a Json Lite Collection Parser to parse the payload.
   * @param edmType the Collection Type.
   * @return a new Json lite Collection Format Parser.
   */
  protected JsonLiteCollectionFormatParser createJsonLiteCollectionParser(EdmCollectionType edmType) {
    Settings s = new Settings(
        this.version,
        this.metadata,
        this.entitySetName,
        this.entityKey,
        null, // FeedCustomizationMapping fcMapping,
        false, // boolean isResponse);
        edmType); // expected type

    return new JsonLiteCollectionFormatParser(s);
  }

  /**
   * Create a Json Complex type parser.
   * @param edmType
   * @return
   */
  protected JsonLiteComplexObjectFormatParser createJsonLiteCTParser(EdmComplexType edmType) {
    Settings s = new Settings(
        this.version,
        this.metadata,
        this.entitySetName,
        this.entityKey,
        null, // FeedCustomizationMapping fcMapping,
        false, // boolean isResponse);
        edmType); // expected type

    return new JsonLiteComplexObjectFormatParser(s);
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
