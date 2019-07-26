package com.jonnymatts.jzonbie.junit;

import com.jonnymatts.jzonbie.Jzonbie;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class JzonbieExtension implements ParameterResolver {

    private static final String DEFAULT_JZONBIE_KEY = "default";

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        final Class<?> parameterClass = parameterContext.getParameter().getType();
        return Jzonbie.class.isAssignableFrom(parameterClass);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        final ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.create(JzonbieExtension.class));
        store.put(DEFAULT_JZONBIE_KEY, new Jzonbie());
        return new Jzonbie();
    }
}