package Visao;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Os sons do jogo: a música de fundo (em loop) e o "boing" do pulo.
 * Não usa arquivos de áudio: as ondas são calculadas por código, como nos videogames antigos,
 * e tocadas com a biblioteca padrão do Java (javax.sound.sampled).
 * Se o computador não tiver saída de som, o jogo continua normalmente, só que mudo.
 * Fica na Visão porque, assim como o desenho, o som é uma saída para quem joga.
 */
public class Som {

    private static final float TAXA = 22050f;                      // amostras por segundo
    private static final AudioFormat FORMATO = new AudioFormat(TAXA, 16, 1, true, false);   // 16 bits, mono

    // Melodia em notas MIDI (60 = dó central, 72 = dó uma oitava acima); -1 é pausa.
    // Cada número dura uma colcheia; são 4 compassos de 8 colcheias.
    private static final int[] MELODIA = {
        72, 76, 79, 76,  74, 77, 81, 77,
        72, 76, 79, 84,  83, 79, 74, -1,
        76, 79, 83, 79,  77, 81, 84, 81,
        79, 77, 76, 74,  72, -1, 72, -1
    };
    // Baixo: uma nota a cada 4 colcheias (alterna com a oitava de cima para dar ritmo)
    private static final int[] BAIXO = { 48, 50, 48, 55, 52, 53, 55, 48 };
    private static final double DURACAO_COLCHEIA = 0.18;            // segundos

    private final Clip musica;
    private final Clip pulo;
    private boolean musicaLigada = true;   // o botão MÚSICA e a tecla M mudam isto

    public Som() {
        musica = criarClip(gerarMusica());
        pulo = criarClip(gerarPulo());
    }

    // ---------- O que o PainelJogo usa ----------

    public void tocarPulo() {
        if (pulo == null) {
            return;
        }
        pulo.stop();
        pulo.setFramePosition(0);          // volta ao começo, para tocar de novo a cada pulo
        pulo.start();
    }

    /** Começa a música do início e repete para sempre. */
    public void iniciarMusica() {
        if (musica == null || !musicaLigada) {
            return;
        }
        musica.stop();
        musica.setFramePosition(0);
        musica.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void pararMusica() {
        if (musica != null) {
            musica.stop();
        }
    }

    /**
     * Botão MÚSICA ou tecla M: liga e desliga só a música (o som do pulo continua).
     * Se o jogo estiver rodando, a música volta de onde parou. Devolve true se ficou ligada.
     */
    public boolean alternarMusica(boolean jogoRodando) {
        musicaLigada = !musicaLigada;
        if (!musicaLigada) {
            pararMusica();
        } else if (jogoRodando && musica != null) {
            musica.loop(Clip.LOOP_CONTINUOUSLY);
        }
        return musicaLigada;
    }

    // ---------- Geração das ondas ----------

    /** Pulo: um som que sobe rápido de grave para agudo (350 Hz até 900 Hz) e some. */
    private short[] gerarPulo() {
        int total = (int) (TAXA * 0.15);
        short[] onda = new short[total];
        double fase = 0;
        for (int i = 0; i < total; i++) {
            double progresso = i / (double) total;
            double frequencia = 350 + 550 * progresso;
            fase += frequencia / TAXA;
            double quadrada = (fase % 1) < 0.5 ? 1 : -1;            // onda quadrada: som de 8 bits
            onda[i] = (short) (quadrada * (1 - progresso) * 0.30 * Short.MAX_VALUE);
        }
        return onda;
    }

    /** Música: melodia em onda quadrada + baixo em onda triangular, misturados. */
    private short[] gerarMusica() {
        int porNota = (int) (TAXA * DURACAO_COLCHEIA);
        short[] onda = new short[MELODIA.length * porNota];

        for (int nota = 0; nota < MELODIA.length; nota++) {
            double freqMelodia = MELODIA[nota] < 0 ? 0 : frequencia(MELODIA[nota]);
            int notaBaixo = BAIXO[(nota / 4) % BAIXO.length] + (nota % 2 == 1 ? 12 : 0);
            double freqBaixo = frequencia(notaBaixo);

            for (int j = 0; j < porNota; j++) {
                double t = j / (double) TAXA;
                double progresso = j / (double) porNota;
                double fim = Math.min(1, (1 - progresso) * 20);     // apaga no finalzinho, evita estalos

                double melodia = 0;
                if (freqMelodia > 0) {
                    melodia = ((freqMelodia * t) % 1) < 0.5 ? 1 : -1;
                    melodia *= (1 - 0.6 * progresso) * fim;         // cada nota vai enfraquecendo
                }
                double fase = (freqBaixo * t) % 1;
                double baixo = (4 * Math.abs(fase - 0.5) - 1) * fim;

                double amostra = 0.16 * melodia + 0.24 * baixo;
                onda[nota * porNota + j] = (short) (amostra * Short.MAX_VALUE);
            }
        }
        return onda;
    }

    /** Converte nota MIDI em frequência: o lá 69 é 440 Hz, e cada semitom multiplica por 2^(1/12). */
    private static double frequencia(int notaMidi) {
        return 440 * Math.pow(2, (notaMidi - 69) / 12.0);
    }

    /** Transforma a onda em bytes e abre um Clip (um som pronto para tocar). */
    private Clip criarClip(short[] onda) {
        byte[] bytes = new byte[onda.length * 2];
        for (int i = 0; i < onda.length; i++) {
            bytes[2 * i] = (byte) onda[i];                 // byte de baixo
            bytes[2 * i + 1] = (byte) (onda[i] >> 8);      // byte de cima
        }
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(FORMATO, bytes, 0, bytes.length);
            return clip;
        } catch (Exception e) {
            System.out.println("Som indisponível: " + e.getMessage());
            return null;
        }
    }
}
