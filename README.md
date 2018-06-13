## Configuration

### Required files

- web.xml
- settings.properties
- Initializer.java

Application file structure
```
Project
|
├── java-src
│   ├── app
│   │   ├── controller
│   │   │   └── Controller.java
│   │   ├── entity
│   │   │   └── Entity.java
│   │   └── init
│   │       └── Initializer.java
│   java-resources
│   ├── conf
│   │   └── settings.properties
│   └── i18n
│       ├── strings_fr.properties
│       └── strings_en.properties
|
└── web-app-folder
    ├── WEB-INF
    │   ├── less
    │   │   └── style.less
    │   └── templates
    │       ├── core
    │       │   ├── body.tpl
    │       │   └── error.tpl
    │       ├── home.tpl
    │       └── login.tpl
    └── public
        ├── css
        │   └── style.css
        │   └── img
        │       └── logo.png
        ├── files
        │   └── file.pdf
        └── js
            └── home.js
```

### web.xml example
```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app version="3.1" xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd">

  <servlet-mapping>
    <servlet-name>default</servlet-name>
    <url-pattern>/public/*</url-pattern>
  </servlet-mapping>

  <session-config>
    <session-timeout>30</session-timeout>
  </session-config>
</web-app>
```

### settings.properties example
```properties
default.language = fr

database.url = jdbc:mysql://localhost:3306/dbname
database.user = root
database.password = root
database.driver = com.mysql.jdbc.Driver
```

### Initializer.java example
```java
public class Initializer implements Initializable {
  @Override
  public void onStart(ServletContext context) {
    //...
  }

  @Override
  public void onRequestStart(App app) {
    //...
  }

  @Override
  public void onRequestFinish(App app) {
    //...
  }

  @Override
  public void handleException(NotFoundException e, App app) {
    View view = new View("core/error");
    view.set("code", 404);
    view.set("message", "Page not found");
    app.getPage().setResponse(view);
  }

  @Override
  public void handleException(ForbiddenException e, App app, Route route) {
    //...
  }

  @Override
  public void handleException(Exception e, App app) {
    //...
  }
}
```

## How it works

### web.core.Initializer.contextInitialized()
- Loading and storage of settings
- Loading and storage of routes
- Loading and storage of language packs
- Template engine configuration
- Database connection pool creation

### web.core.App.init()
...

### web.core.App.run()
- Recuperation of the matching route and access check
- Token verification
- Recuperation of a database connection from the connection pool
- Instantiation of route's controller and execution of the method with arguments in URI (between "/")
- Send the response to browser with web.core.Page.send()
- Closing database connection with web.core.App.clean()

## Help

### Create a controller with actions

```java
public class Example extends Controller {
  @Action(uri = "/", permission = "all")
  public void home() {
    View view = new View("home");
    page.setResponse(view);
  }

  /**
   * @param id corresponds to the first group of parentheses
   * @param slug corresponds to the second group of parentheses
   */
  @Action(uri = "/news/([0-9]+)/([a-z0-9-]+)", permission = "member")
  public void news(String id, String slug) {
    News news = new News(id);

    View view = new View("single-news");
    view.set("news", news);
    view.set("slug", slug);

    page.setResponse(view);
  }

  @Action(uri = "/logout", permission = "member", token = true)
  public void logout() {
    app.getSession().destroy();
    page.addMessage(Message.Type.SUCCESS, t.t("user.disconnected"));
    page.setRedirection(Util.uri("/login"));
  }
}
```

### Connect an user
To connect an user, use `appUser.setId(int id)` and `appUser.setPermissions(List<String> permissions)`.
Session can be obtained with `app.getSession()` and destroyed with `app.getSession().destroy()`.

### Access request variables
To access GET and POST variables, use the `req.get(String param)`.
Route parameters (between "/" in URI) are avalaible in method arguments.

### Manage i18n
In templates, to get the string for `welcome.msg = Salut {0}, alors comme ça tu as {1, number} ans ?`
use `t.t("welcome.msg", ["Dimitri", 30]) }}`.

### Change language
Use `t.setLanguage(String language)` to change strings language,
the changes will be effective only if the properties file for the chosen language exists.

### Manage page render mode
...

### Execute database queries
- Alter queries with entity custom query
- Manage entity after database query
...

### Manage entities
...

### Use token verification
...

### Manage links and redirections
...

### Throw exceptions
...

## Conventions

### Code
- indentation: 2 spaces
- if/class/method line break: same level, 0 indentation
- continuous indentation / other line break: 1 indentation
- brace placement: same line
- xml/html/css/less: file-case, double quotes
- no space before closing html single tags: `<img src="x"/>`
- javascript: camelCase, single quotes
- json: camelCase
- sql tables: PascalCase
- sql fields: camelCase

### Javadoc
- Block comments: Full punctuation
- Single line comment: First letter in uppercase, without dot
- @param and other @tags: lowercase, without dot

### Class member order:
- static fields
- normal fields
- static methods
- constructor
- normal methods (sorted by visibility)

### Method visibility order:
- public
- default
- protected
- private

### Keywords order:
- public / protected /private
- abstract
- default
- static
- final
- transient
- volatile
- synchronized
- native
- strictfp
