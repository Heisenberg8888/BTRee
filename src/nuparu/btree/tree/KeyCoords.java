package nuparu.btree.tree;

public record KeyCoords(BNode node, int index) {
    public int getKey(){
        return node().keys[index];
    }
}
