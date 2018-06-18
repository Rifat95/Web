package web.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import web.exception.ServerException;

public final class InsertQuery extends Query<InsertQuery> {
  private String fields;
  private String markups;

  public InsertQuery(String table) {
    super(table);
    fields = "";
    markups = "";
  }

  public InsertQuery set(String field, Object value) {
    fields += escape(field) + ", ";
    markups += "?, ";
    statementValues.add(value);
    return this;
  }

  /**
   * @return the generated key
   */
  public int execute() {
    ResultSet result = null;
    int id = 0;

    try {
      // Remove the last comma and space from fields and markups
      prepareStatemment("INSERT INTO " + escape(table) + "("
          + fields.substring(0, fields.length() - 2) + ") VALUES ("
          + markups.substring(0, markups.length() - 2) + ")");

      statement.executeUpdate();
      result = statement.getGeneratedKeys();

      if (result.next()) {
        id = result.getInt(1);
      }
    } catch (SQLException e) {
      throw new ServerException(e);
    } finally {
      clean(result);
    }

    return id;
  }
}
