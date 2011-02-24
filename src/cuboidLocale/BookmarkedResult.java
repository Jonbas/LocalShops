package cuboidLocale;

import java.util.ArrayList;

public class BookmarkedResult{
  ArrayList<PrimitiveCuboid> results;
  QuadNode bookmark;
  
  BookmarkedResult(){}
  
  BookmarkedResult(QuadNode node, ArrayList<PrimitiveCuboid> c){
    bookmark = node;
    results = c;
  }
}
