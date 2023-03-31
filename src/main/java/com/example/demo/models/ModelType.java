package com.example.demo.models;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The different types of ER models
 */
@AllArgsConstructor
@Getter
public enum ModelType {
  UNKNOWN(0, "UNKNOWN"),
  BASIC_ENTITY(1, "BASIC_ENTITY"),
  WEAK_ENTITY(2, "WEAK_ENTITY"),
  ONE_MANY_RELATIONSHIP(3, "ONE_MANY_RELATIONSHIP"),
  MANY_MANY_RELATIONSHIP(4, "MANY_MANY_RELATIONSHIP"),
  REFLEXIVE_RELATIONSHIP(5, "REFLEXIVE_RELATIONSHIP");

  private final Integer code;
  private final String value;
  private static final Map<Integer, ModelType> enumFromCode;
  private static final Map<String, ModelType> enumFromValue;

  // todo: not sure if we need the functionality to find relating model based on their code and value.
  static {
    Map<Integer, ModelType> mapFromCode = new ConcurrentHashMap<>();
    Map<String, ModelType> mapFromValue = new ConcurrentHashMap<>();
    for (ModelType instance : ModelType.values()) {
      mapFromCode.put(instance.getCode(), instance);
      mapFromValue.put(instance.getValue(), instance);
    }
    enumFromCode = Collections.unmodifiableMap(mapFromCode);
    enumFromValue = Collections.unmodifiableMap(mapFromValue);
  }

  public static ModelType getFromCode(Integer code) {
    return enumFromCode.get(code);
  }

  public static ModelType getFromValue(String name) {
    return enumFromValue.get(name);
  }

}
