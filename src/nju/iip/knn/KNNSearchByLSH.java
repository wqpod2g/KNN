package nju.iip.knn;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nju.iip.preprocess.Tools;


/**
 * @decription Implement the K-Nearest Neighbor Search by Locality-Sensitive Hashing algorithms
 * @author wangqiang
 * @time 2014-10-18
 */
public class KNNSearchByLSH {
	
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
	
	
	
	
	
	
	
	
	

}
