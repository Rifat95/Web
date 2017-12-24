## Configuration

### Required files

- web.xml
- db.properties
- routes.json
- body.tpl, messages.tpl, error-403.tpl, error-404.tpl, error-500.tpl

File structure example
```
Project
|
├── java-src
│   ├── packagename
│   │   ├── controller
│   │   │   └── java-controllers...
│   │   └── entity
│   │       └── java-entities...
│   └── resources
│       ├── conf
│       │   ├── db.properties
│       │   └── routes.json
│       └── i18n
│           ├── strings_fr.properties
│           └── strings_xx.properties
|
└── web-app-folder
    ├── WEB-INF
    │   ├── less
    │   │   ├── other-less-files...
    │   │   └── style.less
    │   └── templates
    │       ├── core
    │       │   ├── body.tpl
    │       │   ├── error-403.tpl
    │       │   ├── error-404.tpl
    │       │   ├── error-500.tpl
    │       │   ├── messages.tpl
    │       │   └── other-core-templates...
    │       └── page-templates...
    └── public
        ├── css
        │   ├── other-css-files...
        │   └── style.css
        ├── files
        │   ├── img-files...
        │   └── other-files...
        └── js
            └── js-files...
```

### Context parameters that need to be defined in web.xml
- package (project's main package name)
- languages (supported languages list, separated with comma)
- defaultLanguage

web.xml example
```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app
	version="3.1" xmlns="http://xmlns.jcp.org/xml/ns/javaee"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd">

	<servlet>
		<servlet-name>Servlet</servlet-name>
		<servlet-class>web.core.Servlet</servlet-class>
		<load-on-startup>0</load-on-startup>
	</servlet>

	<servlet-mapping>
		<servlet-name>default</servlet-name>
		<url-pattern>/public/*</url-pattern>
	</servlet-mapping>
	<servlet-mapping>
		<servlet-name>Servlet</servlet-name>
		<url-pattern>/</url-pattern>
	</servlet-mapping>

	<context-param>
		<param-name>package</param-name>
		<param-value>packagename</param-value>
	</context-param>
	<context-param>
		<param-name>languages</param-name>
		<param-value>en,fr,de</param-value>
	</context-param>
	<context-param>
		<param-name>defaultLanguage</param-name>
		<param-value>en</param-value>
	</context-param>

	<session-config>
		<session-timeout>30</session-timeout>
	</session-config>
</web-app>
```

## How it works

### web.core.Servlet.init()
- Loading and storage of routes
- Loading and storage of language packs
- Database connection pool creation

### web.core.Servlet.process()
- URI parsing
- Verify that a token match the session ID if type of request is post
- Recuperation of matching route and access check
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
		"controller": "Home",
		"action": "show",
		"permission": "guest",
		"token": false
	},
	{
		"uri": "/news/([0-9]+)/([a-z0-9-]+)",
		"controller": "News",
		"action": "show",
		"permission": "member",
		"token": false
	}
]
```

Controller example for the first route (yourpackage.controller.Home.java)
```java
package yourpackage.controller;

import web.core.Controller;
import web.core.View;

public class Home extends Controller {
	public void show() {
		View v = new View("home")
			.add("var", t.t("Hello"))
			.add("var2", 666);

		page.setTitle(t.t("Home page"));
		page.setView(v);
	}
}
```

Controller example for the second route (yourpackage.controller.News.java)
```java
package yourpackage.controller;

import web.core.Controller;
import yourpackage.entity.News;

public class News extends Controller {
	/**
	 * Route's regular expression : /news/([0-9]+)/([a-z0-9-]+)
	 *
	 * @param id corresponds to the first group of parentheses in route's regular expression
	 * @param slug corresponds to the second group of parentheses in route's regular expression
	 */
	public void show(String id, String slug) {
		News n = new News(id);
		View v = new View("news")
			.add("news", n)
			.add("newsSlug", slug);

		page.setTitle(n.get("title"));
		page.setView(v);
	}
}
```

### Connect an user
To connect an user, use the method `web.core.App.setUser(int userId, String[] permissions)`.
Session can be destroyed with `web.core.Session.destroy()`, session can be obtained with `web.core.app.getSession()`.

### Access request variables
To access GET and POST variables, use the object `req (web.core.Request)` avalaible in controllers.

### Change language
Use `web.core.translator.setLanguage(String language)` to change strings language, the changes will be effective only if
the properties file for the chosen language exists.

## Conventions

### Code
- indentation: 1 tab
- if/class/method line break: 0 tab
- continuous indentation / other line break: 1 tab
- brace placement: same line
- xml/html/css: file-case, double quotes
- 1 space before closing html single tags: `<img src="x" />`
- less variables: camelCase
- javascript: camelCase, double quotes
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
