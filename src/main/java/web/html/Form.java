package web.html;

import web.core.App;

public class Form extends DoubleTag {
    private String id;

    public Form() {
        super("form");
    }

    public Form(String method) {
        this();
        addAttr("method", method);
    }

    public Form(String method, String action) {
        this(method);
        addAttr("action", action);
    }

    public Form(String method, String action, String id) {
        this(method, action);
        this.id = id;
    }

    public void addPostInfos() {
        if (id != null) {
            insert("<input type=\"hidden\" name=\"formId\" value=\"" + id + "\"/>");
        }

        String token = App.getInstance().getSession().getId();
        insert("<input type=\"hidden\" name=\"tk\" value=\"" + token + "\"/>");
    }

    public void addSubmitButton(String value) {
        addPostInfos();
        insert("<p><input type=\"submit\" value=\"" + value + "\"/></p>");
    }
}
