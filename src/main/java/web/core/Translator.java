package web.core;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public final class Translator {
  private ResourceBundle strings;
  private MessageFormat formatter;

  /**
   * @param app required because this constructor is called from App constructor,
   * where App.INSTANCE is not initialized yet.
   */
  @SuppressWarnings("unchecked")
  Translator(App app) {
    Map<String, ResourceBundle> i18nBundles = (Map<String, ResourceBundle>) Servlet.getAttribute("i18nBundles");
    String language = (String) app.getSession().get("language", Servlet.getSetting("default.language"));
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

  @SuppressWarnings("unchecked")
  public void setLanguage(String language) {
    Map<String, ResourceBundle> i18nBundles = (Map<String, ResourceBundle>) Servlet.getAttribute("i18nBundles");
    ResourceBundle bundle = i18nBundles.get(language);

    if (bundle != null) {
      App.getInstance().getSession().set("language", language);
      strings = bundle;
    }
  }
}
