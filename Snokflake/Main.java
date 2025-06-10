package Snokflake;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static int TEST_COUNT = 4096;
    public static void main(String[] args) throws Exception {

        List<Server> servers = new ArrayList<>();
        Set<Long> uniqueIdSet = new HashSet<>();

        for (int center = 0; center < 32; center++) {
            for (int worker = 0; worker < 32; worker++) {
                servers.add(new Server(center,worker));
            }
        }

        Long duplicateCount = 0L;
        for(Server server: servers){
            for(int i=0;i<TEST_COUNT;i++){
                Long id = server.generateUniqueId();
                //동일 id가 존재하면 값 증가시킴 - 의도대로 설계되었다면 최종적으로 0이어야함
                if(uniqueIdSet.contains(id)) duplicateCount++;
                uniqueIdSet.add(id);
            }
        }

        System.out.println("중복되는 ID 개수 : "+duplicateCount);
    }
}