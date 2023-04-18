package com.example.demo.patternMatchingTests;

import static org.junit.Assert.assertEquals;

import com.example.demo.TestObject;
import com.example.demo.models.ModelType;
import com.example.demo.models.ModelUtil;
import org.junit.BeforeClass;
import org.junit.Test;

public class ManyManyRelationshipTest {

  static TestObject testObject;

  @BeforeClass
  public static void init() throws Exception {
    testObject = new TestObject();
    testObject.setup();
  }

  @Test
  public void mondialBelieveTest() {
    ModelType modelType = ModelUtil.patternMatching(testObject.getBelieve(),
        testObject.getMondialSchema());
    assertEquals(modelType, ModelType.MANY_MANY_RELATIONSHIP);
  }

}
