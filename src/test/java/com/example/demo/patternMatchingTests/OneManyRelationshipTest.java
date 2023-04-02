package com.example.demo.patternMatchingTests;

import static org.junit.Assert.assertEquals;

import com.example.demo.TestObject;
import com.example.demo.models.ModelType;
import com.example.demo.models.PatternMatch;
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
  public void mondialCountryTest() {
    ModelType modelType = PatternMatch.patternMatching(testObject.getCountry(), null,
        testObject.getMondialSchema());
    assertEquals(modelType, ModelType.REFLEXIVE_RELATIONSHIP);
  }

}
