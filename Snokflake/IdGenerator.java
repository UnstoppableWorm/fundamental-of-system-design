package Snokflake;

public class IdGenerator {
    Long timestamp;
    Integer sequence;
    long centerWorkerBIt;
    public static final int CENTERBITCOUNT = 5;
    public static final int WORKERBITCOUNT = 5;
    public static final int SEQUENCECOUNT = 12;

    public IdGenerator(int centerId, int workerId) {
        timestamp = 0L;
        sequence = 0;
        centerWorkerBIt = (centerId << WORKERBITCOUNT+SEQUENCECOUNT) | (workerId << SEQUENCECOUNT);
    }

    public synchronized Long generate() throws Exception {
        long time = System.currentTimeMillis();
        long result = (time << (CENTERBITCOUNT+WORKERBITCOUNT+SEQUENCECOUNT));
        result |= centerWorkerBIt;
        synchronized (this.sequence) {
            result |= getSequence(time);
        }
        return result;
    }

    private long getSequence(Long timestamp) throws Exception {
        if (this.timestamp.equals(timestamp)) {
            //미리초당 4096개의 ID 생성 가능하도록 설정
            if(sequence >= 4096) throw new Exception("exceeded the threshodl");
            sequence++;
        } else {
            this.timestamp = timestamp;
            sequence = 0;
        }
        return sequence;
    }
}
