package com.example.demo.modelUtilTests;

import static org.junit.Assert.*;

import com.example.demo.TestObject;
import com.example.demo.models.ModelUtil;
import io.github.MigadaTang.Entity;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

public class InRelationshipWithTest {

  static TestObject testObject;

  @BeforeClass
  public static void init() throws Exception {
    testObject = new TestObject();
    testObject.setup();
  }

  @Test
  public void mondialCountryTest() {
    List<Entity> entities = ModelUtil.inRelationshipWith(testObject.getCountry(),
        testObject.getMondialSchema());
    System.out.println(entities);
    assertTrue(entities.contains(testObject.getReligion()));
    assertTrue(entities.contains(testObject.getContinent()));
  }

}
