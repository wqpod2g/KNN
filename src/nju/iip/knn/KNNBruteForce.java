package nju.iip.knn;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import nju.iip.preprocess.Tools;


/**
 * @description 用暴力方式求出某个帖子的K-Nearest Neighbor，并计算precision
 * @time 201-10-16
 * @author wangqiang
 */
public class KNNBruteForce {
	
	private static int keyWordsNum=3000;//特征词个数
	
	/**
	 *  K most similar
	 */
	private static int k=0;
	
	/**
	 * 整个样本二维矩阵
	 */
	private static ArrayList<ArrayList<Double>>allMatrix=new ArrayList<ArrayList<Double>>();
	
	/**
	 * 某个点与其他所有点的距离
	 */
	private static ArrayList<ArrayList<Double>>distanceResult=new ArrayList<ArrayList<Double>>();
	
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
	 * 计算某个帖子与其余所有帖子的距离
	 * @param x
	 * @return
	 */
	public static ArrayList<ArrayList<Double>> getDistanceResult(ArrayList<Double>x){
		
		for(int i=0;i<allMatrix.size();i++){
			ArrayList<Double>dis=new ArrayList<Double>();
			dis.add(Tools.vectorCosTheta(allMatrix.get(i),x));
			dis.add(allMatrix.get(i).get(keyWordsNum));//添加帖子所属类别
			distanceResult.add(dis);
			
		}
		return distanceResult;
	}
	
	/**
	 * 求出距离前k小的点
	 * @param k
	 * @return ArrayList<ArrayList<Double>>
	 */
	public static ArrayList<ArrayList<Double>>getKNNResult(){
		ArrayList<ArrayList<Double>>KNNDis=new ArrayList<ArrayList<Double>>();
		for(int i=0;i<k+1;i++){
			for(int j=0;j<200-i-1;j++){
				if(distanceResult.get(j).get(0)>distanceResult.get(j+1).get(0)){
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
			long startTime=System.currentTimeMillis();   //获取开始时间
			k=10*l;
		ArrayList<Double>precision=new ArrayList<Double>();
		for(int i=0;i<200;i++){
			getDistanceResult(allMatrix.get(i));
			ArrayList<ArrayList<Double>>KNNDis=getKNNResult();
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
	
	
	
	public static void main(String args[]) throws FileNotFoundException, IOException{
		getAllMatrix();
		
		process();
		
	}
		
	
		
		
}
