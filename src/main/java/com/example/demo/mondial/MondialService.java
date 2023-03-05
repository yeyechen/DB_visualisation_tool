package com.example.demo.mondial;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class MondialService {

  public static final String TABLE_NAME = "economy";

  public MetaData getMetaData(Connection connection, String tableName)
      throws SQLException {
    DatabaseMetaData metaData = connection.getMetaData();
    ResultSet columns = metaData.getColumns(connection.getCatalog(), null, tableName, null);
    ResultSet foreignKeys = metaData.getImportedKeys(connection.getCatalog(), null, tableName);
    ResultSet primaryKey = metaData.getPrimaryKeys(connection.getCatalog(), null, tableName);

    Set<String> keySet = new HashSet<>();
    List<String> keyList = new ArrayList<>();
    MetaData result = new MetaData();

    // process primary keys
    while (primaryKey.next()) {
      primaryKey.getString("PK_NAME");
      String pkColumnName = primaryKey.getString("COLUMN_NAME");
      keySet.add(pkColumnName);
      keyList.add(pkColumnName);
    }
    // add formatted primary keys to result
    result.addPrimaryKey(tupleToString(keyList));

    // process foreign keys
    keyList = new ArrayList<>();
    List<String> toList = new ArrayList<>();
    String keyName = "";
    while (foreignKeys.next()) {
      String currKeyName = foreignKeys.getString("FK_NAME");
      // checking weather compound key or separate key.
      // if separate key, add current info to result, clear current list and start new
      if (!keyName.equals(currKeyName)) {
        keyName = currKeyName;
        // add foreign keys to result
        result.addForeignKey(foreignKeyToString(keyList, toList));
        keyList = new ArrayList<>();
        toList = new ArrayList<>();
      }
      String fkTableName = foreignKeys.getString("FKTABLE_NAME");
      String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
      String pkTableName = foreignKeys.getString("PKTABLE_NAME");
      String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
      String from = fkTableName + "." + fkColumnName;
      String to = pkTableName + "." + pkColumnName;
      keyList.add(from);
      toList.add(to);
      keySet.add(pkColumnName);
    }
    result.addForeignKey(foreignKeyToString(keyList, toList));

    // process attributes (non-key columns)
    while (columns.next()) {
      String attr = columns.getString("COLUMN_NAME");
      if (!keySet.contains(attr)) {
        result.addAttributes(attr);
      }
    }
    return result;
  }

  private String tupleToString(List<String> primaryKeys) {
    if (primaryKeys.size() == 0) {
      return null;
    } else if (primaryKeys.size() == 1) {
      return primaryKeys.get(0);
    } else {
      StringBuilder sb = new StringBuilder("(");
      for (String key : primaryKeys) {
        sb.append(key).append(",");
      }
      sb.deleteCharAt(sb.length() - 1);
      sb.append(")");
      return sb.toString();
    }
  }

  // formulate a SINGLE foreign key, could be compound, that's why we use List
  // for example: (country.capital, country.name, country.code) -> (city.name, city.province, city.country)
  // in Mondial "country" table
  private String foreignKeyToString(List<String> from, List<String> to) {
    assert from.size() == to.size();
    if (from.size() == 0) {
      return null;
    } else if (from.size() == 1) {
      return from.get(0) + "->" + to.get(0);
    } else {
      // the "from" group
      StringBuilder sb = new StringBuilder("(");
      for (String key : from) {
        sb.append(key).append(",");
      }
      sb.deleteCharAt(sb.length() - 1);
      sb.append(")");

      sb.append("->");

      // the "to" group
      sb.append("(");
      for (String key : to) {
        sb.append(key).append(",");
      }
      sb.deleteCharAt(sb.length() - 1);
      sb.append(")");

      return sb.toString();
    }
  }
}
