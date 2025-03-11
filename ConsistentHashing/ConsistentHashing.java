package ConsistentHashing;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ConsistentHashing {
    int replicas;
    TreeMap<Long, VirtualNode> treeMap; //안정해시 구하기용
    Map<String, List<VirtualNode>> virtualNodeMap;
    private final MessageDigest md;

    public ConsistentHashing(int replicas) throws NoSuchAlgorithmException {
        this.replicas = replicas;
        this.md = MessageDigest.getInstance("MD5");
        treeMap = new TreeMap<>();
        virtualNodeMap = new HashMap<>();
    }

    public void addServer(String newServer) {
        for (int i = 0; i < replicas; i++) {
            addVirtualNode(newServer, i);
        }
    }

    public void removeServer(String removedServer) {
        List<VirtualNode> virtualNodeList = virtualNodeMap.get(removedServer);
        virtualNodeMap.remove(removedServer);

        if (virtualNodeList == null) return;

        for (VirtualNode virtualNode : virtualNodeList) {
            virtualNode.delete();
        }
    }

    public void modifyReplicas(int newReplicas) {
        if (this.replicas < newReplicas) {
            for (String server : virtualNodeMap.keySet()) {
                for (int i = this.replicas; i < newReplicas; i++) {
                    addVirtualNode(server, i);
                }
            }
        }

        this.replicas = newReplicas;
    }

    private void addVirtualNode(String newServer, int i) {
        long hashResult = hash(newServer + "v" + i);
        VirtualNode virtualNode = new VirtualNode(newServer, i);

        while (treeMap.containsKey(hashResult)) {
            VirtualNode temp = treeMap.get(hashResult);
            if(temp.isDeleted()){
                treeMap.remove(hashResult);
                break;
            }
            hashResult = hash(newServer + "v" + hashResult);
        }
        treeMap.put(hashResult, virtualNode);

        if (virtualNodeMap.containsKey(newServer)) {
            virtualNodeMap.get(newServer).add(virtualNode);
        } else {
            List<VirtualNode> virtualNodeList = new ArrayList<>();
            virtualNodeList.add(virtualNode);
            virtualNodeMap.put(newServer, virtualNodeList);
        }
    }


    public VirtualNode getServer(String client) {
        //이미 삭제된 가상노드거나, shrink되어 사용하지 않는 replica인 경우 제거하고 재추출.
        while (true) {
            VirtualNode virtualNode;
            Map.Entry<Long, VirtualNode> virtualNodeEntry = treeMap.ceilingEntry(hash(client));
            if (virtualNodeEntry == null) {
                virtualNode = treeMap.ceilingEntry(0L).getValue();
            } else {
                virtualNode = virtualNodeEntry.getValue();
            }
            if (virtualNode.isDeleted() || virtualNode.isNotActivateReplica(this.replicas)) {
                Long key = treeMap.ceilingKey(hash(client));
                if (key == null) {
                    treeMap.remove(treeMap.ceilingKey(0L));
                } else {
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

    public long hash2(String key) {
        long seed = 0xe17a1465L; // 64-bit seed
        byte[] data = key.getBytes(StandardCharsets.UTF_8);
        int length = data.length;

        long h1 = seed;
        long h2 = seed;

        long c1 = 0x87c37b91114253d5L;
        long c2 = 0x4cf5ad432745937fL;

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        while (buffer.remaining() >= 16) {
            long k1 = buffer.getLong();
            long k2 = buffer.getLong();

            k1 *= c1;
            k1 = Long.rotateLeft(k1, 31);
            k1 *= c2;
            h1 ^= k1;

            h1 = Long.rotateLeft(h1, 27);
            h1 += h2;
            h1 = h1 * 5 + 0x52dce729;

            k2 *= c2;
            k2 = Long.rotateLeft(k2, 33);
            k2 *= c1;
            h2 ^= k2;

            h2 = Long.rotateLeft(h2, 31);
            h2 += h1;
            h2 = h2 * 5 + 0x38495ab5;
        }

        long k1 = 0;
        long k2 = 0;
        int remaining = buffer.remaining();

        if (remaining > 0) {
            ByteBuffer tail = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
            tail.put(data, length - remaining, remaining);
            tail.rewind();
            k1 = tail.getLong();
            k2 = tail.getLong();

            k1 *= c1;
            k1 = Long.rotateLeft(k1, 31);
            k1 *= c2;
            h1 ^= k1;

            k2 *= c2;
            k2 = Long.rotateLeft(k2, 33);
            k2 *= c1;
            h2 ^= k2;
        }

        h1 ^= length;
        h2 ^= length;

        h1 += h2;
        h2 += h1;

        h1 ^= (h1 >>> 33);
        h1 *= 0xff51afd7ed558ccdL;
        h1 ^= (h1 >>> 33);
        h1 *= 0xc4ceb9fe1a85ec53L;
        h1 ^= (h1 >>> 33);

        h2 ^= (h2 >>> 33);
        h2 *= 0xff51afd7ed558ccdL;
        h2 ^= (h2 >>> 33);
        h2 *= 0xc4ceb9fe1a85ec53L;
        h2 ^= (h2 >>> 33);

        h1 += h2;
        h2 += h1;

        return h1;
    }
}