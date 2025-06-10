package Snokflake;

import java.util.HashSet;
import java.util.Set;

public class Server {
    int centerId;
    int workerId;
    IdGenerator idGenerator;

    public Server(int centerId, int workerId) {
        this.centerId = centerId;
        this.workerId = workerId;
        this.idGenerator = new IdGenerator(this.centerId,this.workerId);
    }

    public Long generateUniqueId() throws Exception {
        Long id = idGenerator.generate();
        return id;
    }
}
