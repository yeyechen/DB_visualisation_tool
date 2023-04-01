package com.example.demo.patternMatchingTests;

import com.example.demo.models.ModelType;
import com.example.demo.models.PatternMatch;
import io.github.MigadaTang.ER;
import io.github.MigadaTang.Entity;
import io.github.MigadaTang.Relationship;
import io.github.MigadaTang.Schema;
import io.github.MigadaTang.common.AttributeType;
import io.github.MigadaTang.common.Cardinality;
import io.github.MigadaTang.common.DataType;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReflexiveRelationshipTest {

  @BeforeClass
  public static void init() throws Exception {
    ER.initialize();
  }

  @Test
  public void ReflexiveRelationshipTest() {
    // This is Reflexive relationship example in Mondial database ("country" entity and
    // "borders" as relationship):
    Schema reflexiveRelationshipTestSchema = ER.createSchema("Reflexive_Relationship_Test");

    Entity country = reflexiveRelationshipTestSchema.addEntity("country");
    country.addPrimaryKey("code", DataType.VARCHAR);
    country.addAttribute("population", DataType.INT, AttributeType.Mandatory);
    country.addAttribute("area", DataType.DOUBLE, AttributeType.Mandatory);

    Relationship borders = reflexiveRelationshipTestSchema.createRelationship("borders", country,
        country, Cardinality.OneToMany, Cardinality.OneToMany);
    borders.addAttribute("length", DataType.DOUBLE, AttributeType.Mandatory);

    ModelType modelType = PatternMatch.patternMatching(country, null,
        reflexiveRelationshipTestSchema);
    assertEquals(modelType, ModelType.REFLEXIVE_RELATIONSHIP);
  }
}
