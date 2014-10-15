package nju.iip.knn;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nju.iip.preprocess.ChineseTokenizer;
import nju.iip.preprocess.Tools;

public class KNNBruteForce {
	
	/**
	 * 特征向量所包含的关键词Map（每类中取tf*idf前100大的）
	 */	
	private static Map<String,Double> keywordsMap=new LinkedHashMap<String,Double>();
	
	
	/**
	 * 整个样本向量，值为tf*idf
	 */
	private static Map<String,ArrayList<ArrayList<Double>>>allVector=new LinkedHashMap<String,ArrayList<ArrayList<Double>>>();
	
	private static String SamplePath="lily";//文本路径
	
	/**
	 * @Description: 返回特征向量词的map<word,idf>，800个词
	 * @return keywordsMap
	 * @throws IOException
	 */
	public static Map<String,Double>getKeyWordsMap() throws IOException{
		    
	        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("keywords.txt"), "UTF8")); 
	        String line = br.readLine();
	        while(line != null){  
	        	String[] str=line.split("=");
	        	Double idf=Double.parseDouble(str[1]);
	        	keywordsMap.put(str[0],idf);
	            line = br.readLine();    
	        }
	        br.close();
	       
		return keywordsMap;
	}
	
	
	/**
	 * @description 计算一个帖子的特征向量
	 * @param content
	 * @return ArrayList
	 * @throws IOException 
	 */
	public static ArrayList<Double>getOneArticleVector(String content) throws IOException{
		ArrayList<Double>Vector=new ArrayList<Double>();
		Vector.add(1.0);
		Map<String,Long>articleWordsMap=ChineseTokenizer.segStr(content);
		Set<String>words=articleWordsMap.keySet();
		Long size=Long.valueOf(0);
		for(String word:words){
			size=size+articleWordsMap.get(word);
		}
		keywordsMap=getKeyWordsMap();
		Set<String>keywords=keywordsMap.keySet();
		for(String keyword:keywords){
			if(articleWordsMap.containsKey(keyword)){
				Double tf=Double.valueOf(articleWordsMap.get(keyword))/size;
				Vector.add(tf*keywordsMap.get(keyword));
			}
			
			else
				Vector.add(Double.valueOf(0));
		}
		
		return Vector;//返回一个帖子的特征向量X(1,x1，x2....x800)
	}
	
	
	
	/**
	 * @description 计算并获得某一类所有帖子的特征向量
	 * @param classification(某一类txt文件的路径)
	 * @return ArrayList<ArrayList<Double>>
	 * @throws IOException
	 */
	
	public static ArrayList<ArrayList<Double>>getOneClassVector(String classification) throws IOException{
		ArrayList<ArrayList<Double>>classVector=new ArrayList<ArrayList<Double>>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(classification), "UTF8")); 
        String line = br.readLine();
        int n=1;
        while(line != null&&n<=20){  
        	ArrayList<Double>Vector=getOneArticleVector(line);
        	classVector.add(Vector);
        	//System.out.println(i+"      "+line);
        	line = br.readLine();
        	n++;
        }
        br.close();
		
		return classVector;
		
	}
	
	/**
	 * @description 获得整个样本的特征向量
	 * @return Map<String,ArrayList<ArrayList<Double>>>allVector
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	
	public static Map<String,ArrayList<ArrayList<Double>>>getAllVector() throws FileNotFoundException, IOException{
		List<String>fileList=Tools.readDirs(SamplePath);
		for(String article:fileList){
			allVector.put(article, getOneClassVector(article));
		}
		
		return allVector;
	}
	
	
	public static void main(String args[]){
		
	}
		
	
		
		
}
