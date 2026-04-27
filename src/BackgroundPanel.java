import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Color;

public class BackgroundPanel extends JPanel {
    private final Image img;

    public BackgroundPanel(String path) {
        this.img = new ImageIcon(getClass().getResource(path)).getImage();
        this.setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (img != null) {
            int w = getWidth();
            int h = getHeight();
            
            // Proportions: Image takes 60% width, Green takes 40%
            int imgWidth = (int)(w * 0.6); 
            int greenWidth = w - imgWidth;

            // Height: Image takes 60% height instead of 50% (makes it taller)
            int imgHeight = (int)(h * 0.6);
            int topWhiteHeight = h - imgHeight;

            // 1. Draw the White Background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, h);

            // 2. Draw the Apartment Image (Bottom-Left)
            if (img != null) {
                g.drawImage(img, 0, topWhiteHeight, imgWidth, imgHeight, this);
            }

            // 3. Draw the Green Panel (Right Side)
            g.setColor(new Color(7, 66, 38)); 
            g.fillRect(imgWidth, 0, greenWidth, h);
         }
     }
}
