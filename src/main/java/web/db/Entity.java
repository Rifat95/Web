package web.db;

import java.util.HashMap;
import java.util.Map;
import web.annotations.Table;
import web.core.App;
import web.core.Translator;

@Table(name = "", primaryKeys = {})
public abstract class Entity {
  protected App app;
  protected Translator t;
  protected Map<String, Object> data;

  private boolean isNew;
  private String table;
  private String[] primaryKeys;

  public Entity() {
    app = App.getInstance();
    t = app.getT();
    data = new HashMap<>();
    isNew = true;

    Table annotation = getClass().getAnnotation(Table.class);
    table = annotation.name();
    primaryKeys = annotation.primaryKeys();
  }

  public final Object get(String attribute) {
    return data.get(attribute);
  }

  @SuppressWarnings("unchecked")
  public <T extends Entity> T set(String attribute, Object value) {
    data.put(attribute, value);
    return (T) this;
  }

  /**
   * @todo Handle update when there is other fields coming from joins
   */
  public final void save() {
    if (isNew) {
      // Insert
      InsertQuery iQuery = new InsertQuery(table);
      data.entrySet().forEach((entry) -> {
        iQuery.set(entry.getKey(), entry.getValue());
      });

      int generatedKey = iQuery.execute();
      if (generatedKey != 0) {
        data.put(primaryKeys[0], generatedKey); // If generated key exists it should be the first primary key
      }
    } else {
      // Update
      UpdateQuery uQuery = new UpdateQuery(table);
      data.entrySet().forEach((entry) -> {
        uQuery.set(entry.getKey(), entry.getValue());
      });

      for (String pk : primaryKeys) {
        uQuery.where(pk, "=", data.get(pk));
      }

      uQuery.execute();
    }
  }

  public final void delete() {
    DeleteQuery dQuery = new DeleteQuery(table);
    for (String pk : primaryKeys) {
      dQuery.where(pk, "=", data.get(pk));
    }

    dQuery.execute();
  }

  protected final void hydrate(Map<String, Object> data) {
    this.data = data;
    isNew = false;
  }
}
