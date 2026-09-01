package com.pe.cloudapi.shared.infrastructure.configuration;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

//TODO: separar por modulos para evitar este name generator
public class BoundedContextBeanNameGenerator extends AnnotationBeanNameGenerator {

    private static final String ROOT = "com.pe.cloudapi.";

    @Override
    protected @NonNull String buildDefaultBeanName(@NonNull BeanDefinition definition) {
        String simpleName = super.buildDefaultBeanName(definition);
        String context = contextOf(definition.getBeanClassName());

        return context == null
                ? simpleName
                : context + Character.toUpperCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    /**
     * El primer segmento tras la raíz del proyecto: {@code monitoring},
     * {@code alerting}, {@code insights}, {@code iam}, {@code shared}.
     *
     * @return nulo si la clase no vive bajo la raíz, y entonces se deja el
     *         nombre por defecto en vez de inventar un prefijo
     */
    private static @Nullable String contextOf(@Nullable String className) {
        if (className == null || !className.startsWith(ROOT)) {
            return null;
        }
        String path = className.substring(ROOT.length());
        int separator = path.indexOf('.');
        return separator < 0 ? null : path.substring(0, separator);
    }
}
