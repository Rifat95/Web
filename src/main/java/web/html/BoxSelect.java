package web.html;

import java.util.ArrayList;

public class BoxSelect extends DoubleTag {
	private String groupName;
	private ArrayList<String> defaultValues;

	public BoxSelect(String groupName) {
		super("span");
		this.groupName = groupName + "[]";
		defaultValues = new ArrayList<>();
	}

	public void setDefaultValues(ArrayList<String> values) {
		defaultValues = values;
	}

	public void addInput(String value, String title) {
		insert(getInput(value, title).toString());
	}

	/**
	 * This method can be used to customize an input before inserting it into the BoxSelect.
	 *
	 * @param value
	 * @param title
	 * @return pre-filled Input object that match with BoxSelect parameters
	 */
	public Input getInput(String value, String title) {
		String id = groupName + "-" + value;
		Input input = new Input("checkbox", groupName);
		input.addAttr("id", id);
		input.addAttr("value", value);
		input.setSuffix("<label for=\"" + id + "\">" + title + "</label>");

		if (defaultValues.contains(value)) {
			input.addAttr("checked");
		}

		return input;
	}
}
