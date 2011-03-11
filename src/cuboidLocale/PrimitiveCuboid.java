package cuboidLocale;

import org.bukkit.World;

public class PrimitiveCuboid{
  public String name = null;
  public String world = null;
  public long[] xyzA = {0,0,0};
  public long[] xyzB = {0,0,0};
  long lowIndex[] = new long[3];
  long highIndex[] = new long[3];
  
  /**
   * Normalize the corners so that all A is <= B
   * This is CRITICAL for the correct functioning of the MortonCodes, and nice to have for comparison to a point
   */
  final private void normalize(){
    long temp;
    for(int i=0; i<3; i++){
      if(this.xyzA[i] > this.xyzB[i]){
        temp = this.xyzA[i];
        this.xyzA[i] = this.xyzB[i];
        this.xyzB[i] = temp;
      }
    }
  }
  
  public PrimitiveCuboid(long[] xyzA, long[] xyzB){
    this.xyzA = xyzA.clone();
    this.xyzB = xyzB.clone();
    this.normalize();
  }
  
  public PrimitiveCuboid(long xA,long yA,long zA, long xB, long yB, long zB){
    this.xyzA[0] = xA;
    this.xyzA[1] = yA;
    this.xyzA[2] = zA;
    
    this.xyzB[0] = xB;
    this.xyzB[1] = yB;
    this.xyzB[2] = zB;
    
    this.normalize();
  }
  
  final public boolean includesPoint(long x, long y, long z){
    if(this.xyzA[0] <= x && this.xyzA[1] <= y && this.xyzA[2] <= z &&
       this.xyzB[0] >= x && this.xyzB[1] >= y && this.xyzB[2] >= z
    ){
      return true;
    }
    return false;
  }
  
  final public boolean includesPoint(long[] pt){
    return this.includesPoint(pt[0], pt[1], pt[2]);
  }
  
}
