package com.vcampus.client.core.ui.shop;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * A FlowLayout that supports wrapping of components to new lines.
 * Implementation adapted from public domain WrapLayout examples.
 */
public class WrapLayout extends FlowLayout
{
    private Dimension computePreferredLayoutSize(Container target)
    {
        synchronized (target.getTreeLock())
        {
            int targetWidth = target.getWidth();
            if (targetWidth == 0)
            {
                // use parent's width as a fallback
                targetWidth = Integer.MAX_VALUE;
            }

            Insets insets = target.getInsets();
            int hgap = getHgap();
            int vgap = getVgap();
            int maxWidth = targetWidth - (insets.left + insets.right + hgap*2);

            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            for (Component m : target.getComponents())
            {
                if (!m.isVisible()) continue;
                Dimension d = m.getPreferredSize();
                if (rowWidth + d.width > maxWidth)
                {
                    // new row
                    dim.width = Math.max(dim.width, rowWidth);
                    dim.height += rowHeight + vgap;
                    rowWidth = 0;
                    rowHeight = 0;
                }
                rowWidth += d.width + hgap;
                rowHeight = Math.max(rowHeight, d.height);
            }

            dim.width = Math.max(dim.width, rowWidth);
            dim.height += rowHeight;

            dim.width += insets.left + insets.right + hgap*2;
            dim.height += insets.top + insets.bottom + vgap*2;

            return dim;
        }
    }

    public WrapLayout()
    {
        super();
    }

    public WrapLayout(int align)
    {
        super(align);
    }

    public WrapLayout(int align, int hgap, int vgap)
    {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target)
    {
        return computePreferredLayoutSize(target);
    }

    @Override
    public Dimension minimumLayoutSize(Container target)
    {
        Dimension dim = computePreferredLayoutSize(target);
        return dim;
    }
}
