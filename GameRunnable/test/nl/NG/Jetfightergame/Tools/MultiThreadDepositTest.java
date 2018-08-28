package nl.NG.Jetfightergame.Tools;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Geert van Ieperen. Created on 4-7-2018.
 */
public class MultiThreadDepositTest {

    private static final int NOF_SUPPLIERS = 10;
    private static int MAX_VALUE = 5000;
    private AtomicInteger number;
    private Boolean[] isDone;
    private boolean[] seen;
    private MultiThreadDeposit<Integer> dump;

    @Before
    public void setUp() {
        number = new AtomicInteger();
        isDone = new Boolean[NOF_SUPPLIERS];
        Arrays.fill(isDone, false);
        seen = new boolean[MAX_VALUE];
        Arrays.fill(seen, false);
    }

    @Test
    public void iteratorTest() throws InterruptedException {
        dump = new MultiThreadDeposit<>(NOF_SUPPLIERS, 50);
        startup(dump);
        while (Arrays.asList(isDone).contains(false)) {
            for (Integer i : dump) {
                Assert.assertFalse("Received " + i + " a second time", seen[i]);
                seen[i] = true;
            }
            Thread.sleep(1);
        }

        for (int i = 0; i < seen.length; i++) {
            Assert.assertTrue("Missed nr " + i, seen[i]);
        }
    }

    @Test
    public void forEachTest() throws InterruptedException {
        dump = new MultiThreadDeposit<>(NOF_SUPPLIERS, 50);
        startup(dump);
        while (Arrays.asList(isDone).contains(false)) {
            dump.forEach(i -> {
                Assert.assertFalse("Received " + i + " a second time", seen[i]);
                seen[i] = true;
            });
            Thread.sleep(1);
        }

        for (int i = 0; i < seen.length; i++) {
            Assert.assertTrue("missed " + i, seen[i]);
        }
    }

    @Test
    public void delayTest() throws InterruptedException {
        dump = new MultiThreadDeposit<>(NOF_SUPPLIERS, 20);
        Thread.sleep(100);
        startup(dump);
        while (Arrays.asList(isDone).contains(false)) {
            for (Integer i : dump) {
                Assert.assertFalse("Received " + i + " a second time", seen[i]);
                seen[i] = true;
            }
            Thread.sleep(50);
        }
    }

    @Test(timeout = 1000)
    public void tooManyProducersErrorTest() throws InterruptedException {
        try {
            dump = new MultiThreadDeposit<>(NOF_SUPPLIERS, 50);
            for (int i = 0; i < NOF_SUPPLIERS; i++) {
                int nr = i;
                Thread t = new Thread(() -> randomSupplier(dump, nr, 10));
                t.start();
                t.join();
                number.set(0);
            }

            new Thread(() -> {
                int value = number.getAndIncrement();
                dump.accept(value); // should fail, is one more than the supplier max
                Assert.fail();
            }).run();

        } catch (IndexOutOfBoundsException ex) {
            System.out.println(ex.getMessage());
            Assert.assertEquals(ex.getMessage(), "Too many producers. Capacity: " + NOF_SUPPLIERS);
            // success
        }
    }

    private void startup(MultiThreadDeposit<Integer> dump) {
        for (int i = 0; i < NOF_SUPPLIERS; i++) {
            int nr = i;
            new Thread(() -> randomSupplier(dump, nr, MAX_VALUE)).start();
        }
    }

    private void randomSupplier(MultiThreadDeposit<Integer> dump, int index, int maxValue) {
        try {
            int value = number.getAndIncrement();
            while (value < maxValue) {
                dump.accept(value);
                value = number.getAndIncrement();
                Thread.sleep(0, 200);
            }
        } catch (InterruptedException ex) {
            Assert.fail(ex.toString());
        }

        isDone[index] = true;
    }
}