package org.tightblog.ui.filters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.support.MultipartFilter;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;

@PropertySources({
        @PropertySource(value = "classpath:tightblog.properties"),
        @PropertySource(value = "classpath:tightblog-custom.properties", ignoreResourceNotFound = true)
})
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean charEncodingFilter() {
        FilterRegistrationBean<CharacterEncodingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new CharacterEncodingFilter());
        Map<String, String> filterMap = new HashMap<>();
        filterMap.put("encoding", "UTF-8");
        filterMap.put("forceEncoding", "true");
        bean.setInitParameters(filterMap);
        bean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD);
        bean.setOrder(1);
        return bean;
    }

    @Bean
    public FilterRegistrationBean initFilterBean(@Autowired InitFilter initFilter) {
        FilterRegistrationBean<InitFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(initFilter);
        bean.setDispatcherTypes(DispatcherType.REQUEST);
        bean.setOrder(2);
        return bean;
    }

    @Bean
    public FilterRegistrationBean multipartFilter() {
        FilterRegistrationBean<MultipartFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new MultipartFilter());
        bean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD);
        bean.setOrder(3);
        return bean;
    }

    @Bean
    public FilterRegistrationBean springSecurityFilter(@Qualifier("springSecurityFilterChain") Filter filter) {
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(filter);
        bean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD);
        bean.setOrder(4);
        return bean;
    }

    @Bean
    public FilterRegistrationBean bootstrapFilterBean(@Autowired BootstrapFilter bootstrapFilter) {
        FilterRegistrationBean<BootstrapFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(bootstrapFilter);
        bean.setDispatcherTypes(DispatcherType.REQUEST);
        bean.setOrder(5);
        return bean;
    }

    @Bean
    public FilterRegistrationBean persistenceSessionFilterBean(@Autowired PersistenceSessionFilter persistenceSessionFilter) {
        FilterRegistrationBean<PersistenceSessionFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(persistenceSessionFilter);
        bean.setDispatcherTypes(DispatcherType.REQUEST);
        bean.setOrder(6);
        return bean;
    }

    @Bean
    public FilterRegistrationBean requestMappingFilterBean(@Autowired RequestMappingFilter requestMappingFilter) {
        FilterRegistrationBean<RequestMappingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(requestMappingFilter);
        bean.setDispatcherTypes(DispatcherType.REQUEST);
        bean.setOrder(7);
        return bean;
    }

}
