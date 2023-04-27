package com.example.demo.modelUtilTests;

import static org.junit.Assert.assertTrue;

import com.example.demo.TestObject;
import com.example.demo.models.ModelUtil;
import io.github.MigadaTang.Entity;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetManyManyRelationshipEntitiesTest {

  static TestObject testObject;

  @BeforeClass
  public static void init() throws Exception {
    testObject = new TestObject();
    testObject.setup();
  }

  @Test
  public void mondialEncompassesRelationshipTest() {
    Set<Entity> entities = ModelUtil.getManyManyEntities(testObject.getEncompasses());
    assertTrue(entities.contains(testObject.getCountry()));
    assertTrue(entities.contains(testObject.getContinent()));
  }
}
