package SSTABLE;

import java.io.IOException;
import java.util.Collections;
import java.util.TreeMap;

public class Main {
    public static String path = "sstable_kwan";

    public static void main(String[] args) throws Exception {

        //몇번째 데이터블록에 할당해야할지 - 4바이트
        //인덱스 블록 - 4KB - 4byte * 1024개 -
        // 하나의 Index Entry가 가리키는 정보 =
        //key_length(1B) + key(bytes) + block_offset(4B) + block_size(4B)
        //필터 블록 - 128KB - 128byte * 1024개
        //데이터 블록 - 4MB - 4KB * 1024개
        //내부에는 데이터 블록 엔트리
        //리스타트 포인트 엔트리
        //리스타트포인트 길이
        
        //메타인덱스블록? 안쓸거. 푸터? 안쓸거.
        //맨처음에 몇번째 데이터블록및 필터를 써서 데이터를 삽입할지 결정해야 하므로 그 값(4바이트)가 필요함.
        //각 블록? 4k 크기로 가정
        //인덱스블록 - 1 개 - 4kb 쓸거. -> 각 데이터블록의 offset만 저장할거 -> 각 offset 4byte라고 할거. -> 1k개 데이터블록 수용 가능
        //데이터 블록 - 1k개 -> 4k용량 x 1k개 -> 4MB
        //필터블록 - 1개 데이터 블록당 128byte쓸거 -> 1k 개 필요 -> 128k 필요.
        //4KB + 4MB + 128KB -> 8MB로 할당하고 쓰자!
        //0. key, value 구조의 레드블랙트리 3개정도 생성(각 트리 하나 하나가 데이터블록 하나가 될예정).
        TreeMap<String,String> tree = datainItilize();

        //쓰기!
        //. 몇번째 데이터블록에 삽입할지, 블록의 남은 메모리는 얼마인지 가져옴.(메모리상에 있다면 그걸로)
        //1. 트리 내 각 데이터마다 for문
        //. 해당 데이터블록 및 필터블록이 새로 생성이 필요하다면
        //0. 블룸필터 생성
        //0. 데이터블록(메모리상) 생성
        //0. restart블록 생성(메모리상)
        //1. 공유 키 길이(4바이트), 공유하지 않는 키 길이(4바이트), value 길이(4바이트), 공유하지않는키 컨텐츠, value 형태로 직렬화
        //2. 데이터블록에 덧붙일 직렬화 결과값 생성 및 그 길이 계산
        //4. 만약 16의 배수에 해당하는 순번이면 인덱스를 restart포인트로 취급하여 직렬화 결과 및 그 길이 계산
        //1. restart 서브블록에 restart키 길이(4바이트), restart 키, 데이터블록의 인덱스(4바이트) 형태
        //이 결과 해당 데이터블록의 크기보다 커지면, remain 메모리를 0으로 할당하고 해당 데이터를 다음 블록에서 재처리
        //해당 블록내에서 사용 가능하다면,
        //단어를 블룸필터에 할당
        //데이터 블록에 덧붙이기
        //restart 블록에 덧붙이기
        //2. 데이터블록 디스크 write
        //3. 인덱스블록 디스크 write
        write(tree,path);

        //읽기
        //블룸필터를 모두 메모리에 올림
        //해당 키값으로 블룸필터들에서 존재 가능성있는 데이터블록 식별
        //가장 마지막 데이터블록부터 탐색
        //restart 서브블록을 메모리에 올리고, 키값 이진탐색
        //탐색된 키값 오프셋부터 다음 키값 오프셋 이전까지 읽어옴.
        //해당데이터를 역직렬화하여 찾아냄
        read("apple4");
        read("banana230");
        read("mango123");
        read("notexistkey");
    }

    private static void read(String key) throws IOException {
        SSTable ssTable = new SSTable(path);
        String result = ssTable.read(key);

        System.out.println("--------------------");
        System.out.println("검색 시도 키 : "+key);
        if(result == null) {
            System.out.println("키에 매칭되는 Value를 찾을 수 없습니다");
            return;
        }
        System.out.println("검색 결과 Value : "+result);
    }


    private static void write(TreeMap<String, String> tree, String path) throws Exception {
        SSTable ssTable = new SSTable(path);
        ssTable.write(tree);
    }

    private static TreeMap datainItilize() {
        TreeMap<String,String> tree = new TreeMap<>();
        for(int i=0;i<1024;i++){
            tree.put("banana"+i, "bananaColor"+i);
            tree.put("apple"+i, "appleColor"+i);
            tree.put("grape"+i, "grapeColor"+i);
            tree.put("orange"+i, "orangeColor"+i);
            tree.put("kiwi"+i, "kiwiColor");
            tree.put("mango"+i, "mangocolor"+i);
            tree.put("blueberry"+i, "bluecolor"+i);
        }
        return tree;
    }
}
