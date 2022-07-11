class MultiThreadTest {
    public static void main(String[] args) {
        for(int i = 0; i < 1; i++){
            RunnableBplus r = new RunnableBplus();
            Thread[] threads = new Thread[2];
            for(int j = 0; j < threads.length; j++){
                threads[j] = new Thread(r);
            }
            for(int j = 0; j < threads.length; j++){
                threads[j].start();
            }
            for(int j = 0; j < threads.length; j++){
                try{
                    threads[j].join();
                } catch (InterruptedException e) {
                }
            }
            r.getTree();
        }
    }
       
}