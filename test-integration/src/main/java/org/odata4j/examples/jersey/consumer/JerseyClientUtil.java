package org.odata4j.examples.jersey.consumer;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.ext.RuntimeDelegate;

import org.odata4j.consumer.behaviors.OClientBehavior;
import org.odata4j.core.Throwables;
import org.odata4j.examples.jersey.consumer.behaviors.JerseyClientBehavior;
import org.odata4j.examples.jersey.internal.StringProvider2;
import org.odata4j.internal.PlatformUtil;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.impl.provider.header.MediaTypeProvider;
import com.sun.jersey.core.spi.factory.AbstractRuntimeDelegate;
import com.sun.jersey.spi.HeaderDelegateProvider;
import org.odata4j.urlencoder.ConversionUtil;

class JerseyClientUtil {

  static {
    if (PlatformUtil.runningOnAndroid())
      androidJerseyClientHack();
  }

  @SuppressWarnings("unchecked")
  private static void androidJerseyClientHack() {
    try {
      RuntimeDelegate rd = RuntimeDelegate.getInstance();
      Field f = AbstractRuntimeDelegate.class.getDeclaredField("hps");
      f.setAccessible(true);
      Set<HeaderDelegateProvider<?>> hps = (Set<HeaderDelegateProvider<?>>) f.get(rd);
      hps.clear();
      hps.add(new MediaTypeProvider());
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public static Client newClient(JerseyClientFactory clientFactory, OClientBehavior[] behaviors) {
    DefaultClientConfig cc = new DefaultClientConfig();
    cc.getSingletons().add(new StringProvider2());
    if (behaviors != null) {
      for (OClientBehavior behavior : behaviors)
      {
        if (behavior instanceof JerseyClientBehavior) {
          ((JerseyClientBehavior) behavior).modify(cc);
        }
      }
    }
    Client client = clientFactory.createClient(cc);
    if (behaviors != null)
    {
      for (OClientBehavior behavior : behaviors)
      {
        if (behavior instanceof JerseyClientBehavior) {
          ((JerseyClientBehavior) behavior).modifyClientFilters(client);
        }
      }
    }
    return client;
  }

  public static WebResource resource(Client client, String url, OClientBehavior[] behaviors) {
    WebResource resource = client.resource(encodeURl(url));
    if (behaviors != null)
    {
      for (OClientBehavior behavior : behaviors)
      {
        if (behavior instanceof JerseyClientBehavior) {
          ((JerseyClientBehavior) behavior).modifyWebResourceFilters(resource);
        }
      }
    }
    return resource;
  }
  
  /**
   * 
   * =================================================================================================================
   * Fix bug Bug 171678 - query with primary keys hangs
   * example primary keys: 
   * (pick_interpreter='SIM2',pick_name='100 (Top Stage IVF)',pick_obs_no=1,unique_wellbore_identifier='040292797601')
   * The issues in the original code are:
   * (1) matcher.find() takes very long time because Regular expression is invalid 
   * (2) If it does find the matches in some caes, it only returns part of primary key value "(xxx)" 
   *     and will miss URL encoding special characters such as spaces before and after "(Top Stage IVF)"
   * 
   * The fix is use RegEx "\\(([^(]+|(?))?\\)"  and "\\(([^)]+|(?))?\\)"
   * =============================================================================================
   * Original code:
   * Regular expression '\\(([^)(]+|(?))+\\)' matches the string within the bracket in the URL.
   * ================================================================================================
   * String in the bracket are OEntityKey which contain special character,
   * here we are encoding the key in URL.
   * 
   * @param url the url
   * @return the string
   */
  public static String encodeURl(String url) {

    Pattern pattern1 = Pattern.compile("\\((.*)\\)");
    Matcher matcher1 = pattern1.matcher(url);
    while (matcher1.find()) {
      String st = url.substring(matcher1.start(), matcher1.end());
      url = url.replace(st, ConversionUtil.encodeString(st));
    }

    return url;
  }
}
