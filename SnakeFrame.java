import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 * Ventana principal del juego. No forma parte de la jerarquia de herencia
 * de la actividad (esa logica esta en Comida/Serpiente y sus subclases),
 * pero se incluye completa para que el proyecto compile y funcione.
 */
public class SnakeFrame extends JFrame implements KeyListener {

    private SnakePanel gamePanel;

    public SnakeFrame() {
        this.setTitle("Snake - GUI (con herencia: Comida y Serpiente)");
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.gamePanel = new SnakePanel();
        this.add(this.gamePanel);
        this.addKeyListener(this);
        this.setFocusable(true);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gamePanel.detener();
                System.exit(0);
            }
        });
        this.pack();
        this.setVisible(true);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        this.gamePanel.procesarTecla(e.getKeyCode());
        this.requestFocus();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
