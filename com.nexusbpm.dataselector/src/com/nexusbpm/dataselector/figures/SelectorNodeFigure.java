package com.nexusbpm.dataselector.figures;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.nexusbpm.dataselector.animation.AnimationEventListener;
import com.nexusbpm.dataselector.animation.AnimationManager;
import com.nexusbpm.dataselector.animation.AnimationManagerFactory;
import com.nexusbpm.dataselector.util.ColorCache;
import com.nexusbpm.dataselector.util.FontCache;
import com.nexusbpm.dataselector.util.ImageCache;

public class SelectorNodeFigure extends org.eclipse.draw2d.RoundedRectangle implements AnimationEventListener {
    private static final int LINES_OF_VALUE = 4;

    public enum State { INCOMPLETE, WORKING, COMPLETE }
    
    public static final String N_A = "N/A";
    public static final String STD_DEV = "std dev";
    public static final String DISTINCT = "distinct";
    public static final String MEAN = "mean";
    public static final String MIN = "min";
    public static final String MAX = "max";
    public static final String SUM = "sum";
    
    public static final RGB COLOR_POSTIT = new RGB(255, 255, 200);
    public static final RGB COLOR_POSTIT_SHADE = new RGB(255, 220, 160);
    public static final RGB COLOR_POSTIT_SUPER_SHADE = new RGB(192, 165, 120);
    public static final RGB COLOR_BLACK = new RGB(0, 0, 0);
    public static final RGB COLOR_PASTEL_GREEN = new RGB(200, 255, 200);
    public static final RGB COLOR_LIGHT_PASTEL_GREEN = new RGB(225, 255, 225);
    public static final RGB COLOR_DARK_PASTEL_GREEN = new RGB(160, 255, 160);
    public static final RGB COLOR_PASTEL_BLUE = new RGB(180, 240, 255);
    
    public static final RGB COLOR_BACKGROUND = COLOR_PASTEL_GREEN;
    
    public static final String IMAGE_BOX = "icons/16x16/misc/swimlanes_multiple.gif";
    public static final String IMAGE_FORK = "icons/16x16/misc/fork_enabled.gif";
    public static final String IMAGE_DB = "icons/16x16/misc/db.png";
    public static final String IMAGE_DB_CHECK = "icons/16x16/misc/dbcheck.png";
    public static final String IMAGE_DB_DOWN = "icons/16x16/misc/dbdown.png";
    
    private ImageCache imageCache;
    private ColorCache colorCache;
    private FontCache fontCache;
    
    private Display display;
    private AnimationManager manager;
    private State state = State.INCOMPLETE;
    
    private String currentImage = IMAGE_DB;
    
    private String stdDev;
    private String mean;
    private String distinct;
    private String count = N_A;
    private String name;
    private String min;
    private String max;
    private String sum;
    private String segment;
    
    private boolean segmentItalicized;
    
    private Label columnLabel;
    private Label nameLabel = new Label("Untitled");
    private StatisticFigure stats;
    private Figure bottomPanel = new Figure();
    private List<Label> fragments = new ArrayList<Label>();
    Rectangle topPanelBounds = new Rectangle();
    
    public SelectorNodeFigure(
            AnimationManagerFactory animationFactory,
            ImageCache imageCache,
            ColorCache colorCache,
            FontCache fontCache) {
        this.imageCache = imageCache;
        this.colorCache = colorCache;
        this.fontCache = fontCache;
        this.display = Display.getCurrent();
        this.manager = animationFactory.getAnimationManager(getClass().getName(), 100, 10);
        setBackgroundColor(colorCache.getColor(COLOR_BACKGROUND));
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 2;
        layout.horizontalSpacing = 0;
        layout.marginWidth = 1;
        layout.verticalSpacing = 0;
        setLayoutManager(layout);
        columnLabel = new Label();
        for (int i = 0; i < LINES_OF_VALUE; i++) {
            Label temp = new Label("") {
                @Override
                public Dimension getPreferredSize(int hint, int hint2) {
                    if (getText() == null || getText().length() == 0) 
                        return new Dimension(hint, 0);
                    else
                        return super.getPreferredSize(hint, hint2);
                }
            };
            temp.setFont(fontCache.getFont(10, SWT.BOLD));
            fragments.add(temp);
        }
        setCornerDimensions(new Dimension(20, 20));
        nameLabel.setFont(fontCache.getFont(9, SWT.NORMAL));
        Panel topPanel = new Panel() {
            @Override
            public boolean isOpaque() {return false;}
            @Override
            public void setBounds(Rectangle rect) {
                topPanelBounds = rect;
                super.setBounds(rect);
            }
        };
        topPanel.setLayoutManager(new GridLayout(1, false));
        
        for (Label label: fragments) {
            topPanel.add(label, new GridData(SWT.CENTER, SWT.FILL, true, false));
        }
        add(topPanel, new GridData(SWT.CENTER, SWT.FILL, true, true));
        add(nameLabel, new GridData(SWT.CENTER, SWT.FILL, true, false));
        setupMiddlePanel("");
        setupBottomPanel();
        add(stats, new GridData(SWT.FILL, SWT.FILL, true, false));
        add(bottomPanel, new GridData(SWT.FILL, SWT.FILL, true, false));
        setState(State.INCOMPLETE);
    }

    private void setupMiddlePanel(String predictor) {
        stats = new StatisticFigure(predictor, colorCache, fontCache);
        //here we can initialize some defaults that will always be there to ensure ordering
        stats.setNV(DISTINCT, N_A);
        stats.setNV(MIN, N_A);
        stats.setNV(MAX, N_A);
        stats.setNV(MEAN, N_A);
        stats.setNV(STD_DEV, N_A);
        stats.setNV(SUM, N_A);
    }
    
    private void setupBottomPanel() {
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 2;
        layout.marginWidth = 2;
        bottomPanel.setLayoutManager(layout);
        bottomPanel.setOpaque(false);
        columnLabel.setForegroundColor(colorCache.getColor(COLOR_BLACK));
        columnLabel.setIcon(imageCache.getImage(IMAGE_FORK));
        bottomPanel.add(columnLabel, new GridData(SWT.CENTER, SWT.FILL, true,
                false));
    }

    protected String getFittingTextWidth(int maxWidth, String candidate, int start) {
        Font f = fragments.get(0).getFont();
        int segmentEnd = candidate.length();
        while (FigureUtilities.getTextWidth(segment.substring(start, segmentEnd), f) > maxWidth) {
            if (segment.substring(start, segmentEnd - 1).contains(" ")) {
                segmentEnd = segment.substring(start, segmentEnd - 1).lastIndexOf(' ') + start;
            } else {
                segmentEnd--;
            }
        }
        return candidate.substring(start, segmentEnd);
    }

    @Override
    protected void layout() {
        super.layout();
        Font f = fragments.get(0).getFont();
        List<String> fragmentText = new ArrayList<String>();
        List<Integer> heights = new ArrayList<Integer>();
        int maxy=topPanelBounds.height;
        int totaly = 0;
        boolean ycut = false;
        boolean xcut = false;
        if (f != null) {
            int whichLine = 0;
            int lastYVisibleWhich = 0;
            int seekWidth = this.bounds.width - 32;
            int segmentStart = 0;
            while((segmentStart < segment.length()) && (whichLine < fragments.size())) {
                String candidate = getFittingTextWidth(seekWidth, segment, segmentStart);
                Dimension d = FigureUtilities.getTextExtents(candidate, f);
                totaly += d.height + 5;
                if (totaly <= maxy) {
                    lastYVisibleWhich = whichLine;
                }
                heights.add(Integer.valueOf(totaly));
                fragmentText.add(candidate);
                whichLine++;
                segmentStart += candidate.length();
            }
            xcut = (segmentStart != segment.length());
            ycut = (totaly > maxy);
            int theLast = -1;
            if (ycut && xcut) {
                theLast = Math.min(whichLine - 1, lastYVisibleWhich);
            } else if (ycut && !xcut) {
                theLast = lastYVisibleWhich;
            } else if (xcut && !ycut) {
                theLast = whichLine - 1;
            }
            if (theLast != -1) fragmentText.set(theLast, fragmentText.get(theLast) +  "...");
            for (int i = 0; i < fragments.size(); i++) {
                if (i <= lastYVisibleWhich)
                    fragments.get(i).setText(fragmentText.get(i));
                else fragments.get(i).setText("");
            }
        }
    }

    public State getState() {
        return state;
    }
    
    public void setState(State state) {
        if(state == State.WORKING && state == this.state) {
            return;
        }
        this.state = state;
        switch(state) {
            case INCOMPLETE:
                manager.removeListener(this);
                setImage(IMAGE_DB);
                break;
            case WORKING:
                manager.addListener(this);
                break;
            case COMPLETE:
                manager.removeListener(this);
                setImage(IMAGE_DB_CHECK);
                break;
        }
    }
    
    public void setImage(String image) {
        currentImage = image;
        fragments.get(0).setIcon(imageCache.getImage(currentImage));
    }
    
    public void animate(long delay, final int frames, final int currentFrame) {
        display.asyncExec(new Runnable() {
            public void run() {
                if(state != State.WORKING) {
                    return;
                }
                try {
                    int x = 2 * currentFrame / frames;
                    switch(x) {
                        case 0:
                            setImage(IMAGE_DB_DOWN);
                            break;
                        default:
                            setImage(IMAGE_DB);
                            break;
                    }
                } catch(IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    public boolean isSegmentItalicized() {
        return segmentItalicized;
    }
    
    public void setSegmentItalicized(boolean italicized) {
        if(italicized != segmentItalicized) {
            segmentItalicized = italicized;
            int style = SWT.BOLD;
            if(segmentItalicized) {
                style |= SWT.ITALIC;
            }
            for(Label l : fragments) {
                l.setFont(fontCache.getFont(10, style));
            }
        }
    }
    
    public String getSegment() {
        return segment;
    }
    
    public void setSegment(String segment) {
        this.segment = segment;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        updateNameLabel();
    }
    
    public String getSplitColumn() {
        return columnLabel.getText();
    }
    
    public void setSplitColumn(String splitColumn) {
        columnLabel.setText(splitColumn);
        if(splitColumn.length() == 0) {
            columnLabel.setIcon(null);
        } else {
            columnLabel.setIcon(imageCache.getImage(IMAGE_FORK));
        }
    }
    
    public String getPredictor() {
        return stats.getPredictor();
    }
    
    public void setPredictor(String predictor) {
        stats.setPredictor(predictor);
    }
    
    public String getStdDev() {
        return stdDev;
    }
    
    public void setStdDev(String stdDev) {
        this.stdDev = stdDev;
        stats.setNV(STD_DEV, stdDev);
    }
    
    public String getDistinct() {
        return distinct;
    }
    
    public void setDistinct(String count) {
        this.distinct = count;
        stats.setNV(DISTINCT, "" + count);
    }
    
    public String getMean() {
        return mean;
    }
    
    public void setMean(String mean) {
        this.mean = mean;
        stats.setNV(MEAN, mean);
    }
    
    public String getMin() {
        return min;
    }
    
    public void setMin(String min) {
        this.min = min;
        stats.setNV(MIN, min);
    }
    
    public String getMax() {
        return max;
    }
    
    public void setMax(String max) {
        this.max = max;
        stats.setNV(MAX, max);
    }
    
    public String getSum() {
        return sum;
    }
    
    public void setSum(String sum) {
        this.sum = sum;
        stats.setNV(SUM, sum);
    }
    
    public String getCount() {
        return count;
    }
    
    public void setCount(String count) {
        this.count = count;
        updateNameLabel();
    }
    
    public void updateNameLabel() {
        String label = name;
        if(label == null) {
            label = "";
        }
        if(count != null && count.length() > 0 && !count.equals(N_A)) {
            if(label.length() > 0) {
                label += " (" + count +")";
            } else {
                label += count;
            }
        }
        nameLabel.setText(label);
    }
}
