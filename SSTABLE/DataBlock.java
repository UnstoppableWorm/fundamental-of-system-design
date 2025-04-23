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
    int dataOffset; // 현재 데이터블록엔트리의 시작 오프셋
    int restartOffset;
    int restartPointLength;
    String[] keyArr;
    int[] offsetArr;

    public DataBlock(){
        remain = 4096;
        
        dataOffset = 0;

        //우선 restartPoint의 사이즈 초기화
        restartPointLength = 0;
        restartOffset = 0;
        remain -= 8;

        dataBlockEntries = new ArrayList<>();
        restartPointEntries = new ArrayList<>();
    }

    public boolean insert(String key, String value){
        int dataIdx = dataBlockEntries.size();
        int restartIdx = restartPointEntries.size();

        DataBlockEntry prevData = null;
        RestartPointEntry prevRe = null;

        if(dataIdx > 0 && (dataIdx%16 != 0)){
            prevData = dataBlockEntries.get(dataIdx-1);
        }

        if(restartIdx > 0){
            prevRe = restartPointEntries.get(restartIdx-1);
        }

        DataBlockEntry dataBlockEntry = new DataBlockEntry(prevData,key,value);
        RestartPointEntry restartPointEntry = null;
        int dataSize = dataBlockEntry.calcuate();
        int restartSize = 0;

        if(dataIdx%16 == 0){
            restartPointEntry = new RestartPointEntry(prevRe,key,dataOffset);
            restartSize = restartPointEntry.calcuate();
        }

        if(remain < dataSize+restartSize) return false;

        dataBlockEntries.add(dataBlockEntry);
        if(restartPointEntry != null){
            restartPointEntries.add(restartPointEntry);
        }

        dataOffset += dataSize;
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
        buffer.position(0);
        while(offset < restartOffset){
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

        while(offset < restartOffset+restartPointLength){
            int commonKeyLength = buffer.getInt();
            int uniqueKeylength = buffer.getInt();
            int currentRestartOffset = buffer.getInt();
            byte[] uniqueKeyContentBytes = new byte[uniqueKeylength];
            buffer.get(uniqueKeyContentBytes);
            String uniqueKeyContents = new String(uniqueKeyContentBytes,StandardCharsets.UTF_8);

            RestartPointEntry restartPointEntry = new RestartPointEntry(commonKeyLength,uniqueKeylength,currentRestartOffset,uniqueKeyContents);

            RestartPointEntry prev = null;
            if(dataBlock.restartPointEntries.size() > 0){
                prev = dataBlock.restartPointEntries.get(dataBlock.restartPointEntries.size()-1);
            }
            if(prev == null){
                restartPointEntry.recover("");
            }else{
                restartPointEntry.recover(prev.key);
            }


            offset += restartPointEntry.calcuate();
            dataBlock.restartPointEntries.add(restartPointEntry);
        }

        dataBlock.buildSearchList();

        return dataBlock;
    }

    private void buildSearchList() {
        int length = restartPointEntries.size();
        keyArr = new String[length+1];
        offsetArr = new int[length+1];

        String MIN_STRING = String.valueOf(Character.MIN_VALUE).repeat(10);  // "\u0000\u0000..."
        keyArr[0] = MIN_STRING;
        offsetArr[0] = -1;

        for(int i=0;i<length;i++){
            keyArr[i+1] = restartPointEntries.get(i).key;
            offsetArr[i+1] = restartPointEntries.get(i).offset;
        }
    }

    public String search(int dataBlockStartOffset, String key) {

        int left = 0;
        int right = keyArr.length-1;
        int mid = (left+right)/2;
        while(left<right){
            mid = (left+right)/2;
            if(key.compareTo(keyArr[mid]) >= 0){
                left = mid+1;
            }else{
                right = mid;
            }
        }


        //도달 불가능한(앵간하면) 키값이 특정되면 그냥 없다고 가정한다.
        if(mid == 0) return null;

        //한잔은 최저가짜값을 위하여
        //한잔은 현재 키값보다 큰값중 가장 작은을 찾았던 대가를 위하여
        int curOffset = offsetArr[mid-1];

        //그래도 offset이 있다면 실제 찾기를 진행한다!
        //mid*16번째부터, mid*16+15까지 복구한다
        return recoverDataEntries(16*(mid-1), key);
    }
    //kiwi
    //mango
    //orange

    private String recoverDataEntries(int startEntryIdx, String targetKey) {
        DataBlockEntry originBlock = dataBlockEntries.get(startEntryIdx);
        String key = "";

        for(int i=startEntryIdx;i<Math.min(startEntryIdx+16,dataBlockEntries.size()) ;i++){
            DataBlockEntry dataBlockEntry = dataBlockEntries.get(i);
            dataBlockEntry.recover(key);
            key = dataBlockEntry.key;
            if(key.equals(targetKey)) return dataBlockEntry.value;
        }

        return null;
    }
}
