package AutoCompletion;

import java.util.List;

public class Word {
    Word parent;
    String value;
    Boolean isReallyWord;
    Word[] children;
    List<Word> top5;
    Integer searchCount;
    Integer childNodeCount;
    Boolean isAlreadyIncreased;
    Boolean isFinish;

    Integer accumulativeCount;

    public Word(Word parent, String value){
        this.parent = parent;
        this.value = value;
        children = new Word[26];
        this.isReallyWord = false;
        searchCount = 0;
        childNodeCount = 0;
        isAlreadyIncreased = false;
        isFinish = false;
        accumulativeCount = 0;
    }

    public void finish(){
        searchCount++;
        if(isFinish) return;
        isFinish = true;
        this.isReallyWord = true;
        increaseChildNodeCount(0);
    }

    public void increaseChildNodeCount(int count){
        int amount = 1;
        if(isAlreadyIncreased) amount = 0;
        isAlreadyIncreased = true;

        this.childNodeCount += count+amount;
        if(this.parent == null) return;
        this.parent.increaseChildNodeCount(count+amount);
    }

    public List<Word> getTop5(){
        return this.top5;
    }

    public Word putWord(char nextChar){
        if(children[nextChar-'a'] == null){
            children[nextChar-'a'] = new Word(this,this.value+nextChar);
        }

        return children[nextChar-'a'];
    }

    public Word search(char nextChar){
        return children[nextChar-'a'];
    }

    @Override
    public String toString(){
        return this.value;
    }
}
