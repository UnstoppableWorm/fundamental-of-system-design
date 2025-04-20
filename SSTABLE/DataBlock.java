package SSTABLE;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

//데이터 블록 - 4MB - 4KB * 1024개
//내부에는 데이터 블록 엔트리
//리스타트 포인트 엔트리
//리스타트포인트 시작 오프셋, 길이

//https://sslab.dankook.ac.kr/1-4-sstable/
public class DataBlock {

    List<DataBlockEntry> dataBlockEntries;
    List<RestartPointEntry> restartPointEntries;

    int remain; //4kb중 남은 메모리량
    int dataIdx; // 16배수 계산값

    int dataOffset; // 현재 데이터블록엔트리의 시작 오프셋
    int restartOffset;
    int restartPointLength;

    public DataBlock(){
        remain = 4096;
        dataIdx = 0;
        
        dataOffset = 0;

        //우선 restartPoint의 사이즈 초기화
        restartPointLength = 0;
        restartOffset = 0;
        remain -= 8;

        dataBlockEntries = new ArrayList<>();
        restartPointEntries = new ArrayList<>();
    }

    public boolean insert(String key, String value){
        DataBlockEntry prevData = null;
        RestartPointEntry prevRe = null;
        if(dataIdx > 0 && (dataIdx%16 != 0)){
            prevData = dataBlockEntries.get(dataIdx);
            prevRe = restartPointEntries.get(dataIdx);
        }

        DataBlockEntry dataBlockEntry = new DataBlockEntry(prevData,key,value);
        int dataSize = dataBlockEntry.calcuate();

        RestartPointEntry restartPointEntry = new RestartPointEntry(prevRe,key,dataOffset+dataSize);
        int restartSize = restartPointEntry.calcuate();

        if(remain < dataSize+restartSize) return false;

        dataBlockEntries.add(dataBlockEntry);
        restartPointEntries.add(restartPointEntry);
        restartPointLength++;

        remain -= (dataSize+restartSize);
        return true;
    }

    public byte[] serialze(){

        ByteBuffer buffer = ByteBuffer.allocate(4 * 1024);  // 4KB

        //0번부터, 데이터블록이 1번부터 쌓임
        for(DataBlockEntry dataBlockEntry: dataBlockEntries){
             byte[] bytes = dataBlockEntry.serialze();
             buffer.put(bytes);
             restartOffset += bytes.length;
        }

        //데이터블록 바로 다음부터 리스타트포인트 쌓임.
        for(RestartPointEntry restartPointEntry: restartPointEntries){
            byte[] bytes = restartPointEntry.serialze();
            buffer.put(bytes);
            restartPointLength += bytes.length;
        }

        buffer.position(4096-8);
        buffer.putInt(restartOffset);
        buffer.putInt(restartPointLength);

        return buffer.array();
    }

    public static DataBlock deserialize(ByteBuffer buffer){
        buffer.position(4096-8);
        int restartOffset = buffer.getInt();
        buffer.position(4096-4);
        int restartPointLength = buffer.getInt();

        DataBlock dataBlock = new DataBlock();
        dataBlock.restartOffset = restartOffset;
        dataBlock.restartPointLength = restartPointLength;

        int offset = 0;
        while(offset < restartOffset){
            buffer.position(offset);
            int commonKeyLength = buffer.getInt();
            int uniqueKeylength = buffer.getInt();
            int valueLength = buffer.getInt();
            byte[] uniqueKeyContentBytes = new byte[uniqueKeylength];
            buffer.get(uniqueKeyContentBytes);
            String uniqueKeyContents = new String(uniqueKeyContentBytes,StandardCharsets.UTF_8);

            byte[] valueBytes = new byte[valueLength];
            buffer.get(valueBytes);
            String value = new String(valueBytes,StandardCharsets.UTF_8);

            DataBlockEntry dataBlockEntry = new DataBlockEntry(commonKeyLength,uniqueKeylength,valueLength,uniqueKeyContents,value);
            offset += dataBlockEntry.calcuate();

            dataBlock.dataBlockEntries.add(dataBlockEntry);
        }

        return dataBlock;
    }
}
