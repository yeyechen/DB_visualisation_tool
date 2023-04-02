package com.example.demo.patternMatchingTests;

import com.example.demo.TestObject;
import com.example.demo.models.ModelType;
import com.example.demo.models.PatternMatch;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReflexiveRelationshipTest {

  static TestObject testObject;

  @BeforeClass
  public static void init() throws Exception {
    testObject = new TestObject();
    testObject.setup();
  }

  @Test
  public void mondialEncompassesTest() {
    ModelType modelType = PatternMatch.patternMatching(testObject.getCountry(),
        testObject.getContinent(), testObject.getMondialSchema());
    assertEquals(modelType, ModelType.ONE_MANY_RELATIONSHIP);
  }
}
