package web.html;

import web.core.App;

public class Form extends DoubleTag {
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

	@Override
	public String toString() {
		String token = App.getInstance().getSession().getId();
		insert("<input type=\"hidden\" name=\"tk\" value=\"" + token + "\"/>");
		return super.toString();
	}
}
