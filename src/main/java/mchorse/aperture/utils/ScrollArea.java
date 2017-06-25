package mchorse.aperture.utils;

import net.minecraft.util.math.MathHelper;

/**
 * Scrollable area
 * 
 * This class is responsible for storing information for scrollable one 
 * directional objects. 
 */
public class ScrollArea extends Area
{
    /**
     * Size of an element/item in the scroll area
     */
    public int scrollItemSize;

    /**
     * Size of the scrolling area 
     */
    public int scrollSize;

    /**
     * Scroll position 
     */
    public int scroll;

    /**
     * Scroll direction, used primarily in the {@link #clamp()} method 
     */
    public ScrollDirection direction = ScrollDirection.VERTICAL;

    public ScrollArea(int itemSize)
    {
        this.scrollItemSize = itemSize;
    }

    public void setSize(int items)
    {
        this.scrollSize = items * this.scrollItemSize;
    }

    /**
     * Scroll by relative amount 
     */
    public void scrollBy(int x)
    {
        this.scroll += x;
        this.clamp();
    }

    /**
     * Scroll to the position in the scroll area 
     */
    public void scrollTo(int x)
    {
        this.scroll = x;
        this.clamp();
    }

    /**
     * Clamp scroll to the bounds of the scroll size; 
     */
    public void clamp()
    {
        int size = this.direction == ScrollDirection.VERTICAL ? this.h : this.w;

        if (this.scrollSize <= size)
        {
            this.scroll = 0;
        }
        else
        {
            this.scroll = MathHelper.clamp(this.scroll, 0, this.scrollSize - size);
        }
    }

    /**
     * Get index of the cursor based on the {@link #scrollItemSize}.  
     */
    public int getIndex(int x, int y)
    {
        if (!this.isInside(x, y))
        {
            return -1;
        }

        int axis = 0;

        if (this.direction == ScrollDirection.VERTICAL)
        {
            y -= this.y;
            y += this.scroll;

            axis = y;
        }
        else
        {
            x -= this.x;
            x += this.scroll;

            axis = x;
        }

        int index = axis / this.scrollItemSize;

        return index > this.scrollSize / this.scrollItemSize ? -1 : index;
    }

    /**
     * Scroll direction 
     */
    public static enum ScrollDirection
    {
        VERTICAL, HORIZONTAL;
    }
}