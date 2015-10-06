package kinect;

// KinectCapture.java
// Andrew Davison, December 2011, ad@fivedots.psu.ac.th

/* Grab an image from the Kinect camera,
   either at NORMAL or HIGH resolution.

   The image, frame size, and frame rate (FPS) are 
   accessed via  get methods. 

   The interface is meant to be as close to JMFCapture.java at 
      http://fivedots.coe.psu.ac.th/~ad/jg/nui01/
   as possible, so the Kinect can replace the webcam in my NUI
   examples.
*/

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.ByteBuffer;

import org.OpenNI.*;



enum Resolution {
  NORMAL, HIGH
}


public class KinectCapture 
{
  private BufferedImage image = null;
  private int imWidth, imHeight;
  private int fps;

  private boolean isReleased = true;    
           // when Kinect context has been released

  // OpenNI
  private Context context;
  private ImageGenerator imageGen;



  public KinectCapture()
  {  this(Resolution.NORMAL);  }


  public KinectCapture(Resolution res)
  {  configOpenNI(res); }



  private void configOpenNI(Resolution res)
  // create context and image generator
  {
    try {
      context = new Context();
      
      // add the NITE Licence 
      License licence = new License("PrimeSense", "0KOIk2JeIBYClPWVnMoRKn5cdY4=");   // vendor, key
      context.addLicense(licence); 
      
      imageGen = ImageGenerator.create(context);

      MapOutputMode mapMode = null;
      if (res == Resolution.HIGH)
        mapMode = new MapOutputMode(1280, 1024, 15);   // xRes, yRes, FPS
      else   // default to NORMAL
        mapMode = new MapOutputMode(640, 480, 30);

      imageGen.setMapOutputMode(mapMode); 
      imageGen.setPixelFormat(PixelFormat.RGB24);
      
      // set Mirror mode for all 
      context.setGlobalMirror(true);

      context.startGeneratingAll(); 
      System.out.println("Started context generating..."); 
      isReleased = false;

      ImageMetaData imageMD = imageGen.getMetaData();
      imWidth = imageMD.getFullXRes();
      imHeight = imageMD.getFullYRes();
      fps = imageMD.getFPS();
      System.out.println("Kinect (w,h); fps: (" + imWidth + ", " + 
                                               imHeight + "); " + fps);
    } 
    catch (Exception e) {
      System.out.println(e);
      System.exit(1);
    }
  }  // end of configOpenNI()



  public BufferedImage getImage()
  {
    if (isReleased)
      return null;
    try {
      context.waitOneUpdateAll(imageGen);
      ByteBuffer imageBB = imageGen.getImageMap().createByteBuffer();
      return bufToImage(imageBB);
    }
    catch (GeneralException e) 
    {  System.out.println(e); }
    return null;
  }  // end of getImage()



  private BufferedImage bufToImage(ByteBuffer pixelsRGB)
  /* Transform the ByteBuffer of pixel data into a BufferedImage
     Converts RGB bytes to ARGB ints with no transparency. 
  */
  {
    int[] pixelInts = new int[imWidth * imHeight];
 
    int rowStart = 0;
        // rowStart will index the first byte (red) in each row;
        // starts with first row, and moves down

    int bbIdx;               // index into ByteBuffer
    int i = 0;               // index into pixels int[]
    int rowLen = imWidth * 3;    // number of bytes in each row
    for (int row = 0; row < imHeight; row++) {
      bbIdx = rowStart;
      // System.out.println("bbIdx: " + bbIdx);
      for (int col = 0; col < imWidth; col++) {
        int pixR = pixelsRGB.get( bbIdx++ );
        int pixG = pixelsRGB.get( bbIdx++ );
        int pixB = pixelsRGB.get( bbIdx++ );
        pixelInts[i++] = 
           0xFF000000 | ((pixR & 0xFF) << 16) | 
           ((pixG & 0xFF) << 8) | (pixB & 0xFF);
      }
      rowStart += rowLen;   // move to next row
    }

    // create a BufferedImage from the pixel data
     BufferedImage im =  new BufferedImage( imWidth, imHeight, BufferedImage.TYPE_INT_ARGB);
    im.setRGB( 0, 0, imWidth, imHeight, pixelInts, 0, imWidth );
    return im;
  }  // end of bufToImage()



  public Dimension getFrameSize()
  {  return new Dimension(imWidth, imHeight);   }


  public int getFPS()
  {  return fps;  } 



  public void close()
  { 
    try {
      context.stopGeneratingAll();
    }
    catch (StatusException e) {}
    context.release();
    isReleased = true;
  } // end of close()



  public boolean isClosed()
  // the Kinect is only 'closed' when its context has been released
  {  return isReleased;  }


} // end of KinectCapture class

