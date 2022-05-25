public class Bplustree {

    final private static int MAX_CHILD = 5;
    final private static int MAX_KEYS = MAX_CHILD - 1;
    final private static int HALF_MAX_CHILD = ((MAX_CHILD + 1) / 2);

    // Node
    public static abstract class Node {
        int serial;
        int nkeys;
        String[] keys;

        int  isKeyExist(String k){ //　ノードにキーkがあるときはそのインデックス、ないときは-1を返す
            for(int i = 0; i < this.nkeys; i++){
                if(this.keys[i].equals(k)){
                    return i;
                }
                else if(this.keys[i].compareTo(k) > 0){
                    return -1;
                }
            }
            return -1;
        }

        int keyIndex(String k){ //　キーkが入るべきindexを返す
            int i;
            for(i = 0; i < this.nkeys; i++){
                int cmp = k.compareTo(this.keys[i]); 
                if(cmp < 0){
                    break;
                } // k < keys[i]
            }
            return i;
        }

        abstract public SplitRequest insert(String k, String v);
        abstract public String get(String k);
        abstract public StringBuffer getrange(String startKey, int n);
        abstract public boolean delete(String k);
    }

    public static class SplitRequest{ // 分割の際に親にリクエストを送る
        String borderKey; // 真ん中の値
        Node left; // 真ん中より左
        Node right; // 真ん中より右

        // コンストラクタ
        SplitRequest(String b, Node l, Node r){ 
            this.borderKey = b;
            this.left = l;
            this.right = r;
        }
    }

    //interior node
    public class InteriorNode extends Node {
        // 部分木
        Node[] child;

        // コンストラクタ
        private InteriorNode() {
            this.serial = serialNumber++;
            this.nkeys = 0;
            this.keys = new String[MAX_KEYS + 1];
            this.child = new Node[MAX_CHILD + 1];
        }

        // internalノードへのキーkの挿入
        public SplitRequest insert(String k, String v) {
            int ki = this.keyIndex(k);
            SplitRequest req = this.child[ki].insert(k,v); // 再帰
            if(req == null){ // 何もしない
                return null;
            }
            else{ // 子が分割→SplitRequest
                int i;
                String insertedKey = req.borderKey;
                Node lchild = req.left;
                Node rchild = req.right;
                for(i = nkeys; i > 0; i--){ // iはnkeysから1まで

                    int cmp = insertedKey.compareTo(this.keys[i-1]);
                    if(cmp < 0){ // k < keys[i-1]
                        this.keys[i] = this.keys[i-1];
                        this.child[i+1] = this.child[i];
                    } 
                    else{ // k > keys[i]
                        this.keys[i] = insertedKey;
                        this.child[i+1] = rchild;
                        this.child[i] = lchild;
                        this.nkeys++;
                        break;
                    }
                }
                if(i == 0){
                    this.keys[0] = insertedKey; // 左端に追加
                    this.child[1] = rchild;
                    this.child[0] = lchild;
                    this.nkeys++;
                }
            }
            if(this.nkeys > MAX_KEYS){ // ノードが満杯のとき、分割が発生
                    this.split(); // 分割
            }
            return null; // 満杯でないならnullを返す(これより上はreq = nullなので何もしない)

        }

        // internalノードでの分割
        SplitRequest split() {
            int borderIndex = HALF_MAX_CHILD - 1; // key[borderIndex]を親ノードに挿入、他を分割
            InteriorNode l = this;
            InteriorNode r = new InteriorNode();
            for(int i = borderIndex + 1; i < MAX_KEYS + 1; i++ ){ // rとlに分配
                r.keys[i - borderIndex - 1] = l.keys[i];
                l.keys[i] = null;
                r.child[i - borderIndex - 1] = l.child[i];
                l.child[i] = null;
            }
            r.child[MAX_CHILD - borderIndex - 1] = l.child[MAX_CHILD];
            l.child[MAX_CHILD] = null;
            String borderKey = l.keys[borderIndex];
            l.keys[borderIndex] = null;
            l.nkeys = borderIndex;
            r.nkeys = MAX_KEYS - borderIndex;
            if(this != root){ // rootでないなら親にSplitRequest送る
                return new SplitRequest(borderKey, l, r);
            }
            else{ // rootのとき、新たなrootを作る
                InteriorNode newRoot = new InteriorNode();
                newRoot.keys[0] = borderKey;
                newRoot.child[0] = l;
                newRoot.child[1] = r;
                newRoot.nkeys = 1;
                root = newRoot;
                return null;
            }

        }

        // 検索:適切な位置の子をたどる
        public String get(String k){
            int ki = this.keyIndex(k);
            return this.child[ki].get(k);
        }

        // 範囲検索:適切な位置の子をたどる
        public StringBuffer getrange(String startKey, int n){
            int ki = this.keyIndex(startKey);
            return this.child[ki].getrange(startKey, n);
        }

        // 削除:適切な位置の子をたどる
        public boolean delete(String k){
            int ki = this.keyIndex(k);
            boolean req = this.child[ki].delete(k); // 再帰
            if(req == true){ // ki番目の子が消えた
                if(ki > 0){ // 消えたのが左端じゃない
                    InteriorNode t = this;
                    for(int i = ki; i < this.nkeys; i++){ // 左詰め
                        this.keys[i-1] = this.keys[i];
                        this.child[i] = this.child[i+1];
                    }
                    this.keys[this.nkeys - 1] = null; // 右端のキーと子削除
                    this.child[this.nkeys] = null;
                    this.nkeys--;
                    if(this.nkeys == 0){ // キーが一つもなくなったらノードを削除
                        t = null; // ノードを削除
                        return true; // 親に知らせる
                    }
                    return false; // nkeysが1以上のとき、そのまま終了
                }
                else{ // 消えたのが左端
                    InteriorNode t = this;
                    for(int i = ki; i < this.nkeys - 1; i++){ // 左詰め
                        this.keys[i] = this.keys[i+1];
                        this.child[i] = this.child[i+1];
                    }
                    this.child[this.nkeys-1] = this.child[this.nkeys];
                    this.keys[this.nkeys - 1] = null; // 右端のキーと子削除
                    this.child[this.nkeys] = null;
                    this.nkeys--;
                    if(this.nkeys == 0){ // キーが一つもなくなったらノードを削除
                        t = null; // ノードを削除
                        return true; // 親に知らせる
                    }
                    return false; // nkeysが1以上のとき、そのまま終了
                }

            }
            return false;
        }

    }

    

    // leaf node
    public static class LeafNode extends Node {

        // データ
        String[] data;
        // 左隣ノード
        LeafNode prev;
        // 右隣ノード
        LeafNode next;

        // コンストラクタ(空のLeafNode)
        public LeafNode() {
            this.serial = serialNumber++;
            nkeys = 0;
            this.keys = new String[MAX_KEYS + 1];
            this.data = new String[MAX_KEYS + 1];
        }
        // コンストラクタ(要素が一つ入ったLeafNode)
        public LeafNode(String key, String x) {
            this.serial = serialNumber++;
            this.keys = new String[MAX_KEYS + 1];
            this.data = new String[MAX_KEYS + 1];
            this.keys[0] = key; 
            this.data[0] = x;
            this.nkeys = 1; 
        }

        // leafノードへのキーk、データxの挿入
        public SplitRequest insert(String k, String v) {
            int ki = this.isKeyExist(k);
            if (ki >= 0){ // key(k)がもうある(ki番目に一致)
                this.data[ki] = v; // データの置き換え
                System.out.println("the key " + k +  " already exists: updated the value");
                return null;
            }
            else{ // key(k)がまだない場合
                int i;
                for(i = nkeys; i > 0; i--){ // iはnkeysから1まで
                    int cmp = k.compareTo(this.keys[i-1]);
                    if(cmp < 0){ // k < keys[i-1]
                        this.keys[i] = this.keys[i-1]; // 右にずらす
                        this.data[i] = this.data[i-1];
                    } 
                    else{ // k > keys[i-1]
                        this.keys[i] = k;
                        this.data[i] = v;
                        this.nkeys++; // 空いたところに挿入
                        break;
                    }
                }
                if(i == 0){ // k < keys[0]
                    this.keys[0] = k; // 左端に挿入
                    this.data[0] = v;
                    this.nkeys++;
                }
                if(this.nkeys > MAX_KEYS){
                    System.out.println("the key " + k +  " is inserted with split");
                    return this.split();
                }
                System.out.println("the key " + k +  " is inserted");
                return null;
            }
        }

        // Leafノードでの分割
        SplitRequest split() {
            int borderIndex = HALF_MAX_CHILD - 1; // key[j]を親ノードに挿入、他を分割
            LeafNode l = this;
            LeafNode r = new LeafNode();
            for(int i = borderIndex; i < MAX_KEYS + 1; i++ ){ // rとlに分配
                r.keys[i-borderIndex] = l.keys[i];
                l.keys[i] = null;
                r.data[i-borderIndex] = l.data[i];
                l.data[i] = null;
            }
            l.nkeys = borderIndex;
            r.nkeys = MAX_KEYS - borderIndex + 1;
            String borderKey = r.keys[0];
            l.next = r;
            r.prev = l;

            return new SplitRequest(borderKey, l, r);
        }

        // 検索
        public String get(String k){
            int ki = this.isKeyExist(k);
            if (ki < 0){ // キーkが無い
                return null;
            }
            return this.data[ki];
        }

        // 範囲検索:一つ目
        public StringBuffer getrange(String startKey, int n){
            int ki = this.isKeyExist(startKey);
            StringBuffer vals = new StringBuffer();
            if (ki < 0){ // キーkが無い
                vals.append("the key" + startKey +  "not found");
                return vals;
            }
            for(int i =  ki; i < this.nkeys; i++){
                if(n > 0){
                    vals.append("key:");
                    vals.append(this.keys[i]);
                    vals.append(",value:");
                    vals.append(this.data[i]);
                    vals.append("  ");
                    n--;
                }
                else{ // 指定した数に達した
                    return vals;
                }
            }
            if(this.next != null){
                return this.next.getrangeContinue(vals, n);
            }
            return vals;
        }

        // 範囲検索:二個目以降
        public StringBuffer getrangeContinue(StringBuffer vals, int n){
            for(int i =  0; i < this.nkeys; i++){
                if(n > 0){
                    vals.append("key:");
                    vals.append(this.keys[i]);
                    vals.append(",value:");
                    vals.append(this.data[i]);
                    vals.append("  ");
                    n--;
                }
                else{ // 指定した数に達した
                    return vals;
                }
            }
            if(this.next != null){
                return this.next.getrangeContinue(vals, n);
            }
            vals.append("(end of keys)"); // まだ指定した数に達していないがもうキーがない
            return vals; 
        }

        // 削除
        public boolean delete(String k){
            int ki = this.isKeyExist(k);
            LeafNode t = this;
            if (ki >= 0){ // key(k)がもうある(ki番目に一致)とき、削除
                for(int i = ki; i < this.nkeys - 1; i++){ // 左詰め
                    this.keys[i] = this.keys[i+1];
                    this.data[i] = this.data[i+1];
                }
                this.keys[this.nkeys] = null; // 右端のキーと値削除
                this.data[this.nkeys] = null;
                this.nkeys--;
                System.out.println("the key " + k + " is deleted");
                if(nkeys == 0){ // キーが一つもなくなったらノードを削除
                    if(t.next != null && t.prev != null){
                        t.prev.next = t.next;
                        t.next.prev = t.prev; // リーフノード同士のポインタを修正
                    }
                    t = null; // ノードを削除
                    return true; // 親に知らせる
                }
                return false; // nkeysが1以上のとき、そのまま終了
            }
            else{ // key(k)がまだない場合、何もしない
                System.out.println("The key is already deleted");
                return false;
            }
        }

    }


    // 挿入
    void insert(String k, String v){ // tree
        this.root.insert(k, v);
    }

    // ノード番号
    private static int serialNumber = 0;

    // Bplustreeの根
    private Node root;

    // コンストラクタ
    public Bplustree() {
        this.root = null;
    }

    // get(printする)
    public void getPrint(String k){
        if (this.root == null){
            System.out.println("the tree is empty.");
            return;
        }
        String val = this.root.get(k);
        if(val == null){
            System.out.println("the key is not in the tree");
            return;
        }
        System.out.println("key:" + k + ",value:" + val);
    }

    // get(値を返す)
    public String get(String k){
        if (this.root == null){
            return null;
        }
        String val = this.root.get(k);
        return val;
    }

    // 挿入
    public void put(String k, String x){
        if (root == null) {
            root = new LeafNode(k,x);
        }
        else {
            SplitRequest req = root.insert(k,x);
            if (req == null) {
                // 何もしない
            } else {
                // 分割する
                InteriorNode newRoot = new InteriorNode();
                newRoot.keys[0] = req.borderKey;
                newRoot.child[0] = req.left;
                newRoot.child[1] = req.right;
                newRoot.nkeys = 1;
                root = newRoot;
            }
        }
    }

    // 削除
    public void delete(String key){
        if (root == null) {
            System.out.println("the tree is empty");
        }
        else{
            boolean req = this.root.delete(key);
            if (req == false){

            } else {
                Node t = root;
                if(t instanceof InteriorNode){
                    root = ((InteriorNode)t).child[0];
                    t = null;
                }
                else{
                    root = null;
                }
            }
        }
    }

    private static String makedot(Node t){ // 可視化用dotファイル用
        String text = "";
        if(t != null){
            text += "node" + t.serial + "[label = \"";
            for(int i = 0; i < t.nkeys; i++){
                text += "<f" + i + "> " + "|" + t.keys[i] + "|";
            }
            text += "<f" + t.nkeys + ">\"];\n";
            if(t instanceof InteriorNode){
                for(int i =0; i < t.nkeys+1; i++){
                    text += makedot(((InteriorNode)t).child[i]);
                    text += "\"node" + t.serial + "\":f" + i + " -> \"node" + ((InteriorNode)t).child[i].serial + "\"\n"; 
                }
            }
        }   
        return text;
    }

    public String makedot(){
        return makedot(this.root);
    }

    //範囲検索
    public void getrange(String startKey, Integer n){
        if (this.root == null){
            System.out.println("the tree is empty");
        }
        StringBuffer vals = this.root.getrange(startKey,n);
        System.out.println(vals.toString());
    }


    public static void main(String[] args){
    }
} 