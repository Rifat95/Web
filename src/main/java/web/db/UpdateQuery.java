package web.db;

import java.sql.SQLException;
import web.util.ServerException;

public final class UpdateQuery extends Query<UpdateQuery> {
  private String fields;

  public UpdateQuery(String table) {
    super(table);
    fields = "";
  }

  public UpdateQuery set(String field, Object value) {
    fields += " " + escape(field) + " = ?,";
    statementValues.add(value);
    return this;
  }

  public void execute() {
    try {
      // Remove the last comma from fields
      prepareStatemment("UPDATE " + escape(table) + " SET" + fields.substring(0, fields.length() - 1)
          + " WHERE " + conditions);

      statement.executeUpdate();
    } catch (SQLException e) {
      throw new ServerException(e);
    } finally {
      clean(null);
    }
  }
}
