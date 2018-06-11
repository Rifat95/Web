package web.core;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ServletLoader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

@WebListener
public final class Listener implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      ServletContext context = sce.getServletContext();
      ClassLoader loader = context.getClassLoader();

      /*
       * Load application settings.
       */
      Properties settings = new Properties();
      settings.load(loader.getResourceAsStream("conf/settings.properties"));
      settings.put("context.path", context.getContextPath());

      /*
       * Load application routes.
       */
      File file = new File(loader.getResource("conf/routes.json").getPath());
      JSONArray routeList = new JSONArray(FileUtils.readFileToString(file, "UTF-8"));

      int nbRoute = routeList.length();
      Route[] routes = new Route[nbRoute];

      for (int i = 0; i < nbRoute; i++) {
        JSONObject jo = routeList.getJSONObject(i);
        String uri = jo.getString("uri");
        String[] controllerInfos = jo.getString("controller").split("@");
        String permission = jo.getString("permission");
        boolean token = jo.has("token") ? jo.getBoolean("token") : false;

        Class<?> controller = Class.forName("app.controller." + controllerInfos[0]);
        Method action = getMethod(controllerInfos[1], controller);
        routes[i] = new Route(uri, controller, action, permission, token);
      }

      /*
       * Load application language packs.
       *
       * @todo Find supported languages automaticly
       */
      HashMap<String, ResourceBundle> i18nBundles = new HashMap<>();
      String[] languages = settings.getProperty("supported.languages").split(",");

      for (String language : languages) {
        i18nBundles.put(language, ResourceBundle.getBundle("i18n.strings", new Locale(language), loader));
      }

      /*
       * Load database connections.
       */
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(settings.getProperty("database.url"));
      config.setUsername(settings.getProperty("database.user"));
      config.setPassword(settings.getProperty("database.password"));
      config.setDriverClassName(settings.getProperty("database.driver"));

      config.addDataSourceProperty("cachePrepStmts", "true");
      config.addDataSourceProperty("prepStmtCacheSize", "250");
      config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      config.addDataSourceProperty("useServerPrepStmts", "true");
      config.addDataSourceProperty("useLocalSessionState", "true");
      config.addDataSourceProperty("useLocalTransactionState", "true");
      config.addDataSourceProperty("rewriteBatchedStatements", "true");
      config.addDataSourceProperty("cacheResultSetMetadata", "true");
      config.addDataSourceProperty("cacheServerConfiguration", "true");
      config.addDataSourceProperty("elideSetAutoCommits", "true");
      config.addDataSourceProperty("maintainTimeStats", "false");

      HikariDataSource dataSource = new HikariDataSource(config);

      /*
       * Load template engine.
       */
      ServletLoader templateLoader = new ServletLoader(context);
      templateLoader.setPrefix("/WEB-INF/templates/");
      templateLoader.setSuffix(".tpl");
      PebbleEngine templateEngine = new PebbleEngine.Builder()
          .loader(templateLoader)
          .strictVariables(true)
          .build();

      /*
       * Load application initializer.
       */
      @SuppressWarnings("unchecked")
      Class<Initializable> appInitializerClass = (Class<Initializable>) Class.forName("app.init.Initializer");
      Initializable appInitializer = appInitializerClass.newInstance();

      /*
       * Save everything in servlet context.
       */
      context.setAttribute("settings", settings);
      context.setAttribute("routes", routes);
      context.setAttribute("i18nBundles", i18nBundles);
      context.setAttribute("dataSource", dataSource);
      context.setAttribute("templateEngine", templateEngine);
      context.setAttribute("appInitializer", appInitializer);

      appInitializer.onStart(context);
    } catch (IOException | ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
      /*
       * Fatal error.
       *
       * @todo Replace with logger
       */
      e.printStackTrace();
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    /*
     * Close data source.
     */
    ServletContext context = sce.getServletContext();
    HikariDataSource dataSource = (HikariDataSource) context.getAttribute("dataSource");

    if (dataSource != null) {
      dataSource.close();
    }

    /*
     * Tomcat databse connection issue fix, taken from Stackoverflow.
     */
    Enumeration<Driver> drivers = DriverManager.getDrivers();

    while (drivers.hasMoreElements()) {
      try {
        Driver d = drivers.nextElement();
        DriverManager.deregisterDriver(d);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
    Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);

    for (Thread t : threadArray) {
      if (t.getName().contains("Abandoned connection cleanup thread")) {
        synchronized (t) {
          t.stop();
        }
      }
    }
  }

  private Method getMethod(String name, Class<?> c) throws NoSuchMethodException {
    for (Method m : c.getDeclaredMethods()) {
      if (m.getName().equals(name)) {
        return m;
      }
    }

    throw new NoSuchMethodException();
  }
}
