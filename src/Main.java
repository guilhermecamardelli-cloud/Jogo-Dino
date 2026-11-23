import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame("Jogo do Dinossauro");

            janela.add(new TelaInicio(janela));  // primeiro aparece a tela de início
            janela.pack();                       // usa o tamanho que o painel pede (800 x 400)
            janela.setResizable(false);
            janela.setLocationRelativeTo(null);
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.setVisible(true);
        });
    }

    /** Chamado pelo botão START GAME: troca a tela de início pelo jogo, na mesma janela. */
    public static void abrirJogo(JFrame janela) {
        PainelJogo painel = new PainelJogo();

        janela.getRootPane().setDefaultButton(null);   // o ENTER volta a ser só do jogo
        janela.getContentPane().removeAll();
        janela.add(painel);
        janela.pack();

        painel.iniciar();                        // começa o loop do jogo
        painel.requestFocusInWindow();
    }
}
