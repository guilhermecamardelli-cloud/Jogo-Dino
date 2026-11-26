package Visao;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

/**
 * Ícone de alto-falante desenhado por código (sem imagem):
 * com ondinhas quando a música está ligada, com um X vermelho quando está desligada.
 * Implementa a interface Icon do Swing, então qualquer botão sabe desenhá-lo.
 */
public class IconeSom implements Icon {

    private static final int TAMANHO = 20;
    private static final Color COR = new Color(60, 60, 60);
    private static final Color COR_X = new Color(210, 40, 40);

    private final boolean ligado;

    public IconeSom(boolean ligado) {
        this.ligado = ligado;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // o alto-falante: uma caixinha e um cone
        g2.setColor(COR);
        g2.fillRect(x + 2, y + 7, 4, 6);
        g2.fillPolygon(new int[] { x + 6, x + 11, x + 11, x + 6 },
                       new int[] { y + 7, y + 3, y + 17, y + 13 }, 4);

        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (ligado) {
            // duas ondas saindo do cone
            g2.drawArc(x + 8, y + 6, 8, 8, -50, 100);
            g2.drawArc(x + 6, y + 2, 14, 16, -50, 100);
        } else {
            // X vermelho: mudo
            g2.setColor(COR_X);
            g2.drawLine(x + 13, y + 7, x + 19, y + 13);
            g2.drawLine(x + 19, y + 7, x + 13, y + 13);
        }
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return TAMANHO;
    }

    @Override
    public int getIconHeight() {
        return TAMANHO;
    }
}
