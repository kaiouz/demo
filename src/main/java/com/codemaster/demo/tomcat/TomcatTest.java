package com.codemaster.demo.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

public class TomcatTest {

    public static void main(String[] args) throws LifecycleException {

        Connector httpsConnector = new Connector();
        httpsConnector.setPort(8443);


        Tomcat tomcat = new Tomcat();
        Service service = tomcat.getService();

        service.addConnector(httpsConnector);


        Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());

        Tomcat.addServlet(ctx, "hello", new HttpServlet() {
            private static final long serialVersionUID = 3600060857537422698L;

            @Override
            protected void service(HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/plain");
                try (Writer writer = response.getWriter()) {
                    writer.write("Hello, Embedded World from Blue Lotus Software!");
                    writer.flush();
                }
            }
        });
        ctx.addServletMappingDecoded("/*", "hello");

        tomcat.start();

        Connector connector = new Connector();
        connector.setPort(8444);
        connector.setSecure(true);
        connector.setScheme("https");
        connector.setProperty("keyAlias", "alias");
        connector.setProperty("keystorePass", "ddd");
        connector.setProperty("keystoreFile", "/Users/zoukai/temp/domain.p12");
        connector.setProperty("clientAuth", "false");
        connector.setProperty("sslProtocol", "TLS");
        connector.setProperty("SSLEnabled", "true");

        service.removeConnector(httpsConnector);
        service.addConnector(connector);

        tomcat.getServer().await();
    }

}
