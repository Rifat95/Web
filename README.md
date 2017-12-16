## Configuration

### Fichiers obligatoires

- web.xml
- db.properties
- routes.json
- body.tpl, messages.tpl, error-403.tpl, error-404.tpl, error-500.tpl

Example de structure
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

### Paramètres à définir dans le fichier web.xml
- package (le nom du package principal)
- languages (liste des langues supportés, séparés par une virgule)
- defaultLanguage

Exemple de fichier web.xml
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
- continuous indentation: 1 tab
- if/class/method line break: 0 tab
- brace placement: same line
- xml/html/css tags: file-case, simple quotes
- less variables: ?
- javascript: camelCase, simple quotes
- sql tables: PascalCase
- sql fields: camelCase

### Javadoc
- block comment: full punctuation
- @tags: lowercase, without dot at the end
- single line comment: first letter in uppercase, without dot at the end

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

## Fonctionnement

### web.core.Servlet.init()
- Chargement et stockage des routes
- Chargement et stockage des packs de languages
- Création d'un pool connexion à la bdd après avoir charger les informations de connexion

### web.core.Servlet.process()
- Récupération de l'URI demandé
- Vérification du token si la requête est de type POST, qui est censé contenir l'identifiant de session
- Récupération de la route correspondante et check du droit d'accès à celle-ci
- Récupération d'une connection à la bdd à partir du pool
- Instanciation du contrôleur et exécution de la méthode en lui fournissants les arguments contenus dans l'URI (souvent entre "/")
- Envoi de la réponse au navigateur avec web.core.Page.send()
- Fermeture de la connection à la bdd avec web.core.App.clean()

### web.core.App.init()
- Récupération de la variable "userId" stocké en session (O si non existant)
- Récupération de la variable "userPermissions" stocké en session, ("guest" si non existant)
- Instanciation de web.core.Translator
	- Récupération de la langue stocké en session (langue par défaut si non existant)
	- Récupération du pack de lang associé

## Aide

### Connecter l'utilisateur
Pour connecter un utilisateur, utiliser la méthode web.core.App.setUser(int userId, String[] permissions).

### Accéder aux variables de la requête
Pour accéder aux variables GET et POST, utiliser l'objet web.core.Request disponible dans le contrôleur.
