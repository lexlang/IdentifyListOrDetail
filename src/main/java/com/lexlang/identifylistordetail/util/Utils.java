package com.lexlang.identifylistordetail.util;

import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @author lexlang
 * 2020Äê8ÔÂ26ÈÕ
 */
public class Utils {
	
	public static Set<String> alias(Element element){
		Set<String> set=new HashSet<String>();
		if(element==null){
			set.add("");
		}else{
			String tag=element.tagName();
			set.add("tag="+tag);
			if(tag.equals("html") || tag.equals("body")){
			}else{
				Attributes attrs = element.attributes();
				for(Attribute attr:attrs){
					set.add(attr.getKey()+"="+attr.getValue());
				}
			}
			set.add("childs="+element.childNodeSize());
		}
		return set;
	}

	public static void main(String[] args) {
		Document doc = Jsoup.parse("<html><body><a href=\"index.html\" class=\"list\"><p></p><p></p></a></body></html>");
		System.out.println(Utils.alias(doc.selectFirst("a")));
	}

}
