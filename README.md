# Banxi

## Vaadin Pro Key
Some components in this project utilize Vaadin Pro features which require the need of a proKey file.
Once obtained, this file should be placed in the user's home directory as listed below.

Windows: `%userprofile%\.vaadin\proKey`

Mac/Linux: `~/.vaadin/proKey`

## Application Configuration

The following properties in the `application.properties` file should be modified before running.

```
security.require-ssl = true [required to properly utilize Plaid Link Flow]
server.ssl.key-store: path_to_p12_ssl_keystore
server.ssl.key-store-password: password_to_key_store
server.ssl.keyStoreType: PKCS12
server.ssl.keyAlias: 1

plaid.client-id.sandbox = **************************
plaid.client-id.development = **************************

plaid.secret.sandbox = **************************
plaid.secret.development = **************************

spring.datasource.url = jdbc:mariadb://localhost:3306/banxi
spring.datasource.username = banxi
spring.datasource.password = db_password
```



## Running the Application

The project is a standard Maven project. To run it from the command line,
type `mvnw` (Windows), or `./mvnw` (Mac & Linux), then open
http://localhost:8080 in your browser.

You can also import the project to your IDE of choice as you would with any
Maven project. Read more on [how to import Vaadin projects to different IDEs](https://vaadin.com/docs/latest/guide/step-by-step/importing) (Eclipse, IntelliJ IDEA, NetBeans, and VS Code).

## Deploying to Production

To create a production build, call `mvnw clean package -Pproduction` (Windows),
or `./mvnw clean package -Pproduction` (Mac & Linux).
This will build a JAR file with all the dependencies and front-end resources,
ready to be deployed. The file can be found in the `target` folder after the build completes.

Once the JAR file is built, you can run it using
`java -jar target/banxi-1.0-SNAPSHOT.jar`

## Project Structure

- `MainLayout.java` in `src/main/java` contains the navigation setup (i.e., the
  side/top bar and the main menu). This setup uses
  [App Layout](https://vaadin.com/docs/components/app-layout).
- `views` package in `src/main/java` contains the server-side Java views of your application.
- `views` folder in `frontend/` contains the client-side JavaScript views of your application.
- `themes` folder in `frontend/` contains the custom CSS styles.

## Useful Links for Vaadin Flow Framework

- Read the documentation at [vaadin.com/docs](https://vaadin.com/docs).
- Follow the tutorial at [vaadin.com/docs/latest/tutorial/overview](https://vaadin.com/docs/latest/tutorial/overview).
- Create new projects at [start.vaadin.com](https://start.vaadin.com/).
- Search UI components and their usage examples at [vaadin.com/docs/latest/components](https://vaadin.com/docs/latest/components).
- View use case applications that demonstrate Vaadin capabilities at [vaadin.com/examples-and-demos](https://vaadin.com/examples-and-demos).
- Build any UI without custom CSS by discovering Vaadin's set of [CSS utility classes](https://vaadin.com/docs/styling/lumo/utility-classes). 
- Find a collection of solutions to common use cases at [cookbook.vaadin.com](https://cookbook.vaadin.com/).
- Find add-ons at [vaadin.com/directory](https://vaadin.com/directory).
- Ask questions on [Stack Overflow](https://stackoverflow.com/questions/tagged/vaadin) or join our [Discord channel](https://discord.gg/MYFq5RTbBn).
- Report issues, create pull requests in [GitHub](https://github.com/vaadin).


## Deploying using Docker

To build the Dockerized version of the project, run

```
mvn clean package -Pproduction
docker build . -t banxi:latest
```

Once the Docker image is correctly built, you can test it locally using

```
docker run -p 9999:9999 banxi:latest
```
