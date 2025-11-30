# No need for Tomcat image as TightBlog comes with an embedded server.
FROM azul/zulu-openjdk:25

COPY docker/web/application-tbcustom.properties application-tbcustom.properties

# created via keytool -genkeypair -alias tomcat -keyalg RSA -keysize 2048 -keystore serverKeystore.p12 -storetype PKCS12 -storepass changeit -validity 3650
COPY docker/web/serverKeystore.p12 serverKeystore.p12

COPY build/libs/tightblog-4.1.4.war app.war

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.config.location=classpath:/application.properties,application-tbcustom.properties","-jar","/app.war"]
