package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageFilterer {
    public Image Grayscale(Image inputImage) {
        BufferedImage image = inputImage.getImage();
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int grayLevel = (int) (0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue());
                int gray = new Color(grayLevel, grayLevel, grayLevel).getRGB();
                grayImage.setRGB(x, y, gray);
            }
        }

        return new Image(grayImage);
    }

    public Image Negative(Image inputImage) {
        BufferedImage image = inputImage.getImage();
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        BufferedImage negativeImage = new BufferedImage(width, height, image.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int red = 255 - color.getRed();
                int green = 255 - color.getGreen();
                int blue = 255 - color.getBlue();
                int negative = new Color(red, green, blue).getRGB();
                negativeImage.setRGB(x, y, negative);
            }
        }

        return new Image(negativeImage);
    }

    public Image Blur(Image inputImage) {
        BufferedImage image = inputImage.getImage();
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        int radius = 5;

        BufferedImage blurredImage = new BufferedImage(width, height, image.getType());

        float[] kernel = createGaussianKernel(radius);
        //int kernelSize = kernel.length;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float redSum = 0, greenSum = 0, blueSum = 0;
                float weightSum = 0;

                for (int ky = -radius; ky <= radius; ky++) {
                    for (int kx = -radius; kx <= radius; kx++) {
                        int nx = x + kx;
                        int ny = y + ky;

                        if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                            float weight = kernel[ky + radius] * kernel[kx + radius];
                            Color color = new Color(image.getRGB(nx, ny));
                            redSum += weight * color.getRed();
                            greenSum += weight * color.getGreen();
                            blueSum += weight * color.getBlue();
                            weightSum += weight;
                        }
                    }
                }

                int red = Math.min(Math.max((int) (redSum / weightSum), 0), 255);
                int green = Math.min(Math.max((int) (greenSum / weightSum), 0), 255);
                int blue = Math.min(Math.max((int) (blueSum / weightSum), 0), 255);
                blurredImage.setRGB(x, y, new Color(red, green, blue).getRGB());
            }
        }

        return new Image(blurredImage);
    }

    private float[] createGaussianKernel(int radius) {
        float sigma = radius / 3.0f;
        float[] kernel = new float[2 * radius + 1];
        float sum = 0;

        for (int i = -radius; i <= radius; i++) {
            kernel[i + radius] = (float) Math.exp(-(i * i) / (2 * sigma * sigma));
            sum += kernel[i + radius];
        }

        for (int i = 0; i < kernel.length; i++) {
            kernel[i] /= sum;
        }

        return kernel;
    }

    public Image EdgeDetection(Image inputImage) {
        BufferedImage image = inputImage.getImage();
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        BufferedImage edgeImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        Image blurredImage = Blur(new Image(image));

        int[][] gradientMagnitude = new int[width][height];
        int[][] gradientDirection = new int[width][height];

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int gx = (blurredImage.getImage().getRGB(x+1, y-1) & 0xFF) + 2 * (blurredImage.getImage().getRGB(x+1, y) & 0xFF) + (blurredImage.getImage().getRGB(x+1, y+1) & 0xFF) -
                        (blurredImage.getImage().getRGB(x-1, y-1) & 0xFF) - 2 * (blurredImage.getImage().getRGB(x-1, y) & 0xFF) - (blurredImage.getImage().getRGB(x-1, y+1) & 0xFF);
                int gy = (blurredImage.getImage().getRGB(x-1, y+1) & 0xFF) + 2 * (blurredImage.getImage().getRGB(x, y+1) & 0xFF) + (blurredImage.getImage().getRGB(x+1, y+1) & 0xFF) -
                        (blurredImage.getImage().getRGB(x-1, y-1) & 0xFF) - 2 * (blurredImage.getImage().getRGB(x, y-1) & 0xFF) - (blurredImage.getImage().getRGB(x+1, y-1) & 0xFF);

                gradientMagnitude[x][y] = (int) Math.sqrt(gx * gx + gy * gy);
                gradientDirection[x][y] = (int) Math.toDegrees(Math.atan2(gy, gx));
            }
        }

        int[][] suppressedGradient = new int[width][height];
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int direction = gradientDirection[x][y];
                int magnitude = gradientMagnitude[x][y];

                if ((direction >= -22.5 && direction <= 22.5) || (direction >= 157.5 || direction <= -157.5)) {
                    if (magnitude >= gradientMagnitude[x + 1][y] && magnitude >= gradientMagnitude[x - 1][y]) {
                        suppressedGradient[x][y] = magnitude;
                    } else {
                        suppressedGradient[x][y] = 0;
                    }
                } else if ((direction > 22.5 && direction <= 67.5) || (direction < -112.5 && direction >= -157.5)) {
                    if (magnitude >= gradientMagnitude[x + 1][y + 1] && magnitude >= gradientMagnitude[x - 1][y - 1]) {
                        suppressedGradient[x][y] = magnitude;
                    } else {
                        suppressedGradient[x][y] = 0;
                    }
                } else if ((direction > 67.5 && direction <= 112.5) || (direction < -67.5 && direction >= -112.5)) {
                    if (magnitude >= gradientMagnitude[x][y + 1] && magnitude >= gradientMagnitude[x][y - 1]) {
                        suppressedGradient[x][y] = magnitude;
                    } else {
                        suppressedGradient[x][y] = 0;
                    }
                } else if ((direction > 112.5 && direction <= 157.5) || (direction < -22.5 && direction >= -67.5)) {
                    if (magnitude >= gradientMagnitude[x - 1][y + 1] && magnitude >= gradientMagnitude[x + 1][y - 1]) {
                        suppressedGradient[x][y] = magnitude;
                    } else {
                        suppressedGradient[x][y] = 0;
                    }
                }
            }
        }

        int highThreshold = 75;
        int lowThreshold = 30;
        int[][] thresholdedGradient = new int[width][height];

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int magnitude = suppressedGradient[x][y];
                if (magnitude >= highThreshold) {
                    thresholdedGradient[x][y] = 255;
                } else if (magnitude >= lowThreshold) {
                    thresholdedGradient[x][y] = 128;
                } else {
                    thresholdedGradient[x][y] = 0;
                }
            }
        }

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (thresholdedGradient[x][y] == 128) {
                    boolean isEdge = false;
                    outerloop:
                    for (int ky = -1; ky <= 1; ky++) {
                        for (int kx = -1; kx <= 1; kx++) {
                            if (thresholdedGradient[x + kx][y + ky] == 255) {
                                isEdge = true;
                                break outerloop;
                            }
                        }
                    }
                    if (isEdge) {
                        thresholdedGradient[x][y] = 255;
                    } else {
                        thresholdedGradient[x][y] = 0;
                    }
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                edgeImage.setRGB(x, y, new Color(thresholdedGradient[x][y], thresholdedGradient[x][y], thresholdedGradient[x][y]).getRGB());
            }
        }

        return new Image(edgeImage);
    }

    public Image Sepia(Image inputImage) {
        BufferedImage image = inputImage.getImage();
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        BufferedImage sepiaImage = new BufferedImage(width, height, image.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int tr = (int)(0.393 * color.getRed() + 0.769 * color.getGreen() + 0.189 * color.getBlue());
                int tg = (int)(0.349 * color.getRed() + 0.686 * color.getGreen() + 0.168 * color.getBlue());
                int tb = (int)(0.272 * color.getRed() + 0.534 * color.getGreen() + 0.131 * color.getBlue());
                int r = Math.min(tr, 255);
                int g = Math.min(tg, 255);
                int b = Math.min(tb, 255);
                sepiaImage.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        return new Image(sepiaImage);
    }

    public Image IncreaseContrast(Image inputImage) {
        BufferedImage image = inputImage.getImage();
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        BufferedImage contrastImage = new BufferedImage(width, height, image.getType());

        double contrast = 1.5;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));

                int r = (int) (((color.getRed() / 255.0 - 0.5) * contrast + 0.5) * 255.0);
                int g = (int) (((color.getGreen() / 255.0 - 0.5) * contrast + 0.5) * 255.0);
                int b = (int) (((color.getBlue() / 255.0 - 0.5) * contrast + 0.5) * 255.0);

                r = Math.min(Math.max(r, 0), 255);
                g = Math.min(Math.max(g, 0), 255);
                b = Math.min(Math.max(b, 0), 255);

                contrastImage.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        return new Image(contrastImage);
    }
}
