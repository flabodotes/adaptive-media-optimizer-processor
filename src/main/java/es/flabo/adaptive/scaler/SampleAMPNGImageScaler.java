package es.flabo.adaptive.scaler;

import com.liferay.adaptive.media.image.configuration.AMImageConfigurationEntry;
import com.liferay.adaptive.media.image.scaler.AMImageScaledImage;
import com.liferay.adaptive.media.image.scaler.AMImageScaler;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.util.GetterUtil;

import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
* @author Felix Gonzalez
*/
@Component(
  immediate = true, property = {"mime.type=image/png"},
  service = {AMImageScaler.class}
)
public class SampleAMPNGImageScaler implements AMImageScaler {

  @Override
  public boolean isEnabled() {
     return false;
  }

  @Override
  public AMImageScaledImage scaleImage(
     FileVersion fileVersion,
     AMImageConfigurationEntry amImageConfigurationEntry) {

	  System.out.println(" SAMPLEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
	  
     Map<String, String> properties =
        amImageConfigurationEntry.getProperties();

     int maxHeight = GetterUtil.getInteger(properties.get("max-height"));
     int maxWidth = GetterUtil.getInteger(properties.get("max-width"));


     // Custom code here
    
     return null;
  }
 
}
