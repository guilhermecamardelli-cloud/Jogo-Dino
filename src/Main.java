import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame("Jogo do Dinossauro");
            PainelJogo painel = new PainelJogo();

            janela.add(painel);
            janela.pack();                       // usa o tamanho que o painel pede (800 x 400)
            janela.setResizable(false);
            janela.setLocationRelativeTo(null);
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.setVisible(true);

            painel.iniciar();                    // começa o loop depois que a janela aparece
        });
    }
}
