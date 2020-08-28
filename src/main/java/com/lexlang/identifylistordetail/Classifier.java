package com.lexlang.identifylistordetail;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSONObject;
import com.lexlang.identifylistordetail.util.svm.svm_my_predict;
import com.lexlang.identifylistordetail.util.svm.svm_train;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

/**
 * @author lexlang
 * 2020年8月27日
 */
public class Classifier {
	
	private final String[] keys={
		    "number_of_clusters",
		    "density_of_text",
		    "number_of_char",
		    "number_of_a_descendants",
		    "density_of_punctuation",
		    "has_datetime_mata",
		    "number_of_char_log10",
		    "similarity_of_title",
		    "number_of_punctuation",
		    "max_density_of_text",
		    "max_number_of_p_children",
		    "number_of_a_char",
		    "rate_of_a_char",
		    "number_of_p_descendants",
		    "number_of_a_char_log10"
		};
	
	private final String SCALE="{\"11\":{\"min\":0.0,\"max\":67003.0},\"12\":{\"min\":0.0,\"max\":1.4124408384043272},\"13\":{\"min\":0.0,\"max\":24002.0},\"14\":{\"min\":0.0,\"max\":4.826100729955656},\"0\":{\"min\":0.0,\"max\":67.0},\"1\":{\"min\":-1.3738738738738738,\"max\":1080.8292682926829},\"2\":{\"min\":140.0,\"max\":301477.0},\"3\":{\"min\":0.0,\"max\":3374.0},\"4\":{\"min\":-6.853932584269663,\"max\":530.7272727272727},\"5\":{\"min\":0.0,\"max\":1.0},\"6\":{\"min\":2.1492191126553797,\"max\":5.479255625506993},\"7\":{\"min\":0.07142857142857142,\"max\":3.0},\"8\":{\"min\":0.0,\"max\":22142.0},\"9\":{\"min\":0.46153846153846156,\"max\":3709.0},\"10\":{\"min\":0.0,\"max\":8847.0}}";
	
	private JSONObject scaleValues;
	private svm_my_predict pred;
	
	public Classifier(){
		this.scaleValues=JSONObject.parseObject(SCALE);
		if(new File("data.txt.model").exists()){
			this.pred = new svm_my_predict("data.txt.model");
		}
	}
	
	/**
	 * 
	 * @param filePath
	 * @param value 0为详情 1为列表
	 * @throws IOException
	 */
	private void makeLable(String filePath) throws IOException{
		File aFile = new File(filePath+"details");
		File[] aList = aFile.listFiles(new FileFilter(){
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".html");
			}});
		File bFile = new File(filePath+"list");
		File[] bList = bFile.listFiles(new FileFilter(){
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".html");
			}});
		for(int i=0;i<aList.length;i++){
			{
				String html=FileUtils.readFileToString(aList[i], "utf-8");
				String v=-1+" "+values(html)+"\r\n";
				System.out.println(v);
				FileUtils.write(new File("train.txt"),v , "utf-8", true);
			}
			{
				String html=FileUtils.readFileToString(bList[i], "utf-8");
				String v=1+" "+values(html)+"\r\n";
				System.out.println(v);
				FileUtils.write(new File("train.txt"),v , "utf-8", true);
			}
		}
	}
	
	private String values(String html){
		JSONObject res = new ExtractElement(html).getFeatureFuncs();
		StringBuilder sb=new StringBuilder();
		for(int ind=0;ind<keys.length;ind++){
			double v=(res.getDouble(keys[ind])-scaleValues.getJSONObject(ind+"").getDouble("min"))/(scaleValues.getJSONObject(ind+"").getDouble("max")-scaleValues.getJSONObject(ind+"").getDouble("min"));
			sb.append(ind+":"+v+" ");
		}
		return sb.toString();
	}
	
	private void train() throws IOException{
		svm_train.main(new String[] {new File("train.txt").getAbsolutePath(), new File("data.txt.model").getAbsolutePath()});
	}
	
	public String predict(String html) throws IOException{
		return pred.predict("0 "+values(html))<0?"detail":"list";
	}
	
	/**
	 * 数据归一化
	 * @throws IOException
	 */
	private void scaleValue() throws IOException{
		JSONObject res=new JSONObject();
		for(int i=0;i<=14;i++){
			String[] s=getMinMaxValue(i).split("_");
			JSONObject item=new JSONObject();
			item.put("min", Double.parseDouble(s[0]));
			item.put("max", Double.parseDouble(s[1]));
			res.put(i+"", item);
		}
		System.out.println(res.toJSONString());
	}
	
	private String getMinMaxValue(int i) throws IOException{
		List<String> list = FileUtils.readLines(new File("train.txt"), "utf-8");
		double min=1000000;
		double max=-1;
		for(String line:list){
			Matcher m = Pattern.compile("(?<="+i+":).+?(?= )").matcher(line);
			m.find();
			double d=Double.parseDouble(m.group());
			if(min>d){
				min=d;
			}
			if(max<d){
				max=d;
			}
		}
		return min+"_"+max;
	}
	
	public static void main(String[] args) throws IOException {
		File[] fs = new File("I:\\bark\\makeLabel\\detail").listFiles();
		for(File f:fs){
			System.out.println(new Classifier().predict(FileUtils.readFileToString(f, "utf-8")));
		}
	
	}

}
