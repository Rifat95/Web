package web.html;

public class RadioSelect extends DoubleTag {
	private String groupName;
	private String defaultValue;

	public RadioSelect(String groupName) {
		super("span");
		this.groupName = groupName;
		defaultValue = "";
	}

	public void setDefaultValue(String value) {
		defaultValue = value;
	}

	public void addInput(String value, String title) {
		insert(getInput(value, title).toString());
	}

	/**
	 * This method can be used to customize an input before inserting it into the RadioSelect.
	 *
	 * @param value
	 * @param title
	 * @return pre-filled Input object that match with RadioSelect parameters
	 */
	public Input getInput(String value, String title) {
		String id = groupName + "-" + value;
		Input input = new Input("radio", groupName);
		input.addAttr("id", id);
		input.addAttr("value", value);
		input.setSuffix("<label for=\"" + id + "\">" + title + "</label>");

		if (value.equals(defaultValue)) {
			input.addAttr("checked");
		}

		return input;
	}
}
