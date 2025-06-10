package SSTABLE;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SSTable {
    Path path;
    FileChannel channel;
    Integer blockIdx;
    List<DataBlock> dataBlocks;
    List<FilterBlock> filterBlocks;
    int filterBlockstartOffset;
    int filterBlocksizse;

    SSTable(String path) throws IOException {
        this.path = Paths.get(path);
        dataBlocks = new ArrayList<>();
        filterBlocks = new ArrayList<>();
    }

    public String read(String key) throws IOException {
        channel = loadOrCreate();
        filterBlocks = getFilterBlocks();

        for(int i=filterBlocks.size()-1;i>=0;i--){
            FilterBlock filterBlock = filterBlocks.get(i);
            if(filterBlock.mightContain(key)){
                DataBlock dataBlock = getDataBlock(i);
                String result = dataBlock.search(4096*i, key);
                if(result != null) return result;
            }
        }
        channel.close();
        return "";
    }

    private DataBlock getDataBlock(int i) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4*1024);
        channel.read(buffer, i*4096);

        buffer.flip();
        DataBlock block = DataBlock.deserialize(buffer);
        return block;
    }

    private List<FilterBlock> getFilterBlocks() throws IOException {
        filterBlockstartOffset = getFilterBlockStartOffset();
        filterBlocksizse = getFilterBlockSize();
        List<FilterBlock> filterBlocks = new ArrayList<>();

        int idx = 0;
        while(idx*128 < filterBlocksizse){
            ByteBuffer buffer = ByteBuffer.allocate(128);
            channel.read(buffer, filterBlockstartOffset+idx*128);

            buffer.flip();
            FilterBlock block = FilterBlock.deserialize(buffer.array());
            filterBlocks.add(block);

            idx++;
        }

        return filterBlocks;
    }

    private List<DataBlock> getDataBlocks() throws IOException {
        // 읽을 버퍼 크기 (int는 4바이트)
        List<DataBlock> dataBlocks = new ArrayList<>();
        int idx = 0;
        while(idx*4096 < filterBlockstartOffset){
            ByteBuffer buffer = ByteBuffer.allocate(4*1024);
            channel.read(buffer, idx*4096);

            buffer.flip();
            DataBlock block = DataBlock.deserialize(buffer);
            dataBlocks.add(block);

            idx++;
        }

        return dataBlocks;

    }

    public void write(TreeMap<String, String> tree) throws Exception {
        channel = loadOrCreate();
        buildBlockEntryList(tree);
        write(dataBlocks);

        channel.close();
    }

    private int getFilterBlockStartOffset() throws IOException {
        // 읽을 버퍼 크기 (int는 4바이트)
        ByteBuffer buffer = ByteBuffer.allocate(4);
        channel.position(64*1024*1024-8);
        channel.read(buffer);

        buffer.flip();  // 읽기 모드로 전환
        return buffer.getInt();  // ByteBuffer에서 int 값 읽기
    }

    private int getFilterBlockSize() throws IOException {
        // 읽을 버퍼 크기 (int는 4바이트)
        ByteBuffer buffer = ByteBuffer.allocate(4);
        channel.read(buffer, 64*1024*1024-4);

        buffer.flip();  // 읽기 모드로 전환
        return buffer.getInt();  // ByteBuffer에서 int 값 읽기
    }

    private void buildBlockEntryList(TreeMap<String, String > map) {
        DataBlock dataBlock = new DataBlock();
        FilterBlock filterBlock = new FilterBlock();

        boolean inserted = false;
        for(Map.Entry<String,String> entry: map.entrySet()){
            inserted = dataBlock.insert(entry.getKey(),entry.getValue());

            if(!inserted){
                dataBlocks.add(dataBlock);
                filterBlocks.add(filterBlock);

                dataBlock = new DataBlock();
                filterBlock = new FilterBlock();

                if (!dataBlock.insert(entry.getKey(), entry.getValue())) {
                    throw new RuntimeException("단일 엔트리가 블록보다 큼: key=" + entry.getKey());
                }
            }
            filterBlock.add(entry.getKey());
        }

        if(dataBlock != null){
            dataBlocks.add(dataBlock);
            filterBlocks.add(filterBlock);
        }
    }

    private void write(List<DataBlock> blocks) throws Exception {
        byte[] bytes = serialize();

        channel.write(ByteBuffer.wrap(bytes));
    }

    public byte[] serialize(){
        ByteBuffer buffer = ByteBuffer.allocate(64*1024*1024); //64MB 할당하자.
        //데이터블록들 넣고
        for(DataBlock block: dataBlocks){
            byte[] bytes = block.serialze();
            buffer.put(bytes);

            filterBlockstartOffset += bytes.length;
        }

        //필터블록 넣고
        for(FilterBlock block: filterBlocks){
            byte[] bytes = block.serialize();
            buffer.put(bytes);

            filterBlocksizse += bytes.length;
        }

        //필터블록의 시작 오프셋과 사이즈 넣기
        buffer.position(64*1024*1024-8);
        buffer.putInt(filterBlockstartOffset);
        buffer.putInt(filterBlocksizse);

        return buffer.array();
    }

    public FileChannel loadOrCreate() throws IOException {
        FileChannel channel;

        if (Files.exists(path)) {
            System.out.println("파일이 존재합니다. 로딩 중...");
            channel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
        } else {
            System.out.println("파일이 존재하지 않습니다. 새로 생성합니다.");
            Files.createFile(path);
            channel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
        }

        return channel;
    }
}
