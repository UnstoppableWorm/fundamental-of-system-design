package MerkleTree;

public class Node {
    Node left;
    Node right;
    String value;

    public Node(String value,Node left, Node right){
        this.value = value;
        this.left = left;
        this.right = right;
    }

    public String getValue(){
        return this.value;
    }

    public Node plus(Node other){
        return new Node(this.value+other.getValue(),null,null);
    }

    public boolean equals(Node other){
        return this.value.equals(other.getValue());
    }
}
