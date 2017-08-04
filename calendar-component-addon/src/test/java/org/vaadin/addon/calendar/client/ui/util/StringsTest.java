package org.vaadin.addon.calendar.client.ui.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for string utils
 *
 * @author guettler
 * @since 02.08.17
 */
public class StringsTest {

    @Test
    public void format() throws Exception {
        this.assertFormat("Some test here  %s.", 54);
        this.assertFormat("Some test here %s and there %s, and test [%s].  sfsfs !!!", 54, 59, "HAHA");
        this.assertFormat("Some test here %s and there %s, and test [%s].  sfsfs !!!", 54, 59, "HAHA", "DONT SHOW");

        Assert.assertEquals("Formatting is not working", "Some test here  54 %s.", Strings.format("Some test here  %s %s.", 54));
    }

    private void assertFormat(final String format, final Object... args) {
        Assert.assertEquals("Formatting is not working", String.format(format, args), Strings.format(format, args));
    }

}