FROM azul/zulu-openjdk:21.0.1-21.30.15 as base
RUN apt-get update && apt-get install -y maven
COPY <<EOF /settings/settings.xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <localRepository>./.m2</localRepository>
</settings>
EOF

FROM base as deps
WORKDIR /deps
COPY pom.xml /deps/

RUN mvn dependency:go-offline -s /settings/settings.xml

FROM base as build
WORKDIR /sources
COPY . /sources/
COPY --from=deps /deps/.m2 /sources/.m2

RUN mvn clean package -DskipTests -s /settings/settings.xml

FROM azul/zulu-openjdk-alpine:21.0.1-21.30.15-jre-headless

COPY --from=build /sources/target/service.jar .

CMD ["java", "-jar", "service.jar"]
