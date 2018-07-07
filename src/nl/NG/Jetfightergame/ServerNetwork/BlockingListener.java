package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.Tools.Logger;

import java.io.IOException;

/**
 * @author Geert van Ieperen created on 10-5-2018.
 */
public interface BlockingListener {

    /**
     * repeatedly runs {@link #handleMessage()} until it returns false
     * @return an {@link IOException} if a connection error occurs, otherwise null.
     * The stacktrace is printed before returning
     */
    default IOException listen() {
        try {
            // loop until handleMessage returns false (the connection should be closed)
            while (handleMessage());
            Logger.print("Stopped listening " + this);

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
