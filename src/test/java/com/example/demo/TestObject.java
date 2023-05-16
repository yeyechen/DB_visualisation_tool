package com.example.demo;

import io.github.MigadaTang.ER;
import io.github.MigadaTang.Entity;
import io.github.MigadaTang.Relationship;
import io.github.MigadaTang.Schema;
import io.github.MigadaTang.common.AttributeType;
import io.github.MigadaTang.common.Cardinality;
import io.github.MigadaTang.common.DataType;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;

@Getter
public class TestObject {

  private Schema mondialSchema;
  private Entity country;
  private Entity province;
  private Entity countryPop;
  private Entity continent;
  private Entity religion;
  private Entity airport;
  private Relationship borders;
  private Relationship provincePartOf;
  private Relationship countryPopPartOf;
  private Relationship encompasses;
  private Relationship believe;
  private Relationship in;

  public void setup() throws Exception {
    ER.initialize();
    // This is an example ER schema from the Mondial database (partial).
    mondialSchema = ER.createSchema("Mondial_Test");

    country = mondialSchema.addEntity("country");
    country.addPrimaryKey("code", DataType.VARCHAR);
    country.addAttribute("population", DataType.INT, AttributeType.Mandatory);
    country.addAttribute("area", DataType.DOUBLE, AttributeType.Mandatory);

    // Reflexive Relationship
    borders = mondialSchema.createRelationship("borders", country,
        country, Cardinality.OneToMany, Cardinality.OneToMany);
    borders.addAttribute("length", DataType.DOUBLE, AttributeType.Mandatory);

    // Weak Entities
    ImmutablePair<Entity, Relationship> provincePartOfPair = mondialSchema.addWeakEntity("province",
        country, "partOf", Cardinality.OneToOne, Cardinality.ZeroToMany);
    province = provincePartOfPair.left;
    provincePartOf = provincePartOfPair.right;
    province.addPrimaryKey("name", DataType.VARCHAR);
    province.addAttribute("population", DataType.INT, AttributeType.Mandatory);
    province.addAttribute("area", DataType.DOUBLE, AttributeType.Mandatory);

    ImmutablePair<Entity, Relationship> countryPopPartOfPair = mondialSchema.addWeakEntity("CountryPopulation",
        country, "partOf", Cardinality.OneToOne, Cardinality.ZeroToMany);
    countryPop = countryPopPartOfPair.left;
    countryPopPartOf = countryPopPartOfPair.right;
    countryPop.addPrimaryKey("year", DataType.VARCHAR);
    countryPop.addPrimaryKey("population", DataType.INT);

    // One-Many Relationships
    continent = mondialSchema.addEntity("continent");
    encompasses = mondialSchema.createRelationship("encompasses", continent, country,
        Cardinality.ZeroToMany, Cardinality.OneToOne); // should be (1:2), but for test purposes

    airport = mondialSchema.addEntity("airport");
    in = mondialSchema.createRelationship("in", airport, country, Cardinality.OneToOne,
        Cardinality.ZeroToMany);
    airport.addPrimaryKey("iata_code", DataType.VARCHAR);
    airport.addAttribute("name", DataType.VARCHAR, AttributeType.Mandatory);
    airport.addAttribute("country", DataType.VARCHAR, AttributeType.Mandatory);

    // Many-Many Relationship
    religion = mondialSchema.addEntity("religion");
    religion.addPrimaryKey("name", DataType.VARCHAR);
    believe = mondialSchema.createRelationship("believe", country, religion,
        Cardinality.ZeroToMany, Cardinality.OneToMany);
    believe.addAttribute("percent", DataType.DOUBLE, AttributeType.Mandatory);
  }
}
