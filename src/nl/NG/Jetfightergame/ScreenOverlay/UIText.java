package nl.NG.Jetfightergame.ScreenOverlay;

import org.joml.Vector4f;

import java.util.function.Supplier;

public class UIText extends UIElement {
    private Supplier<String> supplier;
    private int align = 0;

    public UIText(int width, int height, Supplier<String> contentSupplier) {
        super(width, height);
        this.supplier = contentSupplier;
    }

    public UIText(int width, int height, int align, Supplier<String> contentSupplier) {
        this(width, height, contentSupplier);
        this.align = align;
    }

    @Override
    public void draw(ScreenOverlay.Painter hud) {
        hud.text(x, y, 30, ScreenOverlay.Font.ORBITRON_MEDIUM, align, new Vector4f(1f), supplier.get());
    }
}
