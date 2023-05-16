package com.example.demo.modelUtilTests;

import static org.junit.Assert.assertEquals;

import com.example.demo.TestObject;
import com.example.demo.models.ModelUtil;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetRelationshipNameBetweenTest {

  static TestObject testObject;

  @BeforeClass
  public static void init() throws Exception {
    testObject = new TestObject();
    testObject.setup();
  }

  @Test
  public void countryContinentTest() {
    String relationshipName = ModelUtil.getRelationshipNameBetween(testObject.getCountry().getName(),
        testObject.getContinent().getName(), testObject.getMondialSchema());
    assertEquals(relationshipName, testObject.getEncompasses().getName());
  }

}
