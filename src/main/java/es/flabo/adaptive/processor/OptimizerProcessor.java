package es.flabo.adaptive.processor;

import com.liferay.adaptive.media.image.configuration.AMImageConfigurationEntry;
import com.liferay.adaptive.media.image.configuration.AMImageConfigurationHelper;
import com.liferay.adaptive.media.image.mime.type.AMImageMimeTypeProvider;
import com.liferay.adaptive.media.image.processor.AMImageProcessor;
import com.liferay.adaptive.media.image.scaler.AMImageScalerTracker;
import com.liferay.adaptive.media.image.service.AMImageEntryLocalService;
import com.liferay.adaptive.media.processor.AMProcessor;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.File;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
	private static final String BLANK = "";	
	
	@Override
	public void process(FileVersion fileVersion, String configurationEntryUuid) throws PortalException {		
		
		LOGGER.debug("Updating entry with optimized version.");

		
	}

	@Override
	public void cleanUp(FileVersion fileVersion) throws PortalException {
		LOGGER.debug("Cleanup");
		
	}

	@Override
	public void process(FileVersion fileVersion) throws PortalException {
		LOGGER.debug("Processing image "+ fileVersion.getFileName() +" to optmize it!!");		

		
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
