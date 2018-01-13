package web.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import web.util.ServerException;

/**
 * @param <E> the entity type
 */
public final class InsertQuery<E extends Entity> extends Query<E, InsertQuery<E>> {
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
		ResultSet result = null;
		int id = 0;

		try {
			// Remove the last comma and space from fields and markups
			prepareStatemment("INSERT INTO `" + table + "`("
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
