package web.db;

import java.sql.SQLException;
import web.util.ServerException;

public final class DeleteQuery extends Query<DeleteQuery> {
  public DeleteQuery(String table) {
    super(table);
  }

  public void execute() {
    try {
      prepareStatemment("DELETE FROM " + escape(table) + " WHERE " + conditions);
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new ServerException(e);
    } finally {
      clean(null);
    }
  }
}
