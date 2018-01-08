package web.html;

import web.core.App;

public class Form extends DoubleTag {
	public Form() {
		this("post");
	}

	public Form(String method) {
		this(method, "");
	}

	public Form(String method, String action) {
		super("form");
		addAttr("method", method);
		addAttr("action", action);
	}

	@Override
	public String toString() {
		String token = App.getInstance().getSession().getId();
		insert("<input type=\"hidden\" name=\"tk\" value=\"" + token + "\" />");
		return super.toString();
	}
}
