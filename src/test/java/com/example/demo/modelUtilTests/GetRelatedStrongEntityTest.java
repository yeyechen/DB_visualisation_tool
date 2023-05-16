package com.example.demo.modelUtilTests;

import static org.junit.Assert.assertEquals;

import com.example.demo.TestObject;
import com.example.demo.models.ModelUtil;
import io.github.MigadaTang.Entity;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetRelatedStrongEntityTest {

  static TestObject testObject;

  @BeforeClass
  public static void init() throws Exception {
    testObject = new TestObject();
    testObject.setup();
  }

  @Test
  public void countryPopTest() {
    Entity strongEntity = ModelUtil.getRelatedStrongEntity(testObject.getCountryPop(),
        testObject.getMondialSchema());
    assertEquals(strongEntity, testObject.getCountry());
  }

  @Test
  public void provinceTest() {
    Entity strongEntity = ModelUtil.getRelatedStrongEntity(testObject.getProvince(),
        testObject.getMondialSchema());
    assertEquals(strongEntity, testObject.getCountry());
  }
}
