package com.example.demo.patternMatchingTests;

import static org.junit.Assert.assertEquals;

import com.example.demo.TestObject;
import com.example.demo.models.ModelType;
import com.example.demo.models.ModelUtil;
import org.junit.BeforeClass;
import org.junit.Test;

public class OneManyRelationshipTest {

  static TestObject testObject;

  @BeforeClass
  public static void init() throws Exception {
    testObject = new TestObject();
    testObject.setup();
  }

  @Test
  public void mondialEncompassesTest() {
    ModelType modelType = ModelUtil.patternMatch(testObject.getCountry(),
        testObject.getMondialSchema());
    assertEquals(modelType, ModelType.ONE_MANY_RELATIONSHIP);
  }

}
