package org.radarcns;

import static org.junit.Assert.assertEquals;

import javax.servlet.ServletContext;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by francesco on 05/03/2017.
 */
public class ExampleMockito {

    @Test
    public void test() {
        final ServletContext servletContext = Mockito.mock(ServletContext.class);
        Mockito.doReturn("hello").when(servletContext).getAttribute("test");

        assertEquals("hello", servletContext.getAttribute("test"));
    }

}
