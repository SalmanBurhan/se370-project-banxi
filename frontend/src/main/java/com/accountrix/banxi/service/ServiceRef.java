package com.accountrix.banxi.service;

import com.vaadin.flow.server.VaadinServlet;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ServiceRef<T>
{
    public static <T> T get(Class<T> serviceType)
    {
        return WebApplicationContextUtils
                .getWebApplicationContext(VaadinServlet.getCurrent().getServletContext())
                .getBean(serviceType);
    }
}
