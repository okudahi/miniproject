class RunnableBplus implements Runnable {
    private Bplustree tree = new Bplustree();
    public void run() {
        for(int i = 10; i < 100; i++){
            tree.put(String.valueOf(i),String.valueOf(i));
        }
    }
    void getTree(){
        tree.makeDotFile();
    }
}