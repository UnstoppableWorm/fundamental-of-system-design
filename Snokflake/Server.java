package Snokflake;

import java.util.HashSet;
import java.util.Set;

public class Server {
    int centerId;
    int workerId;
    IdGenerator idGenerator;
    Set<Long> testSet = new HashSet<>();

    public Server(int centerId, int workerId) {
        this.centerId = centerId;
        this.workerId = workerId;
        this.idGenerator = new IdGenerator(this.centerId,this.workerId);
    }

    public void saveSomething() throws Exception {
        Long id = idGenerator.generate();
        if(testSet.contains(id)) throw new Exception("duplicate id exist");
        testSet.add(id);
    }
}
