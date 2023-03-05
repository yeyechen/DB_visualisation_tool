package com.example.demo.mondial;

import java.util.ArrayList;
import java.util.List;

public class MetaData {

  private final List<String> primaryKeys;
  private final List<String> foreignKeys;
  private final List<String> attributes;

  public MetaData() {
    this.primaryKeys = new ArrayList<>();
    this.foreignKeys = new ArrayList<>();
    this.attributes = new ArrayList<>();
  }

  public MetaData(List<String> primaryKeys, List<String> foreignKeys,
      List<String> attributes) {
    this.primaryKeys = primaryKeys;
    this.foreignKeys = foreignKeys;
    this.attributes = attributes;
  }

  public MetaData(List<String> primaryKeys, List<String> attributes) {
    this.primaryKeys = primaryKeys;
    this.foreignKeys = new ArrayList<>();
    this.attributes = attributes;
  }

  public List<String> getPrimaryKeys() {
    return primaryKeys;
  }

  public List<String> getForeignKeys() {
    return foreignKeys;
  }

  public List<String> getAttributes() {
    return attributes;
  }

  public void addPrimaryKey(String key) {
    if (key != null) {
      this.primaryKeys.add(key);
    }
  }

  public void addForeignKey(String key) {
    if (key != null) {
      this.foreignKeys.add(key);
    }
  }

  public void addAttributes(String attr) {
    if (attr != null) {
      this.attributes.add(attr);
    }
  }

  @Override
  public String toString() {
    return "MetaData{" +
        "primaryKeys=" + primaryKeys +
        ", foreignKeys=" + foreignKeys +
        ", attributes=" + attributes +
        '}';
  }
}
