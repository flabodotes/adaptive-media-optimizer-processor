package es.flabo.liferay.hooks.dlo.model;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Base64;


import es.flabo.liferay.hooks.dlo.utils.DLOUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;

public class DLOEntry {
	
	String originalName=null;
	FileEntry originalEntry=null;
	InputStream originalEntryStream=null;
	File optimizedEntry=null;	
	EntryType originalType = null;
	EntryType optimizedType = null;
	long originalSize;
	long optimizedSize;
	OptimizationType optimizationType=null;	
	float qualityFactorUsed = DLOUtil.DEFAULT_QUALITY;
	double variation = Double.MAX_VALUE;
	
	public static final String MIME_GIF = "image/gif";
	public static final String MIME_PNG = "image/png";
	public static final String MIME_JPEG = "image/jpeg";	
	
//	public DLOEntry() {
//		super();
//	}

	public DLOEntry(InputStream stream, String mime,long size,String name) {
		super();
		this.originalEntry=null;
		this.originalEntryStream = stream;
		
		if (mime.contains(MIME_JPEG)) {			
			this.originalType=EntryType.JPG;
			this.optimizationType=OptimizationType.LOOSY;
		} else if (mime.contains(MIME_PNG)) {
			this.originalType=EntryType.PNG;
			this.optimizationType=OptimizationType.LOSSLESS;
		} else if (mime.contains(MIME_GIF)) {
			this.originalType=EntryType.GIF;
			this.optimizationType=OptimizationType.LOSSLESS;
		} else {
			this.originalType=EntryType.UNKNOW;
		}		
		
		this.originalSize=size;
		this.originalName=DLOUtil.normalize(name);		
	}
	
	public DLOEntry(FileEntry originalEntry) throws PortalException {
		super();
		this.originalEntry =  originalEntry;
		this.originalEntryStream = originalEntry.getContentStream();
		
		if (originalEntry.getMimeType().contains(MIME_JPEG)) {			
			this.originalType=EntryType.JPG;
			this.optimizationType=OptimizationType.LOOSY;
		} else if (originalEntry.getMimeType().contains(MIME_PNG)) {
			this.originalType=EntryType.PNG;
			this.optimizationType=OptimizationType.LOSSLESS;
		} else if (originalEntry.getMimeType().contains(MIME_GIF)) {
			this.originalType=EntryType.GIF;
			this.optimizationType=OptimizationType.LOSSLESS;
		} else {
			this.originalType=EntryType.UNKNOW;
		}		
		
		this.originalSize=originalEntry.getSize();
		this.originalName=DLOUtil.normalize(originalEntry.getTitle());
	}	
	
//	public FileEntry getOriginalEntry() {
//		return originalEntry;
//	}
	
	public String getOriginalEntryURL(ThemeDisplay themeDisplay){
		return DLOUtil.getImageURL(this.originalEntry.getFileEntryId(), themeDisplay);		
	}

	public void setOriginalEntry(FileEntry originalEntry) {
		this.originalEntry = originalEntry;
	}

	public File getOptimizedEntry() {
		return optimizedEntry;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getOptimizedEntryBase64() {
		try {
			return Base64.encode(FileUtils.readFileToByteArray(this.optimizedEntry));
		} catch (IOException e) {
			return "";
		}
	}	

	public void setOptimizedEntry(File optimizedEntry) {
		this.optimizedEntry = optimizedEntry;
		this.optimizedSize=optimizedEntry.length();
	}

	public EntryType getOriginalType() {
		return originalType;
	}

	public void setOriginalType(EntryType originalType) {
		this.originalType = originalType;
	}

	public EntryType getOptimizedType() {
		return optimizedType;
	}

	public void setOptimizedType(EntryType optimizedType) {
		this.optimizedType = optimizedType;
	}

	public OptimizationType getOptimizationType() {
		return optimizationType;
	}

	public void setOptimizationType(OptimizationType optimizationType) {
		this.optimizationType = optimizationType;
	}

	public float getQualityFactorUsed() {
		return qualityFactorUsed;
	}

	public void setQualityFactorUsed(float qualityFactorUsed) {
		this.qualityFactorUsed = qualityFactorUsed;
	}

	public double getVariation() {
		return variation;
	}

	public void setVariation(double variation) {
		this.variation = variation;
	}

	public long getOriginalSize() {
		return originalSize;
	}

	public void setOriginalSize(long originalSize) {
		this.originalSize = originalSize;
	}

	public long getOptimizedSize() {
		return optimizedSize;
	}

	public void setOptimizedSize(long optimizedSize) {
		this.optimizedSize = optimizedSize;
	}

	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}
	
	public InputStream getOriginalEntryStream() {
		return originalEntryStream;
	}

	public void setOriginalEntryStream(InputStream originalEntryStream) {
		this.originalEntryStream = originalEntryStream;
	}

	@Override
	public String toString() {
		return "DLOEntry [originalName=" + originalName + ", originalEntry=" + originalEntry + ", optimizedEntry="
				+ optimizedEntry.getAbsolutePath() + ", originalType=" + originalType + ", optimizedType=" + optimizedType
				+ ", originalSize=" + originalSize + ", optimizedSize=" + optimizedSize + ", optimizationType="
				+ optimizationType + ", qualityFactorUsed=" + qualityFactorUsed + ", variation=" + variation + "]";
	}	
	
}
