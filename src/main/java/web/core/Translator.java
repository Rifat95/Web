package web.core;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public final class Translator {
  private App app;
  private HashMap<String, ResourceBundle> i18nBundles;
  private ResourceBundle strings;
  private MessageFormat formatter;

  /**
   * @param app required because this constructor is called from App constructor,
   * where App.INSTANCE is not initialized yet.
   */
  @SuppressWarnings("unchecked")
  Translator(App app) {
    this.app = app;
    i18nBundles = (HashMap<String, ResourceBundle>) app.getContext().getAttribute("i18nBundles");
    String language = (String) app.getSession().get("language", app.getSetting("default.language"));
    strings = i18nBundles.get(language);
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
   * @param key the string key
   * @param l the arguments
   *
   * @return the string associated with the key
   */
  public String t(String key, List<Object> l) {
    return t(key, l.toArray());
  }

  public void setLanguage(String language) {
    ResourceBundle bundle = i18nBundles.get(language);

    if (bundle != null) {
      app.getSession().set("language", language);
      strings = bundle;
    }
  }
}
