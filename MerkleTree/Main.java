package MerkleTree;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args){
        List<String> listA = new ArrayList<>();
        List<String> listC = new ArrayList<>();

        String value = "val";

        for(int i=0;i<16;i++){
            listA.add(value+i);
            listC.add(value+i);
        }

        //머클트리A와 B는 같은 값을 가지도록 생성
        //A와 B를 비교하면 동일하다는 결과가 나와야함
        MerkleTree merkleTreeA = new MerkleTree(listA);
        MerkleTree merkleTreeB = new MerkleTree(listA);
        System.out.println("-----------COMPARE MERKLE TREE A && B-----------");
        System.out.println(merkleTreeA.compare(merkleTreeB));


        //머클트리C의 경우 A 와 다른 구성을 가지고 있으므로
        //다르다는 결과가 나와야하며, 변경 사항을 올바르게 추적할 수 있어야함
        listC.set(8,"changedVal8");
        listC.set(15,"changedVal15");
        MerkleTree merkleTreeC = new MerkleTree(listC);
        System.out.println("-----------COMPARE MERKLE TREE A && C-----------");
        System.out.println(merkleTreeA.compare(merkleTreeC));
        System.out.println("-----------Diff between A && C-----------");
        MerkleTree.getDiff(merkleTreeA.getRoot(),merkleTreeC.getRoot());
    }
}
