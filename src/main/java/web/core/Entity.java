package web.core;

import java.util.HashMap;
import web.db.DeleteQuery;
import web.db.InsertQuery;
import web.db.SelectQuery;
import web.db.UpdateQuery;

/**
 * Childrens can define a StringMap to alter queries, example:
 * public static final StringMap SETTINGS = new StringMap()
 * .put("conditions", "WHERE status = 'published' AND amount > 0")
 * .put("joins", "LEFT JOIN User ON User.id = EntityX.authorId")
 * .put("order", "EntityX.releaseDate DESC");
 *
 * @param <T> the entity type (for method chaining)
 */
public abstract class Entity<T extends Entity<T>> {
	protected App app;
	protected Page page;
	protected Translator t;
	protected HashMap<String, Object> data;
	protected boolean valid;

	private T instance;
	private Class<T> instanceClass;
	private String template;

	@SuppressWarnings("unchecked")
	public Entity() {
		app = App.getInstance();
		page = app.getPage();
		t = app.getT();
		data = new HashMap<>();
		valid = true;
		instance = (T) this;
		instanceClass = (Class<T>) getClass();
		template = getClass().getSimpleName().toLowerCase();
	}

	public final Object get(String var) {
		return data.get(var);
	}

	public final T set(String var, Object value) {
		data.put(var, value);
		return instance;
	}

	public final T setTemplate(String name) {
		template = name;
		return instance;
	}

	public final T fetch(String id) {
		fetch(Integer.parseInt(id));
		return instance;
	}

	public final T fetch(int id) {
		data = new SelectQuery<>(instanceClass)
			.addCondition("id", "=", id)
			.getData();

		return instance;
	}

	public final T save() {
		if (data.containsKey("id")) {
			UpdateQuery<T> uQuery = new UpdateQuery<>(instanceClass);
			data.entrySet().forEach((entry) -> {
				uQuery.set(entry.getKey(), entry.getValue());
			});

			uQuery.addCondition("id", "=", data.get("id")).execute();
		} else {
			InsertQuery<T> iQuery = new InsertQuery<>(instanceClass);
			data.entrySet().forEach((entry) -> {
				iQuery.set(entry.getKey(), entry.getValue());
			});

			int id = iQuery.execute();
			data.put("id", id);
		}

		return instance;
	}

	public final void delete() {
		new DeleteQuery<>(instanceClass)
			.addCondition("id", "=", data.get("id"))
			.execute();

		data.clear();
	}

	@Override
	public final String toString() {
		data.put("this", this);
		return new View(template, View.ENTITY)
			.setData(data)
			.toString();
	}
}
