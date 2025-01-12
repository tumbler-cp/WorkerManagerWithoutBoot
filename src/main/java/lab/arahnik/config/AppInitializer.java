package lab.arahnik.config;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;
import lab.arahnik.authentication.config.SecurityConfig;
import lab.arahnik.websocket.config.WebSocketConfig;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.EnumSet;

@WebListener
public class AppInitializer implements WebApplicationInitializer {

  @Override
  public void onStartup(ServletContext servletContext) {
    AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
    context.register(WebConfig.class, SecurityConfig.class, WebSocketConfig.class);

    DispatcherServlet dispatcherServlet = new DispatcherServlet(context);
    ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", dispatcherServlet);
    dispatcher.setLoadOnStartup(1);
    dispatcher.addMapping("/");

    MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/tmp", 5242880, 20971520, 0);
    dispatcher.setMultipartConfig(multipartConfigElement);

    FilterRegistration.Dynamic multipartFilter = servletContext.addFilter("multipartFilter", new MultipartFilter());
    multipartFilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
  }

}
