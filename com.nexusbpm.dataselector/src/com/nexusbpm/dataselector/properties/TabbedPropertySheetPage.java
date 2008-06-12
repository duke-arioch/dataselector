/*******************************************************************************
 * Copyright (c) 2001, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.nexusbpm.dataselector.properties;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.views.properties.tabbed.view.Tab;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabDescriptor;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyComposite;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyRegistry;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyViewer;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

/**
 * Extends the tabbed property sheet page to not call setMinSize on the
 * ScrolledComposite that contains the contents of the property tab.
 */
@SuppressWarnings({"restriction", "unchecked"})
public class TabbedPropertySheetPage
	extends org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage
	implements IPropertySheetPage, ILabelProviderListener {
    
    private List<DisposeListener> listeners = new ArrayList<DisposeListener>();
    
	/**
	 * Label provider for the ListViewer.
	 */
	protected class TabbedPropertySheetPageLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (element instanceof TabDescriptor) {
				return ((TabDescriptor) element).getLabel();
			}
			return null;
		}
	}
	
	/**
	 * SelectionChangedListener for the ListViewer.
	 */
	protected class SelectionChangedListener implements ISelectionChangedListener {
		/**
		 * Shows the tab associated with the selection.
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			Tab tab = null;
			TabDescriptor descriptor = (TabDescriptor) selection.getFirstElement();
			
			if (descriptor == null) {
				// pretend the tab is empty.
//				hideTab(currentTab);
			    hideTab(getCurrentTab());
			} else {
				// create tab if necessary
				// can not cache based on the id - tabs may have the same id,
				// but different section depending on the selection
//				tab = (Tab) descriptorToTab.get(descriptor);
			    tab = (Tab) ((Map) getField("descriptorToTab")).get(descriptor);

//				if (tab != currentTab) {
//					hideTab(currentTab);
//				}
			    Object currentTab = getCurrentTab();
			    if(tab != currentTab) {
			        hideTab((Tab) currentTab);
			    }
			    
//				Composite tabComposite = (Composite) tabToComposite.get(tab);
			    Composite tabComposite = (Composite) ((Map) getField("tabToComposite")).get(tab);
				if (tabComposite == null) {
//					tabComposite = createTabComposite();
				    tabComposite = (Composite) executeMethod("createTabComposite", new Class[] {}, null);
					tab.createControls(tabComposite, TabbedPropertySheetPage.this);
					// tabAreaComposite.layout(true);
//					tabToComposite.put(tab, tabComposite);
					((Map) getField("tabToComposite")).put(tab, tabComposite);
				}
				// force widgets to be resized
//				tab.setInput(tabbedPropertyViewer.getWorkbenchPart(),
//					(ISelection) tabbedPropertyViewer.getInput());
				tab.setInput(
				        ((TabbedPropertyViewer) getField("tabbedPropertyViewer")).getWorkbenchPart(),
				        (ISelection) ((TabbedPropertyViewer) getField("tabbedPropertyViewer")).getInput());
				
				// store tab selection
//				storeCurrentTabSelection(descriptor.getLabel());
				executeMethod(
				        "storeCurrentTabSelection",
				        new Class[] {String.class},
				        new Object[] {descriptor.getLabel()});
				
//				if (tab != currentTab) {
//					showTab(tab);
//				}
				currentTab = getCurrentTab();
				if(tab != currentTab) {
				    showTab(tab);
				}
				
				tab.refresh();
			}
//			tabbedPropertyComposite.getTabComposite().layout(true);
			((TabbedPropertyComposite)getField("tabbedPropertyComposite")).getTabComposite().layout(true);
//			currentTab = tab;
			setField("currentTab", tab);
			// don't call resizeScrolledComposite() because it causes the contents to constantly grow
//			resizeScrolledComposite();

			if (descriptor != null) {
//				handleTabSelection(descriptor);
			    executeMethod("handleTabSelection", new Class[] {TabDescriptor.class}, new Object[] {descriptor});
			}
		}
		
		/**
		 * Shows the given tab.
		 */
		private void showTab(Tab target) {
			if (target != null) {
			    Composite tabComposite = (Composite) ((Map) getField("tabToComposite")).get(target);
//				Composite tabComposite = (Composite) tabToComposite.get(target);
				if (tabComposite != null) {
					/**
					 * the following method call order is important - do not
					 * change it or the widgets might be drawn incorrectly
					 */
					tabComposite.moveAbove(null);
					target.aboutToBeShown();
					tabComposite.setVisible(true);
				}
			}
		}
		
		/**
		 * Hides the given tab.
		 */
		private void hideTab(Tab target) {
			if (target != null) {
//				Composite tabComposite = (Composite) tabToComposite.get(target);
			    Composite tabComposite = (Composite) ((Map) getField("tabToComposite")).get(target);
				if (tabComposite != null) {
					target.aboutToBeHidden();
					tabComposite.setVisible(false);
				}
			}
		}
	}
	
	/**
	 * create a new tabbed property sheet page.
	 * 
	 * @param tabbedPropertySheetPageContributor
	 *            the tabbed property sheet page contributor.
	 */
	public TabbedPropertySheetPage(
			ITabbedPropertySheetPageContributor tabbedPropertySheetPageContributor) {
	    super(tabbedPropertySheetPageContributor);
//		contributor = tabbedPropertySheetPageContributor;
//		tabToComposite = new HashMap();
//		selectionQueue = new ArrayList(10);
//		tabSelectionListeners = new ArrayList();
//		initContributor(contributor.getContributorId());
	}
	
	/**
	 * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
//		widgetFactory = new TabbedPropertySheetWidgetFactory();
	    TabbedPropertySheetWidgetFactory widgetFactory = new TabbedPropertySheetWidgetFactory();
	    setField("widgetFactory", widgetFactory);
//		tabbedPropertyComposite = new TabbedPropertyComposite(parent,
//			widgetFactory, hasTitleBar);
	    TabbedPropertyComposite tabbedPropertyComposite =
	        new TabbedPropertyComposite(parent, widgetFactory, ((Boolean) getField("hasTitleBar")).booleanValue());
	    setField("tabbedPropertyComposite", tabbedPropertyComposite);
		widgetFactory.paintBordersFor(tabbedPropertyComposite);
		tabbedPropertyComposite.setLayout(new FormLayout());
		FormData formData = new FormData();
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, 0);
		formData.top = new FormAttachment(0, 0);
		formData.bottom = new FormAttachment(100, 0);
		tabbedPropertyComposite.setLayoutData(formData);

//		tabbedPropertyViewer = new TabbedPropertyViewer(tabbedPropertyComposite.getList());
		TabbedPropertyViewer tabbedPropertyViewer = new TabbedPropertyViewer(tabbedPropertyComposite.getList());
		setField("tabbedPropertyViewer", tabbedPropertyViewer);
		tabbedPropertyViewer.setContentProvider(tabListContentProvider);
		tabbedPropertyViewer.setLabelProvider(new TabbedPropertySheetPageLabelProvider());
		tabbedPropertyViewer.addSelectionChangedListener(new SelectionChangedListener());

		/**
		 * Add a part activation listener.
		 */
//		cachedWorkbenchWindow = getSite().getWorkbenchWindow();
		IWorkbenchWindow cachedWorkbenchWindow = getSite().getWorkbenchWindow();
		setField("cachedWorkbenchWindow", cachedWorkbenchWindow);
//		cachedWorkbenchWindow.getPartService().addPartListener(partActivationListener);
		cachedWorkbenchWindow.getPartService().addPartListener((IPartListener) getField("partActivationListener"));

		/**
		 * Add a label provider change listener.
		 */
//		if (hasTitleBar) {
//			registry.getLabelProvider().addListener(this);
//		}
		if(((Boolean) getField("hasTitleBar")).booleanValue()) {
		    ((TabbedPropertyRegistry) getField("registry")).getLabelProvider().addListener(this);
		}
	}
	
	@Override
	public void dispose() {
	    super.dispose();
	    if(listeners.size() > 0) {
	        Event e = new Event();
	        e.widget = getControl();
    	    DisposeEvent event = new DisposeEvent(e);
    	    event.time = (int) System.currentTimeMillis();
    	    event.widget = getControl();
    	    event.data = this;
    	    for(DisposeListener listener : listeners.toArray(new DisposeListener[listeners.size()])) {
    	        listener.widgetDisposed(event);
    	    }
	    }
	}
	
	public void addDisposeListener(DisposeListener listener) {
	    if(!listeners.contains(listener)) {
	        listeners.add(listener);
	    }
	}
	
	public void removeDisposeListener(DisposeListener listener) {
	    listeners.remove(listener);
	}
	
    @SuppressWarnings("all")
    protected Object executeMethod(String methodName, Class[] paramTypes, Object[] params) {
        Class c = getClass();
        while(c != null) {
            try {
                Method m = c.getDeclaredMethod(methodName, paramTypes);
                if(m != null) {
                    m.setAccessible(true);
                    return m.invoke(this, params);
                }
            } catch(NoSuchMethodException e) {
            } catch(Exception e) {
                e.printStackTrace();
            }
            c = c.getSuperclass();
        }
        return null;
    }
    
    @SuppressWarnings("all")
    protected void setField(String fieldName, Object value) {
        Class c = getClass();
        while(c != null) {
            try {
                Field f = c.getDeclaredField(fieldName);
                if(f != null) {
                    f.setAccessible(true);
                    f.set(this, value);
                    return;
                }
            } catch(NoSuchFieldException e) {
            } catch(Exception e) {
                e.printStackTrace();
            }
            c = c.getSuperclass();
        }
    }
    
    @SuppressWarnings("all")
    protected Object getField(String fieldName) {
        Class c = getClass();
        while(c != null) {
            try {
                Field f = c.getDeclaredField(fieldName);
                if(f != null) {
                    f.setAccessible(true);
                    return f.get(this);
                }
            } catch(NoSuchFieldException e) {
            } catch(Exception e) {
                e.printStackTrace();
            }
            c = c.getSuperclass();
        }
        new IllegalArgumentException(new NoSuchFieldException(fieldName)).printStackTrace();
        return null;
    }
}