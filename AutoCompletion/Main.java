package AutoCompletion;

//칭구들에게 보여줄것
//1. 서버수를 늘려가면 표준편차가 감소하는것
//2. 가상노드를 늘려가면 표준편차가 감소하는것

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        //대충 여러 형태의 검색어들을 자동완성 시스템에 넣어주고,

        //내부 캐쉬디비
        AutoCompletionSystem system = new AutoCompletionSystem(8);

        initialize(system);

        system.buildCache();

        List<String> searchTargets = List.of("ap","ba","go");

        for(String target: searchTargets){
            List<Word> resultWords = system.search(target);
            System.out.println("SEARCH TARGET : "+target);

            System.out.println("SEARCH RESULT :");
            for (Word word: resultWords){
                System.out.println(word);
            }
            System.out.println();
        }
    }

    public static void initialize(AutoCompletionSystem system){
        List<String> words = List.of(
                "apple"
                , "apply"
                ,"application"
                ,"apricot"
                ,"banana"
                ,"band"
                ,"bandage"
                ,"bank"
                ,"bat"
                ,"batch"
                ,"cat"
                ,"catch"
                ,"cater"
                ,"cattle"
                ,"dog"
                ,"dodge"
                ,"door"
                ,"dot"
                ,"dove"
                ,"elephant"
                ,"elevate"
                ,"elite"
                ,"elk"
                ,"elm"
                ,"fish"
                ,"fishing"
                ,"fist"
                ,"fit"
                ,"fix"
                ,"go"
                ,"goal"
                ,"goat"
                ,"god"
                ,"gold"
                ,"hat"
                ,"hatch"
                ,"hate"
                ,"have"
                ,"ice"
                ,"idea"
                ,"ideal"
                ,"identity"
                ,"idle"
                ,"jacket"
                ,"jam"
                ,"jar"
                ,"jazz"
                ,"jeans"
                ,"jelly"
                ,"jewel");

        for(String value: words){
            system.putWord(value);
        }
    }
}
