package bookstore;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.xml.xsd.SimpleXsdSchema;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

    @Bean
    public ServletRegistrationBean messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean(servlet, "/soap/books/*");
    }

    @Bean(name = "bookstore")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema bookstoreSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("BookstorePort");
        wsdl11Definition.setLocationUri("/soap/books/");
        wsdl11Definition.setTargetNamespace("http://www.example.org/bookstore");
        wsdl11Definition.setSchema(bookstoreSchema);
        return wsdl11Definition;
    }
    
    @Bean
    public XsdSchema bookstoreSchema() {
        return new SimpleXsdSchema(new ClassPathResource("schema.xsd"));
    }
}