package web.db;

import java.sql.SQLException;
import web.util.ServerException;

/**
 * @param <E> the entity type
 */
public final class UpdateQuery<E extends Entity> extends Query<E, UpdateQuery<E>> {
    private String fields;

    public UpdateQuery(Class<E> entityClass) {
        super(entityClass);
        fields = "";
    }

    public UpdateQuery<E> set(String field, Object value) {
        fields += " " + field + " = ?,";
        values.add(value);
        return this;
    }

    public void execute() {
        try {
            // Remove the last comma from fields
            prepareStatemment("UPDATE `" + table + "` SET" + fields.substring(0, fields.length() - 1)
                + " WHERE " + conditions);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ServerException(e);
        } finally {
            clean(null);
        }
    }
}
