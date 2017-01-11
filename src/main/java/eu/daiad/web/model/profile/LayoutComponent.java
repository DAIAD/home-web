package eu.daiad.web.model.profile;

public class LayoutComponent {
    
    private int id;
    
    private int x;
    
    private int y;
    
    private int h;
    
    private int w;
    
    private int maxH;
    
    private int minH;
    
    private int maxW;
    
    private int minW;
    
    private boolean isDraggable;
    
    private boolean isResizable;
    
    private boolean statik;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getMaxH() {
        return maxH;
    }

    public void setMaxH(int maxH) {
        this.maxH = maxH;
    }

    public int getMinH() {
        return minH;
    }

    public void setMinH(int minH) {
        this.minH = minH;
    }

    public int getMaxW() {
        return maxW;
    }

    public void setMaxW(int maxW) {
        this.maxW = maxW;
    }

    public int getMinW() {
        return minW;
    }

    public void setMinW(int minW) {
        this.minW = minW;
    }

    public boolean isIsDraggable() {
        return isDraggable;
    }

    public void setIsDraggable(boolean isDraggable) {
        this.isDraggable = isDraggable;
    }

    public boolean isIsResizable() {
        return isResizable;
    }

    public void setIsResizable(boolean isResizable) {
        this.isResizable = isResizable;
    }

    public boolean isStatik() {
        return statik;
    }

    public void setStatik(boolean statik) {
        this.statik = statik;
    }
    
}
