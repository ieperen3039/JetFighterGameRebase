package nl.NG.Tools;

import java.io.IOException;

/**
 * @author Geert van Ieperen created on 10-5-2018.
 */
public interface BlockingListener {

    /**
     * execute {@link #listen()} in a separate thread
     * @param setDaemon if true, this thread will automatically terminate if no other non-daemon threads are running
     */
    default void listenInThread(boolean setDaemon) {
        Thread t = new Thread(this::listen, "Listener-" + getClass().getSimpleName());
        t.setDaemon(setDaemon);
        t.start();
    }

    /**
     * repeatedly runs {@link #handleMessage()} until it returns false.
     */
    default void listen() {
        try {
            // loop until handleMessage returns false (the connection should be closed)
            while (handleMessage()) ;

        } catch (IOException ex) {
            Logger.ERROR.print(ex);
        }
    }

    /**
     * handles one message
     * @return false iff the connection should be closed
     * @throws IOException if any connection errors occur
     */
    boolean handleMessage() throws IOException;
}
