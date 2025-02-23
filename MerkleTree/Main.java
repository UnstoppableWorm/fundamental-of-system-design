package MerkleTree;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args){
        List<String> listA = new ArrayList<>();
        List<String> listB = new ArrayList<>();
        List<String> listC = new ArrayList<>();

        String thanos = "thanos";

        for(int i=0;i<16;i++){
            listA.add(thanos+i);
            listB.add(thanos+i);
            listC.add(thanos+i);
        }

        MerkleTree merkleTreeA = new MerkleTree(listA);
        MerkleTree merkleTreeB = new MerkleTree(listB);

        listC.set(8,"thanos1123123");
        listC.set(15,"thanos12313");
        MerkleTree merkleTreeC = new MerkleTree(listC);


        System.out.println("-----------MERKLE TREE A-----------");
        merkleTreeA.print();
        System.out.println("-----------MERKLE TREE C-----------");
        merkleTreeC.print();
        System.out.println("-----------COMPARE MERKLE TREE A && B-----------");
        System.out.println(merkleTreeA.compare(merkleTreeB));
        System.out.println("-----------COMPARE MERKLE TREE A && C-----------");
        System.out.println(merkleTreeA.compare(merkleTreeC));
        System.out.println("-----------Diff between A && C-----------");
        MerkleTree.getDiff(merkleTreeA.getRoot(),merkleTreeC.getRoot());
    }
}
