package jp.co.worksap.nlp.dartsclone;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.function.BiConsumer;
import java.util.ArrayList;
import java.util.List;

import jp.co.worksap.nlp.dartsclone.details.KeySet;
import jp.co.worksap.nlp.dartsclone.details.DoubleArrayBuilder;

public class DoubleArray {

    private IntBuffer array;
    private ByteBuffer buffer;
    private int size;

    public void setArray(IntBuffer array, int size) {
        this.array = array;
        this.size = size;
    }

    public IntBuffer array() {
        return array;
    }

    public void clear() {
        buffer = null;
    }

    public int size() { return size; }

    public void build(byte[][] keys, int[] values,
                      BiConsumer<Integer, Integer> progressFunction) {
        KeySet keySet = new KeySet(keys, values);
        DoubleArrayBuilder builder = new DoubleArrayBuilder(progressFunction);
        builder.build(keySet);
        buffer = builder.copy();
        array = buffer.asIntBuffer();
    }

    public void open(FileChannel inputFile, long position, long size)
        throws IOException {

        if (position < 0) {
            position = 0;
        }
        if (size < 0) {
            size = inputFile.size();
        }
        buffer = inputFile.map(FileChannel.MapMode.READ_ONLY,
                               position, size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        array = buffer.asIntBuffer();
    }

    public void save(FileChannel outputFile) throws IOException {
        outputFile.write(buffer, size);
    }

    public int[] exactMatchSearch(byte[] key) {
        int[] result = new int[] { -1, 0 };
        int nodePos = 0;
        int unit = array.get(nodePos);

        for (byte k : key) {
            nodePos ^= offset(unit) ^ byteToUint(k);
            unit = array.get(nodePos);
            if (label(unit) != k)
                return result;
        }
        if (!hasLeaf(unit)) {
            return result;
        }
        unit = array.get(nodePos ^ offset(unit));
        result[0] = value(unit);
        result[1] = key.length;
        return result;
    }

    public List<int[]> commonPrefixSearch(byte[] key, int offset,
                                               int maxNumResult) {
        List<int[]> result = new ArrayList<>();

        int nodePos = 0;
        int unit = array.get(nodePos);
        nodePos ^= offset(unit);
        for (int i = offset; i < key.length; i++) {
            byte k = key[i];
            nodePos ^= byteToUint(k);
            unit = array.get(nodePos);
            if (label(unit) != byteToUint(k)) {
                return result;
            }
            
            nodePos ^= offset(unit);
            if (hasLeaf(unit)) {
                if (result.size() < maxNumResult) {
                    int[] r = new int[] { value(array.get(nodePos)), i + 1 };
                    result.add(r);
                }
            }
        }
        return result;
    }

    private boolean hasLeaf(int unit) {
        return ((unit >>> 8) & 1) == 1;
    }
        
    private int value(int unit) {
        return unit & ((1 << 31) - 1);
    }

    private int label(int unit) {
        return unit & ((1 << 31) | 0xFF);
    }

    private int offset(int unit) {
        return ((unit >>> 10) << ((unit & (1 << 9)) >>> 6));
    }

    private int byteToUint(byte b) {
        return b & 0xFF;
    }
}
