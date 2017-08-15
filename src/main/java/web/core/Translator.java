package web.core;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

public final class Translator {
	private ResourceBundle strings;
	private MessageFormat formatter;

	public Translator() {
		String language = (String) App.getInstance().getSession().get("language", "fr");
		strings = Servlet.getBundle(language);
		formatter = new MessageFormat("", strings.getLocale());
	}

	public String t(String key) {
		return strings.getString(key);
	}

	public String t(String key, Object... args) {
		formatter.applyPattern(strings.getString(key));
		return formatter.format(args);
	}

	// For templates only
	public String t(String key, List<Object> l) {
		return t(key, l.toArray());
	}

	public void setLanguage(String language) {
		ResourceBundle bundle = Servlet.getBundle(language);
		if (bundle != null) {
			App.getInstance().getSession().set("language", language);
			strings = bundle;
		}
	}
}