package cuboidLocale;

public class Test{
  public static void main(String[] argc){
    QuadTree tree = new QuadTree();
    java.util.Random rng = new java.util.Random();
    PrimitiveCuboid test;
    int x, y, z;
    int i;
    for(i=0;i<100000;i++){
      x = (rng.nextInt()) % 10000;
      y = (rng.nextInt()) % 10000;
      z = (rng.nextInt()) % 10000;
      test = new PrimitiveCuboid(
      x,y,z,
      x+(rng.nextInt() % 256),
      y+(rng.nextInt() % 256),
      z+(rng.nextInt() % 256)
      );
      tree.insert(test);
      if(i % 47 == 0){
        tree.delete(test);
      }
      if(i %1000 == 0){
        System.err.println(i);
      }
    }
    System.err.println("Nodes:" + tree.ct);
    BookmarkedResult res = new BookmarkedResult();
    x = (rng.nextInt()) % 10000;
    y = (rng.nextInt()) % 10000;
    z = (rng.nextInt()) % 10000;
    for(i=0;i<10000000;i++){
      x+=(rng.nextInt() % 64);
      y+=(rng.nextInt() % 64);
      z+=(rng.nextInt() % 64);
      res = tree.relatedSearch(res.bookmark, x, y, z);
      int size = res.results.size(); 
      if(size > 1){
        System.err.println("S:" + size);
      }
    }
    
  }
}
