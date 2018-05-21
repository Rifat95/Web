package web.core;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

public final class Translator {
	private ResourceBundle strings;
	private MessageFormat formatter;

	public Translator() {
		String defaultLanguage = Servlet.getSetting("default.language");
		String language = (String) App.getInstance().getSession().get("language", defaultLanguage);
		strings = Servlet.getLanguagePack(language);
		formatter = new MessageFormat("", strings.getLocale());
	}

	public String t(String key) {
		return strings.getString(key);
	}

	public String t(String key, Object... args) {
		formatter.applyPattern(strings.getString(key));
		return formatter.format(args);
	}

	/**
	 * For Pebble templates only.
	 * Example of use: t.t("str", ["arg1", "arg2"])
	 *
	 * @param key
	 * @param l the arguments
	 * @return the string associated with the key
	 */
	public String t(String key, List<Object> l) {
		return t(key, l.toArray());
	}

	public void setLanguage(String language) {
		ResourceBundle bundle = Servlet.getLanguagePack(language);
		if (bundle != null) {
			App.getInstance().getSession().set("language", language);
			strings = bundle;
		}
	}
}
