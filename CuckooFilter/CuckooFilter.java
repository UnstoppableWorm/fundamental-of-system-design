package CuckooFilter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CuckooFilter {
    private int bucketSize;
    private int[] bucket;
    private final int MAX_DEPTH = 10;

    public CuckooFilter(int bucketSize){
        this.bucketSize = bucketSize;

        bucket = new int[bucketSize];
        Arrays.fill(bucket,-1);
    }

    public boolean add(String stringValue){
        int value = stringToInt(stringValue);
        int hash1 = hash1(value);
        int fingerPrint = fingerPrint(value);
        int hash2 = hash2(hash1,fingerPrint);

        int idx = whichIdx(hash1,hash2);
        //둘다 차있는경우 킥오프 수행
        if(idx == -1){
            //킥오프 시도후 그 결과 리턴
            boolean result = kickOff(hash1,fingerPrint);

            //만약 MAX_DEPTH 이내에서 킥오프 실패한다면 삽입 거부
            if(!result){
                return false;
            }
        }else if(idx == 0){
            bucket[hash1] = fingerPrint;
        }else if(idx == 1){
            bucket[hash2%bucketSize] = fingerPrint;
        }

        return true;
    }

    public void delete(String stringValue){
        int value = stringToInt(stringValue);
        if(!get(stringValue)) return;

        int fingerPrint = fingerPrint(value);
        int hash1 = hash1(value);
        int hash2 = hash2(value,fingerPrint);

        if(bucket[hash1] == fingerPrint) {
            bucket[hash1] = -1;
            return;
        }

        if(bucket[hash2%bucketSize] == fingerPrint){
            bucket[hash2%bucketSize] = -1;
            return;
        }
    }

    public boolean get(String stringValue){
        int value = stringToInt(stringValue);

        int hash1 = hash1(value);
        int fingerPrint = fingerPrint(value);
        int hash2 = hash2(hash1,fingerPrint);

        return bucket[hash1]>=0 || bucket[hash2%bucketSize]>=0;
    }

    private boolean kickOff(int hash1, int fingerPrint) {
        boolean[] visited = new boolean[bucketSize];
        visited[hash1] = true;
        if(recurSiveKickOff(hash1,1,visited)){
            bucket[hash1] = fingerPrint;
            return true;
        }

        return false;
    }

    private boolean recurSiveKickOff(int hash1, int depth, boolean[] visited) {
        if(depth == MAX_DEPTH) return false;
        int fingerPrint = bucket[hash1];
        int hash2 = getOtherHashFromFirstHash(hash1,fingerPrint);

        //해당 버킷이 비어있으면 ㄱㄱ
        if(bucket[hash2%bucketSize] == -1){
            bucket[hash2%bucketSize] = fingerPrint;
            return true;
        }

        //해당 버킷이 차있으면 다음 버킷을 찾고, 최대 뎁스 10내에서 탐색가능하면 넣는다.

        if(visited[hash2%bucketSize]) return false;
        visited[hash2%bucketSize] = true;
        if(recurSiveKickOff(hash2%bucketSize, depth+1,visited)){
            bucket[hash2%bucketSize] = fingerPrint;
            return true;
        }

        return false;
    }

    public int whichIdx(int hash1, int hash2){
        int hash1Idx = hash1;
        int hash2Idx = hash2%bucketSize;

        //둘다 차있는경우 -1 리턴
        if(bucket[hash1Idx] >= 0 && bucket[hash2Idx] >= 0){
            return  -1;
        }

        //hash1만 차있는경우 hash2 리턴
        if(bucket[hash1Idx] >= 0){
            return  1;
        }

        //hash2만 차있는경우 hash1 리턴
        if(bucket[hash2Idx] >= 0){
            return  0;
        }

        //둘다 비어있는경우 0 리턴
        return 0;
    }


    //h1(x) = hash(x) mod bucketCount
    public int hash1(int value){
        return hashInt(value) % bucketSize;
    }

    //fingerprint f(x) = hash(x) mod 2^m 보통 8비트짜리 핑거프린트를 써서 256으로 나눈다고함.
    public int fingerPrint(int value){
        return hashInt(value) % (1<<8);
    }

    //h2(x) = h1(x) ^ hash(f(x))
    public int hash2(int hash1, int fingerPrint){
        int fingerPrintHash = hashInt(fingerPrint);
        fingerPrintHash &= 0x7FFFFFFF;
        return (hash1 ^ fingerPrintHash);
    }

    public int getOtherHashFromFirstHash(int firstHash, int fingerPrint){
        return hash2(firstHash,fingerPrint);
    }

    public int hashInt(int x) {
        int hash = 0;
        while(x>0){
            hash = hash*31+x%31;
            x /= 31;
        }
        return hash;
    }


    public int stringToInt(String x) {
        int hash = 5381; // DJB2 기반 해시
        for (char ch : x.toCharArray()) {
            hash = ((hash << 5) + hash) + ch; // hash * 33 + ch
        }
        return hash & 0x7FFFFFFF; // 음수 방지
    }

}
