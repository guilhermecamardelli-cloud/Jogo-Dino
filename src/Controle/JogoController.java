package Controle;

import Modelo.Jogo;
import Visao.PainelJogo;
import Visao.TelaInicio;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import javax.swing.Timer;

/**
 * O controle: liga o modelo (Jogo) à visão (telas). Troca da tela de início para o jogo,
 * roda o loop de 60 passos por segundo e transforma as teclas em ações do Jogo.
 */
public class JogoController implements KeyListener {

    private final JFrame janela = new JFrame("Jogo do Dinossauro");
    private Jogo jogo;
    private PainelJogo painel;
    private Timer loop;

    /** Abre a janela mostrando primeiro a tela de início. */
    public void iniciar() {
        janela.add(new TelaInicio(janela, this::abrirJogo));
        janela.pack();                       // usa o tamanho que o painel pede (800 x 400)
        janela.setResizable(false);
        janela.setLocationRelativeTo(null);
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setVisible(true);
    }

    /** Chamado pelo botão START GAME: troca a tela de início pelo jogo, na mesma janela. */
    private void abrirJogo() {
        jogo = new Jogo();
        painel = new PainelJogo(jogo);
        painel.addKeyListener(this);

        janela.getRootPane().setDefaultButton(null);   // o ENTER volta a ser só do jogo
        janela.getContentPane().removeAll();
        janela.add(painel);
        janela.pack();

        // a cada passo: o Jogo se atualiza e o painel se redesenha
        loop = new Timer(1000 / Jogo.FPS, e -> {
            jogo.atualizar();
            painel.repaint();
        });
        loop.start();
        painel.requestFocusInWindow();
    }

    // Teclado: espaço/seta para cima pula, seta para baixo abaixa, Enter reinicia
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_UP:
                jogo.pular();
                break;
            case KeyEvent.VK_DOWN:
                jogo.abaixar(true);
                break;
            case KeyEvent.VK_ENTER:
                if (jogo.getEstado() == Jogo.Estado.GAME_OVER) {
                    jogo.reiniciar();            // só o Enter vale depois do fim de jogo
                }
                break;
            default:
                break;                           // teclas não previstas: ignora
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            jogo.abaixar(false);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // não usado
    }
}
