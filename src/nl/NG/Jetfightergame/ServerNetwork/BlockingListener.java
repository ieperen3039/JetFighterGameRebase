package nl.NG.Jetfightergame.ServerNetwork;

import java.io.IOException;

/**
 * @author Geert van Ieperen created on 10-5-2018.
 */
public interface BlockingListener {

    /**
     * starts listening to the socket until the connection is closed or an exception is thrown
     * @return an {@link IOException} if a connection error occurs
     */
    default IOException listen() {
        try {
            // loop until handleMessage returns false (the connection should be closed)
            while (handleMessage());

        } catch (IOException ex) {
            ex.printStackTrace();
            return ex;
        }

        return null;
    }

    /**
     * handles one message
     * @return false iff the connection should be closed
     * @throws IOException if any connection errors occur
     */
    boolean handleMessage() throws IOException;
}
