import java.awt.Color;
import java.awt.Graphics;

/**
 * Meteoro que cai aos 6600 pontos (66 milhões de anos). Primeiro cai até o alvo;
 * depois a explosão cresce e some. As cores já pensam no modo noturno (que inverte tudo).
 */
public class Meteoro extends ObjetoDoJogo {

    private static final int PASSOS_ATE_O_IMPACTO = 80;
    private static final int RAIO_MAXIMO_EXPLOSAO = 90;

    private static final Color COR_NUCLEO = new Color(0, 40, 160);    // aparece amarelo
    private static final Color COR_FOGO = new Color(0, 130, 255);     // aparece laranja
    private static final Color COR_RASTRO = new Color(0, 200, 255);   // aparece vermelho
    private static final Color COR_EXPLOSAO = new Color(0, 90, 200);  // aparece laranja forte

    private double posX;
    private double posY;
    private final double velocidadeX;
    private final double velocidadeY;
    private final int alvoX;
    private final int alvoY;
    private int passosRestantes = PASSOS_ATE_O_IMPACTO;
    private int raioExplosao;
    private boolean explosaoCrescendo = true;

    /** Começa em (xInicial, yInicial) e cai em direção ao ponto (alvoX, alvoY). */
    public Meteoro(int xInicial, int yInicial, int alvoX, int alvoY) {
        super(xInicial, yInicial, 40, 40);
        this.posX = xInicial;
        this.posY = yInicial;
        this.alvoX = alvoX;
        this.alvoY = alvoY;
        this.velocidadeX = (alvoX - xInicial) / (double) PASSOS_ATE_O_IMPACTO;
        this.velocidadeY = (alvoY - yInicial) / (double) PASSOS_ATE_O_IMPACTO;
    }

    @Override
    public void atualizar() {
        if (passosRestantes > 0) {                          // 1) caindo
            posX += velocidadeX;
            posY += velocidadeY;
            setX((int) posX);
            setY((int) posY);
            passosRestantes--;
        } else if (explosaoCrescendo) {                     // 2) a explosão cresce...
            raioExplosao += 3;
            if (raioExplosao >= RAIO_MAXIMO_EXPLOSAO) {
                explosaoCrescendo = false;
            }
        } else {                                            // 3) ...e depois some
            raioExplosao = Math.max(0, raioExplosao - 3);
        }
    }

    @Override
    public void desenhar(Graphics g) {
        if (!bateu()) {
            int centroX = getX() + getLargura() / 2;
            int centroY = getY() + getAltura() / 2;
            // rastro: bolas menores para trás (de onde o meteoro veio)
            for (int i = 6; i >= 1; i--) {
                bola(g, centroX - (int) (velocidadeX * i * 3), centroY - (int) (velocidadeY * i * 3),
                        getLargura() - i * 5, COR_RASTRO);
            }
            bola(g, centroX, centroY, getLargura(), COR_FOGO);
            bola(g, centroX, centroY, 20, COR_NUCLEO);
        } else {
            // explosão: três bolas, uma dentro da outra
            bola(g, alvoX, alvoY, raioExplosao * 2, COR_EXPLOSAO);
            bola(g, alvoX, alvoY, 2 * (raioExplosao * 6 / 10), COR_FOGO);
            bola(g, alvoX, alvoY, 2 * (raioExplosao * 3 / 10), COR_NUCLEO);
        }
    }

    /** Desenha uma bola de cor sólida a partir do centro. */
    private void bola(Graphics g, int centroX, int centroY, int diametro, Color cor) {
        g.setColor(cor);
        g.fillOval(centroX - diametro / 2, centroY - diametro / 2, diametro, diametro);
    }

    /** True a partir do momento em que o meteoro bate no chão. */
    public boolean bateu() {
        return passosRestantes <= 0;
    }

    /** True quando a explosão já cresceu e sumiu, deixando o esqueleto à mostra. */
    public boolean explosaoTerminou() {
        return bateu() && !explosaoCrescendo && raioExplosao == 0;
    }
}
