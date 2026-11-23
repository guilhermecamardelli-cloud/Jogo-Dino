import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Tela de início com cara de TV antiga.
 * Desenha a TV no paintComponent e coloca dois botões em cima: START GAME e SAIR.
 */
public class TelaInicio extends JPanel {

    // cores
    private static final Color FUNDO_TV = new Color(30, 30, 50);
    private static final Color AZUL_TELA = new Color(45, 55, 170);
    private static final Color GRADE = new Color(80, 95, 210);
    private static final Color LARANJA = new Color(255, 170, 40);
    private static final Color SOMBRA = new Color(120, 50, 10);

    // posição e tamanho da "tela" da TV
    private static final int TELA_X = 40, TELA_Y = 25, TELA_L = 720, TELA_A = 350;

    // objetos do próprio jogo, só para enfeitar a tela
    private final Jogador dino = new Jogador(110, 250, 50, 50);
    private final Obstaculo cacto = new Obstaculo(620, 261, 26, 39, 0, false);

    public TelaInicio(JFrame janela) {
        setPreferredSize(new Dimension(PainelJogo.LARGURA, PainelJogo.ALTURA));
        setBackground(FUNDO_TV);
        setLayout(null);    // sem layout: cada botão vai na posição que a gente escolher

        JButton start = criarBotao("START GAME", 200);
        JButton sair = criarBotao("SAIR", 245);

        start.addActionListener(e -> Main.abrirJogo(janela));
        sair.addActionListener(e -> System.exit(0));

        janela.getRootPane().setDefaultButton(start);   // ENTER também aperta o START GAME
    }

    /** Cria um botão sem fundo e sem borda, só com o texto laranja. */
    private JButton criarBotao(String texto, int y) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font(Font.MONOSPACED, Font.BOLD, 26));
        botao.setForeground(LARANJA);
        botao.setContentAreaFilled(false);
        botao.setBorderPainted(false);
        botao.setFocusPainted(false);
        botao.setBounds(250, y, 300, 40);

        // mouse em cima: aparece a setinha "> "
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setText("> " + texto);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setText(texto);
            }
        });

        add(botao);
        return botao;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);            // pinta o fundo escuro (o "gabinete" da TV)
        Graphics2D g2 = (Graphics2D) g;

        // linhas das quinas, dando a impressão de profundidade
        g2.setColor(new Color(60, 60, 90));
        g2.drawLine(0, 0, TELA_X, TELA_Y);
        g2.drawLine(getWidth(), 0, TELA_X + TELA_L, TELA_Y);
        g2.drawLine(0, getHeight(), TELA_X, TELA_Y + TELA_A);
        g2.drawLine(getWidth(), getHeight(), TELA_X + TELA_L, TELA_Y + TELA_A);

        // a tela azul com os cantos arredondados; daqui em diante só desenha dentro dela
        Shape tela = new RoundRectangle2D.Double(TELA_X, TELA_Y, TELA_L, TELA_A, 60, 60);
        g2.setColor(AZUL_TELA);
        g2.fill(tela);
        g2.setClip(tela);

        // grade
        g2.setColor(GRADE);
        for (int x = TELA_X; x < TELA_X + TELA_L; x += 40) {
            g2.drawLine(x, TELA_Y, x, TELA_Y + TELA_A);
        }
        for (int y = TELA_Y; y < TELA_Y + TELA_A; y += 40) {
            g2.drawLine(TELA_X, y, TELA_X + TELA_L, y);
        }

        // textos de fliperama
        escrever(g2, "1UP", 80, 60, 16);
        escrever(g2, "CREDIT 01", 615, 60, 16);
        escreverCentralizado(g2, "JOGO DO", 110, 28);
        escreverCentralizado(g2, "DINO", 170, 56);

        // chão, dino e cacto (as classes do jogo se desenham sozinhas)
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(TELA_X, 300, TELA_X + TELA_L, 300);
        dino.desenhar(g2);
        cacto.desenhar(g2);

        // linhas de varredura, o efeito de TV de tubo
        g2.setColor(new Color(0, 0, 0, 45));
        for (int y = TELA_Y; y < TELA_Y + TELA_A; y += 3) {
            g2.drawLine(TELA_X, y, TELA_X + TELA_L, y);
        }

        // borda grossa em volta da tela
        g2.setClip(null);
        g2.setStroke(new BasicStroke(6));
        g2.setColor(Color.BLACK);
        g2.draw(tela);
    }

    /** Escreve o texto com uma sombra escura um pouco para baixo e para a direita. */
    private void escrever(Graphics2D g2, String texto, int x, int y, int tamanho) {
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, tamanho));
        g2.setColor(SOMBRA);
        g2.drawString(texto, x + 3, y + 3);
        g2.setColor(LARANJA);
        g2.drawString(texto, x, y);
    }

    private void escreverCentralizado(Graphics2D g2, String texto, int y, int tamanho) {
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, tamanho));
        int largura = g2.getFontMetrics().stringWidth(texto);
        escrever(g2, texto, (getWidth() - largura) / 2, y, tamanho);
    }
}
