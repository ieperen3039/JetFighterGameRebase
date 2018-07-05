package nl.NG.Jetfightergame.Tools;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author Geert van Ieperen. Created on 5-7-2018.
 */
public class StreamCombinationTest {
    /** an indication of the size of the used buffer */
    protected static final int BUFFER_SIZE = 64;
    protected InputStream in;
    protected OutputStream out;

    @Test(timeout = 100)
    public void testBigSingle() throws IOException {
        Random rand = new Random();
        byte[] input = new byte[BUFFER_SIZE * 3];
        rand.nextBytes(input);

        new Thread(() -> {
            System.out.println("Start writing...");
            for (int i = 0; i < input.length; i++) {
                tryWrite(input[i]);
                if (i % 5 == 0) tryFlush();
            }
            tryClose();
        }).start();

        for (byte anInput : input) {
            int read = in.read();
            System.out.println(read);
            Assert.assertEquals(anInput, read);
        }

        if (in.available() > 0) Assert.fail("bytes left: " + in.available());
    }

    @Test(timeout = 100)
    public void testBigMultiple() throws IOException {
        Random rand = new Random();
        byte[] values = new byte[BUFFER_SIZE * 3];
        rand.nextBytes(values);
        List<byte[]> input = new ArrayList<>();

        int n = 0;
        while (n < values.length) {
            int size = rand.nextInt(BUFFER_SIZE / 2 + 1);
            size = Math.min(size, values.length - n);

            byte[] bytes = new byte[size];
            System.arraycopy(values, n, bytes, 0, size);
            input.add(bytes);
            n += size;
        }

        new Thread(() -> {
            System.out.println("Start writing...");

            for (byte[] bytes : input) {
                tryWrite(bytes);
                tryFlush();
            }

            tryClose();
        }).start();

        for (byte[] chunk : input) {
            byte[] bytes = new byte[chunk.length];
            Assert.assertEquals(chunk.length, in.read(bytes));

            System.out.println("---");
            for (int i = 0; i < chunk.length; i++) {
                System.out.println(bytes[i]);
                Assert.assertEquals(chunk[i], bytes[i]);
            }
        }
        if (in.available() > 0) Assert.fail("bytes left: " + in.available());
    }

    @Test(timeout = 100)
    public void testSingle() throws IOException {
        int[] input = new int[]{3, 1, 2};

        new Thread(() -> {
            System.out.println("Start writing...");
            for (int b : input) {
                tryWrite(b);
                tryFlush();
            }
        }).start();

        for (int b : input) {
            int read = in.read();
            System.out.println(read);
            Assert.assertEquals(b, read);
        }

        if (in.available() > 0) Assert.fail("bytes left: " + in.available());
    }

    @Test(timeout = 100)
    public void testMultiple() throws IOException {
        byte[] input = new byte[]{3, 1, 2};

        new Thread(() -> {
            System.out.println("Start writing...");
            tryWrite(input);
            tryFlush();
        }).start();

        byte[] bytes = new byte[input.length];
        Assert.assertEquals(input.length, in.read(bytes));

        for (int i = 0; i < input.length; i++) {
            System.out.println(bytes[i]);
            Assert.assertEquals(input[i], bytes[i]);
        }

        if (in.available() > 0) Assert.fail("bytes left: " + in.available());
    }

    @Test(timeout = 1000)
    public void testOverSized() throws IOException {
        Random rand = new Random();
        byte[] input = new byte[(int) (BUFFER_SIZE * 3.7)];
        rand.nextBytes(input);

        new Thread(() -> {
            System.out.println("Start writing array of " + input.length + " items...");
            tryWrite(input.clone());
            tryFlush();
        }).start();

        byte[] bytes = new byte[input.length];
        int read = in.read(bytes);
        Assert.assertEquals(input.length, read);
        System.out.println(Arrays.toString(bytes));

        for (int i = 0; i < input.length; i++) {
            Assert.assertEquals("index " + i, input[i], bytes[i]);
        }

        if (in.available() > 0) Assert.fail("bytes left: " + in.available());
    }

    @Test(timeout = 100)
    public void testUseAsDataStream() throws IOException {
        DataOutputStream DOS = new DataOutputStream(new BufferedOutputStream(out));
        DataInputStream DIS = new DataInputStream(in);

        double dInput = Math.PI;
        boolean bool = false;
        String strInput = "THIS IS A TEST, DO NOT PANIC";

        new Thread(() -> {
            System.out.println("Start writing...");
            try {
                DOS.writeDouble(dInput);
                DOS.writeBoolean(bool);
                DOS.writeUTF(strInput);
                DOS.flush();
            } catch (IOException e) {
                e.printStackTrace();
                Assert.fail();
            }
        }).start();

        double dRead = DIS.readDouble();
        System.out.println(dRead);
        Assert.assertTrue(dInput - dRead < 1E-9);

        boolean bRead = DIS.readBoolean();
        System.out.println(bRead);
        Assert.assertEquals(bool, bRead);

        String sRead = DIS.readUTF();
        System.out.println(sRead);
        Assert.assertEquals(strInput, sRead);

        if (in.available() > 0) Assert.fail("bytes left: " + in.available());
    }

    private void tryWrite(byte[] input) {
        try {
            out.write(input);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    private void tryClose() {
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    private void tryWrite(int i) {
        try {
            out.write(i);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    private void tryFlush() {
        try {
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
