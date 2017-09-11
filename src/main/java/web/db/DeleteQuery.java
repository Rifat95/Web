package web.db;

import java.sql.SQLException;
import web.core.Entity;
import web.util.ServerException;

public final class DeleteQuery<E extends Entity<E>> extends Query<E, DeleteQuery<E>> {
	public DeleteQuery(Class<E> entityClass) {
		super(entityClass);
	}

	public void execute() {
		try {
			prepareStatemment("DELETE FROM " + table + " WHERE " + conditions);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new ServerException();
		} finally {
			clean();
		}
	}
}
