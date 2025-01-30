package RateLimiter;

import java.text.SimpleDateFormat;
public class RateLimiter {
    private final long rateLimit; //초당 100회 요청까지 가능
    Pair<Long,Long> prev;
    Pair<Long,Long> curr;
    SimpleDateFormat format;

    public RateLimiter(long rateLimit){
        this.rateLimit = rateLimit;
        prev = new Pair<>(0L,0L);
        curr = new Pair<>(0L,0L);
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }
    public RequestResult tryNewRequest(){
        long now = System.currentTimeMillis();
        boolean isSuccess = tryAdd(now);
        return new RequestResult(format.format(now),isSuccess);
    }

    private boolean tryAdd(long now) {
        long nowWithSecond = now/1000;

        //curr 윈도우보다 2초이상 진행되어 모두 리셋
        if(nowWithSecond >= curr.key()+2){
            prev = new Pair<>(nowWithSecond-1,0L);
            curr = new Pair<>(nowWithSecond,1L);
        }else if(nowWithSecond == curr.key()+1){//curr윈도우보다 1초커서 윈도우 업데이트
            prev = curr;
            curr = new Pair<>(nowWithSecond,1L);
        }else if(nowWithSecond == curr.key()){//현재 윈도우에 포함될때
            curr = new Pair<>(nowWithSecond, curr.value()+1);
        }

        double percent = 1 - (now%1000)/1000.0;

        System.out.println("prev time : "+prev.key()+" prev count : "+prev.value());
        System.out.println("curr time : "+curr.key()+" curr count : "+curr.value());
        System.out.println("prev percent is "+percent+" result : "+(prev.value()*percent+curr.value() < rateLimit));
        System.out.println("\n");
        return prev.value()*percent+curr.value() < rateLimit;
    }
}
