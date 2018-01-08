package es.flabo.liferay.hooks.dlo.controller;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.struts.BaseStrutsPortletAction;
import com.liferay.portal.kernel.struts.StrutsPortletAction;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import es.flabo.liferay.hooks.dlo.model.DLOEntry;
import es.flabo.liferay.hooks.dlo.utils.DLOUtil;

import java.io.File;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

public class DLOHookAction extends BaseStrutsPortletAction {
	
	private static final String PARAM_SHOW_VARIATION_INFO = "showVariationInfo";
	private static final String PARAM_VARIATION = "variation";
	private static final String PARAM_IMAGE_OPTIMIZED_BASE64 = "imageOptimizedBase64";
	private static final String PARAM_IMAGE_ORIGINAL_URL = "imageOriginalURL";
	private static final String PARAM_MIME_TYPE_OPTIMIZED = "mimeTypeOptimized";
	private static final String PARAM_MIME_TYPE_ORIGINAL = "mimeTypeOriginal";
	private static final String PARAM_SIZE_OPTIMIZED = "sizeOptimized";
	private static final String PARAM_SIZE_ORIGINAL = "sizeOriginal";
	private static final String PARAM_OPTIMIZATION_TYPE = "optimizationType";
	private static final String PARAM_OPTIMIZED_PATH = "imageOptimizedPath";
	private static final String PARAM_FILE_ENTRY_ID = "fileEntryId";
	private static final String PARAM_REDIRECT = "redirect";
	
	private static final String CHANGE_LOG = "DLO";
	private static final String BLANK = "";	
	private static final String VIEW_JSP = "/portlet/dlo/view.jsp";
	private static final String ERROR_JSP = "/portlet/dlo/error.jsp";
	
	private static final Log LOGGER = LogFactoryUtil.getLog(DLOHookAction.class);

	public DLOHookAction() {
	}

	@Override
	public void processAction(StrutsPortletAction originalStrutsPortletAction, PortletConfig portletConfig,
			ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
		LOGGER.debug("Updating entry with optimized version.");
		try {
			Long fileEntryId = ParamUtil.getLong(actionRequest, PARAM_FILE_ENTRY_ID,0);
			String imageOptimizedPath = ParamUtil.getString(actionRequest, PARAM_OPTIMIZED_PATH,BLANK);
			
			if (fileEntryId!=0 && imageOptimizedPath!=BLANK){
				DLFileEntry imageOriginal = DLOUtil.getImage(Long.valueOf(fileEntryId));
	
				ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
				long userId = themeDisplay.getRealUserId();
				ServiceContext serviceContext = ServiceContextThreadLocal.getServiceContext();
	
				FileEntry entryUpdated = null;
				File file= new File(imageOptimizedPath);
				if (!file.exists()){
					SessionErrors.add(actionRequest, "dlo-cant-update");
				}else{					
					if (userId != 0 && imageOriginal != null && serviceContext != null) {
						entryUpdated = DLAppLocalServiceUtil.updateFileEntry(userId, fileEntryId, null, null, null,
								imageOriginal.getDescription(), CHANGE_LOG, true, file, serviceContext);				
					}
				}
	
				if (entryUpdated != null) {
					file.delete();//Remove from disk
					SessionMessages.add(actionRequest, "dlo-success");
					//String redirect = ParamUtil.getString(actionRequest, PARAM_REDIRECT,"/");
					//actionResponse.sendRedirect(redirect);//devolvemos al usuario al punto donde comenzo
				} else {
					SessionErrors.add(actionRequest, "dlo-cant-update");
				}
			}else{
				SessionErrors.add(actionRequest, "dlo-no-info");
			}		
		} catch (Exception ex) {
			SessionErrors.add(actionRequest, "dlo-exception");
			LOGGER.error(ex);
			LOGGER.error("Error saving optimized entry.");
		}

		LOGGER.debug("Optimization completed");
	}

	@Override
	public String render(StrutsPortletAction originalStrutsPortletAction, PortletConfig portletConfig,
			RenderRequest renderRequest, RenderResponse renderResponse) throws Exception {
		LOGGER.debug("Processing entry for optimization.");
		String result = ERROR_JSP;
		try {
			ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);			
			String redirect = ParamUtil.getString(renderRequest, PARAM_REDIRECT, BLANK);
			String fileEntryId = ParamUtil.getString(renderRequest,PARAM_FILE_ENTRY_ID,null);
			
			if (fileEntryId==null){
				result=ERROR_JSP;
			}else{
				DLFileEntry originalImage = DLOUtil.getImage(Long.valueOf(fileEntryId));
				if (originalImage==null){
					result=ERROR_JSP;
				}else{
//					DLOEntry entry=new DLOEntry(originalImage);
//					boolean processResult = DLOUtil.processImage(entry);
//					LOGGER.debug(entry);
//					
//					if (!processResult || entry==null || entry.getOptimizedEntry()==null){
//						result=ERROR_JSP;
//					}else{
//						renderRequest.setAttribute(PARAM_OPTIMIZATION_TYPE, entry.getOptimizationType().getValue());
//						
//						renderRequest.setAttribute(PARAM_SIZE_ORIGINAL, entry.getOriginalSize());
//						renderRequest.setAttribute(PARAM_SIZE_OPTIMIZED, entry.getOptimizedSize());
//						
//						renderRequest.setAttribute(PARAM_MIME_TYPE_ORIGINAL, entry.getOriginalType().getValue());
//						renderRequest.setAttribute(PARAM_MIME_TYPE_OPTIMIZED, entry.getOptimizedType().getValue());
//						
//						renderRequest.setAttribute(PARAM_REDIRECT, redirect);
//						renderRequest.setAttribute(PARAM_FILE_ENTRY_ID, fileEntryId);
//						
//						renderRequest.setAttribute(PARAM_IMAGE_ORIGINAL_URL, entry.getOriginalEntryURL(themeDisplay));
//						renderRequest.setAttribute(PARAM_IMAGE_OPTIMIZED_BASE64, entry.getOptimizedEntryBase64());
//						
//						renderRequest.setAttribute(PARAM_OPTIMIZED_PATH, entry.getOptimizedEntry().getAbsolutePath());
//						renderRequest.setAttribute(PARAM_VARIATION, entry.getVariation());
//			
//						boolean showVariationInfo = false;
//						if (entry.getVariation() >= DLOUtil.VARIATION_LIMIT_ADVERTISEMENT) {
//							showVariationInfo = true;
//						}
//						renderRequest.setAttribute(PARAM_SHOW_VARIATION_INFO, showVariationInfo);		
//						result=VIEW_JSP;
//					}
				}
			}
		}catch (OutOfMemoryError error){
			LOGGER.error("Not enought memory for process image.");
			result=ERROR_JSP;
			SessionErrors.add(renderRequest, "dlo-memory");
		} catch (Exception ex) {
			LOGGER.error(ex);
			LOGGER.error("Error processing entry/image.");			
			result=ERROR_JSP;
			SessionErrors.add(renderRequest, "dlo-exception");
		}
		return result;
	}
}
