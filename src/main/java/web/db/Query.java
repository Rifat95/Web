package web.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import web.core.App;
import web.core.Entity;
import web.util.StringMap;

/**
 * @param <E> the entity type
 * @param <T> the query type (for method chaining)
 */
public abstract class Query<E extends Entity<E>, T extends Query<E, T>> {
	protected Class<E> entityClass;
	protected StringMap settings;
	protected String table;
	protected String fields;
	protected String conditions;
	protected ArrayList<Object> conditionValues;
	protected PreparedStatement statement;
	protected ResultSet result;

	private T instance;

	@SuppressWarnings("unchecked")
	public Query(Class<E> entityClass) {
		this.entityClass = entityClass;
		table = entityClass.getSimpleName();
		fields = "*";
		conditions = "1 = 1";
		conditionValues = new ArrayList<>();
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

	public final T setFields(String fields) {
		this.fields = fields;
		return instance;
	}

	public final T addCondition(String field, String comparator, Object value) {
		conditions += " AND " + field + " " + comparator + " ?";
		conditionValues.add(value);
		return instance;
	}

	protected final void clean() {
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
			}
		}
	}

	/**
	 * The clean() method is not called here because classes like SelectQuery needs the ResultSet
	 * after the execution of the statement. Childrens must call the clean() method manually.
	 *
	 * @throws SQLException
	 */
	protected void execute() throws SQLException {
		String sql = getSql();
		statement = App.getInstance().getConnection().prepareStatement(sql);

		int i = 1;
		for (Object value : conditionValues) {
			statement.setObject(i, value);
			i++;
		}

		result = statement.executeQuery();
	}

	protected abstract String getSql();
}
