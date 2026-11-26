package Modelo;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * O personagem controlado pelo jogador. Pula, se abaixa e sofre gravidade.
 * Com o passar dos "anos" ele muda de forma: dino, esqueleto, avestruz e barril.
 * Os desenhos ficam na classe 
 */
public class Jogador extends ObjetoDoJogo {

    public enum Forma { DINO, ESQUELETO, AVESTRUZ, BARRIL }

    private static final double GRAVIDADE = 0.8;
    private static final double FORCA_PULO = -16.0;
    private static final int ESCALA = 2;                  // cada pixel do desenho vira 2x2 na tela
    private static final int FRAMES_PISCANDO = 45;        // pisca logo depois de mudar de forma

    private static final Color COR_DINO = new Color(83, 83, 83);
    private static final Color COR_OSSO = new Color(60, 60, 60);   // fica claro no modo noturno

    private double velocidadeY;
    private boolean noChao = true;
    private boolean abaixado;
    private final int chaoY;                               // o y inicial, onde ele pousa
    private Forma forma = Forma.DINO;
    private int passos;                                    // conta os quadros, para animar as pernas
    private int piscando;

    // chaoY guarda a altura do chão: é onde o pulo termina
    public Jogador(int x, int y, int largura, int altura) {
        super(x, y, largura, altura);
        this.chaoY = y;
    }

    // ---------- Movimento ----------

    // Pulo: dá um impulso para cima; a gravidade em atualizar() faz cair
    public void pular() {
        if (noChao) {                                      // no ar, ignora
            velocidadeY = FORCA_PULO;
            noChao = false;
            abaixado = false;
        }
    }

    /** Todas as formas se abaixam, mas só no chão. Soltar a tecla levanta. */
    public void abaixar(boolean ativo) {
        abaixado = ativo && noChao;
    }

    // Cada passo: aplica a gravidade enquanto estiver no ar
    @Override
    public void atualizar() {
        passos++;
        if (piscando > 0) {
            piscando--;
        }
        if (!noChao) {
            setY(getY() + (int) Math.round(velocidadeY));
            velocidadeY += GRAVIDADE;
            if (getY() >= chaoY) {                         // pousou
                setY(chaoY);
                velocidadeY = 0;
                noChao = true;
            }
        }
    }

    /** Troca a forma (o Jogo chama nos anos certos) e faz o personagem piscar. */
    public void evoluirPara(Forma nova) {
        forma = nova;
        abaixado = false;
        piscando = FRAMES_PISCANDO;
    }

    public Forma getForma() {
        return forma;
    }

    public boolean isNoChao() {
        return noChao;
    }

    public boolean isAbaixado() {
        return abaixado;
    }

    // ---------- Desenho ----------

    @Override
    public void desenhar(Graphics g) {
        if (piscando > 0 && (piscando / 4) % 2 == 1) {
            return;                                        // pisca: some e aparece
        }
        String[] quadro = escolherQuadro();
        int larguraDesenho = quadro[0].length() * ESCALA;
        int alturaDesenho = quadro.length * ESCALA;
        int x = getX() + Math.max(0, (getLargura() - larguraDesenho) / 2);
        int y = getY() + getAltura() - alturaDesenho;      // os pés ficam sempre no chão

        Graphics2D g2 = (Graphics2D) g.create();           // cópia, para as transformações não vazarem
        if (abaixado && forma != Forma.DINO) {
            // só o dino tem desenho abaixado; as outras formas ficam achatadas pela metade
            double centro = getX() + getLargura() / 2.0;
            double chao = getY() + getAltura();
            g2.translate(centro, chao);
            g2.scale(1.2, 0.5);
            g2.translate(-centro, -chao);
        }
        if (forma == Forma.BARRIL && noChao) {
            // o barril balança de um lado para o outro enquanto "corre"
            g2.rotate(Math.toRadians(5 * Math.sin(passos / 4.0)), x + larguraDesenho / 2.0, y + alturaDesenho);
        }
        desenharPixels(g2, quadro, x, y, ESCALA);
        g2.dispose();
    }

    /** Escolhe o desenho certo: depende da forma, de estar no ar ou no chão e de qual perna está à frente. */
    private String[] escolherQuadro() {
        boolean pernaA = (passos / 6) % 2 == 0;            // troca de perna a cada 6 quadros
        switch (forma) {
            case ESQUELETO:
                return noChao && !pernaA ? ESQUELETO_B : ESQUELETO;
            case AVESTRUZ:
                if (!noChao) {
                    return AVESTRUZ_C;            // no ar: pernas paradas
                }
                return pernaA ? AVESTRUZ_A : AVESTRUZ_B;
            case BARRIL:
                return BARRIL;
            default:
                if (abaixado) {
                    return pernaA ? ABAIXADO_A : ABAIXADO_B;
                }
                if (!noChao) {
                    return DINO_C;
                }
                return pernaA ? DINO_A : DINO_B;
        }
    }

    /** Cada letra do desenho vira uma cor. */
    @Override
    protected Color corDoPixel(char letra) {
        switch (letra) {
            case '#': return forma == Forma.ESQUELETO ? COR_OSSO : COR_DINO;
            case 'o': return forma == Forma.AVESTRUZ ? Color.BLACK : Color.WHITE;   // olho
            case 'b': return new Color(70, 85, 140);       // avestruz: corpo
            case 'n': return new Color(236, 216, 176);     // avestruz: pescoço e penas
            case 'k': return new Color(225, 80, 40);       // avestruz: bico
            case 'l': return new Color(225, 140, 80);      // avestruz: pernas
            case 'd': return new Color(35, 35, 40);        // barril: ferro
            case 'g': return new Color(110, 110, 115);     // barril: aros
            case 'h': return new Color(150, 150, 160);     // barril: brilho
            case 'y': return new Color(230, 130, 20);      // barril: faixa
            default: return null;
        }
    }

    // ---------- Colisão ----------

    // Sobrescreve a mãe (polimorfismo): a caixa muda se abaixado ou virando barril
    @Override
    public Rectangle getHitbox() {
        if (abaixado) {
            int metade = getAltura() / 2;
            return new Rectangle(getX(), getY() + metade, getLargura() + 10, metade);
        }
        if (forma == Forma.BARRIL) {
            // o barril é mais estreito que a caixa de 50 x 50
            int largura = BARRIL[0].length() * ESCALA;
            int altura = BARRIL.length * ESCALA;
            return new Rectangle(getX() + (getLargura() - largura) / 2, getY() + getAltura() - altura, largura, altura);
        }
        // um pouco menor que o desenho: bater de raspão não conta
        return new Rectangle(getX() + 6, getY() + 4, getLargura() - 12, getAltura() - 8);
    }

    // ---------- Desenhos (pixel art) ----------

    private static final String[] DINO_A = {
        "...............#########.",
        "..............###########",
        "..............##o########",
        "..............###########",
        "..............###########",
        "..............########...",
        "..............#####......",
        "..............####.......",
        "#............#####.......",
        "##...........#####.......",
        "###.........######.......",
        "####.......#######.#.....",
        "#####.....########.##....",
        "#####...###########.#....",
        ".#####.#############.....",
        "..##################.....",
        "...#################.....",
        "....################.....",
        ".....##############......",
        "......############.......",
        "........###..###.........",
        "........###..###.........",
        "........###..............",
        "........###..............",
        "........####............."
    };

    private static final String[] DINO_B = {
        "...............#########.",
        "..............###########",
        "..............##o########",
        "..............###########",
        "..............###########",
        "..............########...",
        "..............#####......",
        "..............####.......",
        "#............#####.......",
        "##...........#####.......",
        "###.........######.......",
        "####.......#######.#.....",
        "#####.....########.##....",
        "#####...###########.#....",
        ".#####.#############.....",
        "..##################.....",
        "...#################.....",
        "....################.....",
        ".....##############......",
        "......############.......",
        "........###..###.........",
        "........###..###.........",
        "..............###........",
        "..............###........",
        "..............####......."
    };

    private static final String[] DINO_C = {
        "...............#########.",
        "..............###########",
        "..............##o########",
        "..............###########",
        "..............###########",
        "..............########...",
        "..............#####......",
        "..............####.......",
        "#............#####.......",
        "##...........#####.......",
        "###.........######.......",
        "####.......#######.#.....",
        "#####.....########.##....",
        "#####...###########.#....",
        ".#####.#############.....",
        "..##################.....",
        "...#################.....",
        "....################.....",
        ".....##############......",
        "......############.......",
        "........###..###.........",
        "........###..###.........",
        "........###..###.........",
        "........###..###.........",
        "........####.####........"
    };

    private static final String[] ABAIXADO_A = {
        ".....................#########",
        ".....................##o######",
        ".....................#########",
        "...##############....#######..",
        ".########################.....",
        "#########################.....",
        ".######################.......",
        "...###################........",
        ".....###############..........",
        ".......###....###.............",
        ".......###....###.............",
        ".......###....###............."
    };

    private static final String[] ABAIXADO_B = {
        ".....................#########",
        ".....................##o######",
        ".....................#########",
        "...##############....#######..",
        ".########################.....",
        "#########################.....",
        ".######################.......",
        "...###################........",
        ".....###############..........",
        ".......###....###.............",
        ".......###....................",
        ".......###....###............."
    };

    private static final String[] ESQUELETO = {
        "..............#########..",
        ".............###########.",
        ".............##...######.",
        ".............###########.",
        ".............#.#.#.#.#.#.",
        "..............#.#.#.#....",
        "..............##.........",
        ".............##..........",
        "............##..#........",
        "#..........##..###.......",
        ".#........##..#.#.#......",
        "..#......##..#######.#...",
        "...##...##..#.#.#.#.##...",
        "....###.#..#######..#....",
        "......###..#.#.#.#.......",
        ".......##..#######.......",
        ".........###.#.#.#.......",
        "..........##.....##......",
        "...........#######.......",
        "........#...#...#........",
        "........#...#...#........",
        "........#...#...#........",
        "........##..##..##.......",
        "........#...#...#........",
        "........###.###.###......"
    };

    private static final String[] ESQUELETO_B = {
        "..............#########..",
        ".............###########.",
        ".............##...######.",
        ".............###########.",
        ".............#.#.#.#.#.#.",
        "..............#.#.#.#....",
        "..............##.........",
        ".............##..........",
        "............##..#........",
        "#..........##..###.......",
        ".#........##..#.#.#......",
        "..#......##..#######.#...",
        "...##...##..#.#.#.#.##...",
        "....###.#..#######..#....",
        "......###..#.#.#.#.......",
        ".......##..#######.......",
        ".........###.#.#.#.......",
        "..........##.....##......",
        "...........#######.......",
        "........#...#...#........",
        "........#...#...#........",
        "........#...#...#........",
        "........#...#............",
        "........#...#............",
        "........###.###.........."
    };

    private static final String[] AVESTRUZ_A = {
        ".................nnnn....",
        ".................nonnkkk.",
        ".................nnnnkk..",
        "..................nnn....",
        "..................nn.....",
        "..................nn.....",
        "..................nn.....",
        "..................nn.....",
        "..................nn.....",
        "..................nn.....",
        "..................nn.....",
        ".tt...bbbbbbbbbbbbnn.....",
        "ttt..bbbbbbbbbbbbbbbb....",
        "tttt.bbbbbbbbbbbbbbbb....",
        ".ttbbbbbbbbbbbbbbbbbb....",
        "..ttbbbbbbbbbbbbbbbb.....",
        "....bbbbbbbbbbbbbb.......",
        "......bbbbbbbbbb.........",
        "..........ll..ll.........",
        ".........ll....ll........",
        ".........ll.....ll.......",
        "........ll......ll.......",
        "........ll......ll.......",
        "........ll......ll.......",
        "........lll.....lll......"
    };

    private static final String[] AVESTRUZ_B = {
        ".................nnnn....",
        ".................nonnkkk.",
        ".................nnnnkk..",
        "..................nnn....",
        "..................nn.....",
        "..................nn.....",
        "..................nn.....",
        "..................nn.....",
        "..................nn.....",
        "..................nn.....",
        "..................nn.....",
        ".tt...bbbbbbbbbbbbnn.....",
        "ttt..bbbbbbbbbbbbbbbb....",
        "tttt.bbbbbbbbbbbbbbbb....",
        ".ttbbbbbbbbbbbbbbbbbb....",
        "..ttbbbbbbbbbbbbbbbb.....",
        "....bbbbbbbbbbbbbb.......",
        "......bbbbbbbbbb.........",
        "..........ll..ll.........",
        "...........ll.ll.........",
        "............ll.ll........",
        "............ll..ll.......",
        "............ll..ll.......",
        "............ll..ll.......",
        "............lll.lll......"
    };

    private static final String[] AVESTRUZ_C = {
        ".................nnnn....",
        ".................nonnkkk.",
        ".................nnnnkk..",
        "..................nnn....",
        "..................nn.....",
        "..................nn.....",
        "..................nn.....",
        "..................nn.....",
        "..................nn.....",
        "..................nn.....",
        "..................nn.....",
        ".tt...bbbbbbbbbbbbnn.....",
        "ttt..bbbbbbbbbbbbbbbb....",
        "tttt.bbbbbbbbbbbbbbbb....",
        ".ttbbbbbbbbbbbbbbbbbb....",
        "..ttbbbbbbbbbbbbbbbb.....",
        "....bbbbbbbbbbbbbb.......",
        "......bbbbbbbbbb.........",
        "..........ll..ll.........",
        "..........ll..ll.........",
        "..........ll..ll.........",
        "..........ll..ll.........",
        "..........ll..ll.........",
        "..........ll..ll.........",
        "..........lll.lll........"
    };

    private static final String[] BARRIL = {
        "..gggggggggggg..",
        ".gddddddddddddg.",
        ".gdhddddddddddg.",
        "gggggggggggggggg",
        "gdhddddddddddddg",
        "gddddddddddddddg",
        "gddddddddddddddg",
        "gddddddddddddddg",
        "gdddyyyydddddddg",
        "gdddyyyydddddddg",
        "gggggggggggggggg",
        "gdhddddddddddddg",
        "gddddddddddddddg",
        "gddddddddddddddg",
        "gddddddddddddddg",
        "gddddddddddddddg",
        "gddddddddddddddg",
        "gggggggggggggggg",
        "gdhddddddddddddg",
        "gddddddddddddddg",
        ".gddddddddddddg.",
        "..gggggggggggg.."
    };
}
