package Snokflake;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        List<Server> servers = new ArrayList<>();

        for (int center = 0; center < 32; center++) {
            for (int worker = 0; worker < 32; worker++) {
                servers.add(new Server(center,worker));
            }
        }

        for(int i=0;i<10000;i++){
            for(Server server: servers){
                server.saveSomething();
            }
        }
    }
}