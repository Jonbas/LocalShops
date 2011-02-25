package cuboidLocale;

import java.util.ArrayList;

public class BookmarkedResult{
  public ArrayList<PrimitiveCuboid> results;
  public QuadNode bookmark;
  
  public BookmarkedResult(){}
  
  public BookmarkedResult(QuadNode node, ArrayList<PrimitiveCuboid> c){
    bookmark = node;
    results = c;
  }
}
