package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.Tools.MatrixStack.GL2;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * @author Geert van Ieperen
 *         created on 9-11-2017.
 * a collection of consumers that operate on an object given in the constructor
 */
public abstract class AutomatedCollection<T> {
    protected final T executer;
    protected Collection<Consumer<T>> items;

    protected AutomatedCollection(T executor) {
        this.executer = executor;
    }

    /**
     * the executable should do something with the executor of this object
     * @param executable
     */
    public void addItem(Consumer<T> executable){
        items.add(executable);
    }

    public void clear(){
        items = new HashSet<>();
    }

    /**
     * draws all the collected items
     * @param gl
     */
    public void draw(GL2 gl){
        if (items == null || items.isEmpty()) return;
        drawItems(gl);
    }

    /**
     * prepare environment and then do {@link #execute()}
     * @param gl
     */
    protected abstract void drawItems(GL2 gl);

    protected void execute() {
        items.forEach(f -> f.accept(executer));
    }
}
