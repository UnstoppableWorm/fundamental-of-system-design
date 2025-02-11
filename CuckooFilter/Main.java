package CuckooFilter;

public class Main {
    public static void main(String[] args){
        CuckooFilter cuckooFilter = new CuckooFilter(26);

        boolean[] isSaved = new boolean[26];

        for(char c='a';c<='z';c++){
            isSaved[c-'a'] = cuckooFilter.add(c+"");
        }

        for(char c='a';c<='z';c++){
            System.out.print("add result of "+c+" is "+ isSaved[c-'a']);
            System.out.print(" | cuckoo filter try get result is "+cuckooFilter.get(c+""));
            if(isSaved[c-'a'] == true && !cuckooFilter.get(c+"")){
                System.out.println(" ! FALSE NEGATIVE");
            }else if(isSaved[c-'a'] == false && cuckooFilter.get(c+"")){
                System.out.println(" ! FALSE POSITIVE");
            }else{
                System.out.println("");
            }
        }
        System.out.println("---------------------------");
        System.out.println("DELETE ALL OF THINGS AFTER UUU");
        System.out.println("---------------------------");

        for(char c='u';c<='z';c++){
            cuckooFilter.delete(c+"");
            isSaved[c-'a'] = false;
        }

        for(char c='a';c<='z';c++){
            System.out.print("add result of "+c+" is "+ isSaved[c-'a']);
            System.out.print(" | cuckoo filter try get result is "+cuckooFilter.get(c+""));
            if(isSaved[c-'a'] == true && !cuckooFilter.get(c+"")){
                System.out.println(" ! FALSE NEGATIVE");
            }else if(isSaved[c-'a'] == false && cuckooFilter.get(c+"")){
                System.out.println(" ! FALSE POSITIVE");
            }else{
                System.out.println("");
            }
        }    }

}
