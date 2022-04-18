package nuparu.btree.tree;

import nuparu.btree.Main;

import java.util.Arrays;

public class BNode {

    public BTree tree;
    public BNode parent;
    public int[] keys; //2t - 1
    public int keyCount;
    public BNode[] children; //2t
    public boolean leaf;

    public BNode(BTree tree){
        this(null,tree);
    }

    public BNode(BNode parent, BTree tree){
        this(parent,tree,new BNode[2*tree.t],0,new int[2*tree.t-1]);
    }

    public BNode(BNode parent, BTree tree, BNode[] children, int keyCount, int[] keys){
        this.tree = tree;
        this.parent = parent;
        this.keys = keys;
        this.keyCount = keyCount;
        this.children = children;
    }

    public BNode setLeaf(boolean leaf){
        this.leaf = leaf;
        return this;
    }

    public void shiftRight(int i){
        children[keyCount+1] = children[keyCount];
        for(int j = keyCount; j > i; j--){
            keys[j] = keys[j - 1];
            children[j] = children[j - 1];
        }
    }

    public void shiftLeft(int i){
        //children[keyCount+] = children[keyCount];
        shiftKeysLeft(i);
        shiftChildrenLeft(i);
    }

    public void shiftKeysLeft(int i){
        for(int j = i; j < keyCount-1; j++){
            keys[j] = keys[ j+ 1];
        }
        keys[keyCount-1] = 0;
    }

    public void shiftChildrenLeft(int i){
        for(int j = i; j < keyCount-1; j++){
            children[j] = children[ j+ 1];
        }
        children[keyCount] = null;
    }

    public void setChild(BNode child, int i){
        children[i] = child;
        if(child != null){
            child.parent = this;
        }
    }

    public int getT(){
        return tree.t;
    }

    public Object[] splitNode(){
        int m = keys[tree.t - 1];

        BNode z = new BNode(tree);
        z.leaf = leaf;
        z.keyCount = getT()-1;

        for(int j = 0; j < getT() - 1; j++){
            z.keys[j] = keys[getT()+j];
            z.setChild(children[getT()+j],j);
        }
        z.setChild(children[2*getT()-1],getT()-1);

        keyCount = getT()-1;

        for(int j = getT()-1; j < 2*getT()-1; j++){
            keys[j] = 0;
            children[j+1] = null;
        }
        children[2*getT()-1] = null;

        Object[] results = new Object[3];
        results[0] = m;
        results[1] = this;
        results[2] = z;

        return results;
    }

    public void splitChild(int i){
        Object[] split = children[i].splitNode();
        int m = (int) split[0];
        BNode left = (BNode) split[1];
        BNode right = (BNode) split[2];

        shiftRight(i);
        setChild(left,i);
        setChild(right,i+1);

        keys[i] = m;
        keyCount++;
    }

    public KeyCoords search(int key){


        int i = 0;
        while(i < keyCount && key > keys[i]){
            i++;
        }

        if(i < keyCount && key == keys[i]){
            return new KeyCoords(this,i);
        }
        if(leaf){
            return null;
        }

        if(key == 76){
            int x = 0;
        }
        return children[i].search(key);
    }

    public KeyCoords min(){
        if(leaf){
            return new KeyCoords(this,0);
        }
        return children[0].min();
    }

    public void remove(int key){
        KeyCoords coords = search(key);
        if(coords == null) return;

        BNode node = coords.node();
        int i = coords.index();

        if(node.leaf){
            node.shiftLeft(i);
            node.keyCount--;
        }
        else{
            KeyCoords min = coords.node().children[i+1].min();

            int ki = min.getKey();
            /*if(key == 76){
                int x = 0;
            }*/
            node.keys[i] = ki;


            min.node().remove(ki);


            node = min.node();

        }
        node.verifyIntegrity();
    }

    public boolean isActuallyWithinTheTree(){
        if(parent == null){
            return tree != null && tree.getRoot() == this;
        }
        return tree != null && parent.hasNodeAsChild(this) && parent.isActuallyWithinTheTree();
    }

    public boolean hasNodeAsChild(BNode node){
        return Arrays.asList(children).contains(node);
    }

    public void verifyIntegrity(){
        if(!isActuallyWithinTheTree()) return;
        if (this.parent == null || this.keyCount >= getT() - 1) {
            return;
        }

        BNode parent = this.parent;
        BNode leftSibling = null;
        BNode rightSibling = null;

        int index = 0;

        for (int j = 0; j < parent.children.length; j++) {
            BNode sibling = parent.children[j];
            if (sibling == this) {
                index = j;
                if (j > 0) {
                    leftSibling = parent.children[j - 1];
                }
                if (j < parent.children.length - 1) {
                    rightSibling = parent.children[j + 1];
                }
                break;
            }
        }

        if (leftSibling != null && leftSibling.keyCount > getT() - 1) {
            rotateValues(new KeyCoords(leftSibling, leftSibling.keyCount - 1),
                    new KeyCoords(parent, index - 1),
                    new KeyCoords(this, 0),
                    EnumSibling.LEFT);
            return;
        } else if (rightSibling != null && rightSibling.keyCount > getT() - 1) {
            rotateValues(new KeyCoords(rightSibling, 0),
                    new KeyCoords(parent, index),
                    new KeyCoords(this, this.keyCount),
                    EnumSibling.RIGHT);
            return;
        }

        BNode mended = null;

        if (leftSibling != null) {

            int upIndex = index - 1;
            int upKey = parent.keys[upIndex];

            mended = leftSibling;
            leftSibling.append(upKey, this);

            parent.shiftKeysLeft(upIndex);
            parent.shiftChildrenLeft(upIndex + 1);
            parent.keyCount--;

        } else if (rightSibling != null) {
            int upIndex = index;
            int upKey = parent.keys[upIndex];

            mended = this;
            this.append(upKey, rightSibling);
            parent.shiftKeysLeft(upIndex);
            parent.shiftChildrenLeft(upIndex+1);
            parent.keyCount--;
        }

        if(parent.parent == null && parent.keyCount == 0) {
            tree.setRoot(mended);
            return;
        }
        mended.parent.verifyIntegrity();
    }
    private static void rotateValues(KeyCoords donor, KeyCoords up, KeyCoords receiver, EnumSibling side){
        BNode donorNode = donor.node();
        BNode upNode = up.node();
        BNode receiverNode = receiver.node();

        int donorIndex = donor.index();
        int upIndex = up.index();
        int receiverIndex = receiver.index();

        int donorKey = donor.getKey();
        int upKey = up.getKey();

        upNode.keys[upIndex] = donorKey;

        if(side == EnumSibling.LEFT){
            receiverNode.shiftRight(0);
            receiverNode.keys[0] = upKey;
            receiverNode.setChild(donorNode.children[donorIndex+1],0);
            receiverNode.keyCount++;

            donorNode.keys[donorIndex] = 0;
            donorNode.children[donorIndex+1] = null;
        }
        else if(side == EnumSibling.RIGHT){
            receiverNode.keys[receiverNode.keyCount] = upKey;
            receiverNode.setChild(donorNode.children[donorIndex],receiverNode.keyCount+1);
            receiverNode.keyCount++;

            donorNode.shiftLeft(0);

        }
        donorNode.keyCount--;

    }

    private void append(int linkingKey, BNode node){
        keys[keyCount] = linkingKey;

        for(int i = 0; i < node.keyCount; i++){
            keys[keyCount+1+i] = node.keys[i];
        }

        for(int i = 0; i < node.children.length; i++){
            if(node.children[i] == null) break;
            setChild(node.children[i],keyCount+i+1);
        }


        keyCount+=1+node.keyCount;
    }

    public String toString() {
        String s = "{";
        for(int i = 0; i < keyCount; i++){
            s+=keys[i];
            if(i != keyCount-1){
                s+=", ";
            }
        }

        s += "}";
        return s;
    }

    public String toPythonifiedString(){
        String s = "(k{";
        for(int i = 0; i < keys.length; i++){
            s+=keys[i];
            if(i != keys.length-1){
                s+=", ";
            }
        }
        s+="} c{";

        for(int i = 0; i < children.length; i++){
            BNode child = children[i];
            String ss ="None";
            if(child!= null){
                ss = child.toPythonifiedString();
            }
            s+=ss;
            if(i != children.length-1){
                s+=", ";
            }
        }
        s+="})";

        return s;
    }

    public void print(String indent, boolean last) {
        System.out.print(indent);
        if (last) {
            System.out.print("\\-");
            indent += "  ";
        } else {
            System.out.print("|-");
            indent += "| ";
        }
        System.out.println(toString());

        for (int i = 0; i < keyCount+1; i++) {
            BNode child = children[i];
            if(child == null) continue;
            boolean end = i == keyCount;
            child.print(indent,end);
            if(end) break;
        }
    }
}
