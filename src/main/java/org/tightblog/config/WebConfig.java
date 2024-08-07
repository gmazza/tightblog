/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.config;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.tightblog.rendering.model.Model;
import org.tightblog.rendering.model.URLModel;
import org.tightblog.rendering.model.UtilitiesModel;
import org.tightblog.rendering.service.ThemeTemplateResolver;
import org.tightblog.rendering.service.ThymeleafRenderer;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/tb-ui/app/get-default-blog");
    }

    @Bean
    public InternalResourceViewResolver jspViewResolver() {
        InternalResourceViewResolver jspViewResolver = new InternalResourceViewResolver("/WEB-INF/jsps/", ".jsp");
        jspViewResolver.setCacheUnresolved(false);
        jspViewResolver.setOrder(0);
        return jspViewResolver;
    }

    @Bean
    public ThymeleafViewResolver thymeleafViewResolver(SpringTemplateEngine standardTemplateEngine) {
        ThymeleafViewResolver thymeleafViewResolver = new ThymeleafViewResolver();
        thymeleafViewResolver.setCacheUnresolved(false);
        thymeleafViewResolver.setTemplateEngine(standardTemplateEngine);
        thymeleafViewResolver.setOrder(1);
        return thymeleafViewResolver;
    }

    // https://www.baeldung.com/spring-custom-validation-message-source#defining-localvalidatorfactorybean
    @Bean
    public LocalValidatorFactoryBean getValidator(MessageSource messageSource) {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }

    // To process resources in the webapp/thymeleaf folder: Atom feeds,
    // common blog theme elements, emails, etc.
    @Bean
    public SpringResourceTemplateResolver standardTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setPrefix("/thymeleaf/");
        resolver.setSuffix(".html");
        // 1 reserved for the ThemeTemplateResolver (blog theme-specific templates)
        resolver.setOrder(2);
        // not modifiable, so can cache
        resolver.setCacheable(true);
        return resolver;
    }

    @Bean
    public SpringTemplateEngine blogTemplateEngine(ThemeTemplateResolver themeTemplateResolver,
                                                   SpringResourceTemplateResolver standardTemplateResolver) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        Set<ITemplateResolver> templateResolvers = new HashSet<>();
        templateResolvers.add(themeTemplateResolver);
        templateResolvers.add(standardTemplateResolver);
        engine.setTemplateResolvers(templateResolvers);
        return engine;
    }

    @Bean
    public SpringTemplateEngine standardTemplateEngine(SpringResourceTemplateResolver standardTemplateResolver) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(standardTemplateResolver);
        return engine;
    }

    /**
     * Standard renderer uses a hardcoded template independent of any specific blog.
     * Examples: Atom feeds and the ExternalSourceController (embedded GitHub code).
     */
    @Bean
    public ThymeleafRenderer standardRenderer(SpringTemplateEngine standardTemplateEngine) {
        ThymeleafRenderer tr = new ThymeleafRenderer();
        tr.setTemplateEngine(standardTemplateEngine);
        return tr;
    }

    /**
     * The blog renderer is for generation of blog-specific pages, and has a more complex
     * strategy for determining the proper templates to use (default or blogger-overridden),
     */
    @Bean
    public ThymeleafRenderer blogRenderer(SpringTemplateEngine blogTemplateEngine) {
        ThymeleafRenderer tr = new ThymeleafRenderer();
        tr.setTemplateEngine(blogTemplateEngine);
        return tr;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Set<Model> pageModelSet(UtilitiesModel utilitiesModel, URLModel urlModel) {
        Set<Model> models = new HashSet<>();
        models.add(utilitiesModel);
        models.add(urlModel);
        return models;
    }

}
