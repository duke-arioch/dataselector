package com.nexusbpm.dataselector;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PerspectiveFactory implements IPerspectiveFactory {
    public static final String NAVIGATOR = IPageLayout.ID_RES_NAV;
    public static final String PROPERTIES = IPageLayout.ID_PROP_SHEET;
    
    public void createInitialLayout(IPageLayout layout) {
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
        
        String editorArea = layout.getEditorArea();
        
        IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, 0.25f, editorArea);
        IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.70f, editorArea);
        
        left.addView(NAVIGATOR);
        
        bottom.addView(PROPERTIES);
    }
}
