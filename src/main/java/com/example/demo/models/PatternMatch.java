package com.example.demo.models;

import static com.example.demo.models.ModelType.*;

import io.github.MigadaTang.Entity;
import io.github.MigadaTang.Relationship;
import io.github.MigadaTang.RelationshipEdge;
import io.github.MigadaTang.Schema;
import io.github.MigadaTang.common.BelongObjType;
import io.github.MigadaTang.common.EntityType;

public class PatternMatch {

  /**
   * The function pattern matches current user selection to one of the five ER models.
   */
  public static ModelType patternMatching(Entity entity1, Entity entity2, Schema schema) {
    assert entity1 != null; // we need at least one entity to match the Basic Entity

    // only one entity is selected
    if (entity2 == null) {
      if (entity1.getEntityType() == EntityType.WEAK) {
        return WEAK_ENTITY;
      }
      // Checking Reflexive Relationship
      // brute force: check all relationship edges whether there exist two edges that have the same
      // relationship object and the same entity object (in our case "entity1").
      for (Relationship relationship : schema.getRelationshipList()) {
        boolean flag = false;
        for (RelationshipEdge edge : relationship.getEdgeList()) {
          if (edge.getConnObjType() == BelongObjType.ENTITY && edge.getConnObj() == entity1) {
            if (flag) {
              return REFLEXIVE_RELATIONSHIP;
            } else {
              flag = true;
            }
          }
        }
      }
      return BASIC_ENTITY;
    } else { // todo: two entities are selected
      return ONE_MANY_RELATIONSHIP;
    }
  }
}
