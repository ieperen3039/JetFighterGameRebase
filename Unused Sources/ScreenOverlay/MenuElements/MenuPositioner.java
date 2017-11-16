package nl.NG.Jetfightergame.ScreenOverlay.MenuElements;

/**
 * @author Geert van Ieperen
 *         created on 9-11-2017.
 */
public abstract class MenuPositioner {
    public static final int STD_MARGIN = 30;
    public static final int STD_BOUND_DIST = 80;
    public final int margin;
    public final int boundaryDistance;
    protected int x;
    protected int y;

    public MenuPositioner() {
        margin = STD_MARGIN;
        boundaryDistance = STD_BOUND_DIST;
    }

    public MenuPositioner(int margin, int boundaryDistance) {
        this.margin = margin;
        this.boundaryDistance = boundaryDistance;
    }

    /**
     * sets the position of this element below the previous
     * @param element
     */
    public abstract void place(MenuElement element);
}
