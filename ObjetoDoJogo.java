import java.awt.Graphics;
import java.awt.Rectangle;

public abstract class ObjetoDoJogo {
    private int x;
    private int y;
    private int largura;
    private int altura;

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

    public int getY() {
        return y;
    }

    public int getLargura() {
        return largura;
    }

    public int getAltura() {
        return altura;
    }

    protected void setX(int x) {
        this.x = x;
    }

    protected void setY(int y) {
        this.y = y;
    }

    public Rectangle getHitbox() {
        return new Rectangle(x, y, largura, altura);
    }

    public boolean colideCom(ObjetoDoJogo outro) {
        return getHitbox().intersects(outro.getHitbox());
    }
}