## 1. Define core: domain & logic & entity
```
business_logic
data_access
```

## 2. Exception
```txt
define & handle all exception
```


## 3. Presentation
```txt
request -> adapter -> controller -> ressponse
```

## 4. application.yml
```yaml
# <!-- create ecommerce/main/resource/application.yaml -->

spring:
  application:
    name: ecommerce

  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: ${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
    properties:
      hibernate:
        format_sql: true
    show-sql: true

server:
  port: ${SERVER_PORT:8080}


```

## 5. create database
```
potgres: db_name = datnecommerce
```

## 6. import api json to postman
```
ecom_dacn.json
```

## 7. container run
```sh
# requirement docker engine
cd ecommerce
docker-compose up --build
```
