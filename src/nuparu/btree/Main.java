package nuparu.btree;

import nuparu.btree.tree.BTree;

public class Main {

    public static void main(String[] args) {
        BTree tree = new BTree(2);
        tree.insert(50);
        tree.insert(70);
        tree.insert(80);
        tree.insert(90);
        tree.insert(60);
        tree.insert(73);
        tree.insert(76);
        tree.insert(61);
        tree.insert(62);
        //tree.print();
        tree.insert(51);
        /*tree.insert(52);
        tree.insert(53);*/
        /*tree.print();
        tree.printPythonified();*/
        tree.print();
        System.out.println("");
        tree.remove(90);
        System.out.println("");
        tree.remove(76);
        System.out.println("");
        tree.remove(73);
        tree.remove(60);
        tree.print();
        tree.remove(62);
        tree.remove(70);
        System.out.println("###################################");
        tree.print();
    }
}
