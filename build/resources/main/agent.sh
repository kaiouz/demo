 docker run -it --rm -v `pwd`:/app -w /app -p 9999:9999 openjdk:8-jre-alpine java \
 -Dcom.sun.management.jmxremote=true \
 -Dcom.sun.management.jmxremote.port=9999 \
 -Dcom.sun.management.jmxremote.rmi.port=9999 \
 -Dcom.sun.management.jmxremote.authenticate=false \
 -Dcom.sun.management.jmxremote.ssl=false \
 -Djava.rmi.server.hostname=127.0.0.1 \
 com.codemaster.demo.jmx.HelloAgent
