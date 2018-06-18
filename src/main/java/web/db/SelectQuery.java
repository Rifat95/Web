package web.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import web.exception.NotFoundException;
import web.exception.ServerException;

/**
 * @todo Handle default joins, sort, groupBy etc with annoations in entities
 * @todo Manage default fields selection because primary keys should always be selected ?
 */
public final class SelectQuery extends Query<SelectQuery> {
  private String fields;
  private String joins;
  private String groupBy;
  private String order;
  private String limit;

  public SelectQuery(String table) {
    super(table);
    fields = "*";
    joins = "";
    groupBy = "";
    order = "";
    limit = "";
  }

  public SelectQuery fields(String fields) {
    this.fields = fields;
    return this;
  }

  public SelectQuery inerJoin(String joinTable, String joinField, String field) {
    join("INNER JOIN", joinTable, joinField, field);
    return this;
  }

  public SelectQuery leftJoin(String joinTable, String joinField, String field) {
    join("LEFT JOIN", joinTable, joinField, field);
    return this;
  }

  public SelectQuery rightJoin(String joinTable, String joinField, String field) {
    join("RIGHT JOIN", joinTable, joinField, field);
    return this;
  }

  public SelectQuery groupBy(String fields) {
    groupBy = " GROUP BY " + fields;
    return this;
  }

  public SelectQuery order(String fields) {
    order = " ORDER BY " + fields;
    return this;
  }

  public SelectQuery limit(int start, int nbEntry) {
    limit = " LIMIT " + start + " OFFSET " + nbEntry;
    return this;
  }

  public int count() {
    int nbLine = 0;
    ResultSet result = null;

    try {
      String tmp = fields;
      fields = "COUNT(*) as nbLine";
      result = execute();
      fields = tmp;

      if (result.next()) {
        nbLine = result.getInt("nbLine");
      }
    } catch (SQLException e) {
      throw new ServerException(e);
    } finally {
      clean(result);
    }

    return nbLine;
  }

  public Map<String, Object> get() {
    Map<String, Object> data = null;
    ResultSet result = null;

    try {
      result = execute();

      if (result.next()) {
        data = getMap(result);
      } else {
        throw new NotFoundException();
      }
    } catch (SQLException e) {
      throw new ServerException(e);
    } finally {
      clean(result);
    }

    return data;
  }

  public <T extends Entity> T get(Class<T> entityClass) {
    T entity = null;
    ResultSet result = null;

    try {
      result = execute();

      if (result.next()) {
        entity = entityClass.newInstance();
        entity.hydrate(getMap(result));
      } else {
        throw new NotFoundException();
      }
    } catch (IllegalAccessException | InstantiationException | SQLException e) {
      throw new ServerException(e);
    } finally {
      clean(result);
    }

    return entity;
  }

  public List<Map<String, Object>> getAll() {
    List<Map<String, Object>> data = new ArrayList<>();
    ResultSet result = null;

    try {
      result = execute();

      while (result.next()) {
        data.add(getMap(result));
      }
    } catch (SQLException e) {
      throw new ServerException(e);
    } finally {
      clean(result);
    }

    return data;
  }

  public <T extends Entity> List<T> getAll(Class<T> entityClass) {
    List<T> entities = new ArrayList<>();
    ResultSet result = null;

    try {
      result = execute();

      while (result.next()) {
        T entity = entityClass.newInstance();
        entity.hydrate(getMap(result));
        entities.add(entity);
      }
    } catch (IllegalAccessException | InstantiationException | SQLException e) {
      throw new ServerException(e);
    } finally {
      clean(result);
    }

    return entities;
  }

  private void join(String type, String joinTable, String joinField, String field) {
    joins += " " + type + " " + escape(joinTable) + " ON " + escape(joinTable) + "." + escape(joinField)
        + " = " + escape(table) + "." + escape(field);
  }

  private ResultSet execute() throws SQLException {
    prepareStatemment("SELECT " + fields + " FROM " + escape(table) + joins + " WHERE " + conditions
        + groupBy + order + limit);

    return statement.executeQuery();
  }

  private Map<String, Object> getMap(ResultSet result) throws SQLException {
    Map<String, Object> map = new HashMap<>();
    ResultSetMetaData metadata = result.getMetaData();
    int nbColumn = metadata.getColumnCount();

    for (int i = 1; i <= nbColumn; i++) {
      map.put(metadata.getColumnLabel(i), result.getObject(i));
    }

    return map;
  }
}
