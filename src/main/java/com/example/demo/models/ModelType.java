package com.example.demo.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The different types of ER models
 */
@AllArgsConstructor
@Getter
public enum ModelType {
  UNKNOWN,
  BASIC_ENTITY,
  WEAK_ENTITY,
  ONE_MANY_RELATIONSHIP,
  MANY_MANY_RELATIONSHIP,
  REFLEXIVE_RELATIONSHIP
}
