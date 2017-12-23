package web.core;

import java.util.HashMap;
import web.db.DeleteQuery;
import web.db.InsertQuery;
import web.db.SelectQuery;
import web.db.UpdateQuery;

/**
 * Childrens can define a StringMap to alter queries, example:
 * public static final StringMap SETTINGS = new StringMap()
 *   .put("conditions", "WHERE status = 'published' AND amount > 0")
 *   .put("joins", "LEFT JOIN User ON User.id = EntityX.authorId")
 *   .put("order", "EntityX.releaseDate DESC");
 */
public abstract class Entity {
	protected App app;
	protected Page page;
	protected Translator t;
	protected HashMap<String, Object> data;
	protected boolean valid;

	public Entity() {
		app = App.getInstance();
		page = app.getPage();
		t = app.getT();
		data = new HashMap<>();
		valid = true;
	}

	public final Object get(String var) {
		return data.get(var);
	}

	public final void set(String var, Object value) {
		data.put(var, value);
	}

	public final void fetch(String id) {
		fetch(Integer.parseInt(id));
	}

	public final void fetch(int id) {
		data = new SelectQuery<>(getClass())
			.addCondition("id", "=", id)
			.getData();
	}

	public final void save() {
		if (data.containsKey("id")) {
			UpdateQuery<?> uQuery = new UpdateQuery<>(getClass());
			data.entrySet().forEach((entry) -> {
				uQuery.set(entry.getKey(), entry.getValue());
			});

			uQuery.addCondition("id", "=", data.get("id")).execute();
		} else {
			InsertQuery<?> iQuery = new InsertQuery<>(getClass());
			data.entrySet().forEach((entry) -> {
				iQuery.set(entry.getKey(), entry.getValue());
			});

			int id = iQuery.execute();
			data.put("id", id);
		}
	}

	public final void delete() {
		new DeleteQuery<>(getClass())
			.addCondition("id", "=", data.get("id"))
			.execute();

		data.clear();
	}

	/**
	 * This method is called by SelectQuery after data insertion in entity, childrens can override
	 * it to alter data.
	 */
	public void init() {}
}
