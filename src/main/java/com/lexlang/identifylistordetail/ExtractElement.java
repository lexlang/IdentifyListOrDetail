package com.lexlang.identifylistordetail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lexlang.identifylistordetail.util.MinDistance;
import com.lexlang.identifylistordetail.util.Similarity;
import com.lexlang.identifylistordetail.util.Utils;

/**
 * @author lexlang
 * 2020年8月26日
 * 抽取页面信息
 */
public class ExtractElement {
	
	private static final String PUNCTUATION = "[！，。？、；：“”‘’《》%（）<>{}「」【】*～`,\\.\\?:;'\"!%\\(\\)]";
	private static final int LIST_MIN_NUMBER=5;
	private static final int LIST_MIN_LENGTH = 10;
	private static final int LIST_MAX_LENGTH = 200;
	private static final double SIMILARITY_THRESHOLD = 0.8;
	private static final String[] METAS_MATCH={"meta[property^='rnews:datePublished']",
			"meta[property^='article:published_time']","meta[property^='og:published_time']","meta[property^='og:release_date']",
			"meta[itemprop^='datePublished']","meta[itemprop^='dateUpdate']","meta[name^='OriginalPublicationDate']","meta[name^='article_date_original']",
			"meta[name^='og:time']","meta[name^='apub:time']","meta[name^='publication_date']","meta[name^='sailthru.date']",
			"meta[name^='PublishDate']","meta[name^='publishdate']","meta[name^='PubDate']","meta[name^='pubtime']",
			"meta[name^='_pubtime']","meta[name^='weibo: article:create_at']","meta[pubdate^='pubdate']"};
	
	private static final String[] METAS_TITLE={"meta[name^='og:title']","meta[name^='title']",
			"meta[property^='og:title']","meta[property^='title']","meta[property^='page:title']"};
	
	private JSONObject feature_funcs=new JSONObject();
	private String html;
	
	public ExtractElement(String html){
		this.html=html;
	}
	
	public JSONObject getFeatureFuncs(){
		Document doc = Jsoup.parse(html);
		Element body = doc.selectFirst("body");
		int number_of_a_char=number_of_a_char(body);
		double number_of_a_char_log10=number_of_a_char_log10(body);
		int number_of_char = number_of_char(body);
		double number_of_char_log10 = number_of_char_log10(body);
		double rate_of_a_char = rate_of_a_char(body);
		int number_of_p_descendants = number_of_p_descendants(body);
		int number_of_a_descendants = number_of_a_descendants(body);
		int number_of_punctuation = number_of_punctuation(body);
		double density_of_punctuation = density_of_punctuation(body);
		int number_of_clusters = number_of_clusters(body);
		double density_of_text = density_of_text(body);
		double max_density_of_text = max_density_of_text(body);
		int max_number_of_p_children = max_number_of_p_children(body);
		int has_datetime_mata=has_datetime_mata(doc);
		double similarity_of_title=similarity_of_title(doc);
		JSONObject res=new JSONObject();
		res.put("number_of_a_char", number_of_a_char);res.put("number_of_a_char_log10",number_of_a_char_log10);res.put("number_of_char", number_of_char);
		res.put("number_of_char_log10", number_of_char_log10);res.put("rate_of_a_char", rate_of_a_char);res.put("number_of_p_descendants", number_of_p_descendants);
		res.put("number_of_a_descendants", number_of_a_descendants);res.put("number_of_punctuation", number_of_punctuation);res.put("density_of_punctuation",density_of_punctuation);
		res.put("density_of_punctuation", density_of_punctuation);res.put("number_of_clusters",number_of_clusters );res.put("density_of_text", density_of_text);
		res.put("max_density_of_text", max_density_of_text);res.put("max_number_of_p_children", max_number_of_p_children);res.put("has_datetime_mata", has_datetime_mata);
		res.put("similarity_of_title", similarity_of_title);
		
		return res;
	}
	
	
	/**
	 * 文档所有a标签的字符链接的个数
	 * @param a
	 * @return
	 */
	public int number_of_a_char(Element doc){
		if(doc==null){
			return 0;
		}
		Elements links = doc.select("a");
		StringBuilder sb=new StringBuilder();
		for(Element link:links){
			sb.append(link.text());
		}
		return sb.toString().replaceAll("\\s*", "").length();
	}
	
	/**
	 * a标签字符的log10
	 * @param doc
	 * @return
	 */
	public double number_of_a_char_log10(Element doc){
		if(doc==null){
			return 0;
		}
		return Math.log10(number_of_a_char(doc)+1);
	}
	
	/**
	 * 正文字符的个数
	 * @param doc
	 * @return
	 */
	public int number_of_char(Element doc){
		if(doc==null){
			return 0;
		}
		return doc.text().replaceAll("\\s*", "").length();
	}
	
	/**
	 * 正文个数log10
	 * @param doc
	 * @return
	 */
	public double number_of_char_log10(Element doc){
		if(doc==null){
			return 0;
		}
		return Math.log10(number_of_char(doc)+1);
	}
	
	/**
	 * a标签占文本的比率
	 * @return
	 */
	public double rate_of_a_char(Element doc){
        int number_of_a_char = number_of_a_char(doc);
        int number_of_char = number_of_char(doc);
        if(number_of_char==0){
        	return 0;
        }
        return 1.0*number_of_a_char/number_of_char;
	}
	
	/**
	 * p标签的个数
	 * @param doc
	 * @return
	 */
	public int number_of_p_descendants(Element doc){
		if(doc==null){
			return 0;
		}
		return doc.select("p").size();
	}
	
	/**
	 * a标签的个数
	 * @param doc
	 * @return
	 */
	public int number_of_a_descendants(Element doc){
		if(doc==null){
			return 0;
		}
		return doc.select("a").size();
	}
	
	/**
	 * 所有元素的个数
	 * @param doc
	 * @return
	 */
	public int number_of_descendants(Element a){
		int sum=a.children().size();
		if(sum>0){
			for(Element p:a.children()){
				sum+=number_of_descendants(p);
			}
		}
		return sum;
	}
	
	/**
	 * 正文标点符号的个数
	 * @param doc
	 * @return
	 */
	public int number_of_punctuation(Element doc){
		if(doc==null){
			return 0;
		}
		String text=doc.text().replaceAll("\\s*", "");
		Matcher m = Pattern.compile(PUNCTUATION).matcher(text);
		int c=0;
		while(m.find()){
			m.group();
			c++;
		}
		return c;
	}
	
	/**
	 * 标点符号稠密程度
	 * @param doc
	 * @return
	 */
	public double density_of_punctuation(Element doc){
		double result = (number_of_char(doc) - number_of_a_char(doc))*1.0/(number_of_punctuation(doc)+1);
		return result==0?1:result;
	}
	
	/**
	 * 获得列表有多少个
	 * @param doc
	 * @return
	 */
	public int number_of_clusters(Element doc){
		if(doc==null){
			return 0;
		}
		return descendants_of_body(doc);
	}
	
	/**
	 * 
	 * @param doc
	 * @return
	 */
	public int descendants_of_body(Element doc){
		JSONObject res=a_descendants_group(doc,"body");
		JSONObject results=new JSONObject();
		for(String key:res.keySet()){
			JSONArray items = res.getJSONArray(key);
			for(int ind=0;ind<items.size();ind++){
				String tKey="";
				for(String k:results.keySet()){
					int minDistance = MinDistance.minDistance(key, k);
					if(minDistance<=1){
						tKey=k;
						break;
					}else if(minDistance==2){
						Matcher m = Pattern.compile("\\d\\d").matcher(key);
						while(m.find()){
							String t =key.replace(m.group(), "A");
							if(MinDistance.minDistance(t, k)==1){
								tKey=k;
								break;
							}
						}
						if(tKey.length()>0){
							break;
						}
					}
				}
				JSONObject item=new JSONObject();
				item.put("path", key);
				item.put("element",items.get(ind));

				if(tKey.length()==0){
					results.put(key, new JSONArray());
					tKey=key;
				}
				results.getJSONArray(tKey).add(item);
			}
		}

		int descendants=0;
		for(String key:results.keySet()){
			JSONArray items = results.getJSONArray(key);
			if(items.size()>=LIST_MIN_NUMBER){
				int sum=0;
				for(int ind=0;ind<items.size();ind++){
					JSONObject item = items.getJSONObject(ind);
					sum+=((Element)item.get("element")).text().replaceAll("\\s*", "").length();
				}
				int average=sum/items.size();
				if(LIST_MIN_LENGTH<average &&  LIST_MAX_LENGTH>average){
					descendants++;
				}
			}
		}

		return descendants;
	}
	
	/**
	 * 文本稠密程度
	 * @param a
	 * @return
	 */
	private double density_of_text(Element a){
		int division=number_of_descendants(a)-number_of_a_descendants(a);
		if(division==0){
			return 0;
		}
		return (number_of_char(a)-number_of_a_char(a))*1.0/division;
	}
	
	/**
	 * 元素最大稠密值
	 * @param a
	 * @return
	 */
	private double max_density_of_text(Element a){
		List<Double> list=_max_density_of_tex(a);
		Collections.sort(list, Collections.reverseOrder());
		return list.get(0);
	}
	
	/**
	 * max_density_of_tex 辅组函数
	 * @param a
	 * @return
	 */
	private List<Double> _max_density_of_tex(Element a){
		List<Double> list = new ArrayList<Double>();
		list.add(density_of_text(a));
		for(Element p:a.children()){
			List<Double> cList=_max_density_of_tex(p);
			for(Double d:cList){
				list.add(d);
			}
		}
		return list;
	}
	
	/**
	 * 最大p元素的个数
	 * @param a
	 * @return
	 */
	private int max_number_of_p_children(Element a){
		List<Integer> list=_max_number_of_p_children(a);
		Collections.sort(list, Collections.reverseOrder());
		return list.get(0);
	}
	
	private List<Integer> _max_number_of_p_children(Element a){
		List<Integer> list = new ArrayList<Integer>();
		list.add(number_of_p_children(a));
		for(Element p:a.children()){
			List<Integer> cList=_max_number_of_p_children(p);
			for(Integer d:cList){
				list.add(d);
			}
		}
		return list;
	}
	
	/**
	 * p标签的个数
	 * @param a
	 * @return
	 */
	private int number_of_p_children(Element a){
		if(a==null){
			return 0;
		}
		int sum=0;
		for(Element p:a.children()){
			if(p.tagName().equals("p")){
				sum++;
			}
		}
		return sum;
	}
	
	/**
	 * 使用的header的发布元素
	 * @param header
	 * @return
	 */
	private int has_datetime_mata(Element header){
		for(String key:METAS_MATCH){
			if(header.select(key).size()>0){
				return 1;
			}
		}
		return 0;
	}
	
	/**
	 * 是否有类似的标题
	 * @param doc
	 * @return
	 */
	private double similarity_of_title(Document doc){
        String title_extract_by_title = extract_by_title(doc);
        String title_extract_by_meta = extract_by_meta_title(doc);
        String title_extract_by_h =extract_by_h_title(doc);
        
        String title_target="";
        if(title_extract_by_meta.length()>0){
        	title_target=title_extract_by_meta;
        }else if(title_extract_by_h.length()>0){
        	title_target=title_extract_by_h;
        }
        
        if(title_target.length()==0){
        	return 2;
        }else if(title_extract_by_h.length()==0){
        	return 3;
        }else{
        	return MinDistance.distanceSimilarity(title_target, title_extract_by_h);
        }
	}
	
	/**
	 * 抽取title标签的内容
	 * @param doc
	 * @return
	 */
	public String extract_by_title(Document doc){
		if(doc.select("title").size()>0){
			return doc.selectFirst("title").text();
		}
		return "";
	}
	
	public String extract_by_meta_title(Document doc){
		for(String key:METAS_TITLE){
			if(doc.select(key).size()>0){
				return doc.selectFirst(key).attr("content");
			}
		}
		return "";
	}
	
	public String extract_by_h_title(Document doc){
		String[] hs={"h1","h2","h3"};
		for(String h:hs){
			if(doc.select(h).size()>0){
				return doc.selectFirst(h).text().trim();
			}
		}
		return "";
	}
	
	private String getParentPath(String key){
		String[] keys = key.split("/");
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<keys.length-1;i++){
			sb.append(keys[i]+"/");
		}
		return sb.toString();
	}
	
	public int getTextLength(JSONArray items){
		int sum=0;
		for(int ind=0;ind<items.size();ind++){
			sum+=items.getIntValue(ind);
		}
		return sum/items.size();
	}
	
	public JSONObject descendants_siblings(JSONObject res){
		JSONObject results=new JSONObject();
		for(String key:res.keySet()){
			JSONArray items = res.getJSONArray(key);
			for(int ind=0;ind<items.size();ind++){
				Element p = (Element) items.get(ind);
				int numOfSiblings=number_of_siblings(p);
				double similarityWithSiblings=similarity_with_siblings(p);
				if(! results.containsKey(key)){
					results.put(key, new JSONArray());
				}
				JSONObject store = new JSONObject();
				store.put("number_of_siblings", numOfSiblings);
				store.put("similarity_with_siblings", similarityWithSiblings);
				results.getJSONArray(key).add(store);
			}
		}
		return results;
	} 
	
	/**
	 * 兄弟元素的个数
	 * @param e
	 * @return
	 */
	public int number_of_siblings(Element e){
		return e.siblingElements().size()+1;
	}
	
	/**
	 * 兄弟相似度的元素
	 * @param e
	 * @return
	 */
	public double similarity_with_siblings(Element e){		
		ArrayList<Double> list = new ArrayList<Double>();
		double sum=0;
		for(Element s:e.siblingElements()){
			sum+=similarity_with_element(e,s);
		}
		if(e.siblingElements().size()==0){
			return 0;
		}else{
			return sum*1.0/e.siblingElements().size();
		}
	}
	
	/**
	 * 两个元素的相似度
	 * @param a
	 * @param b
	 * @return
	 */
	public double similarity_with_element(Element a,Element b){
		return Similarity.similarity(Utils.alias(a), Utils.alias(b));
	}
	
	/**
	 * 相同路径下元素的平均长度
	 * @param a
	 * @param path
	 * @return
	 */
	public JSONObject a_descendants_group_text_length(JSONObject res){
		//JSONObject res=a_descendants_group(a,path);
		JSONObject results=new JSONObject();
		for(String key:res.keySet()){
			JSONArray items = res.getJSONArray(key);
			JSONArray store=new JSONArray();
			for(int ind=0;ind<items.size();ind++){
				Element p = (Element) items.get(ind);
				store.add(p.text().replaceAll("\\s*", "").length());
			}
			results.put(key, store);
		}
		return results;
	} 
	
	/**
	 * 元素集合
	 * @param a
	 * @param path
	 * @return
	 */
	public JSONObject a_descendants_group(Element a,String path){
		JSONObject result=new JSONObject();
		int order=0;
		for(Element p:a.children()){
			if(p.tagName().equals("a")){
				if(! result.containsKey(path+"/"+p.tagName())){
					result.put(path+"/"+p.tagName(), new JSONArray());
				}
				result.getJSONArray(path+"/"+p.tagName()).add(p);
			}
			if(p.children().size()>0){
				JSONObject cResult=a_descendants_group(p,path+"/"+p.tagName()+"["+order+"]");
				Set<String> keys = cResult.keySet();
				for(String key:keys){
					JSONArray items = cResult.getJSONArray(key);
					for(int ind=0;ind<items.size();ind++){
						if(! result.containsKey(key)){
							result.put(key, new JSONArray());
						}
						result.getJSONArray(key).add(items.get(ind));
					}
				}
			}
			order++;
		}
		return result;
	}

	private String pathRaw(String path){
		return path.length()>0?path+"/":path;
	}
	
	public static void main(String[] args) throws IOException {
		String html=FileUtils.readFileToString(new File("C:\\Users\\A\\Desktop\\chinaHr.html"), "utf-8");
		System.out.println(new ExtractElement(html).getFeatureFuncs());
	}

}
