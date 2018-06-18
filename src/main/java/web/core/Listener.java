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
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import web.annotations.Action;

@WebListener
public final class Listener implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      ServletContext context = sce.getServletContext();
      ClassLoader loader = context.getClassLoader();

      loadSettings(context, loader);
      loadRoutes(context, loader);
      loadStrings(context, loader);
      loadTemplateEngine(context);
      loadDatasource(context);

      // Load application initializer
      @SuppressWarnings("unchecked")
      Class<Initializable> appInitializerClass = (Class<Initializable>) Class.forName("app.init.Initializer");
      Initializable appInitializer = appInitializerClass.newInstance();
      context.setAttribute("appInitializer", appInitializer);

      context.setAttribute("contextPath", context.getContextPath());
      appInitializer.onAppStart(context);
    } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      // Fatal error
      // @todo Replace with logger
      e.printStackTrace();
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // Close data source
    ServletContext context = sce.getServletContext();
    HikariDataSource dataSource = (HikariDataSource) context.getAttribute("dataSource");

    if (dataSource != null) {
      dataSource.close();
    }

    // Tomcat databse connection issue fix, taken from Stackoverflow
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

  private void loadSettings(ServletContext context, ClassLoader loader) throws IOException {
    Properties settings = new Properties();
    settings.load(loader.getResourceAsStream("conf/settings.properties"));
    context.setAttribute("settings", settings);
  }

  private void loadRoutes(ServletContext context, ClassLoader loader) {
    ConfigurationBuilder reflectionConfig = new ConfigurationBuilder()
        .addClassLoader(loader)
        .setUrls(ClasspathHelper.forPackage("app.controller", loader))
        .setScanners(new SubTypesScanner());

    Reflections reflections = new Reflections(reflectionConfig);
    Set<Class<? extends Controller>> controllers = reflections.getSubTypesOf(Controller.class);
    List<Route> routes = new ArrayList<>();

    controllers.forEach((controller) -> {
      for (Method method : controller.getDeclaredMethods()) {
        if (method.isAnnotationPresent(Action.class)) {
          Action annotation = method.getDeclaredAnnotation(Action.class);
          routes.add(new Route(annotation.uri(), controller, method, annotation.permission(), annotation.token()));
        }
      }
    });

    context.setAttribute("routes", routes);
  }

  private void loadStrings(ServletContext context, ClassLoader loader) {
    ConfigurationBuilder reflectionConfig = new ConfigurationBuilder()
        .addClassLoader(loader)
        .setUrls(ClasspathHelper.forPackage("i18n", loader))
        .setScanners(new ResourcesScanner());

    Reflections reflections = new Reflections(reflectionConfig);
    Pattern p = Pattern.compile("strings_([a-zA-Z_]+)\\.properties");
    Set<String> resources = reflections.getResources(p);
    Map<String, ResourceBundle> i18nBundles = new HashMap<>();

    resources.forEach((resource) -> {
      Matcher m = p.matcher(resource.replace("i18n/", ""));
      if (m.matches()) {
        String language = m.group(1);
        i18nBundles.put(language, ResourceBundle.getBundle("i18n.strings", new Locale(language), loader));
      }
    });

    context.setAttribute("i18nBundles", i18nBundles);
  }

  private void loadTemplateEngine(ServletContext context) {
    ServletLoader templateLoader = new ServletLoader(context);
    templateLoader.setPrefix("/WEB-INF/templates/");
    templateLoader.setSuffix(".tpl");

    PebbleEngine templateEngine = new PebbleEngine.Builder()
        .loader(templateLoader)
        .strictVariables(true)
        .build();

    context.setAttribute("templateEngine", templateEngine);
  }

  private void loadDatasource(ServletContext context) {
    Properties settings = (Properties) context.getAttribute("settings");

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
    context.setAttribute("dataSource", dataSource);
  }
}
