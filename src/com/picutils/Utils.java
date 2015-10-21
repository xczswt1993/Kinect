package com.picutils;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class Utils {
	public Utils(String fileName) {
			saveFace(fileName);
			changeToGray(fileName);
			zoomImage(fileName, fileName);
	}

	public void changeToGray(String fileName) {
		System.out.println("change to gray ...");
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			File input = new File(fileName);
			BufferedImage image = ImageIO.read(input);

			byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
			Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
			mat.put(0, 0, data);

			Mat mat1 = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
			Imgproc.cvtColor(mat, mat1, Imgproc.COLOR_RGB2GRAY);

			byte[] data1 = new byte[mat1.rows() * mat1.cols() * (int) (mat1.elemSize())];
			mat1.get(0, 0, data1);
			BufferedImage image1 = new BufferedImage(mat1.cols(), mat1.rows(), BufferedImage.TYPE_BYTE_GRAY);
			image1.getRaster().setDataElements(0, 0, mat1.cols(), mat1.rows(), data1);

			File ouptut = new File(fileName);
			ImageIO.write(image1, "jpg", ouptut);

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	public boolean saveFace(String fileName) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		boolean isExist = false;
		System.out.println("\nRunning FaceDetector");
		CascadeClassifier faceDetector = new CascadeClassifier(
				Utils.class.getResource("haarcascade_frontalface_alt.xml").getPath().substring(1));
		Mat image = Highgui.imread(fileName);
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);
		if (faceDetections.toArray().length > 0) {
			System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));
			Rect rectCrop = null;
			for (Rect rect : faceDetections.toArray()) {
				Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
						new Scalar(0, 255, 0));
				rectCrop = new Rect(rect.x, rect.y, rect.width, rect.height);
			}
			Mat image_roi = new Mat(image, rectCrop);
			Highgui.imwrite(fileName, image_roi);
			isExist = true;
			System.out.println("save face...");
		} else {
			isExist = false;
		}
		return isExist;
	}

	public void zoomImage(String src, String dest) {
		System.out.println("zoom picutre ...");
		int w = 125;
		int h = 150;
		double wr = 0, hr = 0;
		File srcFile = new File(src);
		File destFile = new File(dest);
		try {
			BufferedImage bufImg = ImageIO.read(srcFile);
			Image Itemp = bufImg.getScaledInstance(w, h, bufImg.SCALE_SMOOTH);
			wr = w * 1.0 / bufImg.getWidth();
			hr = h * 1.0 / bufImg.getHeight();
			AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(wr, hr), null);
			Itemp = ato.filter(bufImg, null);
			ImageIO.write((BufferedImage) Itemp, dest.substring(dest.lastIndexOf(".") + 1), destFile);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
