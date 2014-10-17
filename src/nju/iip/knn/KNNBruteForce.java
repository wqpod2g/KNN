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


/**
 * @description 用暴力方式求出某个帖子的K-Nearest Neighbor，并计算precision
 * @time 201-10-16
 * @author wangqiang
 */
public class KNNBruteForce {
	
	/**
	 *  K most similar
	 */
	private static int k=20;
	
	/**
	 * 特征向量所包含的关键词Map（每类中取tf*idf前100大的）
	 */	
	private static Map<String,Double> keywordsMap=new LinkedHashMap<String,Double>();
	
	/**
	 * 整个样本二维矩阵，值为tf*idf
	 */
	private static ArrayList<ArrayList<Double>>allMatrix=new ArrayList<ArrayList<Double>>();
	
	/**
	 * 某个点与其他所有点的距离
	 */
	private static ArrayList<ArrayList<Double>>distanceResult=new ArrayList<ArrayList<Double>>();
	
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
	 * @param 帖子所属类别ID
	 * @return ArrayList
	 * @throws IOException 
	 */
	public static ArrayList<Double>getOneArticleVector(String content,Double ID) throws IOException{
		ArrayList<Double>Vector=new ArrayList<Double>();
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
				
				Vector.add(articleWordsMap.get(keyword)*1.0);
			}
			
			else
				Vector.add(Double.valueOf(0));
		}
		Vector.add(ID);
		return Vector;//返回一个帖子的特征向量X(1,x1，x2....x800)
	}
	
	
	
	/**
	 * @description 计算并获得某一类所有帖子的特征向量
	 * @param classification(某一类txt文件的路径)
	 * @return ArrayList<ArrayList<Double>>
	 * @throws IOException
	 */
	
	public static ArrayList<ArrayList<Double>>getOneClassVector(String classification,Double ID) throws IOException{
		ArrayList<ArrayList<Double>>classVector=new ArrayList<ArrayList<Double>>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(classification), "UTF-8")); 
        String line = br.readLine();
        int n=1;
        while(line != null&&n<=20){  
        	ArrayList<Double>Vector=getOneArticleVector(line,ID);
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
	
	public static ArrayList<ArrayList<Double>>getAllMatrix() throws FileNotFoundException, IOException{
		List<String>fileList=Tools.readDirs(SamplePath);
		Double ID=1.0;
		for(String article:fileList){
			allMatrix.addAll(getOneClassVector(article,ID));
			ID++;
		}
		
		return allMatrix;
	}
	
	
	
	
	
	
	/**
	 * @description 求2个向量的欧式距离
	 * @param x
	 * @param y
	 * @return
	 */
	public static Double vectorDistance(ArrayList<Double>x,ArrayList<Double>y){
		Double value=0.0;
		for(int i=0;i<x.size()-1;i++){
			value=value+(x.get(i)-y.get(i))*(x.get(i)-y.get(i));
		}
		return Math.sqrt(value);

	}
	
	
	
	/**
	 * 计算某个帖子与其余所有帖子的距离
	 * @param x
	 * @return
	 */
	public static ArrayList<ArrayList<Double>> getDistanceResult(ArrayList<Double>x){
		
		for(int i=0;i<allMatrix.size();i++){
			ArrayList<Double>dis=new ArrayList<Double>();
			dis.add(allMatrix.get(i).get(800));//添加帖子所属类别
			dis.add(vectorDistance(allMatrix.get(i),x));
			distanceResult.add(dis);
			
		}
		return distanceResult;
	}
	
	/**
	 * 求出距离前k小的点
	 * @param k
	 * @return
	 */
	public static ArrayList<ArrayList<Double>>getKNNResult(){
		ArrayList<ArrayList<Double>>KNNDis=new ArrayList<ArrayList<Double>>();
		for(int i=0;i<k+1;i++){
			for(int j=0;j<200-i-1;j++){
				if(distanceResult.get(j).get(1)<distanceResult.get(j+1).get(1)){
					ArrayList<Double>temp=distanceResult.get(j);
					distanceResult.set(j, distanceResult.get(j+1));
					distanceResult.set(j+1, temp);
				}
			}
		}
		
		for(int m=198-k+1;m<=198;m++){
			KNNDis.add(distanceResult.get(m));
		}
		return KNNDis;
	}
	
	/**
	 * @description 处理数据
	 */
	public static void process(){
		for(int l=1;l<=5;l++){
			k=10*l;
		ArrayList<Double>precision=new ArrayList<Double>();
		for(int i=0;i<200;i++){
			getDistanceResult(allMatrix.get(i));
			ArrayList<ArrayList<Double>>KNNDis=getKNNResult();
			Double n=0.0;
			for(int j=0;j<k;j++){
				if(KNNDis.get(j).get(0)==allMatrix.get(i).get(800)){
					n++;
					
				}
			}
			precision.add(n/k);
			distanceResult.clear();
		}
		System.out.println("当k="+l*10+"：");
		System.out.println("precision均值为："+Tools.getMean(precision));
		System.out.println("precision标准差为："+Tools.getDeviation(precision)+"\n");
		}
	}
	
	
	
	public static void main(String args[]) throws FileNotFoundException, IOException{
		getAllMatrix();
		long startTime=System.currentTimeMillis();   //获取开始时间
		process();
		long endTime=System.currentTimeMillis(); //获取结束时间   
		System.out.println("时间： "+(endTime-startTime)+"ms");

		
	}
		
	
		
		
}
