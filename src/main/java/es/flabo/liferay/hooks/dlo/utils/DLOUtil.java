package es.flabo.liferay.hooks.dlo.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import javax.portlet.RenderRequest;

import com.liferay.document.library.kernel.exception.NoSuchFileException;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.document.library.kernel.util.DLUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.WebKeys;
//import java.lang.management.*;
//import com.sun.management.OperatingSystemMXBean;

import es.flabo.liferay.hooks.dlo.model.DLOEntry;
import es.flabo.liferay.hooks.dlo.model.EntryType;
import es.flabo.liferay.hooks.dlo.utils.gif.OptimizeGif;
import es.flabo.liferay.hooks.dlo.utils.pngtastic.core.PngImage;
import es.flabo.liferay.hooks.dlo.utils.pngtastic.core.PngOptimizer;


public class DLOUtil {

	private static final Log LOGGER = LogFactoryUtil.getLog(DLOUtil.class);

	private static final int VARIATION_SIZE = 100;
	public final static float DEFAULT_QUALITY = 0.85f;
	public final static float MINIMUN_QUALITY = 0.01f;
	public final static float STEP_QUALITY = 0.02f;
	public final static float STEP_VARIATION = 0.01f;
	public final static int VARIATION_LIMIT = 12;
	public final static int VARIATION_LIMIT_ADVERTISEMENT = 20;

	/**
	 * Get file entry from document library
	 * 
	 * @param fileEntryId entry id
	 * @return DLFileEntry File entry
	 */
	public static DLFileEntry getImage(long fileEntryId) {
		try {
			return DLFileEntryLocalServiceUtil.getFileEntry(fileEntryId);
		} catch (PortalException | SystemException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get absolute url to the document
	 * 
	 * @param fileEntryId
	 * @param themeDisplay
	 * @return Full URL
	 */
	public static String getImageURL(long fileEntryId, ThemeDisplay themeDisplay) {
		try {
			FileEntry image = DLAppLocalServiceUtil.getFileEntry(fileEntryId);
			if (image != null) {
				String url = DLUtil.getImagePreviewURL(image, themeDisplay);
				LOGGER.debug("URL for fileEntry " + fileEntryId + " :" + url);
				return url;
			}
		} catch (Exception e) {
			LOGGER.error(e);
		}
		return null;
	}

	/**
	 * Get all the images(gif,jpg or png) of the site
	 * 
	 * @param request
	 * @return List of file entries.
	 * @throws SystemException
	 */
	public static List<DLFileEntry> getSiteImages(RenderRequest request) throws SystemException {
		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		long siteId = themeDisplay.getSiteGroupId();
		// int count = DLFileEntryLocalServiceUtil.getDLFileEntriesCount();
		// LOGGER.debug("Count:" + count);

		int groupCount = DLFileEntryLocalServiceUtil.getGroupFileEntriesCount(siteId);
		LOGGER.debug("Count of the site:" + groupCount);

		List<DLFileEntry> entries = DLFileEntryLocalServiceUtil.getGroupFileEntries(siteId, 0, groupCount);
		List<DLFileEntry> siteImages = new ArrayList<DLFileEntry>();
		Iterator<DLFileEntry> entriesIterator = entries.iterator();

		while (entriesIterator.hasNext()) {
			DLFileEntry entry = entriesIterator.next();
			String mimeType = entry.getMimeType();
			if (mimeType.contains(DLOEntry.MIME_GIF) || mimeType.contains(DLOEntry.MIME_JPEG) || mimeType.contains(DLOEntry.MIME_PNG)) {
				siteImages.add(entry);
			}
		}
		LOGGER.debug("Count of images(gif,jpg and png) in the site:" + siteImages.size());
		return siteImages;
	}

	/**
	 * Process an entry to get one optimized version of the image
	 * 
	 * @param imagen
	 * @return true or false
	 */
	public static boolean processImage(DLOEntry imagen) {		
		if (imagen.getOriginalType()==EntryType.JPG) {
			imagen.setOptimizedType(EntryType.JPG);
			return DLOUtil.processJPG(imagen);
		} else if (imagen.getOriginalType()==EntryType.PNG) {
			imagen.setOptimizedType(EntryType.PNG);
			return DLOUtil.processPNG(imagen)==null?false:true;
		} else if (imagen.getOriginalType()==EntryType.GIF) {
			imagen.setOptimizedType(EntryType.GIF);
			return DLOUtil.processGIF(imagen)==null?false:true;
		} else {
			return false;
		}
	}

	private static void checkResourcesUsed(){
		try{
			int mb = 1024*1024;
	        Runtime runtime = Runtime.getRuntime();
	        long used = ((runtime.totalMemory() - runtime.freeMemory()) / mb);
	        long total = runtime.totalMemory()/mb;
	        long percent = (used*100)/total;
	        LOGGER.debug("Used Memory:"  + percent+ " %");
			if (percent>65){
				LOGGER.debug("Used memory too high.");
				System.gc();
			}
			
			//TODO
//			OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
//			// What % load the overall system is at, from 0.0-1.0
//			double loadTotalCPU = osBean.getSystemCpuLoad()*100;
//			LOGGER.debug("CPU Load:"+loadTotalCPU +" %");
//			if (loadTotalCPU>75){
//				LOGGER.debug("CPU usage too high.");
//				Thread.currentThread().sleep(1000);
//			}
		}catch(Exception ex){
			
		}
	}

	/**
	 * Normalize names
	 * 
	 * @param name
	 * @return Replace white spaces with _
	 */
	public static String normalize(String name) {
		return name.replaceAll(" ", "_");
	}

	/**
	 * Create directories
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private static String makeDirs(String path) throws IOException {
		File out = new File(path);
		if (!out.exists()) {
			if (!out.mkdirs()) {
				throw new IOException("Couldn't create path: " + path);
			}
		}
		return out.getCanonicalPath();
	}

	/**
	 * Optimization of a png pngquant https://pngquant.org/ pngcrush optipng http://optipng.sourceforge.net/ pngng
	 * pngtastic
	 * 
	 * @param imagen
	 * @return
	 */
	public static DLOEntry processPNG(DLOEntry imagen) {
		
		if (imagen != null) {
			checkResourcesUsed();
			imagen = processPNGJava(imagen);
//			DLOEntry optimizedWithJava = processPNGJava(imagen);
//			checkResourcesUsed();
//			DLOEntry optimizedWithPNGTastic = processPNGTastic(imagen);
//
//			if (optimizedWithJava != null && optimizedWithPNGTastic != null) {
//				LOGGER.debug("Result with Java:" + optimizedWithJava.getOptimizedSize()
//						+ "bytes - result with PNGTastic:" + optimizedWithPNGTastic.getOptimizedSize() + "bytes");
//			}
//
//			if (optimizedWithJava.getOptimizedSize() > optimizedWithPNGTastic.getOptimizedSize()) {
//				imagen = optimizedWithPNGTastic;				
//			} else {
//				imagen = optimizedWithJava;
//			}
		}
		return imagen;
	}

	/**
	 * Optimization lossless with Java
	 * 
	 * @param imagen
	 *            Document library entry to optimize
	 * @return
	 */
	public static DLOEntry processPNGJava(DLOEntry imagen) {
		LOGGER.debug("Processing PNG with JAVA");
		
		try {
			InputStream stream = imagen.getOriginalEntry().getContentStream();
			File compressedImageFile = new File(getTempPath()+ imagen.getOriginalName() + ".png");
			OutputStream os = new FileOutputStream(compressedImageFile);
			BufferedImage image = ImageIO.read(stream);

			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
			ImageWriter writer = null;

			if (!writers.hasNext()) {
				stream.close();
				os.close();
				throw new IllegalStateException("No writers found for png");
			}

			while (writers.hasNext()) {
				ImageWriter candidate = writers.next();
				LOGGER.debug("Writer for png: " + candidate.getClass().getSimpleName());

				if (candidate.getClass().getSimpleName().equals("CLibPNGImageWriter")) {
					writer = candidate; // This is the one we want
					break;
				} else if (writer == null) {
					writer = candidate; // Any writer is better than no writer ;-)
				}
			}

			// Prepare output file
			ImageOutputStream ios = ImageIO.createImageOutputStream(os);
			writer.setOutput(ios);

			// Set the compression quality
			ImageWriteParam param = writer.getDefaultWriteParam();
			
			if (param.canWriteCompressed()) {
				LOGGER.debug("Compresion types:" + param.getCompressionTypes().toString());
				param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				param.setCompressionType("DEFAULT");
				param.setCompressionQuality(DLOUtil.DEFAULT_QUALITY);
			}

			writer.write(null, new IIOImage(image, null, null), param);

			// close all streams
			stream.close();
			os.flush();
			os.close();
			ios.flush();
			ios.close();

			writer.dispose();
			LOGGER.debug("New size:" + compressedImageFile.length() + " path:" + compressedImageFile.getAbsoluteFile());
			imagen.setOptimizedEntry(compressedImageFile);
			imagen.setVariation(0);
			
			//clean memory
			image=null;
			writers=null;
			param=null;
			stream=null;
			os=null;
			ios=null;
			compressedImageFile=null;
			
		} catch (Exception e) {
			LOGGER.error(e);
		}

		return imagen;
	}

	/**
	 * Optimization lossless with pngtastic
	 * 
	 * @param imagen
	 *            Document library entry to optimize
	 * @return
	 */
	public static DLOEntry processPNGTastic(DLOEntry imagen) {
		LOGGER.debug("Processing PNG with pngtastic");
		
		try {			
			String fileEntryName = imagen.getOriginalName() + ".png";
			PngImage image = new PngImage(imagen.getOriginalEntry().getContentStream(), fileEntryName);
			String outputFile = getTempPath() + fileEntryName;			

			boolean removeGamma = false;
			Integer compressionLevel = 9;
			PngOptimizer optimizer = new PngOptimizer("debug");
			optimizer.optimize(image, outputFile, removeGamma, compressionLevel);

			imagen.setOptimizedEntry(new File(outputFile));
			imagen.setVariation(0);
		} catch (Exception e) {
			LOGGER.error(e);
		}

		return imagen;
	}

	/**
	 * Process an entry with GIF image
	 * @param imagen
	 * @return Optimized image or null in other case.
	 */
	public static DLOEntry processGIF(DLOEntry imagen) {

		// Parse options
		int blockSize = 1024;
		int dictClear = -1;		
		try {
			LOGGER.debug("Optimizing GIF");
			checkResourcesUsed();
			String fileEntryName = imagen.getOriginalName() + ".png";
			String outputFile = getTempPath() + fileEntryName;

			OptimizeGif.optimizeGif(imagen.getOriginalEntry().getContentStream(), blockSize, dictClear, new File(outputFile));

			imagen.setOptimizedEntry(new File(outputFile));
			imagen.setVariation(0);

		} catch (Exception e) {
			LOGGER.error(e);
			e.printStackTrace();
		}

		return imagen;
	}

	/**
	 * Gets the file path to temporal files and if no exist create it
	 * @return
	 * @throws IOException 
	 */
	public static String getTempPath() throws IOException {
		String dlPath = PropsUtil.get("dl.store.file.system.root.dir");
		String tempPath = dlPath + File.separator + "dlo_temp" + File.separator;
		DLOUtil.makeDirs(tempPath);
		return tempPath;
	}

	/**
	 * Process an JPG image
	 * 
	 * @param imagen
	 * @return 
	 */
	public static boolean processJPG(DLOEntry imagen) {
		DLOEntry processedForSize = DLOUtil.processJPG(imagen, false, true, DLOUtil.DEFAULT_QUALITY);
		DLOEntry processedForQuality = DLOUtil.processJPG(processedForSize, true, false, processedForSize.getQualityFactorUsed());
		if (processedForQuality==null){
			return false;
		}else{
			return true;
		}
	}

	/**
	 * Optimization lossy with native Java
	 * 
	 * @param imagen
	 *            Document library entry to optimize
	 * @param checkVariation
	 *            check differences between original and optimized
	 * @param findQuality
	 *            reduce quality until reduze file size
	 * @param quality
	 *            quality (from 0 to 1)
	 * @return
	 */
	public static DLOEntry processJPG(DLOEntry entry, boolean checkVariation, boolean findQuality, float quality) {
		
		LOGGER.debug("Procesing JPG:" + entry.getOriginalName() + " with size:" + entry.getOriginalSize() + " checkVariation:"
				+ checkVariation + " findQuality:" + findQuality + " quality:" + quality);
		
		checkResourcesUsed();

		try {
			InputStream stream = entry.getOriginalEntry().getContentStream();
			File compressedImageFile = new File(getTempPath()+entry.getOriginalName() + ".jpg");

			OutputStream os = new FileOutputStream(compressedImageFile);

			// create a BufferedImage as the result of decoding the supplied InputStream
			BufferedImage image = ImageIO.read(stream);

			// get all image writers for JPG format
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");

			if (!writers.hasNext()) {
				stream.close();
				os.close();
				throw new IllegalStateException("No writers found for jpg");
			}

			ImageWriter writer = (ImageWriter) writers.next();
			ImageOutputStream ios = ImageIO.createImageOutputStream(os);
			writer.setOutput(ios);

			ImageWriteParam param = writer.getDefaultWriteParam();

			// compress to a given quality
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			String[] types = param.getCompressionTypes();
			if (types.length > 0) {
				param.setCompressionType(types[0]);
				LOGGER.debug("Using compression type:" + param.getCompressionType());
			}
			param.setCompressionQuality(quality);

			// appends a complete image stream containing a single image and
			// associated stream and image metadata and thumbnails to the output
			writer.write(null, new IIOImage(image, null, null), param);

			// close all streams
			stream.close();
			os.flush();
			os.close();
			ios.flush();
			ios.close();
			writer.dispose();
			
			LOGGER.debug("New size:" + compressedImageFile.length());
			entry.setOptimizedEntry(compressedImageFile);
			entry.setQualityFactorUsed(quality);		

			//Clean memory
			types=null;
			param=null;
			//image=null;//not nullable here
			writer=null;
			ios=null;
			os=null;
			compressedImageFile = null;
			stream=null;
			
			// Busqueda de una mejora en espacio reduciendo calidad
			if (entry.getOptimizedEntry().length() >= entry.getOriginalSize() && findQuality
					&& entry.getQualityFactorUsed() > MINIMUN_QUALITY) {
				image=null;
				float newQuality = entry.getQualityFactorUsed() - STEP_QUALITY;
				LOGGER.debug("Optimization with quality:" + entry.getQualityFactorUsed()
						+ " failed, retrying with minor quality:" + newQuality);
				DLOUtil.processJPG(entry, false, true, newQuality);
			}else if(entry.getOptimizedEntry().length() < entry.getOriginalSize() && checkVariation) {
				// Si ya se ha conseguido mejorar espacio, buscamos la mayor reduccion posible en base a la perdida de informacion	
				double variation = getVariation(image, entry.getOptimizedEntry());
				entry.setVariation(variation);
				
				image=null;//clean memory
				LOGGER.debug("Variation with quality " + entry.getQualityFactorUsed() + ":" + variation);
				if (variation < VARIATION_LIMIT) {
					float newQuality = entry.getQualityFactorUsed() - STEP_VARIATION;
					LOGGER.debug("Optimization with quality:" + entry.getQualityFactorUsed()
							+ " has good results with low variation, retrying with minor quality:" + newQuality);
					DLOUtil.processJPG(entry, true, false, newQuality);
				}
			}

		} catch (NoSuchFileException noFile) {
			LOGGER.error("El documento al que hace referencia no se ha encontrado (no esta en data).");
		} catch (Exception e) {
			LOGGER.error(e);
		}
				
		return entry;
	}

	/**
	 * Calculate the variation between two images
	 * 
	 * @param original
	 * @param optimized
	 * @return Variation or MAX_VALUE of Double type in error case.
	 */
	private static double getVariation(BufferedImage original, File optimized) {
		try {
			BufferedImage buffer = ImageIO.read(optimized);
			double variation = getVariation(original,buffer);
			buffer=null;//clean memory
			
			LOGGER.debug("Variation reached:"+variation);
			return variation;
		} catch (IOException e) {
			LOGGER.error(e);
		}
		return Double.MAX_VALUE;
	}
	
	
	
	/**
	 * Calculate the variation between two images
	 * 
	 * @param original
	 * @param optimized
	 * @return Variation or MAX_VALUE of Double type in error case.
	 */
	private static double getVariation(BufferedImage original, BufferedImage optimized) {

		// The base size of the images.
		final int baseSize = VARIATION_SIZE;

		try {			
			BufferedImage originalRescaled = rescale(original, baseSize);
			BufferedImage optimizedRescaled = rescale(optimized, baseSize);
			
			// There are several ways to calculate distances between two vectors, we will calculate the sum of the
			// distances between the RGB values of pixels in the same positions.
			double dist = 0;
			for (int x = 0; x < baseSize; x++)
				for (int y = 0; y < baseSize; y++) {
					Color colorOriginal = new Color(originalRescaled.getRGB(x, y));
					int r1 = colorOriginal.getRed();
					int g1 = colorOriginal.getGreen();
					int b1 = colorOriginal.getBlue();
					
					Color colorOptimized = new Color(optimizedRescaled.getRGB(x, y));
					int r2 = colorOptimized.getRed();
					int g2 = colorOptimized.getGreen();
					int b2 = colorOptimized.getBlue();
					double tempDist = Math.sqrt((r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2));
					dist += tempDist;
				}
			
			//clean memory
			originalRescaled=null;
			optimizedRescaled=null;
			
			return dist;
		} catch (Exception e) {
			LOGGER.error(e);
		}

		return Double.MAX_VALUE;
	}

	public static BufferedImage rescale(BufferedImage image, int baseSize) {

	    Image tmp = image.getScaledInstance(baseSize, baseSize, Image.SCALE_SMOOTH);
	    BufferedImage dimg = new BufferedImage(baseSize, baseSize, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2d = dimg.createGraphics();
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();

	    return dimg;
	}  
	
}
