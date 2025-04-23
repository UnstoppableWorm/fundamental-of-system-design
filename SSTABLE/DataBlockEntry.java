package SSTABLE;

//데이터블록 엔트리는 공유 key 길이, 공유x key 길이, value 길이, 공유x key 컨텐츠, value 로 구성

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DataBlockEntry{
    int commonKeyLength;
    int uniqueKeylength;
    int valueLength;
    String uniqueKeyContents;
    String key;
    String value;
    Byte[] serialzed;

    private DataBlockEntry() {
        return;
    }

    public DataBlockEntry(DataBlockEntry other, String key,String value){
        if(other == null){
            other = new DataBlockEntry();
            other.key = "";
        }

        commonKeyLength = 0;
        String otherKey = other.key;
        while(commonKeyLength < otherKey.length() && commonKeyLength < key.length()) {
            if(otherKey.charAt(commonKeyLength) != key.charAt(commonKeyLength)) break;
            commonKeyLength++;
        }

        uniqueKeylength = key.length() - commonKeyLength;
        uniqueKeyContents = key.substring(commonKeyLength);

        valueLength = value.length();
        this.key = key;
        this.value = value;
    }

    public DataBlockEntry(int commonKeyLength, int uniqueKeylength, int valueLength, String uniqueKeyContents, String value) {
        this.commonKeyLength = commonKeyLength;
        this.uniqueKeylength = uniqueKeylength;
        this.valueLength = valueLength;
        this.uniqueKeyContents = uniqueKeyContents;
        this.value = value;
    }

    public byte[] serialze() {
        ByteBuffer buffer = ByteBuffer.allocate(calcuate());
        buffer.putInt(commonKeyLength);
        buffer.putInt(uniqueKeylength);
        buffer.putInt(valueLength);
        buffer.put(uniqueKeyContents.getBytes(StandardCharsets.UTF_8));
        buffer.put(value.getBytes(StandardCharsets.UTF_8));
        return buffer.array();
    }

    public int calcuate(){
        //commonKeyLength = 4
        //uniqueKeyLength = 4
        //valueLength = 4
        //uniqueKeyContent
        //value
        return 12+uniqueKeylength+valueLength;
    }

    public void recover(String prevKey){
        StringBuilder builder = new StringBuilder();
        builder.append(prevKey.substring(0,commonKeyLength));
        builder.append(uniqueKeyContents);
        this.key = builder.toString();
    }
}