package web.db;

import java.sql.SQLException;
import web.core.Entity;
import web.util.ServerException;

public final class InsertQuery<E extends Entity<E>> extends Query<E, InsertQuery<E>> {
	private String fields;
	private String markups;

	public InsertQuery(Class<E> entityClass) {
		super(entityClass);
		fields = "";
		markups = "";
	}

	public InsertQuery<E> set(String field, Object value) {
		fields += field + ", ";
		markups += "?, ";
		values.add(value);

		return this;
	}

	/**
	 * @return the inserted id
	 */
	public int execute() {
		int id = 0;

		try {
			// Remove the last comma and space from fields and markups
			prepareStatemmentWithKeys("INSERT INTO " + table + "("
				+ fields.substring(0, fields.length() - 2) + ") VALUES ("
				+ markups.substring(0, markups.length() - 2) + ")");
			statement.executeUpdate();
			result = statement.getGeneratedKeys();

			if (result.next()) {
				id = result.getInt(1);
			}
		} catch (SQLException e) {
			throw new ServerException();
		} finally {
			clean();
		}

		return id;
	}
}
