package MerkleTree;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MerkleTree {
    private Node root;

    public MerkleTree(List<String> values){
        List<Node> nodes = new ArrayList<>();
        for(String value: values){
            nodes.add(new Node(value,null,null));
        }

        root = buildTree(nodes);
    }

    public String compare(MerkleTree other){
        return root.equals(other.getRoot()) ? "SAME" : "DIFFERENT";
    }

    public void print(){
        List<List<Node>> graph = new ArrayList<>();
        makeGraph(root,graph,0);

        for(int i=0;i<graph.size();i++){
            for(Node cur: graph.get(i)){
                System.out.print(cur.getValue()+" ");
            }
            System.out.println("");
        }
    }

    private void makeGraph(Node cur, List<List<Node>> graph, int depth){
        if(cur == null) return;

        if(graph.size()-1 < depth){
            graph.add(new ArrayList<>());
        }

        graph.get(depth).add(cur);
        makeGraph(cur.left,graph,depth+1);
        makeGraph(cur.right,graph,depth+1);
    }

    public Node getRoot(){
        return this.root;
    }

    public static void getDiff(Node cur, Node other) {
        if(cur.equals(other)) return;

        if(isLeaf(cur) && isLeaf(other)){
            System.out.println("left value : "+cur.getValue()+" <-> right value : "+other.getValue());
            return;
        }

        getDiff(cur.left,other.left);
        getDiff(cur.right,other.right);
    }

    private static boolean isLeaf(Node cur){
        return cur.left == null && cur.right == null;
    }

    private Node buildTree(List<Node> childNodes) {
        List<Node> nodes = new ArrayList<>();

        for(int i=0;i<childNodes.size();i+=2){
            Node left = childNodes.get(i);
            Node right = i+1 >= childNodes.size() ? new Node("1",null,null) : childNodes.get(i+1);
            String parentHash = hash(left.plus(right));
            Node parent = new Node(parentHash,left,right);
            nodes.add(parent);
        }

        if(nodes.size() == 1){
            return nodes.get(0);
        }

        return buildTree(nodes);
    }

    private String hash(Node node){
        return murmurHash3_32(node.getValue(),42);
    }

    private String murmurHash3_32(String key, int seed) {
        byte[] data = key.getBytes(StandardCharsets.UTF_8);
        int length = data.length;
        int h1 = seed;
        int c1 = 0xcc9e2d51;
        int c2 = 0x1b873593;
        int r1 = 15;
        int r2 = 13;
        int m = 5;
        int n = 0xe6546b64;

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        while (buffer.remaining() >= 4) {
            int k1 = buffer.getInt();
            k1 *= c1;
            k1 = Integer.rotateLeft(k1, r1);
            k1 *= c2;

            h1 ^= k1;
            h1 = Integer.rotateLeft(h1, r2);
            h1 = h1 * m + n;
        }

        int k1 = 0;
        int remaining = buffer.remaining();
        if (remaining > 0) {
            for (int i = 0; i < remaining; i++) {
                k1 ^= (buffer.get() & 0xff) << (i * 8);
            }
            k1 *= c1;
            k1 = Integer.rotateLeft(k1, r1);
            k1 *= c2;
            h1 ^= k1;
        }

        h1 ^= length;
        h1 ^= (h1 >>> 16);
        h1 *= 0x85ebca6b;
        h1 ^= (h1 >>> 13);
        h1 *= 0xc2b2ae35;
        h1 ^= (h1 >>> 16);

        return Integer.toHexString(h1);
    }
}
