package Utilities;

//simple union-find based on int[] arrays
//for  "parent" and "rank"
//implements the "disjoint-set forests" described at
//http://en.wikipedia.org/wiki/Disjoint-set_data_structure
//which have almost constant "amortized" cost per operation
//(actually O(inverse Ackermann))

import java.util.*;

public class UnionFind {

//private int[] _parent;
//private int[] _rank;
public HashMap<Integer,Integer> _parent = new HashMap<>();
private HashMap<Integer,Integer> _rank = new HashMap<>();

public Map<Integer,Integer> getParent(){
	return _parent;
}


public int find(int i) {

 if(!_parent.containsKey(i)) {
	 _parent.put(i, i);
 }
 int p = _parent.get(i);
 if (i == p) {
   return i;
 }
 
 int findP = find(p);
 _parent.put(i, findP);
 
 return _parent.get(i);

}


public void union(int i, int j) {

 int root1 = find(i);
 int root2 = find(j);

 if (root2 == root1) return;

 if(!_rank.containsKey(root1))
		 _rank.put(root1, 0);
 if(!_rank.containsKey(root2))
		 _rank.put(root2, 0);
 
 if (_rank.get(root1) > _rank.get(root2)) {
   _parent.put(root2, root1);
 } else if ( _rank.get(root2) >  _rank.get(root1)) {
	 _parent.put(root1, root2);
 } else {
	 _parent.put(root2, root1); 
   _rank.put(root1, _rank.get(root1) + 1);
 }
}


public UnionFind(Set<Integer> set) {

 for (int i : set) {
   _parent.put(i, i);
   _rank.put(i, 0);
 }
}

}