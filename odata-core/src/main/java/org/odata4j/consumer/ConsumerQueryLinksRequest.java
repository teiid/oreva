package org.odata4j.consumer;

import java.io.Reader;
import java.io.StringReader;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityIds;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.format.FormatType;
import org.odata4j.format.SingleLink;
import org.odata4j.format.json.JsonSingleLinkFormatParser;
import org.odata4j.format.xml.AtomSingleLinkFormatParser;
import org.odata4j.internal.BOMWorkaroundReader;
import org.odata4j.stax2.XMLEventReader2;
import org.odata4j.stax2.util.StaxUtil;

/**
 * Query-links-request implementation.
 */
public class ConsumerQueryLinksRequest extends AbstractConsumerQueryRequestBase<OEntityId> {

  private final String targetNavProp;

  public ConsumerQueryLinksRequest(ODataClient client, String serviceRootUri, EdmDataServices metadata, OEntityId sourceEntity, String targetNavProp) {
    super(client, serviceRootUri, metadata, OEntityIds.toKeyString(sourceEntity));
    this.targetNavProp = targetNavProp;
  }

  public static Func1<String, String> linksPath(final String targetNavProp, final Object[] targetKeyValues) {
    return new Func1<String, String>() {
      public String apply(String input) {
        String keyString = targetKeyValues == null || targetKeyValues.length == 0
            ? "" : OEntityKey.create(targetKeyValues).toKeyString();
        return input + "/$links/" + targetNavProp + keyString;
      }
    };
  }

  @Override
  public Enumerable<OEntityId> execute() throws ODataProducerException {
    ODataClientRequest request = buildRequest(linksPath(targetNavProp, null));
    return Enumerable.create(getClient().getLinks(request)).select(new Func1<SingleLink, OEntityId>() {
      @Override
      public OEntityId apply(SingleLink link) {
        return OEntityIds.parse(getServiceRootUri(), link.getUri());
      }
    });
  }

  private ODataClientRequest getRequest() {
    return buildRequest(linksPath(targetNavProp, null));
  }

  @Override
  public String formatRequest(FormatType formatType) {
    ODataClientRequest request = getRequest();
    return ConsumerBatchRequestHelper.formatSingleRequest(request, formatType);
  }

  @Override
  public Object getResult(ODataVersion version, Object payload, FormatType formatType) {
    Iterable<SingleLink> links = parseLinkQueryResult(new StringReader((String) payload), formatType);
    Object result = Enumerable.create(links).select(new Func1<SingleLink, OEntityId>() {
      @Override
      public OEntityId apply(SingleLink link) {
        return OEntityIds.parse(link.getUri());
      }
    });

    return result;
  }

  private static Iterable<SingleLink> parseLinkQueryResult(Reader reader, FormatType formatType) {
    Iterable<SingleLink> links = null;

    if (formatType.equals(FormatType.ATOM)) {
      XMLEventReader2 linkReader = StaxUtil.newXMLEventReader(new BOMWorkaroundReader(reader));
      links = AtomSingleLinkFormatParser.parseLinks(linkReader);
    } else {
      links = JsonSingleLinkFormatParser.parseLinks(reader);
    }

    return links;
  }

}
