package com.jonnymatts.jzonbie.junit;

import com.jonnymatts.jzonbie.Jzonbie;
import org.junit.jupiter.api.extension.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * JUnit {@link Extension} that provides a {@link Jzonbie} in a test suite.
 * <p>
 * All test methods will use the same Jzonbie. The Jzonbie will be reset after
 * each test.
 * <pre>
 * &#64;ExtendWith(JzonbieExtension.class)
 * class ExampleTest {
 *
 *     &#64;Test
 *     void testMethod(Jzonbie jzonbie) {
 *         jzonbie.prime(get("/"), ok());
 *         ...
 *     }
 * }
 * </pre>
 * <p>
 * The extension can provide a Jzonbie of any class that extends Jzonbie, specified
 * by the {@link JzonbieExtension} annotation. The class provided must have a no
 * argument constructor.
 * <p>
 * <pre>
 * &#64;ExtendWith(JzonbieExtension.class)
 * &#64;JzonbieConfiguration(CustomJzonbie.class)
 * class ExampleTest {
 *
 *     &#64;Test
 *     void testMethod(CustomJzonbie jzonbie) {
 *         jzonbie.prime(get("/"), ok());
 *         ...
 *     }
 * }
 * </pre>
 */
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

    private <T extends Jzonbie> T getJzonbie(Class<T> requestedJzonbieClass) {
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
        return TestJzonbie.class;
    }

    @Target(TYPE)
    @Retention(RUNTIME)
    public @interface JzonbieConfiguration {

        Class<? extends Jzonbie> value() default Jzonbie.class;

    }
}