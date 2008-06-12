package com.nexusbpm.dataselector.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;

import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSTree;

public class TreeLayout {
    public static int DEFAULT_HORIZONTAL_MARGIN = 3;
    public static int DEFAULT_HORIZONTAL_SPACING = 25;
    public static int DEFAULT_VERTICAL_MARGIN = 1;
    public static int DEFAULT_VERTICAL_SPACING = 35;
    
    private int horizontalMargin;
    private int horizontalSpacing;
    private int verticalMargin;
    private int verticalSpacing;
    
    public TreeLayout() {
        this(DEFAULT_HORIZONTAL_MARGIN,
                DEFAULT_HORIZONTAL_SPACING,
                DEFAULT_VERTICAL_MARGIN,
                DEFAULT_VERTICAL_SPACING);
    }
    
    public TreeLayout(int horizontalMargin, int horizontalSpacing, int verticalMargin, int verticalSpacing) {
        this.horizontalMargin = horizontalMargin;
        this.horizontalSpacing = horizontalSpacing;
        this.verticalMargin = verticalMargin;
        this.verticalSpacing = verticalSpacing;
    }
    
    public void layout(LSTree tree) {
        if(tree != null && tree.getRoot() != null) {
            layout(tree.getRoot());
        }
    }
    
    public void layout(LSNode node) {
        if(node == null) return;
        LayoutInformation info = new LayoutInformation(node);
        info.compute();
        info.layout(horizontalMargin, verticalMargin);
    }
    
    private class LayoutInformation {
        LSNode node;
        List<LayoutInformation> children;
        
        /* boundary information for the box containing this node and all descendants.
         * x and y values are relative to the box containing this node and its descendants.
         */
        int x;
        int y;
        int width;
        int height;
        
        public LayoutInformation(LSNode node) {
            this.node = node;
            children = new ArrayList<LayoutInformation>();
            for(LSNode child : node.getSubNodes()) {
                children.add(new LayoutInformation(child));
            }
        }
        
        public void compute() {
            if(children.size() == 0) {
                width = node.getWidth();
                height = node.getHeight();
                x = y = 0;
            } else {
                width = 0;
                height = 0;
                int childHeight = 0;
                for(LayoutInformation child : children) {
                    child.compute();
                    width += child.width;
                    childHeight = Math.max(childHeight, child.height);
                }
                height = node.getHeight() + verticalSpacing + childHeight;
                width += children.size() * horizontalSpacing - horizontalSpacing;
                LayoutInformation first = children.get(0);
                LayoutInformation last = children.get(children.size() - 1);
                int x1 = first.node.getWidth() / 2 + first.x;
                int x2 = width - (last.width - last.node.getWidth() / 2 - last.x);
                x = (x2 - x1) / 2 + x1 - node.getWidth() / 2;
                height = 0;
            }
        }
        
        public void layout(int xMargin, int yMargin) {
            node.setBounds(new Rectangle(xMargin + x, yMargin + y, node.getWidth(), node.getHeight()));
            int leftMargin = xMargin;
            int topMargin = yMargin + node.getHeight() + verticalSpacing;
            for(LayoutInformation child : children) {
                child.layout(leftMargin, topMargin);
                leftMargin += child.width + horizontalSpacing;
            }
        }
    }
}
