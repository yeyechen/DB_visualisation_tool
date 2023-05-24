package com.example.demo.modelUtilTests;

import static org.junit.Assert.assertTrue;

import com.example.demo.TestObject;
import com.example.demo.models.ModelUtil;
import io.github.MigadaTang.ERConnectableObj;
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
    List<ERConnectableObj> entities = ModelUtil.tablesInRelationshipWith(testObject.getCountry(),
        testObject.getMondialSchema());
    assertTrue(entities.contains(testObject.getReligion()));
    assertTrue(entities.contains(testObject.getContinent()));
    assertTrue(entities.contains(testObject.getAirport()));
    assertTrue(entities.contains(testObject.getCountryPop()));
    assertTrue(entities.contains(testObject.getProvince()));
    assertTrue(entities.contains(testObject.getEncompasses()));
    assertTrue(entities.contains(testObject.getBorders()));
  }

  @Test
  public void relationshipTest() {
    List<ERConnectableObj> entities = ModelUtil.tablesInRelationshipWith(testObject.getEncompasses(),
        testObject.getMondialSchema());
    assertTrue(entities.contains(testObject.getContinent()));
    assertTrue(entities.contains(testObject.getCountry()));
  }
}
