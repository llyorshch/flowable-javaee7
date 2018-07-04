# Flowable Java EE 7 Sample Project

Sample project to showcase a Java EE 7 Web application with a basic REST API to interact with the [Flowable BPM engine](https://www.flowable.org/).

The environment setup was:

- GlassFish Server Open Source Edition 4.1.2

- PostgreSQL 9.4

To build the war file, execute the folowing command:

```shell
./mvnw clean package
```

Once the build process has finish, you will find a file called `flowable-javaee7.war` in the `target` directory.

Before deploying the war file to the Glassfish Server, you'll need to create a datasource with the name `jdbc/flowable`. Although this project was created using PostgreSQL, any [supported database](https://www.flowable.org/docs/userguide/index.html#supporteddatabases) is valid.

After deploying the war file to the Glassfish Server, you can use the provided [Postman](https://www.getpostman.com/) collection to interact with the application. These are the endpoints that are included:

| Http Method | Endpoint                         | Description                                                    |
| ----------- | -------------------------------- | -------------------------------------------------------------- |
| GET         | processEngine/name               | Retrieves the name of the engine, that is, "default"           |
| GET         | processEngine/processDefinitions | Retrieves the list of ids of the deployed process definitions) |
| GET         | processEngine/processes          | Retrieves the list of processes                                |
| POST        | processEngine/processes          | Creates a new process and returns the newly created id         |
| GET         | processEngine/tasks              | Retrieves the list of tasks                                    |
| POST        | processEngine/tasks/{id}?action=complete | Completes a task by its id                             |
