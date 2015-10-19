package com.face;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.contrib.FaceRecognizer;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.dao.SqlHelper;
import com.item.PersonItem;
import com.kinect.MainFrom;
import com.picutils.Utils;

public class TestFace {
	public int test(String imgefile){
		Utils utils = new Utils(imgefile);
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		FisherFaceRecognizer fr = new FisherFaceRecognizer();
		List<Mat> trainData = new ArrayList<Mat>();
		SqlHelper sqlHelper = new SqlHelper();
		int size = sqlHelper.size();
		int [] l = new int[size];
		ResultSet rs = sqlHelper.findHead();
		File file = null;
		int i =0;
		try {
			while(rs.next()){
				l[i]= rs.getInt(1);
				file= new File(rs.getString(2));
				if(file.isFile()){
					trainData.add(toGray(file.getAbsolutePath()));
				}
				i++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Mat labels = new Mat(1, trainData.size(), CvType.CV_32S);
		
		labels.put(0, 0, l);
		fr.train(trainData, labels);
		Mat test= toGray(imgefile);
		double [] confidence = new double[1];
		
		fr.predict(test, l, confidence);
		
		return l[0];
		
	}
	public static void main(String []args){
		
		TestFace test = new TestFace();
		SqlHelper sqlHelper = new SqlHelper();
		int index = test.test("face2.jpg");
		PersonItem peson = sqlHelper.findPerson(index);
		System.out.println(peson.getName());
		MainFrom mainFrom = new MainFrom("E:/faceImage/face1.jpg");
		mainFrom.show();
	}
	public Mat toGray(String imgeName){
		Mat inMat = Highgui.imread(imgeName);
		Mat outMat = new Mat();
		Imgproc.cvtColor(inMat, outMat, Imgproc.COLOR_RGB2GRAY);
		return outMat;
	}
	
}
