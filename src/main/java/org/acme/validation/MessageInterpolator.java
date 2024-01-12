package org.acme.validation;

import io.quarkus.hibernate.validator.runtime.locale.LocaleResolversWrapper;
import io.quarkus.runtime.LocalesBuildTimeConfig;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.AggregateResourceBundleLocator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

/**
 * Use AggregateResourceBundleLocator, combining entries of all ValidationMessages-resources of all loaded jars
 *
 * @see AggregateResourceBundleLocator
 * @see PlatformResourceBundleLocator
 * @see DEFAULT_VALIDATION_MESSAGES
 * @see USER_VALIDATION_MESSAGES
 */
@ApplicationScoped
public class MessageInterpolator extends ResourceBundleMessageInterpolator implements jakarta.validation.MessageInterpolator {

    /**
     * Locator combines all validationMessage-properties from
     * <ul>
     * <li>org.hibernate.validator.ValidationMessages</li>
     * <li>ValidationMessages defined in base-jar</li>
     * <li>ValidationMessages defined localy</li>
     * </ul>
     */
    public static final AggregateResourceBundleLocator LOCATOR = new AggregateResourceBundleLocator(
            //load from hibernate's resources
            List.of(DEFAULT_VALIDATION_MESSAGES),
            //load from our validationMessages located in base-jar and current project (see last param true -> 'aggregate')
            new PlatformResourceBundleLocator(ResourceBundleMessageInterpolator.USER_VALIDATION_MESSAGES, null, true));

    /**
     * Get resourceBundle for current locale (see requestContextHolder) and get the String with given key
     *
     * @param aPropertyKey
     * @return
     * @see RequestContextHolder#getLocale()
     */
    public MessageInterpolator(LocalesBuildTimeConfig aConfiguration, LocaleResolversWrapper aLocaleResolversWrapper) {
        super(LOCATOR, aConfiguration.locales, aConfiguration.defaultLocale, aLocaleResolversWrapper, false);
    }

    /**
     * resolve given property in user ValidationMessages for current Locale
     *
     * @param aPropertyKey
     * @return If found: value of property in current locale. If not found the propertyKey.
     */
    public static String getString(String aPropertyKey) {
        return getString(aPropertyKey, () -> aPropertyKey);
    }

    /**
     * resolve given property in user ValidationMessages for current Locale
     *
     * @param aPropertyKey
     * @param aFallback
     * @return If found: value of property in current locale. If not found the fallback.
     */
    public static String getString(String aPropertyKey, String aFallback) {
        return getString(aPropertyKey, () -> aFallback);
    }

    /**
     * resolve given property in user ValidationMessages for current Locale
     *
     * @param aPropertyKey
     * @param aFallback
     * @return If found: value of property in current locale. If not found the supplied fallback.
     */
    public static String getString(String aPropertyKey, Supplier<String> aFallbackSupplier) {
        ResourceBundle resourceBundle = LOCATOR.getResourceBundle(Locale.getDefault());
        if (resourceBundle.containsKey(aPropertyKey)) {
            return resourceBundle.getString(aPropertyKey);
        }
        return aFallbackSupplier.get();
    }
}
