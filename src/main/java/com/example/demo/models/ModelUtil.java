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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.Assert;

public class ModelUtil {

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

  // helper function to find the strong entity which relate to a given weak entity
  public static Entity getRelatedStrongEntity(Entity weakEntity, Schema schema) {
    assert weakEntity.getEntityType() == EntityType.WEAK;
    for (Relationship relationship : schema.getRelationshipList()) {
      boolean flag = false;
      Entity tempEntity = null;
      for (RelationshipEdge edge : relationship.getEdgeList()) {
        if (edge.getConnObj() == weakEntity) {
          flag = true;
          if (tempEntity != null) {
            return tempEntity;
          }
          tempEntity = (Entity) edge.getConnObj();
          continue;
        }
        if (flag) {
          return (Entity) edge.getConnObj();
        }
        tempEntity = (Entity) edge.getConnObj();
      }
    }
    return null;
  }

  // helper function to find the entity of the many side in a One-Many relationship
  public static Entity getParentEntity(Entity childEntity, Schema schema) {
    Assert.assertSame(childEntity.getEntityType(), EntityType.STRONG);
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

  // helper function to find a list of ERConnectableObj in relationship with a given ERConnectableObj
  // including the relationship itself
  // so basically any tables that can be joined with the current table
  public static List<ERConnectableObj> tablesInRelationshipWith(ERConnectableObj table, Schema schema) {
    if (table instanceof Entity && ((Entity) table).getEntityType() == EntityType.WEAK) {
      return List.of(Objects.requireNonNull(getRelatedStrongEntity((Entity) table, schema)));
    }
    Set<ERConnectableObj> result = new HashSet<>();
    for (Relationship relationship : schema.getRelationshipList()) {
      if (relationship == table) {
        return relationship.getEdgeList().stream().map(RelationshipEdge::getConnObj).toList();
      }
      boolean flag = false;
      Entity tempEntity = null;
      for (RelationshipEdge edge : relationship.getEdgeList()) {
        if (edge.getConnObj() == table) {
          flag = true;
          // exclude reflexive relationships, because the table itself will be
          // added anyway in the upstream function
          if (tempEntity != null && tempEntity != table) {
            result.add(tempEntity);
          }
          if (relationship.getAttributeList().size() > 0) {
            result.add(relationship);
          }
          tempEntity = (Entity) edge.getConnObj();
          continue;
        }
        if (flag) {
          result.add(edge.getConnObj());
          if (relationship.getAttributeList().size() > 0) {
            result.add(relationship);
          }
        }
        tempEntity = (Entity) edge.getConnObj();
      }
    }
    return new ArrayList<>(result);
  }

  // helper function to find the relationship table name between two entities
  public static String getRelationshipNameBetween(String entityName1, String entityName2, Schema schema) {
    for (Relationship relationship : schema.getRelationshipList()) {
      boolean flag1 = false;
      boolean flag2 = false;
      for (RelationshipEdge edge : relationship.getEdgeList()) {
        if (edge.getConnObj().getName().equals(entityName1)) {
          flag1 = true;
        }
        if (edge.getConnObj().getName().equals(entityName2)) {
          flag2 = true;
        }
      }
      if (flag1 && flag2) {
        return relationship.getName();
      }
    }
    return "";
  }
}
