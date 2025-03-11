package ConsistentHashing;

public class VirtualNode{
    String server;
    private int replicaIdx;
    private boolean isDeleted;

    public VirtualNode(String server, int replicaIdx){
        this.server = server;
        this.replicaIdx = replicaIdx;
        isDeleted = false;
    }

    public void delete(){
        isDeleted = true;
    }

    public boolean isDeleted(){
        return this.isDeleted;
    }

    public boolean isNotActivateReplica(int replicas){
        return this.replicaIdx >= replicas;
    }
}