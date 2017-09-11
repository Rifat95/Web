package web.db;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import web.core.Entity;
import web.util.NotFoundException;
import web.util.ServerException;

public final class SelectQuery<E extends Entity<E>> extends Query<E, SelectQuery<E>> {
	private String fields;
	private String joins;
	private String groupBy;
	private String order;
	private String limit;

	public SelectQuery(Class<E> entityClass) {
		super(entityClass);
		fields = "*";
		joins = "";
		groupBy = "";
		order = "";
		limit = "";

		if (!settings.isEmpty()) {
			if (settings.contains("fields")) {
				fields = settings.get("fields");
			}
			if (settings.contains("conditions")) {
				conditions = settings.get("conditions");
			}
			if (settings.contains("joins")) {
				joins = " " + settings.get("joins");
			}
			if (settings.contains("groupBy")) {
				groupBy = " " + settings.get("groupBy");
			}
			if (settings.contains("order")) {
				order = " " + settings.get("order");
			}
			if (settings.contains("limit")) {
				limit = " " + settings.get("limit");
			}
		}
	}

	public SelectQuery<E> setFields(String fields) {
		this.fields = fields;
		return this;
	}

	public SelectQuery<E> addInerJoin(String table, String tableField, String localField) {
		joins += " INNER JOIN " + table
			+ " ON " + table + "." + tableField + " = " + this.table + "." + localField;
		return this;
	}

	public SelectQuery<E> addLeftJoin(String table, String tableField, String localField) {
		joins += " LEFT JOIN " + table
			+ " ON " + table + "." + tableField + " = " + this.table + "." + localField;
		return this;
	}

	public SelectQuery<E> addRightJoin(String table, String tableField, String localField) {
		joins += " RIGHT JOIN " + table
			+ " ON " + table + "." + tableField + " = " + this.table + "." + localField;
		return this;
	}

	public SelectQuery<E> setGroupBy(String fields) {
		groupBy = " GROUP BY " + fields;
		return this;
	}

	public SelectQuery<E> setOrder(String fields) {
		order = " ORDER BY " + fields;
		return this;
	}

	public SelectQuery<E> setLimit(int start, int nbEntry) {
		limit = " LIMIT " + start + " OFFSET " + nbEntry;
		return this;
	}

	public int count() {
		int nbLine = 0;

		try {
			String tmp = fields;
			fields = "COUNT(*) as nbLine";
			execute();
			fields = tmp;

			if (result.next()) {
				nbLine = result.getInt("nbLine");
			}
		} catch (SQLException e) {
			throw new ServerException();
		} finally {
			clean();
		}

		return nbLine;
	}

	public E get() {
		E entity = null;

		try {
			execute();
			if (result.next()) {
				entity = entityClass.newInstance();
				ResultSetMetaData meta = result.getMetaData();
				int nbColumn = meta.getColumnCount();

				for (int i = 1; i <= nbColumn; i++) {
					entity.set(meta.getColumnName(i), result.getObject(i));
				}
			}
		} catch (Exception e) {
			throw new ServerException();
		} finally {
			clean();
		}

		if (entity == null) {
			throw new NotFoundException();
		} else {
			return entity;
		}
	}

	public ArrayList<E> getAll() {
		ArrayList<E> entities = new ArrayList<>();

		try {
			execute();
			while (result.next()) {
				E entity = entityClass.newInstance();
				ResultSetMetaData meta = result.getMetaData();
				int nbColumn = meta.getColumnCount();

				for (int i = 1; i <= nbColumn; i++) {
					entity.set(meta.getColumnName(i), result.getObject(i));
				}

				entities.add(entity);
			}
		} catch (Exception e) {
			throw new ServerException();
		} finally {
			clean();
		}

		return entities;
	}

	public HashMap<String, Object> getData() {
		HashMap<String, Object> data = null;

		try {
			execute();
			if (result.next()) {
				data = new HashMap<>();
				ResultSetMetaData meta = result.getMetaData();
				int nbColumn = meta.getColumnCount();

				for (int i = 1; i <= nbColumn; i++) {
					data.put(meta.getColumnName(i), result.getObject(i));
				}
			}
		} catch (SQLException e) {
			throw new ServerException();
		} finally {
			clean();
		}

		if (data == null) {
			throw new NotFoundException();
		} else {
			return data;
		}
	}

	private void execute() throws SQLException {
		prepareStatemment("SELECT " + fields + " FROM " + table + joins + " WHERE " + conditions
			+ groupBy + order + limit);
		result = statement.executeQuery();
	}
}
