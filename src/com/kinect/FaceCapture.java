package com.kinect;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author stone
 */
public class FaceCapture {
    public void saveFace() {
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println("\nRunning FaceDetector");

        CascadeClassifier faceDetector = new CascadeClassifier(FaceCapture.class.getResource("haarcascade_frontalface_alt.xml").getPath().substring(1));
        Mat image = Highgui.imread("screancapture.jpg");
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);
        System.out.println(String.format("Detected %s faces",  faceDetections.toArray().length));
        Rect rectCrop=null;
        for (Rect rect : faceDetections.toArray()) {
        Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
            new Scalar(0, 255, 0));
        rectCrop = new Rect(rect.x, rect.y, rect.width, rect.height);
        }
        Mat image_roi = new Mat(image,rectCrop);
        Highgui.imwrite("screancapture.jpg",image_roi);

        System.out.println("save face...");
    }

}
