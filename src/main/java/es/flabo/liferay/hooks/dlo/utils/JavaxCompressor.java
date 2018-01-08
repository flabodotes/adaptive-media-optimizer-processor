package es.flabo.liferay.hooks.dlo.utils;
import java.io.*;
import java.util.*;
import java.awt.image.*;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;

 
public class JavaxCompressor {
	public static void main(String[] args) throws IOException {		
		  
		  
	      System.out.println("Reader formats:");
	      String formats[] = ImageIO.getReaderFormatNames();
	      for (int i =0 ; i<formats.length; i++){
	    	  System.out.println("-Format:"+formats[i]);
	      }		  
		
	      File input = new File("test.jpg");
	      if (!input.exists()){
	    	System.out.println("File not found:"+input.getAbsolutePath());  
	    	return;
	      }
	      
	      BufferedImage image = ImageIO.read(input);
	      File compressedImageFile = new File("test_compress.jpg");
	      OutputStream os =new FileOutputStream(compressedImageFile);

	      Iterator<ImageWriter>writers =  ImageIO.getImageWritersByFormatName("jpeg");
	      ImageWriter writer = (ImageWriter) writers.next();

	      ImageOutputStream ios = ImageIO.createImageOutputStream(os);
	      writer.setOutput(ios);

	      ImageWriteParam param = writer.getDefaultWriteParam();
	      
	      param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	      param.setCompressionQuality(1f);
	      	      
	      System.out.println("Compression types:");
	      String types[] = param.getCompressionTypes();
	      for (int i =0 ; i<types.length; i++){
	    	  System.out.println("-Type:"+types[i]);
	      }
	      //param.setCompressionType("JPEG-LS");
	      System.out.println("Is compression lossless:"+param.isCompressionLossless());
	      writer.write(null, new IIOImage(image, null, null), param);
	      
	      os.close();
	      ios.close();
	      writer.dispose();
	   }
}
