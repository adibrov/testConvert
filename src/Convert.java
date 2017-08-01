import ij.ImageJ;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by dibrov on 11/03/17.
 */
public class    Convert {

    public static byte[] load8bitPngToByteBuffer(String path) {

        BufferedImage bi = null;
        try (FileInputStream f = new FileInputStream(path)) {
            bi = ImageIO.read(f);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
    }
    public static short[] load16bitPngToByteBuffer(String path) {

        BufferedImage bi = null;
        try (FileInputStream f = new FileInputStream(path)) {
            bi = ImageIO.read(f);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return ((DataBufferUShort) bi.getRaster().getDataBuffer()).getData();
    }



    public static void convertTIFFToRaw(String pPathToTIFF,
                                         String pName) {

        Img<UnsignedShortType> img =
                null;
//        ImgOpener iop = new ImgOpener();




        try {

            img = (Img<UnsignedShortType>) new ImgOpener().openImgs(pPathToTIFF)
                    .get(0);
            String name = pName + img.dimension(0) + "x"+img.dimension(1)+"x" + img.dimension(2)+".raw";
            FileOutputStream f = new FileOutputStream(name);
            RandomAccess<UnsignedShortType> ra = img.randomAccess();

            // ra.get().

            int ndim = img.numDimensions();
            if (ndim != 3) {
                throw new IllegalArgumentException("don't know what to do with it yet... ndim != 3");
            }
            int x = (int) img.dimension(0);
            int y = (int) img.dimension(1);
            int z = (int) img.dimension(2);

            System.out.println("converting a tiff with dims: " + x
                    + " "
                    + y
                    + " "
                    + z);

            byte[] arr = new byte[2 * x * y * z];

            int mask1 = 0B1111111100000000;
            int mask2 = 0B0000000011111111;

            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    for (int k = 0; k < z; k++) {
                        int pos[] =
                                {i, j, k};
                        ra.setPosition(pos);
                        int curr = ra.get().getInteger();
                        // System.out.println("integer is: " + curr);
                        arr[2
                                * (i + x * j
                                + x * y
                                * k)] =
                                (byte) ((mask1
                                        & (ra.get().getInteger())) >>> 8);
                        arr[2 * (i + x * j + x * y * k)
                                + 1] = (byte) ((mask2 & ra.get().getInteger()));
                        // System.out.println("byte1 is: " +arr[2 * (i + x * j + x * y *
                        // k)]);
                        // System.out.println("byte2 is: " +arr[2 * (i + x * j + x * y * k)
                        // + 1]);
                    }
                }
            }

            f.write(arr);
            f.close();
            System.out.println("done converting");


        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("finally!");
        }
    }

    public static void main(String[] args) {
        convertTIFFToRaw("resources/img/single_stack_level_0.tif", "resources/img/single_stack_level_0");

    }

}
