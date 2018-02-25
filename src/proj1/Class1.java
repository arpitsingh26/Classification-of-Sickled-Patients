package proj1;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.*;
import org.opencv.objdetect.CascadeClassifier;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.*;
import java.util.List;

import java.awt.image.DataBufferByte;

public class Class1 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 try {
	         System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
	         CascadeClassifier cellDetector = new CascadeClassifier("F:/Softwares/image_processing/cascade1.xml");
	         File input = new File("F:/Softwares/image_processing/image.jpg");
	         BufferedImage image = ImageIO.read(input);	

	         byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	         Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
	         mat.put(0, 0, data);
	         
	         Mat gray=new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
	         Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
	         
	         
	         MatOfRect cellDetections = new MatOfRect();
	         cellDetector.detectMultiScale(gray, cellDetections);
	         for (Rect rect : cellDetections.toArray()) {
	             Imgproc.rectangle(gray, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),new Scalar(0, 255, 0));
	         }
	         
	         byte[] dataface= new byte[image.getHeight()* image.getWidth() * (int)(gray.elemSize())];
	         gray.get(0, 0, dataface);
	         BufferedImage imageface = new BufferedImage(gray.cols(),gray.rows(), BufferedImage.TYPE_BYTE_GRAY);
	         imageface.getRaster().setDataElements(0, 0, gray.cols(), gray.rows(), dataface);
	         File facef = new File("F:/Softwares/image_processing/output/facef.jpg");
	         ImageIO.write(imageface, "jpg", facef); 
	         //cell detection done
	         
	         Mat thresh1=new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
	         Mat thresh=new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
	         int operation=2+2;
	         int area=0;
	         int hullarea=0;
	         float solidity=0;
	         double perimeter=0.0,testval=0.0;
	         List <Double> roundness = new ArrayList<>();
	         int[] bin=new int[10];
	         for (int i=0;i<10;i++){
	        	 bin[i]=0;
	         }
	         
	         Mat element=Imgproc.getStructuringElement(0,new Size(2*2+1,2*2+1),new Point(2,2));
	         Imgproc.morphologyEx( gray, thresh1, operation, element );
	         Imgproc.threshold(thresh1, thresh,15, 255, Imgproc.THRESH_BINARY);

	         List <MatOfPoint> contours = new ArrayList<MatOfPoint>();
	 		 Mat hierarchy = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
	 		 Imgproc.findContours( thresh, contours, hierarchy,0,2, new Point(0, 0) );
	 		 
	 		 Mat drawing=new Mat(image.getHeight()*4, image.getWidth()*4, CvType.CV_8UC3); 
	 		 
	 		 MatOfInt hull = new MatOfInt();
	 		 int j=0;
	 		 for( int i = 0; i< contours.size() ; i++ ){
	 			if((Imgproc.contourArea(contours.get(i))>500)&&(Imgproc.contourArea(contours.get(i))<8000)){
	 	       		Scalar color = new Scalar( 255,255,255 );
	 	       		Imgproc.drawContours( drawing, contours, i, color, 2, 8, hierarchy, 0, new Point() );
	 	       		area = (int)Imgproc.contourArea(contours.get(i));
	 	       		Imgproc.convexHull(contours.get(i),hull,false);
	 	       		
	 	       	    MatOfPoint mopOut = new MatOfPoint();
	 	            mopOut.create((int)hull.size().height,1,CvType.CV_32SC2);
	 	            for(int i1 = 0; i1 < hull.size().height ; i1++){
	 	            	int index = (int)hull.get(i1, 0)[0];
	 	            	double[] point = new double[] {
	 	            			(contours.get(i)).get(index, 0)[0], (contours.get(i)).get(index, 0)[1]
	 	            	};
	 	            	mopOut.put(i1, 0, point);
	 	            }    
	 				hullarea = (int)Imgproc.contourArea(mopOut);

	 				if (hullarea !=0) solidity = ((float)area)/hullarea;
	 				//else solidity=(float)(area/0.00000000001);
	 				
	 				if (solidity>=0.8){
	 					Imgproc.drawContours( drawing, contours, i, color, 2, 8, hierarchy, 1, new Point() );
	 					
	 					List<MatOfPoint2f> newContours = new ArrayList<>();
	 					 for(MatOfPoint point : contours) {
	 					     MatOfPoint2f newPoint = new MatOfPoint2f(point.toArray());
	 					     newContours.add(newPoint);
	 					 }
	 					 
	 					perimeter=Imgproc.arcLength(newContours.get(i), true);
	 					roundness.add((4*area*3.141)/(perimeter*perimeter));
	 					for (int k=0;k<10;k++){
	 						if(roundness.get(j)<=(0.1*(1+k))){
	 						   bin[k]=bin[k]+1;
	 						   break;
	 					    }
	 					}
	 				}
	 				else roundness.add(Double.MAX_VALUE);
	 				System.out.print("Area: "+area+" Hullarea: "+hullarea+" Solidity: "+solidity);
	 				if (roundness.get(j)!=Double.MAX_VALUE) System.out.print(" Roundness: "+roundness.get(j));
	 				System.out.println();
	 				j++;
	 			}
	 		 }
	 		 int roundnesssize=0;
	 		 for (int k=0;k<roundness.size();k++){
	 			 if (roundness.get(k)!=Double.MAX_VALUE) roundnesssize++;
	 		 }
	 		 System.out.println(roundnesssize);
	 		 
	 		 for (int k=0;k<10;k++){
	 			 System.out.print(bin[k]+" ");
	 		 }
	 		 System.out.println();
	 		 
	 		for (int k=0;k<10;k++){
	 			 System.out.print((float)bin[k]/roundness.size()+" ");
	 		 }
	 		 System.out.println();
	 		 
	       	 byte[] data5 = new byte[drawing.cols()* drawing.rows() * (int)(drawing.elemSize())];
	         drawing.get(0, 0, data5);
	         BufferedImage image5 = new BufferedImage(drawing.cols(),drawing.rows(), BufferedImage.TYPE_BYTE_GRAY);
	         image5.getRaster().setDataElements(0, 0, drawing.cols(), drawing.rows(), data5);
	         File finalf = new File("F:/Softwares/image_processing/output/final.jpg");
	         ImageIO.write(image5, "jpg", finalf);
	 		 
	 		 byte[] data1 = new byte[image.getHeight()* image.getWidth() * (int)(gray.elemSize())];
	         gray.get(0, 0, data1);
	         BufferedImage image1 = new BufferedImage(gray.cols(),gray.rows(), BufferedImage.TYPE_BYTE_GRAY);
	         image1.getRaster().setDataElements(0, 0, gray.cols(), gray.rows(), data1);
	         File grayf = new File("F:/Softwares/image_processing/output/gray.jpg");
	         ImageIO.write(image1, "jpg", grayf);
	         
	         byte[] data2 = new byte[image.getHeight()* image.getWidth() * (int)(thresh1.elemSize())];
	         thresh1.get(0, 0, data2);
	         BufferedImage image2 = new BufferedImage(thresh1.cols(),thresh1.rows(), BufferedImage.TYPE_BYTE_GRAY);
	         image2.getRaster().setDataElements(0, 0, thresh1.cols(), thresh1.rows(), data2);
	         File thresh1f = new File("F:/Softwares/image_processing/output/thresh1.jpg");
	         ImageIO.write(image2, "jpg", thresh1f);
	         
	         byte[] data3 = new byte[image.getHeight()* image.getWidth() * (int)(thresh.elemSize())];
	         thresh.get(0, 0, data3);
	         BufferedImage image3 = new BufferedImage(thresh.cols(),thresh.rows(), BufferedImage.TYPE_BYTE_GRAY);
	         image3.getRaster().setDataElements(0, 0, thresh.cols(), thresh.rows(), data3);
	         File threshf = new File("F:/Softwares/image_processing/output/thresh.jpg");
	         ImageIO.write(image3, "jpg", threshf);
	         /*
	         Mat mat1 = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC1);
	         Imgproc.cvtColor(mat, mat1, Imgproc.COLOR_RGB2GRAY);

	         byte[] data1 = new byte[mat1.rows() * mat1.cols() * (int)(mat1.elemSize())];
	         mat1.get(0, 0, data1);
	         BufferedImage image1 = new BufferedImage(mat1.cols(),mat1.rows(), BufferedImage.TYPE_BYTE_GRAY);
	         image1.getRaster().setDataElements(0, 0, mat1.cols(), mat1.rows(), data1);

	         File output = new File("C:/Users/Arpit/Downloads/grayscale.jpg");
	         ImageIO.write(image1, "jpg", output);
	         */
	         
	      } catch (Exception e) {
	         System.out.println("Error: " + e.getMessage());
	      }
		
	}
	
	
	  
	 
	
	
}
