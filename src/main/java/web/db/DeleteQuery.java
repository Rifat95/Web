package web.db;

import java.sql.SQLException;
import web.core.Entity;
import web.util.ServerException;

public final class DeleteQuery<E extends Entity<E>> extends Query<E, DeleteQuery<E>> {
	public DeleteQuery(Class<E> entityClass) {
		super(entityClass);
	}

	@Override
	public void execute() {
		try {
			super.execute();
		} catch (SQLException e) {
			throw new ServerException();
		} finally {
			clean();
		}
	}

	@Override
	protected String getSql() {
		return "DELETE FROM " + table + " WHERE " + conditions;
	}
}
