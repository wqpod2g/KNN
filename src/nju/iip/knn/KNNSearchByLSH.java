package nju.iip.knn;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import nju.iip.preprocess.Tools;


/**
 * @decription Implement the K-Nearest Neighbor Search by Locality-Sensitive Hashing algorithms
 * @author wangqiang
 * @time 2014-10-18
 */
public class KNNSearchByLSH {
	
	private static int keyWordsNum=3000;//特征词个数
	
	private static int HashNum=5;//hash函数个数

	
	/**
	 *  K most similar
	 */
	private static int k=0;
	
	/**
	 * 某个点与其他所有点的距离
	 */
	private static ArrayList<ArrayList<Double>>distanceResult=new ArrayList<ArrayList<Double>>();
	
	/**
	 * 5组随机法向量，每组3个
	 */
	private static Map<Integer,ArrayList<ArrayList<Double>>>allRandomVectors=new LinkedHashMap<Integer,ArrayList<ArrayList<Double>>>();
	/**
	 * 5组桶，每组8个
	 */
	private static Map<Integer,Map<Integer,ArrayList<ArrayList<Double>>>>allBuckets=new LinkedHashMap<Integer,Map<Integer,ArrayList<ArrayList<Double>>>>();

	
	/**
	 * 整个样本二维矩阵
	 */
	private static ArrayList<ArrayList<Double>>allMatrix=new ArrayList<ArrayList<Double>>();
	
	
	private static String SamplePath="lily";//文本路径
	
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
			allMatrix.addAll(Tools.getOneClassVector(article,ID));
			ID++;
		}
		
		return allMatrix;
	}
	
	
	/**
	 * @description 生成三个随机向量
	 * @return
	 */
	public static ArrayList<ArrayList<Double>>getRandomVector(){
		ArrayList<ArrayList<Double>>randomVectors=new ArrayList<ArrayList<Double>>();
		for(int j=0;j<3;j++){
			ArrayList<Double>randomVector=new ArrayList<Double>();
			for(int i=0;i<keyWordsNum+1;i++){
				int num=new Random().nextInt(3)-1;//产生-1,0,1的随机数
				randomVector.add(num*1.0);
			}
			
			randomVectors.add(randomVector);
			
		}
		
		return randomVectors;
	}
	
	/**
	 * 
	 * @return allRandomVectors
	 */
	public static Map<Integer,ArrayList<ArrayList<Double>>>getAllRandomVectors(){
		
		for(int i=0;i<HashNum;i++){
			ArrayList<ArrayList<Double>>randomVectors=getRandomVector();
			allRandomVectors.put(i,randomVectors);
			
		}
		return allRandomVectors;
	}
	
	/**
	 * @计算某篇帖子hash后的值即桶号
	 * @param x
	 * @return
	 */
    public static int getBucketsNum(ArrayList<Double> x,ArrayList<ArrayList<Double>>randomVectors){
    	int bucketsNum=0;
    	ArrayList<Integer>hashBit=new ArrayList<Integer>();
    	for(int i=0;i<3;i++){
    		if(Tools.vectorProduct(x,randomVectors.get(i))>0){
    			hashBit.add(1);
    		}
    		else{
    			hashBit.add(0);
    		}
    	}
    	bucketsNum=hashBit.get(0)*4+hashBit.get(1)*2+hashBit.get(2);
    	return bucketsNum;
		
	}
    
    /**
     * @description 一次hash得到一组桶(8个)
     * @return buckets
     */
    public static Map<Integer,ArrayList<ArrayList<Double>>> getOneBuckets(ArrayList<ArrayList<Double>>randomVectors){
    	Map<Integer,ArrayList<ArrayList<Double>>>buckets=new LinkedHashMap<Integer,ArrayList<ArrayList<Double>>>();//共设8个桶，每个同存放hash值相同的向量
    	for(int i=0;i<200;i++){
    		int bucketsNum=getBucketsNum(allMatrix.get(i),randomVectors);
    		ArrayList<ArrayList<Double>>tempList=new ArrayList<ArrayList<Double>>();
    		if(buckets.get(bucketsNum) != null){
    			tempList=buckets.get(bucketsNum);
    		}
    		tempList.add(allMatrix.get(i));
    		buckets.put(bucketsNum, tempList);
    	}
    	return buckets;
    	
    }
    
    /**
     * @description 获得5组每组8个桶
     * @return allBuckets
     */
    public static Map<Integer,Map<Integer,ArrayList<ArrayList<Double>>>>getAllBuckets(){
    	for(int i=0;i<HashNum;i++){
    		allBuckets.put(i, getOneBuckets(allRandomVectors.get(i)));
    	}
    	return allBuckets;
    }
    
	/**
	 * @description 对于某个帖子X合并和它同一个桶中的帖子
	 * @param x
	 * @return
	 */
    public static ArrayList<ArrayList<Double>>getOneArticleMatrix(ArrayList<Double> x){
    	ArrayList<ArrayList<Double>>oneArticleaMatrix=new ArrayList<ArrayList<Double>>();
    	for(int i=0;i<5;i++){
    		int bucketsNum=getBucketsNum(x,allRandomVectors.get(i));
    		oneArticleaMatrix.addAll(allBuckets.get(i).get(bucketsNum));
    	}
    	return oneArticleaMatrix;
    }
    
    
    /**
	 * 计算某个帖子与其余所有帖子的距离
	 * @param x
	 * @return
	 */
	public static ArrayList<ArrayList<Double>> getDistanceResult(ArrayList<Double>x){
		ArrayList<ArrayList<Double>>distanceResult=new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>>oneArticleaMatrix=getOneArticleMatrix(x);
		for(int i=0;i<oneArticleaMatrix.size();i++){
			ArrayList<Double>dis=new ArrayList<Double>();
			dis.add(Tools.vectorCosTheta(oneArticleaMatrix.get(i),x));
			dis.add(oneArticleaMatrix.get(i).get(keyWordsNum));//添加帖子所属类别
			distanceResult.add(dis);
			
		}
		return distanceResult;
	}
    
	/**
	 * 求出距离前k小的点
	 * @param k
	 * @return ArrayList<ArrayList<Double>>
	 */
	public static ArrayList<ArrayList<Double>>getKNNResult(ArrayList<Double>x){
		ArrayList<ArrayList<Double>>distanceResult=getDistanceResult(x);
		ArrayList<ArrayList<Double>>KNNDis=new ArrayList<ArrayList<Double>>();
		for(int i=0;i<k+1;i++){
			for(int j=0;j<distanceResult.size()-i-1;j++){
				if(distanceResult.get(j).get(0)>distanceResult.get(j+1).get(0)){
					ArrayList<Double>temp=distanceResult.get(j);
					distanceResult.set(j, distanceResult.get(j+1));
					distanceResult.set(j+1, temp);
				}
			}
		}
		for(int m=distanceResult.size()-2-k+1;m<=distanceResult.size()-2;m++){
			KNNDis.add(distanceResult.get(m));
		}
		return KNNDis;
	}
	
	
	
	
	/**
	 * 处理数据
	 */
    public static void process(){
    	for(int l=1;l<=5;l++){
    		long startTime=System.currentTimeMillis();   //获取开始时间
    		k=10*l;
    		ArrayList<Double>precision=new ArrayList<Double>();
    		for(int i=0;i<200;i++){
    			ArrayList<ArrayList<Double>>KNNDis=getKNNResult(allMatrix.get(i));
    			Double n=0.0;
    			for(int j=0;j<k;j++){
    				if(KNNDis.get(j).get(1)==allMatrix.get(i).get(keyWordsNum)){
    					n++;
    				}
    			}
    			precision.add(n/k);
    			distanceResult.clear();
    		}
    		System.out.println("当k="+l*10+"：");
    		System.out.println("precision均值为："+Tools.getMean(precision));
    		System.out.println("precision标准差为："+Tools.getDeviation(precision));
    		long endTime=System.currentTimeMillis(); //获取结束时间   
    		System.out.println("时间： "+(endTime-startTime)+"ms"+"\n");
    		
    		}
    		
    		
    	}
    	
    	
	
	public static void main(String[] args) throws FileNotFoundException, IOException{
		allMatrix=getAllMatrix();//获得所有帖子的特征向量矩阵
		allRandomVectors=getAllRandomVectors();
    	allBuckets=getAllBuckets();
    	long startTime=System.currentTimeMillis();   //获取开始时间
		process();
		long endTime=System.currentTimeMillis(); //获取结束时间   
		System.out.println("时间： "+(endTime-startTime)+"ms");
	}
	
	

}
