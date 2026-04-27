import java.awt.*;
import javax.swing.*;

public class RoundedTextField extends JTextField {
    private int radius;

    public RoundedTextField(int radius) {
        this.radius = radius;
        setOpaque(false); // Required for custom painting
    }

    @Override
    protected void paintComponent(Graphics g) {
         Graphics2D g2 = (Graphics2D) g.create();
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2.setColor(getBackground());
         // Fills the rounded background
         g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
         super.paintComponent(g);
         g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
         Graphics2D g2 = (Graphics2D) g.create();
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2.setColor(getForeground());
         // Draws the rounded outline
         g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
         g2.dispose();
    }
}
