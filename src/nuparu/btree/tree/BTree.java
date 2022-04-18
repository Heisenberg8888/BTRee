package nuparu.btree.tree;

import nuparu.btree.Main;

public class BTree {
    private BNode root;
    protected int t;

    public BTree(int t){
        this.t = t;
        this.root = new BNode(this).setLeaf(true);
    }

    public void setRoot(BNode newRoot){
        newRoot.parent = null;
        root = newRoot;
    }

    public BNode getRoot(){
        return this.root;
    }

    public void insert(int key){
        if(root.keyCount == 2 * t -1){
            BNode s = new BNode(this);
            s.children[0] = root;
            s.leaf = false;
            s.keyCount = 0;
            root = s;
            s.splitChild(0);
            insertNonFull(s,key);
        }
        else{
            insertNonFull(root,key);
        }
    }

    private void insertNonFull(BNode node, int key){
        int i = 0;
        while(i < node.keyCount && key > node.keys[i]){
            i++;
        }

        if(node.leaf){
            node.shiftRight(i);
            node.keys[i] = key;
            node.keyCount++;
        }
        else{
            if(node.children[i].keyCount == 2 * t - 1){
                node.splitChild(i);
                if(key > node.keys[i]){
                    i++;
                }
            }
            insertNonFull(node.children[i],key);
        }
    }


    public void remove(int key){
        root.remove(key);
    }

    public void print() {
        root.print("",false);
    }


    public void printPythonified() {
        System.out.println(root.toPythonifiedString());
    }
}
