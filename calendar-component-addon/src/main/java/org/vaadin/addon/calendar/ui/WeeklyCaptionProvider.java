package org.vaadin.addon.calendar.ui;

import java.time.temporal.TemporalAccessor;

/**
 * A Function to provide a date caption format
 *
 * @author guettler
 * @since 01.08.17
 */
@FunctionalInterface
public interface WeeklyCaptionProvider {

    String captionFrom(TemporalAccessor date);
}
