package com.example.demo.models;

import static com.example.demo.models.ModelType.BASIC_ENTITY;
import static com.example.demo.models.ModelType.MANY_MANY_RELATIONSHIP;
import static com.example.demo.models.ModelType.ONE_MANY_RELATIONSHIP;
import static com.example.demo.models.ModelType.REFLEXIVE_RELATIONSHIP;
import static com.example.demo.models.ModelType.UNKNOWN;
import static com.example.demo.models.ModelType.WEAK_ENTITY;

import com.example.demo.input.handler.InputService;
import com.example.demo.visualisation.VisualService;
import io.github.MigadaTang.Attribute;
import io.github.MigadaTang.ERBaseObj;
import io.github.MigadaTang.ERConnectableObj;
import io.github.MigadaTang.Entity;
import io.github.MigadaTang.Relationship;
import io.github.MigadaTang.RelationshipEdge;
import io.github.MigadaTang.Schema;
import io.github.MigadaTang.common.Cardinality;
import io.github.MigadaTang.common.EntityType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.junit.Assert;

public class ModelUtil {

  public static ModelType patternMatchBasedOnSelection(
      Map<ERConnectableObj, List<Attribute>> selectionInfo, Schema schema) {
    // max number of table user can choose: 2
    if (selectionInfo.keySet().size() > 1) {
      // strictly based on the model: one table contains attribute info, the other table can only be
      // selected by the key
      ERConnectableObj attrTable;
      ERConnectableObj keyTable;
      Iterator<ERConnectableObj> keyIterator = selectionInfo.keySet().iterator();

      ERConnectableObj table1 = keyIterator.next();
      ERConnectableObj table2 = keyIterator.next();

      if (table1 instanceof Relationship || table2 instanceof Relationship) {
        return UNKNOWN;
      }

      // classify
      Iterator<Attribute> tableNameIterator = selectionInfo.get(table1).iterator();
      if (tableNameIterator.hasNext() && tableNameIterator
          .next().getIsPrimary()) {
        attrTable = table2;
        keyTable = table1;
      } else {
        attrTable = table1;
        keyTable = table2;
      }

      Relationship relationship = getRelationshipBetween(attrTable.getName(), keyTable.getName(),
          schema);

      Optional<RelationshipEdge> attrEdge = relationship.getEdgeList().stream()
          .filter(edge -> edge.getConnObj() == attrTable).findFirst();
      Optional<RelationshipEdge> keyEdge = relationship.getEdgeList().stream()
          .filter(edge -> edge.getConnObj() == keyTable).findFirst();

      if (attrEdge.isEmpty() || keyEdge.isEmpty()) {
        return UNKNOWN;
      } else {
        // check paper, only in this situation we have a One-Many Relationship, otherwise we treat
        // every selection UNKNOWN, suggesting no visualisation
        if ((attrEdge.get().getCardinality() == Cardinality.OneToOne
            || attrEdge.get().getCardinality() == Cardinality.ZeroToOne) && (
            keyEdge.get().getCardinality() == Cardinality.OneToMany || keyEdge.get()
                .getCardinality() == Cardinality.ZeroToMany)) {
          InputService.getSelectionInfo().remove(keyTable);
          return ONE_MANY_RELATIONSHIP;
        }
        return UNKNOWN;
      }
    } else {
      return ModelUtil.patternMatch(selectionInfo.keySet().iterator().next(), schema);
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
      // handle Subset case, treat Subset the same as Basic Entity, with extra pk from the main entity
      if (entity.getEntityType() == EntityType.SUBSET) {
        try {
          String foreignKey = VisualService.getForeignKeyName(entity.getName(),
              entity.getBelongStrongEntity().getName());
          if (!entity.getAttributeList().stream().map(ERBaseObj::getName).toList().contains(foreignKey)) {
            entity.addPrimaryKey(foreignKey,
                io.github.MigadaTang.common.DataType.TEXT);
          }
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
      if (entity.getEntityType() == EntityType.UNKNOWN) {
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

  // helper function to find a list of entities in relationship (either one-many or many-many)
  // with the given entity
  public static List<Entity> inRelationshipWith(Entity entity, Schema schema) {
    if (entity.getEntityType() == EntityType.WEAK) {
      return new ArrayList<>();
    }
    List<Entity> result = new ArrayList<>();
    for (Relationship relationship : schema.getRelationshipList()) {
      boolean flag = false;
      Entity tempEntity = null;
      for (RelationshipEdge edge : relationship.getEdgeList()) {
        if (((Entity) edge.getConnObj()).getEntityType() == EntityType.WEAK) {
          continue;
        }
        if (edge.getConnObj() == entity) {
          flag = true;
          // does not allow reflexive relationships
          if (tempEntity != null && tempEntity != entity) {
            result.add(tempEntity);
          }
          tempEntity = (Entity) edge.getConnObj();
          continue;
        }
        if (flag) {
          result.add((Entity) edge.getConnObj());
        }
        tempEntity = (Entity) edge.getConnObj();
      }
    }
    return result;
  }

  // helper function to find a list of ERConnectableObj in relationship with a given ERConnectableObj
  // including the relationship itself
  // so basically any tables that can be joined with the current table
  public static List<ERConnectableObj> tablesConnectableWith(ERConnectableObj table, Schema schema) {
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
  public static Relationship getRelationshipBetween(String entityName1, String entityName2, Schema schema) {
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
        return relationship;
      }
    }
    return null;
  }
}
