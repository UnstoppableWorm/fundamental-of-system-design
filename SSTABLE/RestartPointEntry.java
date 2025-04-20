package SSTABLE;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class RestartPointEntry {
    int commonKeyLength;
    int uniqueKeylength;
    String uniqueKeyContents;
    String key;
    int offset;
    Byte[] serialzed;

    private RestartPointEntry() {
        return;
    }

    public RestartPointEntry(RestartPointEntry other, String key, int offset){
        if(other == null){
            other = new RestartPointEntry();
            other.key = "";
        }

        int chIdx = 0;
        commonKeyLength = 0;
        String otherKey = other.key;
        while(chIdx < otherKey.length() && chIdx < key.length()) {
            if(otherKey.charAt(chIdx) != key.charAt(chIdx)) break;
            commonKeyLength++;
        }

        uniqueKeylength = key.length() - commonKeyLength;
        uniqueKeyContents = key.substring(commonKeyLength);

        this.key = key;
        this.offset = offset;
    }

    public int calcuate(){
        //commonKeyLength = 4
        //uniqueKeyLength = 4
        //offset = 4
        //uniqueKeyContent
        return 12+uniqueKeylength;
    }

    public byte[] serialze() {
        ByteBuffer buffer = ByteBuffer.allocate(calcuate());
        buffer.putInt(commonKeyLength);
        buffer.putInt(uniqueKeylength);
        buffer.putInt(offset);
        buffer.put(uniqueKeyContents.getBytes(StandardCharsets.UTF_8));

        return buffer.array();
    }
}
