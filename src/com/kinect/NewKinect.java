package com.kinect;

import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_core.cvLoad;
import static org.bytedeco.javacpp.opencv_core.cvPoint;
import static org.bytedeco.javacpp.opencv_imgproc.CV_AA;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.cvRectangle;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author stone
 */
public class NewKinect {

	DaemonThread myThread = null;
	private static final int DELAY = 35;

	BufferedImage image = null;

	opencv_core.IplImage grabbedImage;
	private volatile boolean isRunning;
	KinectCapture camera = null;

	private int imageCount = 0;
	private long totalTime = 0;

	CanvasFrame rootFrame = null;
	JPanel jp = null;
	JButton jb = null;


	opencv_objdetect.CvHaarClassifierCascade classifier;
	opencv_core.CvSeq faces;

	public NewKinect() {

		rootFrame = new CanvasFrame("hello");
		rootFrame.setLayout(new FlowLayout());
		jp = new JPanel();
		jb = new JButton("hello");
		jp.add(jb);
		rootFrame.add(jp);
		jb.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					jbActionPerformed(evt);
				} catch (IOException ex) {
					Logger.getLogger(NewKinect.class.getName()).log(Level.SEVERE, null, ex);
				} catch (AWTException ex) {
					Logger.getLogger(NewKinect.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});
		String classifierName = "haarcascade_frontalface_alt.xml";

		try {
			File classifierFile = Loader.extractResource(classifierName, null, "classifier", ".xml");
			if (classifierFile == null || classifierFile.length() <= 0) {
				throw new IOException("Could not extract \"" + classifierName + "\" from Java resources.");
			}
			System.out.println(classifierName);
			// Preload the opencv_objdetect module to work around a known bug.
			Loader.load(opencv_objdetect.class);

			// We can "cast" Pointer objects by instantiating a new object of
			// the desired class.
			classifier = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
			classifierFile.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		myThread = new DaemonThread();
		Thread t = new Thread(myThread);
		t.setDaemon(true);
		t.start();

	}

	private void jbActionPerformed(ActionEvent evt) throws IOException, AWTException {

		screenCapture(image);
		FaceCapture fCapture = new FaceCapture();
		fCapture.saveFace();
		// To change body of generated methods, choose Tools | Templates.
	}

	public static void main(String args[]) {

		Loader.load(opencv_objdetect.class);
		new NewKinect();

	}

	class DaemonThread implements Runnable {

		@Override
		public void run() {
			camera = new KinectCapture();
			// update panel and window sizes to fit video's frame size
			Dimension frameSize = camera.getFrameSize();

			long duration;
			BufferedImage im = null;
			isRunning = true;
			System.out.println("Snap delay: " + DELAY + "ms");

			while (isRunning) {
				long startTime = System.currentTimeMillis();
				im = camera.getImage(); // take a snap

				duration = System.currentTimeMillis() - startTime;

				if (im == null) {
					System.out.println("Problem loading image " + (imageCount + 1));
				} else {
					image = im; // only update image if im contains something
					imageCount++;
					totalTime += duration;
					Java2DFrameConverter frameCon = new Java2DFrameConverter();
					Frame frm = frameCon.convert(image);
					OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();

					grabbedImage = converter.convert(frm);

					int width = grabbedImage.width();
					int height = grabbedImage.height();
					opencv_core.IplImage grayImage = opencv_core.IplImage.create(width, height, IPL_DEPTH_8U, 1);

					opencv_core.CvMemStorage storage = opencv_core.CvMemStorage.create();
					cvClearMemStorage(storage);
					cvCvtColor(grabbedImage, grayImage, CV_BGR2GRAY);

					faces = cvHaarDetectObjects(grayImage, classifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
					int total = faces.total();
					for (int i = 0; i < total; i++) {
						CvRect r = new CvRect(cvGetSeqElem(faces, i));
						int x = r.x(), y = r.y(), w = r.width(), h = r.height();
						cvRectangle(grabbedImage, cvPoint(x, y), cvPoint(x + w, y + h), CvScalar.RED, 1, CV_AA, 0);

					}
					// cvThreshold(grayImage, grayImage, 64, 255,
					// CV_THRESH_BINARY);

					Frame rotatedFrame = converter.convert(grabbedImage);
					rootFrame.showImage(rotatedFrame);

				}
				if (duration < DELAY) {
					try {
						Thread.sleep(DELAY - duration); // wait until delay time
														// has passed
					} catch (Exception ex) {
					}
				}
			}
			camera.close();
		}

	}

	private void screenCapture(BufferedImage orgImg) throws IOException, AWTException {

		int imageWidth = orgImg.getWidth();
		int imageHeight = orgImg.getHeight();
		BufferedImage newPic = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);
		ColorConvertOp cco = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
		cco.filter(orgImg, newPic);
		File file = new File("screancapture.jpg");
		ImageIO.write(newPic, "jpg", file);
		System.out.println("hello1");
	}
}
