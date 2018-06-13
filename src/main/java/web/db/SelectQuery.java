package web.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import web.util.NotFoundException;
import web.util.ServerException;

/**
 * @param <E> the entity type
 */
public final class SelectQuery<E extends Entity> extends Query<E, SelectQuery<E>> {
  private String fields;
  private String joins;
  private String groupBy;
  private String order;
  private String limit;

  public SelectQuery(Class<E> entityClass) {
    super(entityClass);
    fields = "*";
    joins = "";
    groupBy = "";
    order = "";
    limit = "";
  }

  public SelectQuery<E> fields(String fields) {
    this.fields = fields;
    return this;
  }

  public SelectQuery<E> inerJoin(String table, String tableField, String localField) {
    join("INNER JOIN", table, tableField, localField);
    return this;
  }

  public SelectQuery<E> leftJoin(String table, String tableField, String localField) {
    join("LEFT JOIN", table, tableField, localField);
    return this;
  }

  public SelectQuery<E> rightJoin(String table, String tableField, String localField) {
    join("RIGHT JOIN", table, tableField, localField);
    return this;
  }

  public SelectQuery<E> groupBy(String fields) {
    groupBy = " GROUP BY " + fields;
    return this;
  }

  public SelectQuery<E> order(String fields) {
    order = " ORDER BY " + fields;
    return this;
  }

  public SelectQuery<E> limit(int start, int nbEntry) {
    limit = " LIMIT " + start + " OFFSET " + nbEntry;
    return this;
  }

  public int count() {
    ResultSet result = null;
    int nbLine = 0;

    try {
      String tmp = fields;
      fields = "COUNT(*) as nbLine";
      result = getResultSet();
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

  public void fill(Entity entity) {
    ResultSet result = null;
    boolean noResult = true;

    try {
      result = getResultSet();
      if (result.next()) {
        entity.init(result);
        noResult = false;
      }
    } catch (SQLException e) {
      throw new ServerException(e);
    } finally {
      clean(result);
    }

    if (noResult) {
      throw new NotFoundException();
    }
  }

  public E get() {
    ResultSet result = null;
    E entity = null;

    try {
      result = getResultSet();
      if (result.next()) {
        entity = entityClass.newInstance();
        entity.init(result);
      }
    } catch (IllegalAccessException | InstantiationException | SQLException e) {
      throw new ServerException(e);
    } finally {
      clean(result);
    }

    if (entity == null) {
      throw new NotFoundException();
    } else {
      return entity;
    }
  }

  public ArrayList<E> getAll() {
    ResultSet result = null;
    ArrayList<E> entities = new ArrayList<>();

    try {
      result = getResultSet();
      while (result.next()) {
        E entity = entityClass.newInstance();
        entity.init(result);
        entities.add(entity);
      }
    } catch (IllegalAccessException | InstantiationException | SQLException e) {
      throw new ServerException(e);
    } finally {
      clean(result);
    }

    return entities;
  }

  private void join(String type, String table, String tableField, String localField) {
    joins += " " + type + " " + escape(table) + " ON " + escape(table) + "." + escape(tableField)
        + " = " + escape(this.table) + "." + escape(localField);
  }

  private ResultSet getResultSet() throws SQLException {
    prepareStatemment("SELECT " + fields + " FROM " + escape(table) + joins + " WHERE " + conditions
        + groupBy + order + limit);

    return statement.executeQuery();
  }
}
