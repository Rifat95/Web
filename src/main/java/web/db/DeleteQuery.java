package web.db;

import java.sql.SQLException;
import web.util.ServerException;

/**
 * @param <E> the entity type
 */
public final class DeleteQuery<E extends Entity> extends Query<E, DeleteQuery<E>> {
    public DeleteQuery(Class<E> entityClass) {
        super(entityClass);
    }

    public void execute() {
        try {
            prepareStatemment("DELETE FROM `" + table + "` WHERE " + conditions);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ServerException(e);
        } finally {
            clean(null);
        }
    }
}
