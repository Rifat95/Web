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
	protected HashMap<String, Object> rawData;
	protected boolean valid;

	public Entity() {
		app = App.getInstance();
		page = app.getPage();
		t = app.getT();
		rawData = new HashMap<>();
		valid = true;
	}

	public final int getId() {
		return (int) rawData.get("id");
	}

	public final void set(String var, Object value) {
		rawData.put(var, value);
	}

	public final void fetch(String id) {
		fetch(Integer.parseInt(id));
	}

	public final void fetch(int id) {
		rawData = new SelectQuery<>(getClass())
			.addCondition("id", "=", id)
			.getData();
	}

	public final void save() {
		if (valid) {
			if (rawData.containsKey("id")) {
				UpdateQuery<?> uQuery = new UpdateQuery<>(getClass());
				rawData.entrySet().forEach((entry) -> {
					uQuery.set(entry.getKey(), entry.getValue());
				});

				uQuery.addCondition("id", "=", rawData.get("id")).execute();
			} else {
				InsertQuery<?> iQuery = new InsertQuery<>(getClass());
				rawData.entrySet().forEach((entry) -> {
					iQuery.set(entry.getKey(), entry.getValue());
				});

				int id = iQuery.execute();
				rawData.put("id", id);
			}
		}
	}

	public final void delete() {
		new DeleteQuery<>(getClass())
			.addCondition("id", "=", rawData.get("id"))
			.execute();

		rawData.clear();
	}

	/**
	 * This method is called by SelectQuery after data insertion in entity, childrens must override
	 * it to set their attributes.
	 */
	public abstract void init();
}
