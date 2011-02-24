package cuboidLocale;

import java.util.ArrayList;

public class QuadNode{
  QuadNode parent = null;
  
  //children. Lower Left 0; Right + 1; Upper + 2
  /*    +0 |+1
   * +2  2 | 3
   * ------------
   * +0  0 | 1
   * 
   */
  QuadNode[] quads = new QuadNode[4];
  
  QuadNode(long x, long z, long size, QuadNode parent){
    this.x = x;
    this.z = z;
    this.size = size;
    this.parent = parent;
    if(parent == null){
      return;
    }
    //If the parent is holding cuboids it is our next list holder
    if(parent.cuboids.size() != 0){
      this.nextListHolder = parent;
    }else{//Otherwise whatever it's next list holder is is also ours (even null)
      this.nextListHolder = parent.nextListHolder;
    }
  }
  
  String getInfo(){
    return "(" + x + "," + z + "; " + size + ")"; 
  }
  
  //Length of a side, always a power of two
  long size;
  //Indexed by least x and least z
  long x; //Traditional X in 2d
  long z; //What would be Y but this is minecraft
  
  //We only hold the cuboids that fit completely inside of us.
  //Cuboids are always held in their minimal bounding node
  ArrayList<PrimitiveCuboid> cuboids = new ArrayList<PrimitiveCuboid>();
  //We only hold our list of cuboids, but to prevent duplicating lists and
  //having to climb to the root to find all of the cuboids that we need to look at
  //Point to the next highest node with cuboids to examine
  QuadNode nextListHolder;
}
