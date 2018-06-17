package web.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import web.core.App;

/**
 * @param <T> the query type for method chaining
 *
 * @todo Handle table name escaping for all database engines
 */
public abstract class Query<T extends Query<T>> {
  protected String table;
  protected Connection connection;
  protected PreparedStatement statement;
  protected List<Object> statementValues;
  protected String conditions;

  public Query(String table) {
    this.table = table;
    connection = App.getInstance().getConnection();
    statementValues = new ArrayList<>();
    conditions = "1 = 1";
  }

  @SuppressWarnings("unchecked")
  public final T where(String field, String comparator, Object value) {
    conditions += " AND " + escape(field) + " " + comparator + " ?";
    statementValues.add(value);
    return (T) this;
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
