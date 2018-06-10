## Configuration

### Required files

- web.xml
- settings.properties
- db.properties
- routes.json

File structure example
```
Project
|
├── java-src
│   ├── packagename
│   │   ├── controller
│   │   │   └── Controller.java
│   │   └── entity
│   │       └── Entity.java
│   └── resources
│       ├── conf
│       │   ├── settings.properties
│       │   ├── db.properties
│       │   └── routes.json
│       └── i18n
│           ├── strings_fr.properties
|
└── web-app-folder
    ├── WEB-INF
    │   ├── less
    │   │   └── style.less
    │   └── templates
    │       ├── core
    │       │   ├── body.tpl
    │       │   ├── error.tpl
    │       │   ├── messages.tpl
    │       └── home.tpl
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

### Settings that need to be defined in settings.properties
- supported.languages (separated with comma)
- default.language

## How it works

### web.core.Servlet.init()
- Loading and storage of routes
- Loading and storage of language packs
- Database connection pool creation

### web.core.Servlet.process()
- URI parsing
- Recuperation of the matching route and access check
- Token verification
- Recuperation of a database connection from the connection pool
- Instantiation of route's controller and execution of the method with arguments in URI (between "/")
- Send the response to browser with web.core.Page.send()
- Closing database connection with web.core.App.clean()

### web.core.App.init()
- Recuperation of the variable "userId" stored in session (O if not found)
- Recuperation of the variable "userPermissions" stored in session, ("guest" if not found)
- Instantiation of web.core.Translator
    - Recuperation of the variable "language" stored in session (defaultLanguage if not found)
    - Recuperation of the associated language pack

## Help

### Define a route and create a controller
routes.json example
```json
[
    {
        "uri": "/",
        "controller": "Home@show",
        "permission": "all"
    },
    {
        "uri": "/login",
        "controller": "Auth@login",
        "permission": "guest"
    },
    {
        "uri": "/news/([0-9]+)/([a-z0-9-]+)",
        "controller": "News@show",
        "permission": "member",
        "token": true
    }
]
```

Controller example for the first route (app.controller.Home)
```java
package app.controller;

import web.core.Controller;
import web.core.View;

public class Home extends Controller {
    public void show() {
        View v = new View("home");
        v.add("var", t.t("Hello"));
        v.add("var2", 666);

        page.setResponse(v);
    }
}
```

Controller example for the third route (app.controller.News)
```java
package app.controller;

import web.core.Controller;
import app.entity.News;

public class News extends Controller {
    /**
     * Route's regular expression : /news/([0-9]+)/([a-z0-9-]+)
     *
     * @param id corresponds to the first group of parentheses
     * @param slug corresponds to the second group of parentheses
     */
    public void show(String id, String slug) {
        News n = new News(id);
        View v = new View("news");
        v.add("news", n);
        v.add("newsSlug", slug);

        page.setResponse(v);
    }
}
```

### Connect an user
To connect an user, use the method `web.core.App.setUser(int userId, List<String> permissions)`.
Session can be obtained with `web.core.app.getSession()` and destroyed with `web.core.Session.destroy()`.

### Access request variables
To access GET and POST variables, use the object `req (web.core.Request)` avalaible in controllers.
Route's parameters (between "/" in URI) are avalaible in method's arguments.

### Manage i18n
In templates, to get the string for `welcome.msg = Salut {0}, alors comme ça tu as {1, number} ans ?`
use `t.t("welcome.msg", ["Dimitri", 30]) }}`.

### Change language
Use `web.core.translator.setLanguage(String language)` to change strings language,
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
- indentation: 4 spaces
- if/class/method line break: same level, 0 indentation
- continuous indentation / other line break: 1 indentation
- brace placement: same line
- xml/html/css/less: file-case, double quotes
- no space before closing html single tags: `<img src="x"/>`
- javascript: camelCase, double quotes
- json: camelCase
- sql tables: PascalCase
- sql fields: camelCase

### Javadoc
- block comment: full punctuation
- @param and other @tags: lowercase, without dot
- single line comment: first letter in uppercase, without dot

### Class member order:
- static fields
- normal fields
- constructor
- static methods
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
