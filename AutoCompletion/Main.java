package AutoCompletion;

//칭구들에게 보여줄것
//1. 서버수를 늘려가면 표준편차가 감소하는것
//2. 가상노드를 늘려가면 표준편차가 감소하는것

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        //대충 여러 형태의 검색어들을 자동완성 시스템에 넣어주고,

        AutoCompletionSystem system = new AutoCompletionSystem(3);

        system.putWord("ab");
        system.putWord("aba");
        system.putWord("abb");
        system.putWord("aca");
        system.putWord("acb");
        system.putWord("acd");



        system.buildCache();

        List<Word> words = system.search("");

        for(Word word: words){
            CacheDB cacheDB = system.findCacheDB(word);
            System.out.println(word.value+" : "+word.searchCount+" cache db : "+cacheDB.lastWord.value);
        }
        //몇개 단어로 검색 출력

    }
}
