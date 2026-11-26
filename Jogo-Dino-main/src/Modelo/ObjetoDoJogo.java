package Modelo;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/** Classe mãe de tudo o que aparece no jogo. Guarda posição e tamanho e define o que todos sabem fazer. */
public abstract class ObjetoDoJogo {
    // Encapsulamento: atributos privados, acessados só por métodos
    private int x;
    private int y;
    private int largura;
    private int altura;

    // Construtor valida o tamanho: objeto sem largura/altura não faz sentido
    public ObjetoDoJogo(int x, int y, int largura, int altura) {
        if (largura <= 0 || altura <= 0) {
            throw new IllegalArgumentException("Largura e altura devem ser maiores que zero.");
        }
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
    }

    // Cada filha faz do seu jeito (polimorfismo)
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

    // protected: só as filhas mudam a posição
    protected void setX(int x) {
        this.x = x;
    }

    protected void setY(int y) {
        this.y = y;
    }

    // A caixa usada na colisão; a filha pode sobrescrever
    public Rectangle getHitbox() {
        return new Rectangle(x, y, largura, altura);
    }

    // Colisão: as duas caixas (hitbox) se cruzam?
    public boolean colideCom(ObjetoDoJogo outro) {
        return getHitbox().intersects(outro.getHitbox());
    }

    /** Devolve a cor de cada letra do desenho. Quem usa pixel art sobrescreve; '.' (null) é vazio. */
    protected Color corDoPixel(char letra) {
        return null;
    }

    /** Desenha um texto por linha, um quadradinho por letra, usando corDoPixel() para escolher a cor. */
    protected void desenharPixels(Graphics g, String[] desenho, int x, int y, int escala) {
        for (int linha = 0; linha < desenho.length; linha++) {
            for (int coluna = 0; coluna < desenho[linha].length(); coluna++) {
                Color cor = corDoPixel(desenho[linha].charAt(coluna));
                if (cor != null) {
                    g.setColor(cor);
                    g.fillRect(x + coluna * escala, y + linha * escala, escala, escala);
                }
            }
        }
    }
}
