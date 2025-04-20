package AutoCompletion;

import java.util.HashMap;
import java.util.List;

public class CacheDB {
    public HashMap<String,Word> cache;
    public Word lastWord;

    public CacheDB(Word lastWord){
        this.lastWord = lastWord;
        cache = new HashMap<>();
    }

    public boolean isAfter(Word word){
        return lastWord.value.compareTo(word.value) >= 0;
    }

    public void put(Word word){
        cache.put(word.value,word);
    }

    public List<Word> search(Word word){
        Word result = cache.get(word.value);
        return result.top5;
    }
}
