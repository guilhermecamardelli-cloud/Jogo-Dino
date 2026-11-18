import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Jogador extends ObjetoDoJogo {

    private static final double GRAVIDADE = 0.8;
    private static final double FORCA_PULO = -15.0;

    private double velocidadeY;
    private boolean noChao;
    private boolean abaixado;
    private final int chaoY;

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
        if (!noChao) {
            y += (int) Math.round(velocidadeY);
            velocidadeY += GRAVIDADE;

            if (y >= chaoY) {
                y = chaoY;
                velocidadeY = 0;
                noChao = true;
            }
        }
    }

    @Override
    public void desenhar(Graphics g) {
        g.setColor(new Color(83, 83, 83));
        if (abaixado) {
            int hAbaixado = altura / 2;
            g.fillRect(x, y + hAbaixado, largura + 10, hAbaixado);
        } else {
            g.fillRect(x, y, largura, altura);
        }
    }

    @Override
    public Rectangle getHitbox() {
        if (abaixado) {
            int hAbaixado = altura / 2;
            return new Rectangle(x, y + hAbaixado, largura + 10, hAbaixado);
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
