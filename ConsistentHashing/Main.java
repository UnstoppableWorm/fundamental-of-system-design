package ConsistentHashing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//칭구들에게 보여줄것
//1. 서버수를 늘려가면 표준편차가 감소하는것
//2. 가상노드를 늘려가면 표준편차가 감소하는것

public class Main {
    public static void main(String[] args) throws Exception {

        int serverCount = 5;
        int replica = 500;
        int client = 10000;


        //표준편차 조절해주자
        ConsistentHashing ch = new ConsistentHashing(replica);
        List<Integer> list;

        //서버수 조절해주자
        for(int i=0;i<serverCount;i++){
            ch.addServer("server"+i);
        }

        //기본 표준편차
        System.out.println("------------after create consistent hash----------");
        test(serverCount, client, ch);

        //서버 지우고 표준편차 보기
        int remove = 3;
        System.out.println("------------after remove server----------");
        for(int i=serverCount-remove;i<serverCount;i++){
            ch.removeServer("server"+i);
        }
        test(serverCount-remove, client, ch);

        //서버 늘리고 표준편차 보기
        int add = 3;
        System.out.println("------------after add server-------------");
        for(int i=serverCount-remove;i<serverCount;i++){
            ch.addServer("server"+i);
        }
        test(serverCount, client, ch);

        //가상노드 줄이고 표준편차 보기
        int modifeid = 5;
        System.out.println("------------after modify virtual node replica to : "+modifeid);
        ch.modifyReplicas(modifeid);
        test(serverCount, client, ch);

        //가상노드 늘리고 표준편차 보기
        modifeid = 1000;
        System.out.println("------------after modify virtual node replica to : "+modifeid);
        ch.modifyReplicas(modifeid);
        test(serverCount, client, ch);
    }

    private static List<Integer> test(int serverCount, int client, ConsistentHashing ch) {
        List<Integer> list = new ArrayList<>();
        Map<String,Integer> clientBalance = new HashMap<>();

        for(int i = 0; i< serverCount; i++){
            clientBalance.put("server"+i,0);
        }

        for(int i = 0; i< client; i++){
            VirtualNode virtualNode = ch.getServer("client"+i);
            String server = virtualNode.server;

            clientBalance.put(server, clientBalance.get(server)+1);
        }

        for(String server: clientBalance.keySet().stream().sorted().collect(Collectors.toList())){
            //System.out.println(server+" has clints of "+balance.get(server));
            list.add(clientBalance.get(server));
        }
        printCoefficient(list);

        for(int value: list){
            System.out.println(value);
        }
        return list;
    }

    public static void printCoefficient(List<Integer> intNumbers){
            List<Double> numbers = intNumbers.stream().map(i->(double)i).collect(Collectors.toList());

            double mean = numbers.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double variance = numbers
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .map(num -> Math.pow(num - mean, 2))
                    .average()
                    .orElse(0.0);

            double coefficient = Math.sqrt(variance)/mean;
            System.out.println("Coefficient : " + coefficient);
    }

}
