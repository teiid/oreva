package org.odata4j.test.unit.core;

import junit.framework.Assert;

import org.junit.Test;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmProperty.CollectionKind;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;

public class EdmTypeTest {

  @Test
  public void edmTypeTests() {
    Assert.assertTrue(EdmType.getSimple("Edm.String").isSimple()); // keep this test first, or at least before EdmSimpleType is loaded
    Assert.assertTrue(EdmType.getSimple("My.Custom.Type") == null);
  }
  
  @Test
  public void edmCollectionTypes(){
    Assert.assertTrue(EdmDataServices.newBuilder().resolveType("Collection(Edm.String)").build().equals(new EdmCollectionType(CollectionKind.Collection, EdmSimpleType.STRING)));
    Assert.assertTrue(EdmDataServices.newBuilder().resolveType("Bag(Edm.String)").build().equals(new EdmCollectionType(CollectionKind.Bag, EdmSimpleType.STRING)));
    Assert.assertTrue(EdmDataServices.newBuilder().resolveType("List(Edm.String)").build().equals(new EdmCollectionType(CollectionKind.List, EdmSimpleType.STRING)));
  }
}
