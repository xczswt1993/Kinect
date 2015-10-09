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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
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

import com.sun.org.apache.bcel.internal.generic.NEW;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author stone
 */
public class NewKinect  extends CanvasFrame{

	DaemonThread myThread = null;
	Thread t = null;
	private static final int DELAY = 35;

	BufferedImage image = null;

	opencv_core.IplImage grabbedImage;
	private volatile boolean isRunning;
	KinectCapture camera = null;

	private int imageCount = 0;
	private long totalTime = 0;

	CanvasFrame rootFrame = null;
	JPanel jp = null;
	JButton jb1 = null;
	JButton jb2 = null;
	JButton jb3 = null;
	JButton jb4 = null;
	opencv_objdetect.CvHaarClassifierCascade classifier;
	opencv_core.CvSeq faces;

	public NewKinect() {
		super("hello");
		
		this.setLayout(new FlowLayout());
		this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		
		jp = new JPanel();
		jb1 = new JButton("识别人脸");
//		jb2 = new JButton("打开摄像头");
//		jb3 = new JButton("关闭");
		jb4 = new JButton("退出");
		GridLayout gLayout = new GridLayout(2, 1);
		gLayout.setVgap(80);
		jp.setLayout(gLayout);
		jp.add(jb1);
		jp.add(jb2);
		jp.add(jb3);
		jp.add(jb4);
		this.setSize(800, 700);
		this.add(jp);
		rootFrame = this;
		jb1.addActionListener(new java.awt.event.ActionListener() {
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
		t = new Thread(myThread);
		t.setDaemon(true);
		t.start();

	}

	private void jbActionPerformed(ActionEvent evt) throws IOException, AWTException {

//		screenCapture(image);
//		FaceCapture fCapture = new FaceCapture();
//		fCapture.saveFace();

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
					if(total<=0){
						JOptionPane.showMessageDialog(rootFrame, "对准摄像头，请勿遮挡脸部！","警告", JOptionPane.WARNING_MESSAGE);
					}
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
					} catch (InterruptedException ex) {
					}
				}
			}
			
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
