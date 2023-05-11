package com.example.demo.models;

import static com.example.demo.models.ModelType.BASIC_ENTITY;
import static com.example.demo.models.ModelType.MANY_MANY_RELATIONSHIP;
import static com.example.demo.models.ModelType.ONE_MANY_RELATIONSHIP;
import static com.example.demo.models.ModelType.REFLEXIVE_RELATIONSHIP;
import static com.example.demo.models.ModelType.UNKNOWN;
import static com.example.demo.models.ModelType.WEAK_ENTITY;

import io.github.MigadaTang.ERConnectableObj;
import io.github.MigadaTang.Entity;
import io.github.MigadaTang.Relationship;
import io.github.MigadaTang.RelationshipEdge;
import io.github.MigadaTang.Schema;
import io.github.MigadaTang.common.Cardinality;
import io.github.MigadaTang.common.EntityType;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.junit.Assert;

public class ModelUtil {

  private static ModelType downwardConversion(ModelType original) {
    switch (original) {
      case MANY_MANY_RELATIONSHIP -> {
        return ONE_MANY_RELATIONSHIP;
      }
      case ONE_MANY_RELATIONSHIP -> {
        return WEAK_ENTITY;
      }
      // todo: handel other cases
      default -> {return UNKNOWN;}
    }
  }

  /**
   * The function pattern matches current user selection to one of the five ER models.
   */
  public static ModelType patternMatch(ERConnectableObj object, Schema schema) {
    assert object != null;

    // two situations: user selected table is an Entity or a Relationship
    if (object instanceof Entity entity) {
      if (entity.getEntityType() == EntityType.WEAK) {
        return WEAK_ENTITY;
      }
      // Checking One-Many Relationship: brute force
      for (Relationship relationship : schema.getRelationshipList()) {
        boolean cond1 = false;
        boolean cond2 = false;
        for (RelationshipEdge edge : relationship.getEdgeList()) {
          Cardinality cardinality = edge.getCardinality();
          if (edge.getConnObj() == entity && (cardinality == Cardinality.OneToOne
              || cardinality == Cardinality.ZeroToOne)) {

            cond1 = true;
          } else if (edge.getConnObj() != entity
              && (cardinality == Cardinality.OneToMany
              || cardinality == Cardinality.ZeroToMany)) {
            cond2 = true;
          }
        }
        if (cond1 && cond2) {
          return ONE_MANY_RELATIONSHIP;
        }
      }
      if (entity.getEntityType() == EntityType.SUBSET
          || entity.getEntityType() == EntityType.UNKNOWN) {
        return UNKNOWN;
      }
      return BASIC_ENTITY;
    } else if (object instanceof Relationship relationship) {
      Entity entity = null;
      boolean cond1 = false;
      boolean cond2 = false;
      boolean sameEntity = false;
      for (RelationshipEdge edge : relationship.getEdgeList()) {
        if (edge.getCardinality() == Cardinality.OneToMany
            || edge.getCardinality() == Cardinality.ZeroToMany) {
          if (!cond1) {
            cond1 = true;
          } else {
            cond2 = true;
          }
        }
        if (entity == null) {
          entity = (Entity) edge.getConnObj();
        } else {
          sameEntity = edge.getConnObj() == entity;
        }
      }
      if (cond2 && sameEntity) {
        return REFLEXIVE_RELATIONSHIP;
      } else if (cond2) {
        return MANY_MANY_RELATIONSHIP;
      }
      return UNKNOWN;
    }
    return UNKNOWN;
  }

  // to handel situations where user select attributes from two tables (must be of type entity)
  public static ModelType patternMatch(ERConnectableObj entity1, ERConnectableObj entity2, Schema schema) {
    Assert.assertTrue(entity1 instanceof Entity);
    Assert.assertTrue(entity2 instanceof Entity);

    // todo: handel all situations
    ModelType result = UNKNOWN;
    for (Relationship relationship : schema.getRelationshipList()) {
      // flags for cardinality (0-> unknown (one-one), 1-> one-many, 2-> many-many)
      int cardinality = 0;
      Iterator<RelationshipEdge> edgeIterator = relationship.getEdgeList().iterator();
      RelationshipEdge edge1 = edgeIterator.next();
      RelationshipEdge edge2 = edgeIterator.next();
      if ((edge1.getConnObj() == entity1 || edge1.getConnObj() == entity2) && (
          edge2.getConnObj() == entity1 || edge2.getConnObj() == entity2)) {
        if (edge1.getCardinality() == Cardinality.OneToMany
            || edge1.getCardinality() == Cardinality.ZeroToMany) {
          cardinality++;
        }
        if (edge2.getCardinality() == Cardinality.OneToMany
            || edge2.getCardinality() == Cardinality.ZeroToMany) {
          cardinality++;
        }
      }

      if (cardinality == 1) {
        result = ONE_MANY_RELATIONSHIP;
      } else if (cardinality == 2) {
        result = MANY_MANY_RELATIONSHIP;
      }
    }
    return downwardConversion(result);
  }

  // helper function to find the strong entity which relate to a given weak entity
  public static Entity getRelatedStrongEntity(Entity weakEntity, Schema schema) {
    assert weakEntity.getEntityType() == EntityType.WEAK;
    for (Relationship relationship : schema.getRelationshipList()) {
      boolean flag = false;
      for (RelationshipEdge edge : relationship.getEdgeList()) {
        Entity tempEntity = (Entity) edge.getConnObj();
        if (flag) {
          return tempEntity;
        }
        if (tempEntity == weakEntity) {
          flag = true;
        }
      }
    }
    return null;
  }

  // helper function to find the entity of the many side in a One-Many relationship
  public static Entity getParentEntity(Entity childEntity, Schema schema) {
    assert childEntity.getEntityType() == EntityType.STRONG;
    for (Relationship relationship : schema.getRelationshipList()) {
      boolean flag = false;
      Entity manySideEntity = null;
      for (RelationshipEdge edge : relationship.getEdgeList()) {
        Cardinality cardinality = edge.getCardinality();
        if (cardinality == Cardinality.OneToOne || cardinality == Cardinality.ZeroToOne) {
          if (edge.getConnObj() == childEntity) {
            flag = true;
          }
        } else if (cardinality == Cardinality.OneToMany || cardinality == Cardinality.ZeroToMany) {
          manySideEntity = (Entity) edge.getConnObj();
        }
      }
      if (flag && manySideEntity != null) {
        return manySideEntity;
      }
    }
    return null;
  }

  // helper function to find entities of a Many-Many Relationship given the relationship
  // note that the function returns a set with only one element when the relationship is Reflexive
  public static Set<Entity> getManyManyEntities(Relationship relationship) {
    Set<Entity> entities = new HashSet<>();
    for (RelationshipEdge edge : relationship.getEdgeList()) {
      entities.add((Entity) edge.getConnObj());
    }
    return entities;
  }
}
