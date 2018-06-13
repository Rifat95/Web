package web.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import web.core.App;

/**
 * @param <E> the entity type
 * @param <T> the query type, for method chaining
 *
 * @todo Handle table name escaping for all database engines
 */
public abstract class Query<E extends Entity, T extends Query<E, T>> {
  protected Class<E> entityClass;
  protected Connection connection;
  protected String table;
  protected String conditions;
  protected PreparedStatement statement;
  protected ArrayList<Object> statementValues;

  private T instance;

  @SuppressWarnings("unchecked")
  public Query(Class<E> entityClass) {
    this.entityClass = entityClass;
    connection = App.getInstance().getConnection();
    table = entityClass.getSimpleName();
    conditions = "1 = 1";
    statementValues = new ArrayList<>();
    instance = (T) this;
  }

  public final T where(String field, String comparator, Object value) {
    conditions += " AND " + escape(field) + " " + comparator + " ?";
    statementValues.add(value);
    return instance;
  }

  protected final void prepareStatemment(String sql) throws SQLException {
    statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

    int i = 1;
    for (Object value : statementValues) {
      statement.setObject(i, value);
      i++;
    }
  }

  protected final String escape(String str) {
    return "`" + str + "`";
  }

  protected final void clean(ResultSet result) {
    if (result != null) {
      try {
        result.close();
      } catch (SQLException e) {
        // Ignore
      }
    }

    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException e) {
        // Ignore
      } finally {
        statement = null;
      }
    }
  }
}
