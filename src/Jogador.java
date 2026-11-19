import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Jogador extends ObjetoDoJogo {

    private static final double GRAVIDADE = 0.8;
    private static final double FORCA_PULO = -15.0;

    // Desenho em pixels: '#' = pixel pintado, 'o' = olho, '.' = vazio. Cada pixel vale 2x2 na tela.
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

    private static final Color COR_DINO = new Color(83, 83, 83);
    private static final Color COR_OSSO = new Color(60, 60, 60);   // fica claro no modo noturno

    private double velocidadeY;
    private boolean noChao;
    private boolean abaixado;
    private final int chaoY;
    private boolean esqueleto;
    private int passos;

    public Jogador(int x, int y, int largura, int altura) {
        super(x, y, largura, altura);
        this.chaoY = y;
        this.velocidadeY = 0;
        this.noChao = true;
        this.abaixado = false;
    }

    public void pular() {
        if (noChao) {
            velocidadeY = FORCA_PULO;
            noChao = false;
            abaixado = false;
        }
    }

    public void abaixar(boolean ativo) {
        if (ativo) {
            if (noChao) {
                abaixado = true;
            }
        } else {
            abaixado = false;
        }
    }

    @Override
    public void atualizar() {
        passos++;
        if (!noChao) {
            setY(getY() + (int) Math.round(velocidadeY));
            velocidadeY += GRAVIDADE;

            if (getY() >= chaoY) {
                setY(chaoY);
                velocidadeY = 0;
                noChao = true;
            }
        }
    }

    /** Depois do meteoro, o dinossauro vira esqueleto. */
    public void virarEsqueleto() {
        esqueleto = true;
        abaixado = false;
    }

    public boolean isEsqueleto() {
        return esqueleto;
    }

    @Override
    public void desenhar(Graphics g) {
        int escala = Math.max(1, getLargura() / 25);
        if (esqueleto) {
            desenharPixels(g, ESQUELETO, getX(), getY() + getAltura() - ESQUELETO.length * escala, escala, COR_OSSO);
        } else if (abaixado) {
            String[] quadro = (passos / 6) % 2 == 0 ? ABAIXADO_A : ABAIXADO_B;
            desenharPixels(g, quadro, getX(), getY() + getAltura() - quadro.length * escala, escala, COR_DINO);
        } else {
            String[] quadro = DINO_C;                       // no ar: pernas paradas
            if (noChao) {
                quadro = (passos / 6) % 2 == 0 ? DINO_A : DINO_B;   // correndo: troca as pernas
            }
            desenharPixels(g, quadro, getX(), getY(), escala, COR_DINO);
        }
    }

    /** Pinta o desenho pixel por pixel; o olho 'o' fica da cor do fundo. */
    private void desenharPixels(Graphics g, String[] desenho, int x, int y, int escala, Color cor) {
        for (int linha = 0; linha < desenho.length; linha++) {
            String texto = desenho[linha];
            for (int coluna = 0; coluna < texto.length(); coluna++) {
                char c = texto.charAt(coluna);
                if (c == '#') {
                    g.setColor(cor);
                } else if (c == 'o') {
                    g.setColor(Color.WHITE);
                } else {
                    continue;
                }
                g.fillRect(x + coluna * escala, y + linha * escala, escala, escala);
            }
        }
    }

    @Override
    public Rectangle getHitbox() {
        if (abaixado) {
            int hAbaixado = getAltura() / 2;
            return new Rectangle(getX(), getY() + hAbaixado, getLargura() + 10, hAbaixado);
        }
        return super.getHitbox();
    }

    public boolean isNoChao() {
        return noChao;
    }

    public boolean isAbaixado() {
        return abaixado;
    }
}
