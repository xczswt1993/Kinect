
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.LocatorEx.Snapshot;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.bytedeco.javacpp.Loader;
import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvScalar;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_core.cvLoad;
import static org.bytedeco.javacpp.opencv_core.cvPoint;
import static org.bytedeco.javacpp.opencv_imgproc.CV_AA;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.cvRectangle;
import org.bytedeco.javacpp.opencv_objdetect;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;

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

    BufferedImage image = null;//image from kinect

    opencv_core.IplImage grabbedImage;
    private volatile boolean isRunning;
    KinectCapture camera = null;

    private int imageCount = 0;
    private long totalTime = 0;

    CanvasFrame rootFrame = null;
    JPanel jp = null;
    JButton jb = null;

    String classifierName = null;
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
        try {
            URL url = new URL("https://raw.github.com/Itseez/opencv/2.4.0/data/haarcascades/haarcascade_frontalface_alt.xml");
            File file = Loader.extractResource(url, null, "classifier", ".xml");
            file.deleteOnExit();
            classifierName = file.getAbsolutePath();
            // Preload the opencv_objdetect module to work around a known bug.
            Loader.load(opencv_objdetect.class);

            // We can "cast" Pointer objects by instantiating a new object of the desired class.
            classifier = new opencv_objdetect.CvHaarClassifierCascade(cvLoad(classifierName));
            if (classifier.isNull()) {
                System.err.println("Error loading classifier file \"" + classifierName + "\".");
                System.exit(1);
            }
        } catch (IOException ex) {
            Logger.getLogger(NewKinect.class.getName()).log(Level.SEVERE, null, ex);
        }
        myThread = new DaemonThread();
        Thread t = new Thread(myThread);
        t.setDaemon(true);
        t.start();

    }

    private void jbActionPerformed(ActionEvent evt) throws IOException, AWTException {

        screenCapture(image);
        saveFace();//To change body of generated methods, choose Tools | Templates.
    }

    public static void main(String args[]) throws IOException {

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
                im = camera.getImage();  // take a snap

                duration = System.currentTimeMillis() - startTime;

                if (im == null) {
                    System.out.println("Problem loading image " + (imageCount + 1));
                } else {
                    image = im;   // only update image if im contains something
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

                    faces = cvHaarDetectObjects(grayImage, classifier, storage,
                            1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
                    int total = faces.total();
                    for (int i = 0; i < total; i++) {
                        CvRect r = new CvRect(cvGetSeqElem(faces, i));
                        int x = r.x(), y = r.y(), w = r.width(), h = r.height();
                        cvRectangle(grabbedImage, cvPoint(x, y), cvPoint(x + w, y + h), CvScalar.RED, 1, CV_AA, 0);

                    }
//                        cvThreshold(grayImage, grayImage, 64, 255, CV_THRESH_BINARY);

                    Frame rotatedFrame = converter.convert(grabbedImage);
                    rootFrame.showImage(rotatedFrame);

                }
                if (duration < DELAY) {
                    try {
                        Thread.sleep(DELAY - duration);  // wait until delay time has passed
                    } catch (Exception ex) {
                    }
                }
            }
            camera.close();
        }

    }

//    private void screenCapture(opencv_core.IplImage orgImg) {
//        //  opencv_core.IplImage imgThreshold = cvCreateImage(cvGetSize(orgImg), 8, 1);
// 
//        cvSaveImage("dsmthreshold.jpg", orgImg);
//        System.out.println("hello");
//
//        // return imgThreshold;
//    }
    private void screenCapture(BufferedImage orgImg) throws IOException, AWTException {

        int imageWidth = orgImg.getWidth();
        int imageHeight = orgImg.getHeight();
        BufferedImage newPic = new BufferedImage(imageWidth, imageHeight,
                BufferedImage.TYPE_3BYTE_BGR);
        ColorConvertOp cco = new ColorConvertOp(ColorSpace
                .getInstance(ColorSpace.CS_GRAY), null);
        cco.filter(orgImg, newPic);
        File file = new File("screancapture.jpg");
         ImageIO.write(newPic, "jpg", file);
        System.out.println("hello1");
    }

    private void saveFace() {
        int x = 0, y = 0, height = 0, width = 0;
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println("\nRunning FaceDetector");

        org.opencv.objdetect.CascadeClassifier faceDetector = new org.opencv.objdetect.CascadeClassifier();
        faceDetector.load("haarcascade_frontalface_alt.xml");
        Mat image = Highgui.imread("screancapture.jpg");
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);
        System.out.println(String.format("Detected %s faces",  faceDetections.toArray().length));
        org.opencv.core.Rect rectCrop=null;
        for (org.opencv.core.Rect rect : faceDetections.toArray()) {
        Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
            new Scalar(0, 255, 0));
        rectCrop = new org.opencv.core.Rect(rect.x, rect.y, rect.width, rect.height);
        }
        Mat image_roi = new Mat(image,rectCrop);
        Highgui.imwrite("cropimage_912.jpg",image_roi);

        System.out.println("save face...");
    }
}
