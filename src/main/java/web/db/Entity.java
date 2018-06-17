package web.db;

import java.util.HashMap;
import java.util.Map;
import web.annotations.Table;
import web.core.App;
import web.core.Translator;

@Table(name = "", primaryKeys = "")
public abstract class Entity {
  protected App app;
  protected Translator t;
  protected Map<String, Object> data;

  private String table;
  private String[] primaryKeys;
  private boolean isNew;

  public Entity() {
    app = App.getInstance();
    t = app.getT();
    data = new HashMap<>();

    Table tableInfos = getClass().getAnnotation(Table.class);
    table = tableInfos.name();
    primaryKeys = tableInfos.primaryKeys();
    isNew = true;
  }

  public final Object get(String attr) {
    return data.get(attr);
  }

  /**
   * @todo Handle update when there is other fields coming from joins
   */
  public final void save() {
    if (isNew) {
      // Update
      UpdateQuery uQuery = new UpdateQuery(table);
      data.entrySet().forEach((entry) -> {
        uQuery.set(entry.getKey(), entry.getValue());
      });

      for (String pk : primaryKeys) {
        uQuery.where(pk, "=", data.get(pk));
      }

      uQuery.execute();
    } else {
      // Insert
      InsertQuery iQuery = new InsertQuery(table);
      data.entrySet().forEach((entry) -> {
        iQuery.set(entry.getKey(), entry.getValue());
      });

      int generatedKey = iQuery.execute();
      if (generatedKey != 0) {
        data.put(primaryKeys[0], generatedKey); // If generated key exists there should be only one primary key
      }
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
