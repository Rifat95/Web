package web.core;

import java.util.Arrays;
import java.util.List;

public final class AppUser {
    private int id;
    private List<String> permissions;

    AppUser() {
        id = 0;
        permissions = Arrays.asList("guest");
    }

    public int getId() {
        return id;
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
