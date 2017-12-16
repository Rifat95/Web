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

### Servlet context parameters that need to be defined
- package (project's main package name)
- languages (supported languages list, separated with comma)
- defaultLanguage

web.xml file example
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

## Conventions

### Code
- indentation: 1 tab
- if/class/method line break: 0 tab
- continuous indentation / other line break: 1 tab
- brace placement: same line
- xml/html/css tags: file-case, simple quotes
- less variables: ?
- javascript: camelCase, simple quotes
- sql tables: PascalCase
- sql fields: camelCase

### Javadoc
- block comment: full punctuation
- @tags: lowercase, without dot
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

## Starting operations

### web.core.Servlet.init()
- Loading and storage of routes
- Loading and storage of language packs
- Database connection pool creation

### web.core.Servlet.process()
- URI parsing
- Verify that a token match the session ID if type of request is post
- Recuperation of matching route and access check
- Recuperation of a database connection from the connection pool
- Instantiation of route's controller and execution of the method with URI arguments (between "/")
- Send the response to browser with web.core.Page.send()
- Closing database connection with web.core.App.clean()

### web.core.App.init()
- Recuperation of the variable "userId" stored in session (O if not found)
- Recuperation of the variable "userPermissions" stored in session, ("guest" if not found)
- Instantiation of web.core.Translator
	- Recuperation of the variable "language" stored in session (defaultLanguage if not found)
	- Recuperation of the associated language pack

## Help

### Connecting the user
To connect an user, use the method `web.core.App.setUser(int userId, String[] permissions)`.

### Access request variables
To access GET and POST variables, use the object `req (web.core.Request)` avalaible in controllers.
