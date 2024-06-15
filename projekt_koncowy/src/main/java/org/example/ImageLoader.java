package org.example;

import org.example.Image;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageLoader {
    private String defaultDirectory;

    public ImageLoader(String defaultDirectory) {
        this.defaultDirectory = defaultDirectory;
    }

    public ImageLoader() {
        this.defaultDirectory = System.getProperty("user.home");
    }

    public Image loadImageFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Wybierz obraz do wczytania");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Obrazy", "jpg", "jpeg", "png", "bmp"));

        if (defaultDirectory != null && !defaultDirectory.isEmpty()) {
            File defaultDir = new File(defaultDirectory);
            if (defaultDir.exists() && defaultDir.isDirectory()) {
                fileChooser.setCurrentDirectory(defaultDir);
            }
        }

        int userSelection = fileChooser.showOpenDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File imageFile = fileChooser.getSelectedFile();
            try {
                BufferedImage bufferedImage = ImageIO.read(imageFile);
                return new Image(bufferedImage);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Błąd podczas wczytywania obrazu: " + e.getMessage(), "Informacja", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        return null;
    }
}
