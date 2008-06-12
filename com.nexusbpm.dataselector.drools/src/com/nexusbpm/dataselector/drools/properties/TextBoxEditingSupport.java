package com.nexusbpm.dataselector.drools.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

public abstract class TextBoxEditingSupport extends EditingSupport {
    private TextBoxCellEditor editor;
    private FormToolkit toolkit;
    
    public TextBoxEditingSupport(ColumnViewer viewer, FormToolkit toolkit) {
        super(viewer);
        this.toolkit = toolkit;
    }
    
    @Override
    protected boolean canEdit(Object element) {
        return true;
    }
    
    protected abstract String[] getNames();
    
    @Override
    protected CellEditor getCellEditor(Object element) {
        editor = new TextBoxCellEditor((Composite) getViewer().getControl());
        return editor;
    }
    
    protected class TextBoxCellEditor extends CellEditor implements KeyListener, ICompletionListener, FocusListener {
        private AssistedTextViewer viewer;
        private ContentAssistant ca;
        
        private boolean focusLost;
        private boolean assisting;
        private long lastAssistance;
        
        protected TextBoxCellEditor(Composite parent) {
            super(parent);
        }
        protected Control createControl(Composite parent) {
            viewer = new AssistedTextViewer(parent, SWT.SINGLE);
            viewer.setDocument(new Document());
            ca = new ContentAssistant();
            ca.setContentAssistProcessor(new CompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
            ca.setProposalSelectorBackground(toolkit.getColors().getBackground());
            ca.addCompletionListener(this);
            ca.install(viewer);
            toolkit.adapt(viewer.getControl(), false, false);
            viewer.getTextWidget().addKeyListener(this);
            return viewer.getControl();
        }
        public void assistSessionEnded(ContentAssistEvent event) {
            lastAssistance = System.currentTimeMillis();
            assisting = false;
            if(focusLost) {
                setFocus();
            }
            viewer.getTextWidget().reattachFocusListeners(this);
        }
        public void assistSessionStarted(ContentAssistEvent event) {
            assisting = true;
            focusLost = false;
            viewer.getTextWidget().detachFocusListeners(this);
        }
        public void focusGained(FocusEvent e) {
            focusLost = false;
        }
        public void focusLost(FocusEvent e) {
            focusLost = true;
        }
        public void selectionChanged(ICompletionProposal proposal, boolean smartToggle) {
        }
        public void keyPressed(KeyEvent e) {
        }
        public void keyReleased(KeyEvent e) {
            boolean assisting = this.assisting || lastAssistance + 125 > System.currentTimeMillis();
            if(e.keyCode == 27 && !assisting) {
                fireCancelEditor();
                deactivate();
            } else if(e.keyCode == 13 && !assisting){
                fireApplyEditorValue();
                deactivate();
            } else if(e.keyCode == 32 && e.stateMask == SWT.CTRL) {
                // content assist on ctrl-space
                ca.showPossibleCompletions();
            } else if(e.character == '{') {
                // content assist on ${ but not on $${
                StyledText text = viewer.getTextWidget();
                Point p = text.getSelection();
                if(p.x == p.y && p.x >= 2 && text.getTextRange(p.x - 2, 2).equals("${") &&
                        !(p.x >= 3 && text.getTextRange(p.x - 3, 3).equals("$${"))) {
                    ca.showPossibleCompletions();
                }
            }
        }
        protected Object doGetValue() {
            return viewer.getTextWidget().getText();
        }
        protected void doSetFocus() {
            viewer.getTextWidget().setFocus();
            viewer.getTextWidget().selectAll();
        }
        protected void doSetValue(Object value) {
            viewer.getTextWidget().setText(value == null ? "" : value.toString());
        }
    }
    
    protected class CompletionProcessor implements IContentAssistProcessor {
        public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
            String[] names = getNames();
            if(names == null) {
                return new ICompletionProposal[0];
            }
            StyledText text = viewer.getTextWidget();
            Point p = text.getSelection();
            boolean includePrefix = true;
            if(p.x == p.y && p.x >= 2 && text.getTextRange(p.x - 2, 2).equals("${") &&
                    !(p.x >= 3 && text.getTextRange(p.x - 3, 3).equals("$${"))) {
                includePrefix = false;
            }
            ICompletionProposal[] proposals = new ICompletionProposal[names.length];
            for(int index = 0; index < names.length; index++) {
                p = viewer.getSelectedRange();
                if(includePrefix) {
                    proposals[index] = new CompletionProposal(
                            "${" + names[index] + "}", p.x, p.y, names[index].length() + 3, null, names[index], null, null);
                } else {
                    proposals[index] = new CompletionProposal(
                            names[index] + "}", p.x, p.y, names[index].length() + 1, null, names[index], null, null);
                }
            }
            return proposals;
        }
        public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
            return new IContextInformation[0];
        }
        public char[] getCompletionProposalAutoActivationCharacters() {
            return null;
        }
        public char[] getContextInformationAutoActivationCharacters() {
            return null;
        }
        public IContextInformationValidator getContextInformationValidator() {
            return null;
        }
        public String getErrorMessage() {
            return null;
        }
    }
    /**
     * @see AssistedStyledText
     */
    protected static class AssistedTextViewer extends TextViewer {
        public AssistedTextViewer(Composite parent, int styles) {
            super(parent, styles);
        }
        @Override
        protected StyledText createTextWidget(Composite parent, int styles) {
            return new AssistedStyledText(parent, styles);
        }
        @Override
        public AssistedStyledText getTextWidget() {
            return (AssistedStyledText) super.getTextWidget();
        }
    }
    /**
     * This class keeps track of the focus listeners that are added, so that at any point
     * the normal focus listeners can be switched out for a replacement focus listener, and
     * then the normal focus listeners can be switched back in later.
     * 
     * The purpose of this is to allow the focus to be transferred to the content assistance
     * pop-up window without the normal focus listeners thinking the focus has been lost.
     */
    protected static class AssistedStyledText extends StyledText {
        private List<FocusListener> listeners;
        private boolean detached;
        public AssistedStyledText(Composite parent, int style) {
            super(parent, style);
            listeners = new ArrayList<FocusListener>();
        }
        @Override
        public void addFocusListener(FocusListener listener) {
            listeners.add(listener);
            if(!detached) {
                super.addFocusListener(listener);
            }
        }
        @Override
        public void removeFocusListener(FocusListener listener) {
            listeners.remove(listener);
            super.removeFocusListener(listener);
        }
        public void detachFocusListeners(FocusListener replacement) {
            detached = true;
            super.addFocusListener(replacement);
            for(FocusListener l : listeners) {
                super.removeFocusListener(l);
            }
        }
        public void reattachFocusListeners(FocusListener replacement) {
            detached = false;
            for(FocusListener l : listeners) {
                super.addFocusListener(l);
            }
            super.removeFocusListener(replacement);
        }
        /* The mouse listener added in the ColumnViewerEditor specifically listens for quick
         * mouse double-clicks after the editor is activated and cancels editing. The result
         * is that you cannot click within the text box until a few seconds after the editor
         * is activated. This is counter-intuitive, so we just don't add that listener.
         */
        @Override
        public void addMouseListener(MouseListener listener) {
            if(!listener.getClass().getName().startsWith("org.eclipse.jface.viewers.ColumnViewerEditor$")) {
                super.addMouseListener(listener);
            }
        }
    }
}
