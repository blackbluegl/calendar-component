package org.vaadin.addon.calendar.client.ui.util;

/**
 * A sting util class
 *
 * @author guettler
 * @since 02.08.17
 */
public class Strings {

    public static String format(final String format, final Object... args) {
        String retVal = format;
        for (final Object current : args) {
            retVal = retVal.replaceFirst("[%][s]", current.toString());
        }
        return retVal;
    }

}
