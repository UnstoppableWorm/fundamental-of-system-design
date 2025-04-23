package Snokflake;

public class IdGenerator {
    Long timestamp;
    Integer sequence;
    long centerworkerBIt;

    public IdGenerator(int centerId, int workerId) {
        timestamp = 0L;
        sequence = 0;
        centerworkerBIt = (centerId << 15) | (workerId << 10);
    }

    public synchronized Long generate() throws Exception {
        long time = System.currentTimeMillis();
        long result = (time << 22);
        result |= centerworkerBIt;
        synchronized (this.sequence) {
            result |= getSequence(time);
        }
        return result;
    }

    private long getSequence(Long timestamp) throws Exception {
        if (this.timestamp.equals(timestamp)) {
            if(sequence >= 4096) throw new Exception("exceeded the threshodl");
            sequence++;
        } else {
            this.timestamp = timestamp;
            sequence = 0;
        }
        return sequence;
    }
}
