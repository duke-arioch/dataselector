/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.nexusbpm.dataselector.figures;

import org.eclipse.draw2d.AbstractBackground;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;

/**
 * A figure that can display text and/or an image.
 * <p>
 * When using an org.eclipse.draw2d.Label with a GridLayout the
 * text alignment is not always obeyed because the location of
 * the text is determined by comparing the actual size with the
 * preferred size but the grid layout will set the preferred
 * size to the actual size (which causes the text to be left
 * aligned).
 * <p>
 * This class fixes that problem by adding the method
 * {@link #getSubStringTextPreferredSize()} and using that size
 * instead of the preferred size in {@link #calculateLocations()}.
 * The rest of the code in this class exists only because of the
 * interconnected private implementation methods in the superclass
 * which cannot be overridden or called from subclasses and must
 * instead be duplicated.
 */
public class Label extends org.eclipse.draw2d.Label {
    private static String ELLIPSIS = "..."; //$NON-NLS-1$
    
    private Dimension iconSize = new Dimension(0, 0);
    private Point iconLocation;
    private Point textLocation;
    private int textAlignment = CENTER;
    private int iconAlignment = CENTER;
    private int labelAlignment = CENTER;
    
    /**
     * Construct an empty Label.
     * 
     * @since 2.0
     */
    public Label() {
    }
    
    /**
     * Construct a Label with passed String as its text.
     * 
     * @param s the label text
     * @since 2.0
     */
    public Label(String s) {
        setText(s);
    }
    
    /**
     * Construct a Label with passed Image as its icon.
     * 
     * @param i the label image
     * @since 2.0
     */
    public Label(Image i) {
        setIcon(i);
    }
    
    /**
     * Construct a Label with passed String as text and passed Image as its icon.
     * 
     * @param s the label text
     * @param i the label image
     * @since 2.0
     */
    public Label(String s, Image i) {
        setText(s);
        setIcon(i);
    }
    
    private void alignOnHeight(Point loc, Dimension size, int alignment) {
        Insets insets = getInsets();
        switch(alignment) {
            case TOP:
                loc.y = insets.top;
                break;
            case BOTTOM:
                loc.y = bounds.height - size.height - insets.bottom;
                break;
            default:
                loc.y = (bounds.height - size.height) / 2;
        }
    }
    
    private void alignOnWidth(Point loc, Dimension size, int alignment) {
        Insets insets = getInsets();
        switch(alignment) {
            case LEFT:
                loc.x = insets.left;
                break;
            case RIGHT:
                loc.x = bounds.width - size.width - insets.right;
                break;
            default:
                loc.x = (bounds.width - size.width) / 2;
        }
    }
    
    private void calculateAlignment() {
        switch(getTextPlacement()) {
            case EAST:
            case WEST:
                alignOnHeight(textLocation, getTextSize(), textAlignment);
                alignOnHeight(iconLocation, iconSize, iconAlignment);
                break;
            case NORTH:
            case SOUTH:
                alignOnWidth(textLocation, getSubStringTextSize(), textAlignment);
                alignOnWidth(iconLocation, iconSize, iconAlignment);
                break;
        }
    }
    
    /**
     * Calculates the size of the Label using the passed Dimension as the size of the Label's 
     * text.
     * 
     * @param txtSize the precalculated size of the label's text
     * @return the label's size
     * @since 2.0
     */
    protected Dimension calculateLabelSize(Dimension txtSize) {
        int gap = getIconTextGap();
        if(getIcon() == null || getText().equals("")) //$NON-NLS-1$
            gap = 0;
        Dimension d = new Dimension(0, 0);
        if(getTextPlacement() == WEST || getTextPlacement() == EAST) {
            d.width = iconSize.width + gap + txtSize.width;
            d.height = Math.max(iconSize.height, txtSize.height);
        } else {
            d.width = Math.max(iconSize.width, txtSize.width);
            d.height = iconSize.height + gap + txtSize.height;
        }
        return d;
    }
    
    private void calculateLocations() {
        textLocation = new Point();
        iconLocation = new Point();
        
        calculatePlacement();
        calculateAlignment();
        Dimension offset = getSize().getDifference(//getPreferredSize());
                getSubStringTextPreferredSize());
        offset.width += getTextSize().width - getSubStringTextSize().width;
        switch(labelAlignment) {
            case CENTER:
                offset.scale(0.5f);
                break;
            case LEFT:
                offset.scale(0.0f);
                break;
            case RIGHT:
                offset.scale(1.0f);
                break;
            case TOP:
                offset.height = 0;
                offset.scale(0.5f);
                break;
            case BOTTOM:
                offset.height = offset.height * 2;
                offset.scale(0.5f);
                break;
            default:
                offset.scale(0.5f);
                break;
        }
        
        switch(getTextPlacement()) {
            case EAST:
            case WEST:
                offset.height = 0;
                break;
            case NORTH:
            case SOUTH:
                offset.width = 0;
                break;
        }
        
        textLocation.translate(offset);
        iconLocation.translate(offset);
    }
    
    private void calculatePlacement() {
        int gap = getIconTextGap();
        if(getIcon() == null || getText().equals("")) //$NON-NLS-1$
            gap = 0;
        Insets insets = getInsets();
        
        switch(getTextPlacement()) {
            case EAST:
                iconLocation.x = insets.left;
                textLocation.x = iconSize.width + gap + insets.left;
                break;
            case WEST:
                textLocation.x = insets.left;
                iconLocation.x = getSubStringTextSize().width + gap + insets.left;
                break;
            case NORTH:
                textLocation.y = insets.top;
                iconLocation.y = getTextSize().height + gap + insets.top;
                break;
            case SOUTH:
                textLocation.y = iconSize.height + gap + insets.top;
                iconLocation.y = insets.top;
        }
    }
    
    private void clearLocations() {
        iconLocation = textLocation = null;
    }
    
    /**
     * Returns the current alignment of the Label's icon. The default is 
     * {@link PositionConstants#CENTER}.
     * 
     * @return the icon alignment
     * @since 2.0
     */
    public int getIconAlignment() {
        return iconAlignment;
    }
    
    /**
     * Returns the bounds of the Label's icon.
     * 
     * @return the icon's bounds
     * @since 2.0
     */
    public Rectangle getIconBounds() {
        Rectangle bounds = getBounds();
        return new Rectangle(bounds.getLocation().translate(getIconLocation()), iconSize);
    }
    
    /**
     * Returns the location of the Label's icon relative to the Label.
     * 
     * @return the icon's location
     * @since 2.0
     */
    protected Point getIconLocation() {
        if(iconLocation == null)
            calculateLocations();
        return iconLocation;
    }
    
    /**
     * @see IFigure#getMinimumSize(int, int)
     */
    public Dimension getMinimumSize(int w, int h) {
        if(minSize != null)
            return minSize;
        minSize = new Dimension();
        if(getLayoutManager() != null)
            minSize.setSize(getLayoutManager().getMinimumSize(this, w, h));
        
        Dimension labelSize = calculateLabelSize(FigureUtilities.getTextExtents(ELLIPSIS, getFont()).intersect(FigureUtilities.getTextExtents(getText(), getFont())));
        Insets insets = getInsets();
        labelSize.expand(insets.getWidth(), insets.getHeight());
        minSize.union(labelSize);
        return minSize;
    }
    
    /**
     * @see IFigure#getPreferredSize(int, int)
     */
    public Dimension getPreferredSize(int wHint, int hHint) {
        if(prefSize == null) {
            prefSize = calculateLabelSize(getTextSize());
            Insets insets = getInsets();
            prefSize.expand(insets.getWidth(), insets.getHeight());
            if(getLayoutManager() != null)
                prefSize.union(getLayoutManager().getPreferredSize(this, wHint, hHint));
        }
        if(wHint >= 0 && wHint < prefSize.width) {
            Dimension minSize = getMinimumSize(wHint, hHint);
            Dimension result = prefSize.getCopy();
            result.width = Math.min(result.width, wHint);
            result.width = Math.max(minSize.width, result.width);
            return result;
        }
        return prefSize;
    }
    
    protected Dimension getSubStringTextPreferredSize() {
        Dimension size = calculateLabelSize(getTextSize());
        Insets insets = getInsets();
        size.expand(insets.getWidth(), insets.getHeight());
        return size;
    }
    
    /**
     * Returns the current alignment of the Label's text. The default text alignment is 
     * {@link PositionConstants#CENTER}.
     * 
     * @return the text alignment
     */
    public int getTextAlignment() {
        return textAlignment;
    }
    
    /**
     * Returns the location of the label's text relative to the label.
     * 
     * @return the text location
     * @since 2.0
     */
    protected Point getTextLocation() {
        if(textLocation != null)
            return textLocation;
        calculateLocations();
        return textLocation;
    }
    
    /**
     * @see IFigure#invalidate()
     */
    public void invalidate() {
        prefSize = null;
        minSize = null;
        clearLocations();
        super.invalidate();
    }
    
    /**
     * @see Figure#paintFigure(Graphics)
     */
    protected void paintFigure(Graphics graphics) {
        if(isOpaque()) {
            graphics.fillRectangle(getBounds());
            if (getBorder() instanceof AbstractBackground) {
                ((AbstractBackground) getBorder()).paintBackground(this, graphics, NO_INSETS);
            }
        }
        Rectangle bounds = getBounds();
        graphics.translate(bounds.x, bounds.y);
        if(getIcon() != null)
            graphics.drawImage(getIcon(), getIconLocation());
        if(!isEnabled()) {
            graphics.translate(1, 1);
            graphics.setForegroundColor(ColorConstants.buttonLightest);
            graphics.drawText(getSubStringText(), getTextLocation());
            graphics.translate(-1, -1);
            graphics.setForegroundColor(ColorConstants.buttonDarker);
        }
        graphics.drawText(getSubStringText(), getTextLocation());
        graphics.translate(-bounds.x, -bounds.y);
    }
    
    /**
     * This method sets the alignment of the icon within the bounds of the label. If the label
     * is larger than the icon, then the icon will be aligned according to this alignment.
     * Valid values are:
     * <UL>
     *   <LI><EM>{@link PositionConstants#CENTER}</EM>
     *   <LI>{@link PositionConstants#TOP}
     *   <LI>{@link PositionConstants#BOTTOM}
     *   <LI>{@link PositionConstants#LEFT}
     *   <LI>{@link PositionConstants#RIGHT}
     * </UL>
     * @param align the icon alignment 
     * @since 2.0
     */
    public void setIconAlignment(int align) {
        if(iconAlignment == align)
            return;
        iconAlignment = align;
        clearLocations();
        repaint();
    }
    
    /**
     * Sets the label's icon size to the passed Dimension.
     * 
     * @param d the new icon size
     * @deprecated the icon is automatically displayed at 1:1
     * @since 2.0
     */
    public void setIconDimension(Dimension d) {
        if(d.equals(iconSize))
            return;
        iconSize = d;
        revalidate();
    }
    
    /**
     * Sets the alignment of the label (icon and text) within the figure. If this 
     * figure's bounds are larger than the size needed to display the label, the 
     * label will be aligned accordingly. Valid values are:
     * <UL>
     *   <LI><EM>{@link PositionConstants#CENTER}</EM>
     *   <LI>{@link PositionConstants#TOP}
     *   <LI>{@link PositionConstants#BOTTOM}
     *   <LI>{@link PositionConstants#LEFT}
     *   <LI>{@link PositionConstants#RIGHT}
     * </UL>
     * 
     * @param align label alignment
     */
    public void setLabelAlignment(int align) {
        if(labelAlignment == align)
            return;
        labelAlignment = align;
        clearLocations();
        repaint();
    }
    
    /**
     * Sets the alignment of the text relative to the icon within the label. The text 
     * alignment must be orthogonal to the text placement. For example, if the placement 
     * is EAST, then the text can be aligned using TOP, CENTER, or BOTTOM. Valid values are:
     * <UL>
     *   <LI><EM>{@link PositionConstants#CENTER}</EM>
     *   <LI>{@link PositionConstants#TOP}
     *   <LI>{@link PositionConstants#BOTTOM}
     *   <LI>{@link PositionConstants#LEFT}
     *   <LI>{@link PositionConstants#RIGHT}
     * </UL>
     * @see #setLabelAlignment(int)
     * @param align the text alignment
     * @since 2.0
     */
    public void setTextAlignment(int align) {
        if(textAlignment == align)
            return;
        textAlignment = align;
        clearLocations();
        repaint();
    }
}
