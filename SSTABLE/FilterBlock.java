package SSTABLE;

import java.util.BitSet;

public class FilterBlock {
    private final BitSet bitSet = new BitSet(1024);
    private final int bitSetSize = 1024;

    // 간단한 해시 함수 시드값
    private final int[] seeds = {17, 31, 73};

    // 키 삽입
    public void add(String key) {
        for (int seed : seeds) {
            int hash = hash(key, seed);
            int index = Math.abs(hash % bitSetSize);
            bitSet.set(index);
        }
    }

    // 키 포함 여부 확인
    public boolean mightContain(String key) {
        for (int seed : seeds) {
            int hash = hash(key, seed);
            int index = Math.abs(hash % bitSetSize);
            if (!bitSet.get(index)) return false;
        }
        return true;
    }

    // 해시 함수 (JDK 기본 해시 + 시드 조합)
    private int hash(String key, int seed) {
        int h = 0;
        for (char c : key.toCharArray()) {
            h = h * seed + c;
        }
        return h;
    }

    // 직렬화
    public byte[] serialize() {
        byte[] raw = new byte[128]; // 128 bytes
        byte[] bits = bitSet.toByteArray();
        System.arraycopy(bits, 0, raw, 0, Math.min(bits.length, 128));
        return raw;
    }

    // 역직렬화
    public static FilterBlock deserialize(byte[] data) {
        FilterBlock block = new FilterBlock();
        BitSet bs = BitSet.valueOf(data);
        block.bitSet.or(bs);
        return block;
    }
}