package com.example.demo.patternMatchingTests;

import static org.junit.Assert.assertEquals;

import com.example.demo.TestObject;
import com.example.demo.models.ModelType;
import com.example.demo.models.ModelUtil;
import org.junit.BeforeClass;
import org.junit.Test;

public class WeakEntityTest {

  static TestObject testObject;

  @BeforeClass
  public static void init() throws Exception {
    testObject = new TestObject();
    testObject.setup();
  }

  @Test
  public void mondialProvinceTest() {
    ModelType modelType = ModelUtil.patternMatch(testObject.getProvince(),
        testObject.getMondialSchema());
    assertEquals(modelType, ModelType.WEAK_ENTITY);
  }

  @Test
  public void mondialCountryPopTest() {
    ModelType modelType = ModelUtil.patternMatch(testObject.getCountryPop(),
        testObject.getMondialSchema());
    assertEquals(modelType, ModelType.WEAK_ENTITY);
  }
}
