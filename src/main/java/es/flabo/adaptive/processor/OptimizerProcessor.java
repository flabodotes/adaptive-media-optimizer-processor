package es.flabo.adaptive.processor;

import com.liferay.adaptive.media.exception.AMRuntimeException;
import com.liferay.adaptive.media.image.configuration.AMImageConfigurationEntry;
import com.liferay.adaptive.media.image.configuration.AMImageConfigurationHelper;
import com.liferay.adaptive.media.image.mime.type.AMImageMimeTypeProvider;
import com.liferay.adaptive.media.image.model.AMImageEntry;
import com.liferay.adaptive.media.image.processor.AMImageProcessor;
import com.liferay.adaptive.media.image.scaler.AMImageScaledImage;
import com.liferay.adaptive.media.image.scaler.AMImageScaler;
import com.liferay.adaptive.media.image.scaler.AMImageScalerTracker;
import com.liferay.adaptive.media.image.service.AMImageEntryLocalService;
import com.liferay.adaptive.media.processor.AMProcessor;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import es.flabo.liferay.hooks.dlo.model.DLOEntry;
import es.flabo.liferay.hooks.dlo.utils.DLOUtil;

/**
* @author Felix Gonzalez
*/
@Component(
		immediate = true,
		property = "model.class.name=com.liferay.portal.kernel.repository.model.FileVersion",
		service = {AMImageProcessor.class, AMProcessor.class}
	)
public class OptimizerProcessor implements AMImageProcessor {

	private static final Log LOGGER = LogFactoryUtil.getLog(OptimizerProcessor.class);
	
	@Override
	public void process(FileVersion fileVersion, String configurationEntryUuid) throws PortalException {		
		
		if (!_amImageMimeTypeProvider.isMimeTypeSupported(fileVersion.getMimeType())) {
			LOGGER.info("Mime Type unsupported");
			return;
		}

		Optional<AMImageConfigurationEntry> amImageConfigurationEntryOptional =
			_amImageConfigurationHelper.getAMImageConfigurationEntry(
				fileVersion.getCompanyId(), configurationEntryUuid);

		if (!amImageConfigurationEntryOptional.isPresent()) {
			LOGGER.error("Configuration not found");
			return;
		}		

		AMImageConfigurationEntry amImageConfigurationEntry = amImageConfigurationEntryOptional.get();

		LOGGER.debug("Using config:"+amImageConfigurationEntry.getName());
		
		AMImageEntry amImageEntry = _amImageEntryLocalService.fetchAMImageEntry(
			amImageConfigurationEntry.getUUID(),
			fileVersion.getFileVersionId());

		try {

			if (amImageEntry != null){				
				LOGGER.debug("Removing existing AM Entry");
				_amImageEntryLocalService.deleteAMImageEntry(amImageEntry.getAmImageEntryId());
			}

			AMImageScaler amImageScaler = _amImageScalerTracker.getAMImageScaler(fileVersion.getMimeType());

			if (amImageScaler == null) {
				LOGGER.error("Scaler not found");
				return;
			}else{
				LOGGER.debug("Using scaler "+amImageScaler.toString());
			}			
			
			//First scale the image
			AMImageScaledImage amImageScaledImage = amImageScaler.scaleImage(fileVersion, amImageConfigurationEntry);
			InputStream amImageScaledImageStream = amImageScaledImage.getInputStream();
			
			//Second compress the scaled image
			DLOEntry entry=new DLOEntry(amImageScaledImageStream,fileVersion.getMimeType(),fileVersion.getSize(),fileVersion.getFileName());
			DLOEntry compressedImage = DLOUtil.processImage(entry);
			File compressedFile =  compressedImage.getOptimizedEntry();
			
			LOGGER.debug("FileVersion size: "+fileVersion.getSize() +
					"bytes ScaledImage size:"+amImageScaledImage.getSize()+
					"bytes Compressed size:"+compressedImage.getOptimizedSize());
			
			InputStream compressedFileStream = new FileInputStream(compressedFile);

			_amImageEntryLocalService.addAMImageEntry(
				amImageConfigurationEntry, fileVersion,
				amImageScaledImage.getHeight(),
				amImageScaledImage.getWidth(), compressedFileStream,
				amImageScaledImage.getSize());
			try{
				compressedFileStream.close();
				compressedFile.delete();
				entry.getOriginalEntryStream().close();
				compressedImage.getOriginalEntryStream().close();
			}catch (Exception ex){
				
			}
		}
		catch (PortalException | IOException e) {
			throw new AMRuntimeException.IOException(e);		
		}		
	}

	@Override
	public void cleanUp(FileVersion fileVersion) throws PortalException {
		LOGGER.debug("Cleanup");
		
	}

	@Override
	public void process(FileVersion fileVersion) throws PortalException {
		LOGGER.debug("Processing image "+ fileVersion.getFileName() +" to optmize it!!");		

		
		Iterable<AMImageConfigurationEntry> amImageConfigurationEntries =
				_amImageConfigurationHelper.getAMImageConfigurationEntries(
					fileVersion.getCompanyId());

			
		for (AMImageConfigurationEntry config : amImageConfigurationEntries){
			try {
				process(fileVersion, config.getUUID());
			} catch (PortalException e) {
				LOGGER.error("Exception processing image with config "+config.getName());
			}
		}
				
	}

	@Reference(unbind = "-")
	public void setAMImageConfigurationHelper(
		AMImageConfigurationHelper amImageConfigurationHelper) {

		_amImageConfigurationHelper = amImageConfigurationHelper;
	}

	@Reference(unbind = "-")
	public void setAMImageEntryLocalService(
		AMImageEntryLocalService amImageEntryLocalService) {

		_amImageEntryLocalService = amImageEntryLocalService;
	}

	@Reference(unbind = "-")
	public void setAMImageMimeTypeProvider(
		AMImageMimeTypeProvider amImageMimeTypeProvider) {

		_amImageMimeTypeProvider = amImageMimeTypeProvider;
	}

	@Reference(unbind = "-")
	public void setAMImageScalerTracker(
		AMImageScalerTracker amImageScalerTracker) {

		_amImageScalerTracker = amImageScalerTracker;
	}

	private AMImageConfigurationHelper _amImageConfigurationHelper;
	private AMImageEntryLocalService _amImageEntryLocalService;
	private AMImageMimeTypeProvider _amImageMimeTypeProvider;
	private AMImageScalerTracker _amImageScalerTracker;
 
}
