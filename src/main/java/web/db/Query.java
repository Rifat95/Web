package web.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import web.core.App;
import web.core.Entity;
import web.util.StringMap;

/**
 * @param <E> the entity type
 * @param <T> the query type, for method chaining
 */
public abstract class Query<E extends Entity<E>, T extends Query<E, T>> {
	protected Class<E> entityClass;
	protected StringMap settings;
	protected String table;
	protected String conditions;
	protected ArrayList<Object> values;
	protected PreparedStatement statement;
	protected ResultSet result;

	private T instance;

	@SuppressWarnings("unchecked")
	public Query(Class<E> entityClass) {
		this.entityClass = entityClass;
		table = entityClass.getSimpleName();
		conditions = "1 = 1";
		values = new ArrayList<>();
		instance = (T) this;

		try {
			settings = (StringMap) entityClass.getField("SETTINGS").get(null);
			if (settings.contains("table")) {
				table = settings.get("table");
			}
		} catch (Exception e) {
			settings = new StringMap();
		}
	}

	public final T addCondition(String field, String comparator, Object value) {
		conditions += " AND `" + field + "` " + comparator + " ?";
		values.add(value);
		return instance;
	}

	protected final void prepareStatemment(String sql) throws SQLException {
		statement = App.getInstance().getConnection().prepareStatement(sql);

		int i = 1;
		for (Object value : values) {
			statement.setObject(i, value);
			i++;
		}
	}

	protected final void prepareStatemmentWithKeys(String sql) throws SQLException {
		statement = App.getInstance().getConnection()
			.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

		int i = 1;
		for (Object value : values) {
			statement.setObject(i, value);
			i++;
		}
	}

	protected final void clean() {
		if (result != null) {
			try {
				result.close();
			} catch (SQLException e) {
				// Ignore
			} finally {
				result = null;
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
