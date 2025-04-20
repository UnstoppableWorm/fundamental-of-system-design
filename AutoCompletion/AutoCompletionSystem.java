package AutoCompletion;

import java.util.ArrayList;
import java.util.List;

public class AutoCompletionSystem {
    public Word root;
    public Integer totalCount;
    public Integer dbCount;
    public Word[] lastWordOfCacheDBs;
    public CacheDB[] cacheDBS;
    Integer nodeCountThreshold;

    public AutoCompletionSystem(Integer DBCount){
        root = new Word(null,"");
        totalCount = 0;
        this.dbCount = DBCount;
        lastWordOfCacheDBs = new Word[dbCount];
        cacheDBS = new CacheDB[dbCount];
    }

    public void putWord(String value){
        Word next = root;

        //10개의 요청중 1개만 받아들이는 로직 구현할까?

        for(char nextChar: value.toCharArray()){
            next = next.putWord(nextChar);
        }
        next.finish();
        totalCount++;
    }

    public void buildCache(){
        //top5 생성
        buildTop5(root);

        this.nodeCountThreshold = (root.childNodeCount / dbCount);

        if(root.childNodeCount % dbCount != 0) this.nodeCountThreshold++;

        //각 디비의 끝단어 세팅해야함. 뭐기준? 특정검색어까지의 노드수가 counterPerDb보다 큰것중 가장작은값까지.
        findLastWordPerDBAndBuildCacheDB();

        //트라이 정보 투하!!
        putTriInfoToCache(root);
    }

    private List<Word> buildTop5(Word cur) {
        List<Word> tops = new ArrayList<>();

        tops.add(cur);

        for(Word word: cur.children){
            if(word == null) continue;
            List<Word> childTops = buildTop5(word);
            tops.addAll(childTops);
        }

        tops.sort((a, b) -> Integer.compare(b.searchCount, a.searchCount));

        if (tops.size() > 5) {
            tops = tops.subList(0, 5);  // 상위 5개만 추리기
        }

        cur.top5 = tops;
        return tops;
    }

    private void putTriInfoToCache(Word cur) {
        if(cur == null) return;

        CacheDB cacheDB = findCacheDB(cur);
        cacheDB.put(cur);

        for(Word word: cur.children){
            if(word == null) continue;
            putTriInfoToCache(word);
        }
    }

    public CacheDB findCacheDB(Word word) {
        int idx = 0;
        for(int i = 0; i< cacheDBS.length; i++){
            if(cacheDBS[i].isAfter(word)) {
                idx = i;
                break;
            };
        }

        return cacheDBS[Math.max(0,idx)];
    }

    private void buildCacheDB() {
        for(int i=0;i<dbCount;i++){
            cacheDBS[i] = new CacheDB(lastWordOfCacheDBs[i]);
        }
    }

    public void findLastWordPerDBAndBuildCacheDB(){
        Word cur = root;
        int right = nodeCountThreshold;
        for(int i=0;i<dbCount;i++){
            Word after = getBeforeThreshold(root, (i+1)*nodeCountThreshold);
            lastWordOfCacheDBs[i] = after;
        }

        buildCacheDB();
    }

    private Word getAfter(Word cur) {
        if(cur == null) return null;

        while(true){
            Word parent = cur.parent;
            if(parent == null) return cur;
            for(Word word: parent.children){
                if(word.value.compareTo(cur.value) <= 0) continue;
                return getFirst(word);
            }
            return getAfter(cur.parent);
        }
    }

    private Word getFirst(Word cur) {
        for(Word word: cur.children){
            if(word == null) continue;
            return getFirst(word);
        }
        return cur;
    }

    public Word getBeforeThreshold(Word cur, int right){
        Word last = null;

        //cur도 노드니까 1부터 시작!
        Integer accumulativeCount = 1;
        if(right <= 1){
            Word result = new Word(null,cur.value);
            result.accumulativeCount = cur.accumulativeCount + accumulativeCount;
            return result;
        }

        for(Word word: cur.children){
            if(word == null) continue;

            if(accumulativeCount + word.childNodeCount < right){
                accumulativeCount += word.childNodeCount;
                last = word;
                continue;
            }

            last = getBeforeThreshold(word, right-accumulativeCount);
            break;
        }
        if(last == null){
            Word result = new Word(null,cur.value);
            result.accumulativeCount = accumulativeCount;
            return result;
        }
        Word result = new Word(null,last.value);
        result.accumulativeCount = last.accumulativeCount + accumulativeCount;
        return result;
    }

    public List<Word> search(String value){
        Word word = new Word(null,value);
        CacheDB cacheDB = findCacheDB(word);
        return cacheDB.search(word);
    }
}
