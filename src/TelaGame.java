import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TelaGame extends JPanel implements ActionListener {
    private static final int LARGURA_TELA = 800;

    private Jogador jogador;
    private Obstaculo cacto;
    private Obstaculo passaro;
    private Timer timer;

    public TelaGame() {
        setPreferredSize(new Dimension(LARGURA_TELA, 400));
        setBackground(Color.WHITE);

        jogador = new Jogador(100, 250, 50, 50);
        //                         x    y   larg alt vel passaro?
        cacto   = new Obstaculo( 700, 260, 40, 40, 6, false);
        passaro = new Obstaculo(1100, 180, 60, 42, 6, true);

        // Loop do jogo a ~60 FPS
        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        jogador.desenhar(g);
        cacto.desenhar(g);
        passaro.desenhar(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        jogador.atualizar();
        cacto.atualizar();
        passaro.atualizar();

        // quando sai pela esquerda, volta pela direita
        if (cacto.saiuDaTela())   cacto.setX(LARGURA_TELA);
        if (passaro.saiuDaTela()) passaro.setX(LARGURA_TELA);

        repaint();
    }

    public static void main(String[] args) {
        JFrame janela = new JFrame("Meu Jogo");
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setResizable(false);

        TelaGame jogo = new TelaGame();
        janela.add(jogo);

        janela.pack();
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);
    }
}
