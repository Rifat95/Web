package web.core;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ServletLoader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

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
       * Load application routes by finding controllers trough reflection.
       */
      ConfigurationBuilder reflectionConfig = new ConfigurationBuilder()
          .addClassLoader(loader)
          .setUrls(ClasspathHelper.forPackage("app.controller", loader))
          .setScanners(new SubTypesScanner());
      Reflections reflections = new Reflections(reflectionConfig);

      Set<Class<? extends Controller>> controllers = reflections.getSubTypesOf(Controller.class);
      ArrayList<Route> routes = new ArrayList<>();

      controllers.forEach((controller) -> {
        for (Method method : controller.getDeclaredMethods()) {
          if (method.isAnnotationPresent(Action.class)) {
            Action annotation = method.getDeclaredAnnotation(Action.class);
            routes.add(new Route(annotation.uri(), controller, method, annotation.permission(), annotation.token()));
          }
        }
      });

      /*
       * Load application language packs by scanning avalaible resources trough reflexion.
       */
      reflectionConfig = new ConfigurationBuilder()
          .addClassLoader(loader)
          .setUrls(ClasspathHelper.forPackage("i18n", loader))
          .setScanners(new ResourcesScanner());
      reflections = new Reflections(reflectionConfig);

      Pattern p = Pattern.compile("strings_([a-zA-Z_]+)\\.properties");
      Set<String> resources = reflections.getResources(p);
      HashMap<String, ResourceBundle> i18nBundles = new HashMap<>();

      resources.forEach((resource) -> {
        Matcher m = p.matcher(resource.replace("i18n/", ""));
        if (m.matches()) {
          String language = m.group(1);
          i18nBundles.put(language, ResourceBundle.getBundle("i18n.strings", new Locale(language), loader));
        }
      });

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

      appInitializer.onAppStart(context);
    } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
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
}
