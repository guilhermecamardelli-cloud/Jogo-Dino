import java.awt.Graphics;
import java.awt.Rectangle;

public abstract class ObjetoDoJogo {
    protected int x;
    protected int y;
    protected int largura;
    protected int altura;

    public ObjetoDoJogo(int x, int y, int largura, int altura) {
        if (largura <= 0 || altura <= 0) {
            throw new IllegalArgumentException("Largura e altura devem ser maiores que zero.");
        }
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
    }

    public abstract void atualizar();

    public abstract void desenhar(Graphics g);

    public int getX() {
        return x;
    }

    protected void setX(int x) {
        this.x = x;
    }

    public Rectangle getHitbox() {
        return new Rectangle(x, y, largura, altura);
    }

    public boolean colideCom(ObjetoDoJogo outro) {
        return getHitbox().intersects(outro.getHitbox());
    }
}