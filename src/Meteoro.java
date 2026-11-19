import java.awt.Color;
import java.awt.Graphics;

/**
 * Meteoro que cai nos 6600 pontos (66 milhões de anos) e extingue o dinossauro.
 * Herda de ObjetoDoJogo: atualizar() faz ele cair e desenhar() mostra a bola de fogo.
 * As cores são escolhidas já pensando no modo noturno (que inverte tudo): azul vira laranja.
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
    private int passosRestantes;
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
        this.passosRestantes = PASSOS_ATE_O_IMPACTO;
        this.raioExplosao = 0;
    }

    @Override
    public void atualizar() {
        if (passosRestantes > 0) {
            posX += velocidadeX;
            posY += velocidadeY;
            setX((int) posX);
            setY((int) posY);
            passosRestantes--;
        } else if (explosaoCrescendo) {
            raioExplosao += 3;                       // a explosão cresce...
            if (raioExplosao >= RAIO_MAXIMO_EXPLOSAO) {
                explosaoCrescendo = false;
            }
        } else if (raioExplosao > 0) {
            raioExplosao = Math.max(0, raioExplosao - 3);   // ...e depois some
        }
    }

    @Override
    public void desenhar(Graphics g) {
        if (passosRestantes > 0) {
            int centroX = getX() + getLargura() / 2;
            int centroY = getY() + getAltura() / 2;
            // rastro: bolas menores para trás (de onde o meteoro veio)
            for (int i = 6; i >= 1; i--) {
                int rx = centroX - (int) (velocidadeX * i * 3);
                int ry = centroY - (int) (velocidadeY * i * 3);
                int tam = getLargura() - i * 5;
                g.setColor(COR_RASTRO);
                g.fillOval(rx - tam / 2, ry - tam / 2, tam, tam);
            }
            g.setColor(COR_FOGO);
            g.fillOval(getX(), getY(), getLargura(), getAltura());
            g.setColor(COR_NUCLEO);
            g.fillOval(getX() + 10, getY() + 10, getLargura() - 20, getLargura() - 20);
        } else if (raioExplosao > 0) {
            g.setColor(COR_EXPLOSAO);
            g.fillOval(alvoX - raioExplosao, alvoY - raioExplosao, raioExplosao * 2, raioExplosao * 2);
            g.setColor(COR_FOGO);
            int menor = raioExplosao * 6 / 10;
            g.fillOval(alvoX - menor, alvoY - menor, menor * 2, menor * 2);
            g.setColor(COR_NUCLEO);
            int centro = raioExplosao * 3 / 10;
            g.fillOval(alvoX - centro, alvoY - centro, centro * 2, centro * 2);
        }
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
