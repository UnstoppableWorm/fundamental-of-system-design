public class Main {
    public static void main(String[] args) throws InterruptedException {
        RateLimiter rateLimiter = new RateLimiter(100);
        int failCount = 0;
        for(int i=0;i<200;i++){
            Thread.sleep(5);
            RequestResult result = rateLimiter.tryNewRequest();
            failCount = result.isSuccess()? failCount+1:failCount;
        }

        System.out.println("fail count : "+failCount);
    }
}