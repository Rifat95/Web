package web.db;

import java.sql.SQLException;
import web.core.Entity;
import web.util.ServerException;

public final class UpdateQuery<E extends Entity<E>> extends Query<E, UpdateQuery<E>> {
	private String fields;

	public UpdateQuery(Class<E> entityClass) {
		super(entityClass);
		fields = "";
	}

	public UpdateQuery<E> set(String field, Object value) {
		fields += " " + field + " = ?,";
		values.add(value);
		return this;
	}

	public void execute() {
		try {
			// Remove the last comma from fields
			prepareStatemment("UPDATE " + table + " SET" + fields.substring(0, fields.length() - 1)
				+ " WHERE " + conditions);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new ServerException();
		} finally {
			clean();
		}
	}
}
