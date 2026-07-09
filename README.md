# SCHEDULE PROJECT

## Initial config:

In this project i dont push my ``application-local.properties`` because its my local configurations, obviously...

In ``src/main/resources/application.properties`` you just have:

``` 
#APPLICATION
spring.application.name=agenda
spring.profiles.active=local

#DATABASE CONNECTION
spring.datasource.url=jdbc:postgresql://localhost:5432/agenda_db

#JPA / HIBERNATE CONFIGS
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true 
```
So you need to config your own local properties and the url to connect a data base, ok?...