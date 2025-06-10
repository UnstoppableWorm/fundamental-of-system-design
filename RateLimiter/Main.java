package RateLimiter;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        //초당 최대 요청 개수 100개로 설정
        RateLimiter rateLimiter = new RateLimiter(100);
        long failCount = 0L;
        for(int i=0;i<200;i++){
            Thread.sleep(5);
            RequestResult result = rateLimiter.tryNewRequest();
            failCount = result.isSuccess()? failCount+1:failCount;
        }

        System.out.println("fail count : "+failCount);
    }
}