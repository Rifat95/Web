package web.html;

public class Input extends SingleTag {
    public Input(String type) {
        super("input");
        addAttr("type", type);
    }

    public Input(String type, String name) {
        this(type);
        addAttr("name", name);
    }

    public Input(String type, String name, String placeholder) {
        this(type, name);
        addAttr("placeholder", placeholder);
        addAttr("title", placeholder);
    }
}
