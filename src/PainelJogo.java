import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.JPanel;

/**
 * O "cérebro" do jogo: roda o loop, cria os objetos, trata o teclado, a colisão,
 * a pontuação, o dia e a noite e a evolução do personagem.
 * Guarda tudo numa lista de ObjetoDoJogo e manda cada um se atualizar e se desenhar (polimorfismo).
 */
public class PainelJogo extends JPanel implements Runnable, KeyListener {

    // ---------- Constantes ----------
    public static final int LARGURA = 800;
    public static final int ALTURA = 400;
    public static final int CHAO_Y = 300;          // altura da linha do chão

    // Tamanhos
    private static final int JOGADOR_X = 100;
    private static final int JOGADOR_TAMANHO = 50;  // 50x50; abaixado vira 60x25
    private static final int CACTO_PEQUENO_L = 26, CACTO_PEQUENO_A = 39;
    private static final int CACTO_GRANDE_L = 34, CACTO_GRANDE_A = 51;
    private static final int PASSARO_L = 60, PASSARO_A = 42;
    private static final int PASSARO_ALTURA_MIN = 5, PASSARO_ALTURA_MAX = 115;   // base do pássaro acima do chão

    // 1 ponto = 10 mil anos, então 6600 pontos = 66 milhões de anos (quando caiu o meteoro de verdade)
    private static final int PONTOS_PASSARO = 600;              // os pássaros começam a aparecer
    private static final int PONTOS_TROCA_DIA_NOITE = 1650;     // dia e noite se alternam
    private static final int PONTOS_METEORO = 6600;             // vira esqueleto (o jogo continua)
    private static final int PONTOS_AVESTRUZ = 8800;            // 88 milhões de anos: vira avestruz
    private static final int PONTOS_BARRIL = 11000;             // 110 milhões de anos: vira barril
    private static final int FRAMES_MENSAGEM = 150;
    private static final int FPS = 60;
    private static final int VELOCIDADE_INICIAL = 5;
    private static final int VELOCIDADE_MAXIMA = 11;
    private static final int PONTOS_POR_NIVEL = 400;   // a cada 400 pontos o jogo fica 1 mais rápido

    private enum Estado { JOGANDO, METEORO, GAME_OVER }

    // ---------- Atributos ----------

    private Jogador jogador;

    private Chao chao;
    private Meteoro meteoro;                     // só existe enquanto o meteoro cai e explode
    private Ceu astro;                         // sol de dia, lua de noite
    private ArrayList<Ceu> nuvens;
    private int framesAteNuvem;
    private String mensagem;                     // aviso no meio da tela (extinção, evolução)
    private int framesMensagem;

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
        if (estado == Estado.METEORO) {
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

        // A explosão do meteoro já apagou: sai da lista e amanhece
        if (meteoro != null && meteoro.explosaoTerminou()) {
            objetos.remove(meteoro);
            meteoro = null;
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

        atualizarCeu();
        verificarColisoes();

        if (framesMensagem > 0) {
            framesMensagem--;
        }

        // Pontuação: 45 pontos por segundo (3 pontos a cada 4 quadros); a cada 400 pontos, mais rápido
        contadorFrames++;
        if (contadorFrames % 4 == 0) {
            pontuacao += 3;
        }
        if (jogador.getForma() == Jogador.Forma.DINO && pontuacao >= PONTOS_METEORO) {
            chamarMeteoro();
            return;
        }
        evoluirJogador();
        int novaVelocidade = Math.min(VELOCIDADE_INICIAL + pontuacao / PONTOS_POR_NIVEL, VELOCIDADE_MAXIMA);
        if (novaVelocidade != velocidade) {
            velocidade = novaVelocidade;

            chao.setVelocidade(velocidade);
        }
    }

    // =================================================================
    //  CÉU: sol, lua e nuvens
    // =================================================================

    /** Põe o sol (ou a lua) no ponto certo do céu, sorteia nuvens novas e tira as que já saíram. */
    private void atualizarCeu() {
        boolean noite = modoNoturno();
        astro.ajustar((pontuacao % PONTOS_TROCA_DIA_NOITE) / (double) PONTOS_TROCA_DIA_NOITE, noite);

        Iterator<Ceu> it = nuvens.iterator();
        while (it.hasNext()) {
            Ceu n = it.next();
            n.setNoite(noite);
            if (n.saiuDaTela()) {
                it.remove();
                objetos.remove(n);
            }
        }

        framesAteNuvem--;
        if (framesAteNuvem <= 0) {
            criarNuvem(LARGURA, 25 + sorteio.nextInt(140));
            framesAteNuvem = 90 + sorteio.nextInt(180);
        }
    }

    /** A nuvem entra na lista logo depois do céu, para ficar atrás do jogador e dos obstáculos. */
    private void criarNuvem(int x, int y) {
        Ceu nuvem = new Ceu(x, y, sorteio);
        nuvem.setNoite(modoNoturno());
        nuvens.add(nuvem);
        objetos.add(2, nuvem);
    }

    // =================================================================
    //  EVOLUÇÃO (modo infinito)
    // =================================================================

    /** Dinossauro, esqueleto, avestruz e barril de petróleo: a forma muda com os "anos". */
    private void evoluirJogador() {
        if (jogador.getForma() == Jogador.Forma.ESQUELETO && pontuacao >= PONTOS_AVESTRUZ) {
            jogador.evoluirPara(Jogador.Forma.AVESTRUZ);
            avisar("OS DINOS VIRARAM AVES: AVESTRUZ!");
        } else if (jogador.getForma() == Jogador.Forma.AVESTRUZ && pontuacao >= PONTOS_BARRIL) {
            jogador.evoluirPara(Jogador.Forma.BARRIL);
            avisar("E VIRARAM PETRÓLEO: BARRIL!");
        }
    }

    private void avisar(String texto) {
        mensagem = texto;
        framesMensagem = FRAMES_MENSAGEM;
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

    /**
     * O meteoro cai; quando bate, o dinossauro vira esqueleto, os obstáculos somem
     * e o jogo continua (modo infinito) enquanto a explosão se apaga.
     */
    private void atualizarMeteoro() {
        meteoro.atualizar();
        astro.ajustar((pontuacao % PONTOS_TROCA_DIA_NOITE) / (double) PONTOS_TROCA_DIA_NOITE, true);   // já é noite: lua no céu
        if (meteoro.bateu()) {
            jogador.evoluirPara(Jogador.Forma.ESQUELETO);
            for (Obstaculo o : obstaculos) {
                objetos.remove(o);
            }
            obstaculos.clear();
            framesAteProximo = 150;          // um tempo sem obstáculos para respirar
            avisar("DINO EXTINTO");
            estado = Estado.JOGANDO;
        }
    }

    // =================================================================
    //  OBSTÁCULOS E COLISÃO
    // =================================================================

    /** Sorteia um cacto ou um pássaro (às vezes um bando) e agenda o próximo. */
    private void gerarObstaculo() {
        int extra = 0;                       // espaço a mais depois de um bando
        // pássaro depois de 600 pontos: 1 chance em 3 (1 em 2 no modo noturno)
        boolean sorteouPassaro = modoNoturno() ? sorteio.nextBoolean() : sorteio.nextInt(3) == 0;
        if (pontuacao >= PONTOS_PASSARO && sorteouPassaro) {
            int quantos = sorteio.nextInt(100) < 10 ? 2 : 1;      // 1 em 10 vem em bando de 2
            int x = LARGURA;
            for (int i = 0; i < quantos; i++) {
                criarPassaro(x);
                x += 150 + sorteio.nextInt(120);
            }
            extra = (quantos - 1) * 200;
        } else {
            boolean grande = sorteio.nextBoolean();          // cacto grande ou pequeno
            int largura = grande ? CACTO_GRANDE_L : CACTO_PEQUENO_L;
            int altura = grande ? CACTO_GRANDE_A : CACTO_PEQUENO_A;
            adicionar(new Obstaculo(LARGURA, CHAO_Y - altura, largura, altura, velocidade, false));
        }

        // Distância até o próximo: um pulo dura uns 38 frames, então quanto mais
        // rápido, maior o espaço, para sempre dar tempo de pousar e pular de novo
        int distancia = 450 + velocidade * 30 + sorteio.nextInt(300) + extra;
        framesAteProximo = distancia / velocidade;
    }

    /**
     * Pássaro com altura e velocidade sorteadas. Se a base fica abaixo de 50 px o dino
     * em pé bate (é preciso pular); mais alto que isso ele passa por baixo sem fazer nada.
     */
    private void criarPassaro(int x) {
        int base = PASSARO_ALTURA_MIN + sorteio.nextInt(PASSARO_ALTURA_MAX - PASSARO_ALTURA_MIN + 1);
        int velocidadeDoPassaro = velocidade + sorteio.nextInt(3);      // voa um pouco mais rápido que o chão
        adicionar(new Obstaculo(x, CHAO_Y - base - PASSARO_A, PASSARO_L, PASSARO_A, velocidadeDoPassaro, true));
    }

    private void adicionar(Obstaculo o) {
        obstaculos.add(o);
        objetos.add(o);
    }

    /** Alterna dia e noite (cores invertidas) a cada 1650 pontos; o meteoro e a explosão são sempre de noite. */
    private boolean modoNoturno() {
        if (meteoro != null) {
            return true;
        }
        return (pontuacao / PONTOS_TROCA_DIA_NOITE) % 2 == 1;
    }

    private void verificarColisoes() {
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

        astro = new Ceu();
        objetos.add(astro);        // depois o sol/lua e as nuvens, ainda atrás do jogador
        nuvens = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            criarNuvem(120 + i * 270 + sorteio.nextInt(100), 25 + sorteio.nextInt(140));
        }
        framesAteNuvem = 120;
        astro.ajustar(0, false);

        // x, y (topo do dino quando está no chão), largura, altura
        jogador = new Jogador(JOGADOR_X, CHAO_Y - JOGADOR_TAMANHO, JOGADOR_TAMANHO, JOGADOR_TAMANHO);
        objetos.add(jogador);

        obstaculos = new ArrayList<>();
        framesAteProximo = 60;   // 1 segundo até o primeiro

        meteoro = null;
        mensagem = null;
        framesMensagem = 0;
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
            // 1 ponto = 10 mil anos, então 100 pontos = 1 milhão de anos
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            g.drawString(String.format("%.1f", pontuacao / 100.0).replace('.', ',') + " mi anos", LARGURA - 112, 48);

            if (estado == Estado.GAME_OVER) {
                g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
                g.drawString("GAME OVER", LARGURA / 2 - 75, ALTURA / 2 - 20);
                g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
                g.drawString("Aperte ENTER para jogar de novo", LARGURA / 2 - 145, ALTURA / 2 + 10);
            }

            if (framesMensagem > 0 && mensagem != null) {
                g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
                FontMetrics medidas = g.getFontMetrics();
                g.drawString(mensagem, (LARGURA - medidas.stringWidth(mensagem)) / 2, ALTURA / 2 - 80);
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
        } else if (tecla == KeyEvent.VK_ENTER) {   // GAME_OVER: só o Enter vale
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
