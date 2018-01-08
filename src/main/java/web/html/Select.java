package web.html;

public class Select extends DoubleTag {
	private String defaultValue;

	public Select() {
		super("select");
		defaultValue = "";
	}

	public Select(String name) {
		this();
		addAttr("name", name);
	}

	public Select(String name, String title) {
		this(name);
		addAttr("title", title);
		insert("<option disabled selected>" + title + "</option>");
	}

	public void setDefaultValue(String value) {
		defaultValue = value;
	}

	public void addOption(String value, String title) {
		if (value.equals(defaultValue)) {
			insert("<option value=\"" + value + "\" selected>" + title + "</option>");
		} else {
			insert("<option value=\"" + value + "\">" + title + "</option>");
		}
	}
}
