package Controle;

import Modelo.Jogo;
import Modelo.Ranking;
import Visao.PainelJogo;
import Visao.Som;
import Visao.TelaInicio;
import Visao.TelaRanking;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * O controle: liga o modelo (Jogo) à visão (telas). Troca da tela de início para o jogo,
 * roda o loop de 60 passos por segundo, transforma as teclas em ações do Jogo e toca os sons.
 */
public class JogoController implements KeyListener {

    private final JFrame janela = new JFrame("Jogo do Dinossauro");
    private Jogo jogo;
    private PainelJogo painel;
    private Timer loop;
    private final Som som = new Som();       // música de fundo e som do pulo
    private final Ranking ranking = new Ranking();   // os 5 melhores, salvos em ranking.txt
    private TelaRanking telaRanking;         // só existe enquanto a tela de recorde está aberta

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
        painel.aoClicarMusica(this::alternarMusica);    // o botão MÚSICA faz o mesmo que a tecla M

        janela.getRootPane().setDefaultButton(null);   // o ENTER volta a ser só do jogo
        janela.getContentPane().removeAll();
        janela.add(painel);
        janela.pack();

        // a cada passo: o Jogo se atualiza e o painel se redesenha
        loop = new Timer(1000 / Jogo.FPS, e -> {
            if (telaRanking != null) {
                telaRanking.repaint();           // na tela de recorde só redesenha (estrelas e cursor piscam)
                return;
            }
            Jogo.Estado antes = jogo.getEstado();
            jogo.atualizar();
            if (antes != Jogo.Estado.GAME_OVER && jogo.getEstado() == Jogo.Estado.GAME_OVER) {
                som.pararMusica();               // acabou de bater: a música para
            }
            painel.repaint();
        });
        loop.start();
        som.iniciarMusica();
        painel.requestFocusInWindow();
    }

    /** Botão MÚSICA ou tecla M: liga/desliga a música e atualiza o texto do botão. */
    private void alternarMusica() {
        Jogo.Estado estado = jogo.getEstado();
        boolean ligada = som.alternarMusica(estado != Jogo.Estado.GAME_OVER);
        painel.mostrarMusica(ligada);
    }

    /** Troca o que aparece na janela (jogo ou tela de recorde) e passa o teclado para a tela nova. */
    private void mostrar(JPanel tela) {
        janela.getContentPane().removeAll();
        janela.add(tela);
        janela.pack();
        janela.revalidate();
        janela.repaint();
        tela.requestFocusInWindow();
    }

    /** Depois do GAME OVER: abre a tela de recorde (escreve o nome se entrou no ranking). */
    private void abrirRanking() {
        telaRanking = new TelaRanking(ranking, jogo.getPontuacao());
        telaRanking.addKeyListener(this);
        mostrar(telaRanking);
    }

    /** Da tela de recorde de volta para um jogo novo. */
    private void voltarAoJogo() {
        telaRanking = null;
        jogo.reiniciar();
        mostrar(painel);
        som.iniciarMusica();                     // a música recomeça junto com o jogo
    }

    /** Teclas na tela de recorde: setas andam, Enter escolhe, Backspace apaga, letras digitam direto. */
    private void teclaNoRanking(KeyEvent e) {
        int tecla = e.getKeyCode();
        if (!telaRanking.isDigitando()) {
            if (tecla == KeyEvent.VK_ENTER) {
                voltarAoJogo();
            }
            return;
        }
        switch (tecla) {
            case KeyEvent.VK_LEFT:
                telaRanking.mover(-1, 0);
                break;
            case KeyEvent.VK_RIGHT:
                telaRanking.mover(1, 0);
                break;
            case KeyEvent.VK_UP:
                telaRanking.mover(0, -1);
                break;
            case KeyEvent.VK_DOWN:
                telaRanking.mover(0, 1);
                break;
            case KeyEvent.VK_BACK_SPACE:
                telaRanking.apagar();
                break;
            case KeyEvent.VK_ENTER:
                if (telaRanking.escolher()) {    // escolheu END: salva o nome no Ranking
                    ranking.adicionar(telaRanking.getNome(), jogo.getPontuacao());
                    telaRanking.terminar();
                }
                break;
            default:
                telaRanking.digitar(e.getKeyChar());
                break;
        }
    }

    // Teclado: espaço/seta para cima pula, seta para baixo abaixa, Enter abre o ranking, M liga/desliga a música
    @Override
    public void keyPressed(KeyEvent e) {
        if (telaRanking != null) {
            teclaNoRanking(e);
            return;
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_UP:
                if (jogo.pular()) {
                    som.tocarPulo();             // só faz barulho se o pulo aconteceu mesmo
                }
                break;
            case KeyEvent.VK_DOWN:
                jogo.abaixar(true);
                break;
            case KeyEvent.VK_ENTER:
                if (jogo.getEstado() == Jogo.Estado.GAME_OVER) {
                    abrirRanking();              // só o Enter vale depois do fim de jogo
                }
                break;
            case KeyEvent.VK_M:
                alternarMusica();
                break;
            default:
                break;                           // teclas não previstas: ignora
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (telaRanking == null && e.getKeyCode() == KeyEvent.VK_DOWN) {
            jogo.abaixar(false);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // não usado
    }
}
