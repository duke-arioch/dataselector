package com.nexusbpm.dataselector.figures;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.SWT;

import com.nexusbpm.dataselector.util.ColorCache;
import com.nexusbpm.dataselector.util.FontCache;

public class StatisticFigure extends org.eclipse.draw2d.Figure {
    private ColorCache colorCache;
    private FontCache fontCache;
    
    Map<String, Label[]> columns = new HashMap<String, Label[]>();
    Label predictorLabel = new Label();
    
    public StatisticFigure(String predictor, ColorCache colorCache, FontCache fontCache) {
        super();
        this.colorCache = colorCache;
        this.fontCache = fontCache;
        GridLayout gl = new GridLayout(2, false);
        gl.marginWidth = -1;
        gl.marginHeight = -1;
        gl.horizontalSpacing = -1;
        gl.verticalSpacing = -1;
        setLayoutManager(gl);
        Label label = new Label("");
        label.setBorder(bfact());
        label.setBackgroundColor(colorCache.getColor(SelectorNodeFigure.COLOR_POSTIT_SUPER_SHADE));
        label.setOpaque(true);
        add(label, new GridData(SWT.FILL, SWT.FILL, false, false));
        predictorLabel = new Label(predictor);
//        predictorLabel.setText(predictor);
        predictorLabel.setBorder(bfact());
        predictorLabel.setBackgroundColor(colorCache.getColor(SelectorNodeFigure.COLOR_POSTIT_SHADE));
        predictorLabel.setLabelAlignment(PositionConstants.CENTER);
        predictorLabel.setForegroundColor(colorCache.getColor(SelectorNodeFigure.COLOR_BLACK));
        predictorLabel.setOpaque(true);
        predictorLabel.setFont(fontCache.getFont(8, SWT.BOLD));
        GridData gd =  new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.horizontalSpan = 1;
        add(predictorLabel, gd);
        setOpaque(true);
        setBackgroundColor(colorCache.getColor(SelectorNodeFigure.COLOR_POSTIT_SUPER_SHADE));
        setBorder(new CompartmentFigureBorder());
    }
    
    public Border bfact() {
        return new LineBorder(1) {
            public Insets getInsets(IFigure figure) {
                return new Insets(2,4,2,4);
            }
        };
    }
    
    public void setNV(String name, String value) {
        Label[] label = columns.get(name);
        if (label != null) {
            label[0].setText(name);
            label[1].setText(value);
        } else {
            Label label1 = new Label(name);
            label1.setFont(fontCache.getFont(8, SWT.BOLD));
            label1.setBackgroundColor(colorCache.getColor(SelectorNodeFigure.COLOR_POSTIT_SHADE));
            label1.setOpaque(true);
            Label label2 =  new Label(value);
            label1.setBorder(bfact());
            label2.setBorder(bfact());
            label1.setLabelAlignment(PositionConstants.LEFT);
            label2.setLabelAlignment(PositionConstants.RIGHT);
            label2.setBackgroundColor(colorCache.getColor(SelectorNodeFigure.COLOR_POSTIT));
            label2.setOpaque(true);
            columns.put(name, new Label[] {label1, label2});
            add(label1, new GridData(SWT.FILL, SWT.FILL, false, false));
            add(label2, new GridData(SWT.FILL, SWT.FILL, true, false));
        }
    }
    public class CompartmentFigureBorder extends AbstractBorder {
        public Insets getInsets(IFigure figure) {
              return new Insets(0,0,0,0);
        }
        
        public void paint(IFigure figure, Graphics graphics, Insets insets) {
            graphics.setForegroundColor(colorCache.getColor(SelectorNodeFigure.COLOR_BLACK));
            graphics.drawLine(
                    getPaintRectangle(figure, insets).getTopLeft(),
                    tempRect.getTopRight());
            graphics.drawLine(
                    getPaintRectangle(figure, insets).getBottomLeft().getTranslated(0, -1),
                    tempRect.getBottomRight().getTranslated(0, -1));
        }
    }
    public String getPredictor() {
        return predictorLabel.getText();
    }
    public void setPredictor(String predictor) {
        predictorLabel.setText(predictor);
    }
}