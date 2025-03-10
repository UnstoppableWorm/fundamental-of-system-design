package ConsistentHashing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ConsistentHashing {
    int replicas;
    TreeMap<Long,VirtualNode> treeMap; //안정해시 구하기용
    Map<String,List<VirtualNode>> virtualNodeMap;
    private final MessageDigest md;

    public ConsistentHashing(int replicas) throws NoSuchAlgorithmException {
        this.replicas = replicas;
        this.md = MessageDigest.getInstance("MD5");
        treeMap = new TreeMap<>();
        virtualNodeMap = new HashMap<>();
    }

    public void addServer(String newServer){
        for(int i=0;i<replicas;i++){
            addVirtualNode(newServer, i);
        }
    }

    public void removeServer(String removedServer){
        List<VirtualNode> virtualNodeList = virtualNodeMap.get(removedServer);
        virtualNodeMap.remove(removedServer);

        for(VirtualNode virtualNode : virtualNodeList){
            virtualNode.delete();
        }
    }

    public void modifyReplicas(int newReplicas){
        if(this.replicas < newReplicas){
            addNewVirtualNodes(this.replicas, newReplicas);
        }

        this.replicas = newReplicas;
    }

    private void addNewVirtualNodes(int oldReplicas, int newReplicas) {
        for(String server: virtualNodeMap.keySet()){
            for(int i=oldReplicas;i<newReplicas;i++){
                addVirtualNode(server,i);
            }
        }
    }

    private void addVirtualNode(String newServer, int i) {
        long hashResult = hash(newServer + i);
        VirtualNode virtualNode = new VirtualNode(newServer, i);

        while(treeMap.containsKey(hashResult)){
            hashResult = hash(newServer +hashResult);
        }
        treeMap.put(hashResult, virtualNode);

        if(virtualNodeMap.containsKey(newServer)){
            virtualNodeMap.get(newServer).add(virtualNode);
        }else{
            List<VirtualNode> virtualNodeList = new ArrayList<>();
            virtualNodeList.add(virtualNode);
            virtualNodeMap.put(newServer,virtualNodeList);
        }
    }


    public VirtualNode getServer(String client){
        //이미 삭제된 가상노드거나, shrink되어 사용하지 않는 replica인 경우 제거하고 재추출.
        while(true){
            VirtualNode virtualNode;
            Map.Entry<Long,VirtualNode> virtualNodeEntry  = treeMap.ceilingEntry(hash(client));
            if(virtualNodeEntry == null){
                virtualNode = treeMap.ceilingEntry(0L).getValue();
            }else{
                virtualNode = virtualNodeEntry.getValue();
            }
            if(virtualNode.isDeleted || virtualNode.isNotActivateReplica(this.replicas)){
                Long key = treeMap.ceilingKey(hash(client));
                if(key == null){
                    treeMap.remove(treeMap.ceilingKey(0L));
                }else{
                    treeMap.remove(treeMap.ceilingKey(hash(client)));
                }
                continue;
            }
            return virtualNode;
        }
    }

    private long hash(String key) {
        md.reset();
        md.update(key.getBytes());
        byte[] digest = md.digest();
        long hash = ((long) (digest[3] & 0xFF) << 24) |
                ((long) (digest[2] & 0xFF) << 16) |
                ((long) (digest[1] & 0xFF) << 8) |
                ((long) (digest[0] & 0xFF));
        return hash;
    }
}
