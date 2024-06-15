package org.example;

import java.awt.image.BufferedImage;

public class Image {
    private BufferedImage image;
    private int width;
    private int height;

    public Image(BufferedImage image) {
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
