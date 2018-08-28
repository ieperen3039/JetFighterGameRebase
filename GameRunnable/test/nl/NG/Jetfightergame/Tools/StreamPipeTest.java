package nl.NG.Jetfightergame.Tools;

import org.junit.Before;

/**
 * @author Geert van Ieperen. Created on 5-7-2018.
 */
public class StreamPipeTest extends StreamCombinationTest {
    @Before
    public void setUp() throws Exception {
        StreamPipe pipe = new StreamPipe(BUFFER_SIZE);
        in = pipe.getInputStream();
        out = pipe.getOutputStream();
    }
}