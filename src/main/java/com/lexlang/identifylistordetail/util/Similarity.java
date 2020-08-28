package com.lexlang.identifylistordetail.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author lexlang
 * 2020年8月26日
 */
public class Similarity {
	
	/**
	 * 简单相似度
	 * @param a
	 * @param b
	 * @return
	 */
	public static double similarity(Set<String> a,Set<String> b){
		return intersection(a,b).size()*1.0/union(a,b).size();
	}
	
	public static Set<String> intersection(Set<String> a,Set<String> b){
		Set<String> c=copy(a);
		c.retainAll(b);
		return c;
	}
	
	public static Set<String> union(Set<String> a,Set<String> b){
		Set<String> c=copy(a);
		c.addAll(b);
		return c;
	}
	
	public static Set<String> copy(Set<String> a){
		Set<String> c=new HashSet<String>();
		Iterator<String> it = a.iterator();
		while(it.hasNext()){
			c.add(it.next());
		}
		return c;
	}
	
	public static void main(String[] args){
		Set<String> a=new HashSet<String>();
		a.add("h");a.add("e");a.add("l");a.add("l");a.add("o");
		Set<String> c=new HashSet<String>();
		c.add("h");c.add("o");c.add("e");c.add("l");c.add("d");
		System.out.println(Similarity.similarity(a, c));
	}
	
}
