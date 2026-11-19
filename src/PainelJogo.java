import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.JPanel;

/**
 * Controla o jogo: loop, desenho, teclado, colisão, pontuação e estado.
 *
 * Alinhado com as classes do grupo:
 *   - Jogador(x, y, largura, altura), pular(), abaixar(boolean)
 *   - Obstaculo(x, y, largura, altura, velocidade, boolean passaro), saiuDaTela()
 *   - Medidas iguais às da TelaGame: tela 800x400, chão em y = 300
 *
 * O Chao fica no começo da lista de objetos (desenhado atrás de tudo) e recebe
 * setVelocidade() sempre que o jogo acelera.
 */
public class PainelJogo extends JPanel implements Runnable, KeyListener {

    // ---------- Constantes ----------
    public static final int LARGURA = 800;
    public static final int ALTURA = 400;
    public static final int CHAO_Y = 300;          // altura da linha do chão

    // Tamanhos (mesmos da TelaGame; o cacto segue a proporção da imagem, 2:3)
    private static final int JOGADOR_X = 100;
    private static final int JOGADOR_TAMANHO = 50;  // 50x50; abaixado vira 60x25
    private static final int CACTO_PEQUENO_L = 26, CACTO_PEQUENO_A = 39;
    private static final int CACTO_GRANDE_L = 34, CACTO_GRANDE_A = 51;
    private static final int PASSARO_L = 60, PASSARO_A = 42;

    private static final int PONTOS_PASSARO = 400;
    // dia e noite se alternam a cada 1650 pontos; 6600 = 4 x 1650, então o meteoro cai no fim de uma noite
    private static final int PONTOS_TROCA_DIA_NOITE = 1650;
    // 1 ponto = 10 mil anos: 6600 pontos = 66 milhões de anos, quando caiu o meteoro de verdade
    private static final int PONTOS_METEORO = 6600;
    private static final int FPS = 60;
    private static final int VELOCIDADE_INICIAL = 6;
    private static final int VELOCIDADE_MAXIMA = 14;

    private enum Estado { JOGANDO, METEORO, EXTINTO, GAME_OVER }

    // ---------- Atributos ----------

    private Jogador jogador;

    private Chao chao;
    private Meteoro meteoro;

    private ArrayList<Obstaculo> obstaculos;     // só os obstáculos, para a colisão
    private int framesAteProximo;

    private ArrayList<ObjetoDoJogo> objetos;        // tudo o que é atualizado e desenhado
    private int pontuacao;
    private int velocidade;
    private int contadorFrames;
    private Estado estado;
    private final Random sorteio = new Random();
    private Thread threadDoJogo;

    public PainelJogo() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(Color.WHITE);
        setFocusable(true);          // sem isso o painel não recebe as teclas
        addKeyListener(this);
        reiniciar();
    }

    /** Começa o loop. A Main chama depois do janela.setVisible(true). */
    public void iniciar() {
        if (threadDoJogo == null) {
            threadDoJogo = new Thread(this);
            threadDoJogo.setDaemon(true);
            threadDoJogo.start();
            requestFocusInWindow();
        }
    }

    // =================================================================
    //  LOOP
    // =================================================================

    @Override
    public void run() {
        while (true) {
            atualizar();
            repaint();
            try {
                Thread.sleep(1000 / FPS);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /** Um passo do jogo. É synchronized porque o desenho roda em outra thread. */
    public synchronized void atualizar() {
        if (estado == Estado.METEORO || estado == Estado.EXTINTO) {
            atualizarMeteoro();
            return;
        }
        if (estado != Estado.JOGANDO) {
            return;
        }

        // Polimorfismo: cada objeto se atualiza do seu jeito
        for (ObjetoDoJogo objeto : objetos) {
            objeto.atualizar();
        }

        // Remove quem saiu da tela
        Iterator<Obstaculo> it = obstaculos.iterator();
        while (it.hasNext()) {
            Obstaculo o = it.next();
            if (o.saiuDaTela()) {
                it.remove();
                objetos.remove(o);
            }
        }

        // Cria um novo quando chega a hora
        framesAteProximo--;
        if (framesAteProximo <= 0) {
            gerarObstaculo();
        }

        verificarColisoes();

        // Pontuação: 20 pontos por segundo; a cada 200 pontos, mais rápido
        contadorFrames++;
        if (contadorFrames % 2 == 0) {
            pontuacao++;
        }
        if (pontuacao >= PONTOS_METEORO) {
            chamarMeteoro();
            return;
        }
        int novaVelocidade = Math.min(VELOCIDADE_INICIAL + pontuacao / 300, VELOCIDADE_MAXIMA);
        if (novaVelocidade != velocidade) {
            velocidade = novaVelocidade;

            chao.setVelocidade(velocidade);
        }
    }

    // =================================================================
    //  METEORO (6600 pontos = 66 milhões de anos)
    // =================================================================

    /** O jogo para e um meteoro cai do alto, em direção ao dinossauro. */
    private void chamarMeteoro() {
        int alvoX = jogador.getX() + jogador.getLargura() / 2;
        int alvoY = CHAO_Y - 10;
        meteoro = new Meteoro(alvoX + 450, -60, alvoX, alvoY);
        objetos.add(meteoro);
        estado = Estado.METEORO;
    }

    /** O meteoro cai; quando bate, o dinossauro vira esqueleto. */
    private void atualizarMeteoro() {
        meteoro.atualizar();
        if (meteoro.bateu() && !jogador.isEsqueleto()) {
            jogador.virarEsqueleto();
            estado = Estado.EXTINTO;
        }
    }

    // =================================================================
    //  OBSTÁCULOS E COLISÃO
    // =================================================================

    /** Sorteia um cacto ou um pássaro e agenda o próximo. */
    public void gerarObstaculo() {
        Obstaculo novo;
        // pássaro depois de 400 pontos: 1 chance em 2 (2 em 3 no modo noturno)
        boolean sorteouPassaro = modoNoturno() ? sorteio.nextInt(3) != 0 : sorteio.nextBoolean();
        if (pontuacao >= PONTOS_PASSARO && sorteouPassaro) {
            // Baixo: a base fica 5 px acima do chão, tem que pular.
            // Alto: a base fica 35 px acima do chão; o dino em pé (50 de altura) bate,
            //       mas abaixado (25 de altura) passa por baixo.
            int base = sorteio.nextBoolean() ? CHAO_Y - 5 : CHAO_Y - 35;
            novo = new Obstaculo(LARGURA, base - PASSARO_A, PASSARO_L, PASSARO_A, velocidade, true);
        } else {
            boolean grande = sorteio.nextBoolean();          // cacto grande ou pequeno
            int largura = grande ? CACTO_GRANDE_L : CACTO_PEQUENO_L;
            int altura = grande ? CACTO_GRANDE_A : CACTO_PEQUENO_A;
            novo = new Obstaculo(LARGURA, CHAO_Y - altura, largura, altura, velocidade, false);
        }
        obstaculos.add(novo);
        objetos.add(novo);

        // Distância até o próximo: um pulo dura uns 38 frames, então quanto mais
        // rápido, maior o espaço, para sempre dar tempo de pousar e pular de novo
        int distancia = 350 + velocidade * 25 + sorteio.nextInt(300);
        framesAteProximo = distancia / velocidade;
    }

    /** Alterna dia e noite (cores invertidas) a cada 1650 pontos; o meteoro sempre cai de noite. */
    private boolean modoNoturno() {
        if (estado == Estado.METEORO || estado == Estado.EXTINTO) {
            return true;
        }
        return (pontuacao / PONTOS_TROCA_DIA_NOITE) % 2 == 1;
    }

    public void verificarColisoes() {
        for (Obstaculo o : obstaculos) {
            if (jogador.colideCom(o)) {
                estado = Estado.GAME_OVER;
                return;
            }
        }
    }

    // =================================================================
    //  REINÍCIO
    // =================================================================

    /** Volta tudo para o começo. */
    public synchronized void reiniciar() {
        pontuacao = 0;
        contadorFrames = 0;
        velocidade = VELOCIDADE_INICIAL;
        objetos = new ArrayList<>();

        chao = new Chao(0, CHAO_Y, LARGURA, ALTURA - CHAO_Y, velocidade);
        objetos.add(chao);       // primeiro o chão, para ficar atrás de tudo

        // x, y (topo do dino quando está no chão), largura, altura
        jogador = new Jogador(JOGADOR_X, CHAO_Y - JOGADOR_TAMANHO, JOGADOR_TAMANHO, JOGADOR_TAMANHO);
        objetos.add(jogador);

        obstaculos = new ArrayList<>();
        framesAteProximo = 60;   // 1 segundo até o primeiro

        meteoro = null;
        estado = Estado.JOGANDO;
    }

    // =================================================================
    //  DESENHO
    // =================================================================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);     // limpa o fundo
        synchronized (this) {
            // Polimorfismo: o painel não sabe se é chão, jogador ou obstáculo
            for (ObjetoDoJogo objeto : objetos) {
                objeto.desenhar(g);
            }

            g.setColor(Color.DARK_GRAY);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
            g.drawString(String.format("%05d", pontuacao), LARGURA - 80, 30);

            if (estado == Estado.GAME_OVER) {
                g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
                g.drawString("GAME OVER", LARGURA / 2 - 75, ALTURA / 2 - 20);
                g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
                g.drawString("Aperte ENTER para jogar de novo", LARGURA / 2 - 145, ALTURA / 2 + 10);
            }

            if (estado == Estado.EXTINTO && meteoro.explosaoTerminou()) {
                g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
                g.drawString("DINO EXTINTO", LARGURA / 2 - 90, ALTURA / 2 - 80);
                g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
                g.drawString("Aperte ENTER para jogar de novo", LARGURA / 2 - 145, ALTURA / 2 - 55);
            }

            if (modoNoturno()) {
                // inverte todas as cores já desenhadas: fundo preto, desenhos claros
                g.setXORMode(Color.WHITE);
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setPaintMode();
            }
        }
    }

    // =================================================================
    //  TECLADO
    // =================================================================

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        int tecla = e.getKeyCode();

        if (estado == Estado.JOGANDO) {
            switch (tecla) {
                case KeyEvent.VK_SPACE:
                case KeyEvent.VK_UP:
                    jogador.pular();         // o Jogador ignora se estiver no ar
                    break;
                case KeyEvent.VK_DOWN:
                    jogador.abaixar(true);
                    break;
                default:
                    break;                      // Enter e teclas não previstas: ignora
            }
        } else if (estado == Estado.METEORO) {
            // meteoro caindo: nenhuma tecla vale
        } else if (tecla == KeyEvent.VK_ENTER) {   // GAME_OVER ou EXTINTO: só o Enter vale
            reiniciar();
        }
    }

    @Override
    public synchronized void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            jogador.abaixar(false);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // não usado
    }
}
