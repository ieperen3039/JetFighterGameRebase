package nl.NG.Jetfightergame.Engine;

/**
 * @author Geert van Ieperen
 * created on 7-2-2018.
 */
public class GLException extends RuntimeException {
    public GLException(String message) {
        super(message);
    }

    public GLException(Throwable cause) {
        super(cause);
    }
}
