package web.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import web.core.App;
import web.core.Translator;

public abstract class Entity {
  protected App app;
  protected Translator t;
  protected int id;
  protected HashMap<String, Object> dataToSave;

  public Entity() {
    app = App.getInstance();
    t = app.getT();
    id = 0;
    dataToSave = new HashMap<>();
  }

  public final void get(int id) {
    new SelectQuery<>(getClass())
        .where("id", "=", id)
        .fill(this);
  }

  public final void delete() {
    new DeleteQuery<>(getClass())
        .where("id", "=", id)
        .execute();
  }

  public final int getId() {
    return id;
  }

  /**
   * Children can override this method:
   * - To indicate which attributes must be saved.
   * - To check if attributes value are valid and throw exception if not.
   * - To set some default values before saving a new entity.
   */
  public void save() {
    if (id > 0) { // Update
      UpdateQuery<?> uQuery = new UpdateQuery<>(getClass());
      dataToSave.entrySet().forEach((entry) -> {
        uQuery.set(entry.getKey(), entry.getValue());
      });

      uQuery.where("id", "=", id).execute();
    } else { // Insert
      InsertQuery<?> iQuery = new InsertQuery<>(getClass());
      dataToSave.entrySet().forEach((entry) -> {
        iQuery.set(entry.getKey(), entry.getValue());
      });

      id = iQuery.execute();
    }
  }

  /**
   * Children must implement this method to set attributes.
   *
   * @param result the data returned by the select query
   *
   * @throws java.sql.SQLException
   */
  protected abstract void init(ResultSet result) throws SQLException;
}
