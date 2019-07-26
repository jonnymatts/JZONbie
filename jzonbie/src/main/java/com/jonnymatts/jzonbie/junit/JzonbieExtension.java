package com.jonnymatts.jzonbie.junit;

import com.jonnymatts.jzonbie.Jzonbie;
import org.junit.jupiter.api.extension.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class JzonbieExtension implements ParameterResolver, BeforeAllCallback, BeforeEachCallback, AfterAllCallback {

    private static Jzonbie jzonbie;

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        final Class<?> parameterClass = parameterContext.getParameter().getType();
        return Jzonbie.class.isAssignableFrom(parameterClass);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        final Class<? extends Jzonbie> type = (Class<? extends Jzonbie>) parameterContext.getParameter().getType();
        return getJzonbie(type);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        final Class<? extends Jzonbie> jzonbieClass = getJzonbieClass(context);
        JzonbieExtension.jzonbie = jzonbieClass.newInstance();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        jzonbie.reset();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        jzonbie.stop();
    }

    public static Jzonbie getJzonbie() {
        return getJzonbie(Jzonbie.class);
    }

    public static <T extends Jzonbie> T getJzonbie(Class<T> requestedJzonbieClass) {
        final Class<? extends Jzonbie> jzonbieClass = jzonbie.getClass();
        if(requestedJzonbieClass.isAssignableFrom(jzonbieClass)) {
            return requestedJzonbieClass.cast(jzonbie);
        }
        throw new IllegalStateException(String.format("Incorrect Jzonbie type configured. Requested: %s, Configured: %s", requestedJzonbieClass.getName(), jzonbieClass.getName()));
    }

    private Class<? extends Jzonbie> getJzonbieClass(ExtensionContext extensionContext) {
        final JzonbieConfiguration classAnnotation = extensionContext.getRequiredTestClass().getAnnotation(JzonbieConfiguration.class);
        if(classAnnotation != null) {
            return classAnnotation.value();
        }
        return Jzonbie.class;
    }

    @Target(TYPE)
    @Retention(RUNTIME)
    public @interface JzonbieConfiguration {

        Class<? extends Jzonbie> value() default Jzonbie.class;

    }
}