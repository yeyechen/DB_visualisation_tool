package com.example.demo.modelUtilTests;

import static org.junit.Assert.assertEquals;

import com.example.demo.TestObject;
import com.example.demo.models.ModelUtil;
import io.github.MigadaTang.Entity;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetParentEntityTest {

  static TestObject testObject;

  @BeforeClass
  public static void init() throws Exception {
    testObject = new TestObject();
    testObject.setup();
  }

  @Test
  public void mondialReligionTest() {
    Entity parent = ModelUtil.getParentEntity(testObject.getReligion(),
        testObject.getMondialSchema());
    assertEquals(parent, testObject.getCountry());
  }
}
