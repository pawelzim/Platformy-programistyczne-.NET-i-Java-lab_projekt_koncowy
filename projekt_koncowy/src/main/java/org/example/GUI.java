package org.example;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GUI {
    private JButton loadImageButton;
    private JLabel startImageLabel;
    private JPanel mainPanel;
    private JCheckBox negativeCheckBox;
    private JCheckBox blurCheckBox;
    private JCheckBox edgeDetectionCheckBox;
    private JCheckBox greyscaleCheckBox;
    private JButton processImageButton;
    private Image loadedImage;
    private JLabel greyscaleLabel;
    private JLabel edgeDetectionLabel;
    private JLabel blurLabel;
    private JLabel negativeLabel;
    private JButton clearImgesButton;
    private JCheckBox sepiaCheckBox;
    private JCheckBox increaseContrastCheckBox;
    private JLabel sepiaLabel;
    private JLabel increaseContrastLabel;
    private JComboBox<Integer> numberOfThreadsComboBox;
    private List<Image> filteredImages = new ArrayList<>();

    public GUI() {
        for (int i = 1; i <= 6; i++) {
            numberOfThreadsComboBox.addItem(i);
        }
        numberOfThreadsComboBox.setSelectedIndex(0);

        loadImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImageLoader imageLoader = new ImageLoader("C:/Users/pawel/OneDrive/Pulpit/Projekty/IntelliJ Projects/Platformy programistyczne .NET i Java/Projekt_koncowy/projekt_koncowy");
                loadedImage = imageLoader.loadImageFromFile();

                if (loadedImage != null) {
                    BufferedImage bufferedImage = loadedImage.getImage();
                    BufferedImage scaledImage = scaleImage(bufferedImage, 320, 320);
                    clearLabels();
                    startImageLabel.setIcon(new ImageIcon(scaledImage));
                } else {
                    JOptionPane.showMessageDialog(null, "Brak zdjęcia lub błąd przy wczytywaniu.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        processImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (loadedImage != null && startImageLabel.getIcon() != null) {
                    try {
                        applyFilters();
                        displayFilteredImages();
                    } catch (InterruptedException | ExecutionException ex) {
                        JOptionPane.showMessageDialog(null, "Błąd przy nakładaniu filtrów: " + ex.getMessage(), "Informacja", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Brak załadowanego zdjęcia.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        clearImgesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (startImageLabel.getIcon() != null) {
                    clearLabels();
                } else {
                    JOptionPane.showMessageDialog(null, "Brak załadowanego zdjęcia.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
                }

            }
        });
    }

    private BufferedImage scaleImage(BufferedImage srcImage, int maxWidth, int maxHeight) {
        int width = srcImage.getWidth();
        int height = srcImage.getHeight();
        double aspectRatio = (double) width / height;

        if (width > maxWidth) {
            width = maxWidth;
            height = (int) (maxWidth / aspectRatio);
        }

        if (height > maxHeight) {
            height = maxHeight;
            width = (int) (maxHeight * aspectRatio);
        }

        BufferedImage scaledImage = new BufferedImage(width, height, srcImage.getType());
        Graphics2D g2d = scaledImage.createGraphics();

        g2d.drawImage(srcImage, 0, 0, width, height, null);
        g2d.dispose();

        return scaledImage;
    }

    private void applyFilters() throws InterruptedException, ExecutionException {
        ImageFilterer filterer = new ImageFilterer();
        List<Callable<Image>> tasks = new ArrayList<>();

        if (greyscaleCheckBox.isSelected()) {
            tasks.add(() -> filterer.Grayscale(loadedImage));
        }

        if (negativeCheckBox.isSelected()) {
            tasks.add(() -> filterer.Negative(loadedImage));
        }

        if (blurCheckBox.isSelected()) {
            tasks.add(() -> filterer.Blur(loadedImage));
        }

        if (edgeDetectionCheckBox.isSelected()) {
            tasks.add(() -> filterer.EdgeDetection(loadedImage));
        }

        if (sepiaCheckBox.isSelected()) {
            tasks.add(() -> filterer.Sepia(loadedImage));
        }

        if (increaseContrastCheckBox.isSelected()) {
            tasks.add(() -> filterer.IncreaseContrast(loadedImage));
        }

        if (tasks.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nie zaznaczono żadnego checkBox.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int numberOfThreads = (Integer) numberOfThreadsComboBox.getSelectedItem();
        long startTime = System.nanoTime();
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(tasks.size(), numberOfThreads));
        List<Future<Image>> futures = executor.invokeAll(tasks);

        filteredImages.clear();
        for (Future<Image> future : futures) {
            filteredImages.add(future.get());
        }
        executor.shutdown();
        long endTime = System.nanoTime();

        long duration = endTime - startTime;
//        JOptionPane.showMessageDialog(null, "Czas przetwarzania obrazów: " + duration / 1_000_000 + " ms", "Czas", JOptionPane.INFORMATION_MESSAGE);
        System.out.println("Czas wykonania applyFilters dla " + numberOfThreads + " wątków: " + duration / 1_000_000 + " ms");
    }


    private void displayFilteredImages() {
        int imageIndex = 0;
        int maxWidth = 250;
        int maxHeight = 250;

        if (greyscaleCheckBox.isSelected() && imageIndex < filteredImages.size()) {
            greyscaleLabel.setIcon(new ImageIcon(scaleImage(filteredImages.get(imageIndex).getImage(), maxWidth, maxHeight)));
            imageIndex++;
        }
        if (negativeCheckBox.isSelected() && imageIndex < filteredImages.size()) {
            negativeLabel.setIcon(new ImageIcon(scaleImage(filteredImages.get(imageIndex).getImage(), maxWidth, maxHeight)));
            imageIndex++;
        }
        if (blurCheckBox.isSelected() && imageIndex < filteredImages.size()) {
            blurLabel.setIcon(new ImageIcon(scaleImage(filteredImages.get(imageIndex).getImage(), maxWidth, maxHeight)));
            imageIndex++;
        }
        if (edgeDetectionCheckBox.isSelected() && imageIndex < filteredImages.size()) {
            edgeDetectionLabel.setIcon(new ImageIcon(scaleImage(filteredImages.get(imageIndex).getImage(), maxWidth, maxHeight)));
            imageIndex++;
        }
        if (sepiaCheckBox.isSelected() && imageIndex < filteredImages.size()) {
            sepiaLabel.setIcon(new ImageIcon(scaleImage(filteredImages.get(imageIndex).getImage(), maxWidth, maxHeight)));
            imageIndex++;
        }
        if (increaseContrastCheckBox.isSelected() && imageIndex < filteredImages.size()) {
            increaseContrastLabel.setIcon(new ImageIcon(scaleImage(filteredImages.get(imageIndex).getImage(), maxWidth, maxHeight)));
        }
    }

    private void clearLabels() {
        startImageLabel.setIcon(null);
        greyscaleLabel.setIcon(null);
        edgeDetectionLabel.setIcon(null);
        blurLabel.setIcon(null);
        negativeLabel.setIcon(null);
        sepiaLabel.setIcon(null);
        increaseContrastLabel.setIcon(null);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("GUI");
        frame.setContentPane(new GUI().mainPanel);
        frame.setPreferredSize(new Dimension(1500, 800));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(5, 12, new Insets(0, 0, 0, 0), -1, -1));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(4, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 2), new Dimension(-1, 2), new Dimension(-1, 2), 0, false));
        startImageLabel = new JLabel();
        startImageLabel.setText("");
        mainPanel.add(startImageLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(400, 400), 1, false));
        final Spacer spacer2 = new Spacer();
        mainPanel.add(spacer2, new GridConstraints(0, 0, 4, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        mainPanel.add(spacer3, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        greyscaleLabel = new JLabel();
        greyscaleLabel.setText("");
        mainPanel.add(greyscaleLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        negativeLabel = new JLabel();
        negativeLabel.setText("");
        mainPanel.add(negativeLabel, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sepiaLabel = new JLabel();
        sepiaLabel.setText("");
        mainPanel.add(sepiaLabel, new GridConstraints(0, 7, 1, 5, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        edgeDetectionLabel = new JLabel();
        edgeDetectionLabel.setText("");
        mainPanel.add(edgeDetectionLabel, new GridConstraints(1, 4, 1, 2, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        blurLabel = new JLabel();
        blurLabel.setText("");
        mainPanel.add(blurLabel, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        increaseContrastLabel = new JLabel();
        increaseContrastLabel.setText("");
        mainPanel.add(increaseContrastLabel, new GridConstraints(1, 7, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loadImageButton = new JButton();
        loadImageButton.setText("Załaduj zdjęcie");
        mainPanel.add(loadImageButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(130, 30), new Dimension(130, 30), new Dimension(130, 30), 1, false));
        numberOfThreadsComboBox = new JComboBox();
        numberOfThreadsComboBox.setToolTipText("");
        mainPanel.add(numberOfThreadsComboBox, new GridConstraints(2, 8, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        processImageButton = new JButton();
        processImageButton.setText("Nałóż filtry");
        mainPanel.add(processImageButton, new GridConstraints(3, 8, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(130, 30), new Dimension(130, 30), new Dimension(130, 30), 1, false));
        clearImgesButton = new JButton();
        clearImgesButton.setText("Wyczyść obrazy");
        mainPanel.add(clearImgesButton, new GridConstraints(3, 9, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(130, 30), new Dimension(130, 30), new Dimension(130, 30), 0, false));
        greyscaleCheckBox = new JCheckBox();
        greyscaleCheckBox.setText("Greyscale");
        mainPanel.add(greyscaleCheckBox, new GridConstraints(2, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        negativeCheckBox = new JCheckBox();
        negativeCheckBox.setText("Negative");
        mainPanel.add(negativeCheckBox, new GridConstraints(3, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        blurCheckBox = new JCheckBox();
        blurCheckBox.setText("Blur");
        mainPanel.add(blurCheckBox, new GridConstraints(2, 4, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        edgeDetectionCheckBox = new JCheckBox();
        edgeDetectionCheckBox.setText("Edge Detection");
        mainPanel.add(edgeDetectionCheckBox, new GridConstraints(3, 4, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sepiaCheckBox = new JCheckBox();
        sepiaCheckBox.setText("Sepia");
        mainPanel.add(sepiaCheckBox, new GridConstraints(2, 7, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        increaseContrastCheckBox = new JCheckBox();
        increaseContrastCheckBox.setText("Zwiększony kontrast");
        mainPanel.add(increaseContrastCheckBox, new GridConstraints(3, 7, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
