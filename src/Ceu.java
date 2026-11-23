import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

/**
 * Tudo o que fica no céu, atrás do jogo: o sol (de dia), a lua (de noite) e as nuvens.
 * É uma classe só porque os dois funcionam igual: são desenhos de pixels que mudam de cor
 * quando vira noite. A diferença é só o movimento, escolhido pelo atributo "astro".
 */
public class Ceu extends ObjetoDoJogo {

    // ---------- Desenhos (pixel art) ----------

    private static final String[] NUVEM_A = {
        "............wwww..............",
        "...........wwwwww.............",
        "..........wwbbbbww.wwww.......",
        "......wwwwwbbbbbbwwwwwww......",
        ".....wwwwwbbbbbbbbwbbbbww.....",
        ".....wbbbbbbbbbbbbbbbbbbwwww..",
        "....wbbbbbbbbbbbbbbbbbbbbwwww.",
        ".....bbbbbbbbbbbbbbbbbbbbbbbw.",
        ".....bbbbbbbbbbbbbbbbbbbbbbbb.",
        "......bbbbbb....bbbbbbb.bbbb.."
    };

    private static final String[] NUVEM_B = {
        "..........wwww..........",
        ".........wwwwww.........",
        "........wwbbbbww.ww.....",
        "....wwwwwbbbbbbwwwww....",
        "...wwwwwbbbbbbbbwbbww...",
        "...wbbbbbbbbbbbbbbbbwww.",
        "...bbbbbbbbbbbbbbbbbbww.",
        "....bbbb...bb...bbbbbbb."
    };

    private static final String[] NUVEM_C = {
        "...................wwwwww...........",
        "...........wwwwww.wwwwwwww..........",
        "..........wwwwwwwwwbbbbbbww.........",
        ".........wwbbbbbbwbbbbbbbbw.........",
        ".....wwwwwbbbbbbbbbbbbbbbbb.wwww....",
        "....wwwwwbbbbbbbbbbbbbbbbbbwwwwww...",
        "....wbbbbbbbbbbbbbbbbbbbbbbwbbbbw...",
        "...wbbbbbbbbbbbbbbbbbbbbbbbbbbbbbw..",
        "....bbbbbbbbbbbbbbbbbbbbbbbbbbbbb...",
        "....bbbbbb.bbbbbbbbbbbbbbbbbbbbbb...",
        ".....bbbb......bbbbbb..bbbbbbbbb...."
    };

    private static final String[][] FORMAS = { NUVEM_A, NUVEM_B, NUVEM_C };

    private static final String[] SOL = {
        ".....................",
        "..........r..........",
        "..........r..........",
        "..........r..........",
        "....r...........r....",
        ".....r...yyy...r.....",
        ".......yyyyyyy.......",
        "......yyysssyyy......",
        "......yysssssyy......",
        ".....yysssssssyy.....",
        ".rrr.yysssssssyy.rrr.",
        ".....yysssssssyy.....",
        "......yysssssyy......",
        "......yyysssyyy......",
        ".......yyyyyyy.......",
        ".....r...yyy...r.....",
        "....r...........r....",
        "..........r..........",
        "..........r..........",
        "..........r..........",
        "....................."
    };

    private static final String[] LUA = {
        "...................",
        ".......mmmmm.......",
        ".....mmmmmmmmm.....",
        "...mmmmmmmmmmmmm...",
        "...mmcccmmmmmcmm...",
        "..mmcccccmmmcccmm..",
        "..mmcccccmmmmcmmm..",
        ".mmmcccccmmmmmmmmm.",
        ".mmmmcccmmmcccmmmm.",
        ".mmmmmmmmmcccccmmm.",
        ".mmmmmmmmmcccccmmm.",
        ".mmmmmmmmmcccccmmm.",
        "..mmmmcccmmcccmmm..",
        "..mmmmcccmmmmmmmm..",
        "...mmmcccmmmmmmm...",
        "...mmmmmmmmmmmmm...",
        ".....mmmmmmmmm.....",
        ".......mmmmm.......",
        "..................."
    };

    private static final int ESCALA_NUVEM = 3;
    private static final int ESCALA_ASTRO = 2;

    // nuvem: tons claros (à noite o painel inverte as cores, e estes cinzas ficam escuros)
    private static final Color COR_CORPO = new Color(176, 208, 240);
    private static final Color COR_BRILHO = new Color(232, 242, 252);
    private static final Color COR_CORPO_NOITE = new Color(205, 205, 215);
    private static final Color COR_BRILHO_NOITE = new Color(170, 170, 185);

    // sol e lua
    private static final Color SOL_NUCLEO = new Color(255, 232, 90);
    private static final Color SOL_BORDA = new Color(255, 196, 0);
    private static final Color SOL_RAIO_A = new Color(255, 170, 0);
    private static final Color SOL_RAIO_B = new Color(255, 215, 60);
    private static final Color LUA_CORPO = new Color(70, 70, 75);
    private static final Color LUA_CRATERA = new Color(118, 118, 124);

    private final boolean astro;          // true: sol/lua; false: nuvem
    private final String[] desenho;       // só da nuvem
    private final double velocidade;      // só da nuvem
    private double posX;
    private boolean noite;
    private int passos;                   // faz os raios do sol piscarem

    /** Cria o sol/lua. A posição no céu é ajustada depois, por ajustar(). */
    public Ceu() {
        super(0, 0, SOL[0].length() * ESCALA_ASTRO, SOL.length * ESCALA_ASTRO);
        this.astro = true;
        this.desenho = null;
        this.velocidade = 0;
    }

    /** Cria uma nuvem: sorteia o formato; a velocidade é pequena (0,4 a 1,3 pixel por passo), bem mais lenta que o chão. */
    public Ceu(int x, int y, Random sorteio) {
        this(FORMAS[sorteio.nextInt(FORMAS.length)], x, y, 0.4 + sorteio.nextDouble() * 0.9);
    }

    private Ceu(String[] desenho, int x, int y, double velocidade) {
        super(x, y, desenho[0].length() * ESCALA_NUVEM, desenho.length * ESCALA_NUVEM);
        this.astro = false;
        this.desenho = desenho;
        this.velocidade = velocidade;
        this.posX = x;
    }

    /** O PainelJogo avisa quando vira noite: a nuvem troca de cor e o sol vira lua. */
    public void setNoite(boolean noite) {
        this.noite = noite;
    }

    /** Só do sol/lua: progresso vai de 0 a 1 durante a fase e muda a posição no céu. */
    public void ajustar(double progresso, boolean noite) {
        this.noite = noite;
        setX((int) (740 - progresso * 680));
        setY((int) (140 - 100 * Math.sin(Math.PI * progresso)));
    }

    @Override
    public void atualizar() {
        if (astro) {
            passos++;
        } else {
            posX -= velocidade;
            setX((int) Math.round(posX));
        }
    }

    @Override
    public void desenhar(Graphics g) {
        if (astro) {
            desenharPixels(g, noite ? LUA : SOL, getX(), getY(), ESCALA_ASTRO);
        } else {
            desenharPixels(g, desenho, getX(), getY(), ESCALA_NUVEM);
        }
    }

    @Override
    protected Color corDoPixel(char letra) {
        switch (letra) {
            case 'w': return noite ? COR_BRILHO_NOITE : COR_BRILHO;
            case 'b': return noite ? COR_CORPO_NOITE : COR_CORPO;
            case 's': return SOL_NUCLEO;
            case 'y': return SOL_BORDA;
            case 'r': return (passos / 20) % 2 == 0 ? SOL_RAIO_A : SOL_RAIO_B;
            case 'm': return LUA_CORPO;
            case 'c': return LUA_CRATERA;
            default: return null;
        }
    }

    public boolean saiuDaTela() {
        return getX() + getLargura() < 0;
    }
}
