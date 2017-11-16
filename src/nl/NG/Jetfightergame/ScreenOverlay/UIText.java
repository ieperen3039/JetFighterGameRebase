package nl.NG.Jetfightergame.ScreenOverlay;

import org.joml.Vector4f;

import java.util.function.Supplier;

public class UIText extends UIElement {
    Supplier<String> supplier;
    private int align = 0;

    public UIText(int width, int height, Supplier<String> contentSupplier) {
        super(0, 0, width, height);
        this.supplier = contentSupplier;
    }

    public UIText(int width, int height, int align, Supplier<String> contentSupplier) {
        this(width, height, contentSupplier);
        this.align = align;
    }

    @Override
    public void draw(Hud hud) {
        hud.text(x, y, 30, Hud.Font.MEDIUM, align, supplier.get(), new Vector4f(1f));
    }
}
