import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JPanel;

/**
 * SnakePanel: contiene el ciclo de juego y el dibujado.
 *
 * MODIFICACIONES PARA LA ACTIVIDAD DE HERENCIA:
 * 1) El atributo "comida" ahora es de tipo Comida (clase BASE), pero en
 *    tiempo de ejecucion puede contener objetos Comida, ComidaEspecial o
 *    ComidaVenenosa (POLIMORFISMO). El panel llama siempre a los mismos
 *    metodos (getPuntos, getColor, getNombre) y cada subclase responde
 *    de forma distinta.
 * 2) El atributo "serpiente" ahora es de tipo Serpiente (clase BASE), pero
 *    se instancia como SerpienteFantasma, que agrega un modo especial que
 *    se activa al comer una ComidaEspecial.
 */
public class SnakePanel
        extends JPanel
        implements Runnable {

    private static final int ALTO = 20;
    private static final int ANCHO = 40;
    private static final int CELL_SIZE = 25;
    private static final int PADDING = 10;

    private static final int POWERUP_NINGUNO = 0;
    private static final int POWERUP_VELOCIDAD = 1;
    private static final int POWERUP_PUNTOS = 2;
    private static final int POWERUP_REDUCIR = 3;

    // --- Herencia: se usa el TIPO BASE para permitir polimorfismo ---
    private Serpiente serpiente;
    private Comida comida;

    private int puntaje = 0;
    private int record = 0;
    private int nivel = 1;
    private long velocidad = 150L;
    private boolean pausa = false;
    private boolean enCurso = false;
    private String estadoPantalla = "inicio";
    private Posicion powerUpPos = null;
    private int powerUpTipo = 0;
    private int ticksSinPowerUp = 0;
    private static final int TICKS_PARA_POWERUP = 30;
    private int powerUpDuracion = 0;
    private static final int POWERUP_VIDA = 40;
    private int efectoActivo = 0;
    private int ticksEfecto = 0;
    private static final int DURACION_EFECTO = 20;
    private String proximaDireccion = "right";
    private Thread gameThread;
    private boolean corriendo = true;
    private long tiempoUltimaActualizacion = 0L;
    private static final String ARCHIVO_RECORD = "record.dat";

    private static final Color COLOR_FONDO = new Color(40, 44, 52);
    private static final Color COLOR_SERPIENTE_CABEZA = new Color(76, 175, 80);
    private static final Color COLOR_SERPIENTE_CUERPO = new Color(56, 142, 60);
    private static final Color COLOR_BORDE = new Color(144, 202, 249);
    private static final Color COLOR_TEXTO = new Color(255, 255, 255);
    private static final Color COLOR_PU_VELOCIDAD = new Color(33, 150, 243);
    private static final Color COLOR_PU_PUNTOS = new Color(255, 193, 7);
    private static final Color COLOR_PU_REDUCIR = new Color(156, 39, 176);
    private static final Color COLOR_FANTASMA = new Color(224, 224, 224);

    private Random random = new Random();
    private String mensajeFlotante = "";
    private int ticksMensaje = 0;
    private static final int TICKS_MENSAJE = 15;

    public SnakePanel() {
        this.setPreferredSize(new Dimension(1020, 580));
        this.setBackground(COLOR_FONDO);
        this.setFocusable(true);
        this.cargarRecord();
        this.gameThread = new Thread(this);
        this.gameThread.start();
    }

    @Override
    public void run() {
        while (this.corriendo) {
            long ahora;
            if (this.enCurso && (ahora = System.currentTimeMillis()) - this.tiempoUltimaActualizacion >= this.velocidad) {
                this.actualizar();
                this.tiempoUltimaActualizacion = ahora;
            }
            this.repaint();
            try {
                Thread.sleep(16L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void actualizar() {
        if (this.pausa) {
            return;
        }
        this.serpiente.cambiarDireccion(this.proximaDireccion);
        int nuevaFila = this.serpiente.getCabeza().getFila() + this.desplazamientoFila();
        int nuevaColumna = this.serpiente.getCabeza().getColumna() + this.desplazamientoColumna();
        Posicion nuevaPos = new Posicion(nuevaFila, nuevaColumna);
        boolean comeComida = nuevaPos.equals(this.comida.getPosicion());
        boolean comePowerUp = this.powerUpPos != null && nuevaPos.equals(this.powerUpPos);
        this.serpiente.mover(comeComida || comePowerUp);

        // Si la serpiente esta en SerpienteFantasma, se actualiza su temporizador especial
        if (this.serpiente instanceof SerpienteFantasma fantasma) {
            fantasma.actualizarModoFantasma();
        }

        if (this.serpiente.colisionaConBorde(ALTO, ANCHO) || this.serpiente.colisionaConsigoMisma()) {
            this.reproducirSonido("muerte");
            this.enCurso = false;
            this.estadoPantalla = "gameover";
            if (this.puntaje > this.record) {
                this.record = this.puntaje;
                this.guardarRecord();
            }
            return;
        }

        if (this.serpiente.colisionaCon(this.comida.getPosicion())) {
            this.procesarComidaComida();
        }

        if (comePowerUp && this.serpiente.colisionaCon(this.powerUpPos)) {
            this.aplicarPowerUp(this.powerUpTipo);
            this.reproducirSonido("powerup");
            this.powerUpPos = null;
            this.powerUpTipo = 0;
        }

        this.gestionarPowerUp();

        // Si la comida especial expiro (metodo EXCLUSIVO de ComidaEspecial), se regenera
        if (this.comida instanceof ComidaEspecial especial) {
            especial.avanzarTick();
            if (especial.expiro()) {
                this.comida = new Comida(ALTO, ANCHO, this.serpiente.getCuerpo());
            }
        }

        if (this.efectoActivo != 0) {
            ++this.ticksEfecto;
            if (this.ticksEfecto >= DURACION_EFECTO) {
                this.efectoActivo = 0;
                this.ticksEfecto = 0;
                this.velocidad = this.calcularVelocidadBase();
            }
        }
        if (this.ticksMensaje > 0) {
            --this.ticksMensaje;
        }
    }

    /**
     * Procesa el evento de comer la comida actual usando POLIMORFISMO:
     * sin importar si "comida" es Comida, ComidaEspecial o ComidaVenenosa,
     * se llaman los mismos metodos (getPuntos, getNombre) y cada subclase
     * responde segun su propia logica sobrescrita.
     */
    private void procesarComidaComida() {
        int puntosBase = this.comida.getPuntos();
        int puntosFinales = (this.efectoActivo == POWERUP_PUNTOS) ? puntosBase * 3 : puntosBase;
        this.puntaje = Math.max(0, this.puntaje + puntosFinales);
        this.mostrarMensaje((puntosFinales >= 0 ? "+" : "") + puntosFinales + " " + this.comida.getNombre());

        if (this.comida instanceof ComidaVenenosa venenosa) {
            this.reproducirSonido("muerte");
            int quitar = Math.min(venenosa.getSegmentosAQuitar(), this.serpiente.getLongitud() - 3);
            for (int i = 0; i < quitar; i++) {
                if (this.serpiente.getLongitud() > 3) {
                    this.serpiente.getCuerpo().removeLast();
                }
            }
        } else {
            this.reproducirSonido("comer");
            if (this.comida instanceof ComidaEspecial && this.serpiente instanceof SerpienteFantasma fantasma) {
                // Bono: comer la comida especial activa el modo fantasma de la serpiente
                fantasma.activarModoFantasma();
            }
        }

        this.comida = this.generarSiguienteComida();
        this.verificarNivel();
    }

    /**
     * Decide, de forma aleatoria, que tipo de Comida generar a continuacion.
     * Este metodo es el punto central donde se aprovecha la HERENCIA:
     * las tres clases (Comida, ComidaEspecial, ComidaVenenosa) se tratan
     * de manera uniforme porque todas son-un tipo de Comida.
     */
    private Comida generarSiguienteComida() {
        int probabilidad = this.random.nextInt(100);
        if (probabilidad < 15) {
            return new ComidaEspecial(ALTO, ANCHO, this.serpiente.getCuerpo());
        } else if (probabilidad < 30) {
            return new ComidaVenenosa(ALTO, ANCHO, this.serpiente.getCuerpo());
        } else {
            return new Comida(ALTO, ANCHO, this.serpiente.getCuerpo());
        }
    }

    private void gestionarPowerUp() {
        if (this.powerUpPos == null) {
            ++this.ticksSinPowerUp;
            if (this.ticksSinPowerUp >= TICKS_PARA_POWERUP) {
                this.generarPowerUp();
                this.ticksSinPowerUp = 0;
                this.powerUpDuracion = 0;
            }
        } else {
            ++this.powerUpDuracion;
            if (this.powerUpDuracion >= POWERUP_VIDA) {
                this.powerUpPos = null;
                this.powerUpTipo = 0;
            }
        }
    }

    private void generarPowerUp() {
        this.powerUpTipo = this.random.nextInt(3) + 1;
        Posicion candidata;
        do {
            candidata = new Posicion(this.random.nextInt(ALTO - 2) + 1, this.random.nextInt(ANCHO - 2) + 1);
        } while (this.estaOcupada(candidata));
        this.powerUpPos = candidata;
    }

    private boolean estaOcupada(Posicion pos) {
        if (pos.equals(this.comida.getPosicion())) {
            return true;
        }
        for (Posicion segmento : this.serpiente.getCuerpo()) {
            if (pos.equals(segmento)) {
                return true;
            }
        }
        return false;
    }

    private void aplicarPowerUp(int tipo) {
        this.efectoActivo = tipo;
        this.ticksEfecto = 0;
        switch (tipo) {
            case POWERUP_VELOCIDAD:
                this.velocidad = Math.max(50L, this.calcularVelocidadBase() - (long) (this.calcularVelocidadBase() * 0.4));
                this.mostrarMensaje("\u00a1TURBO!");
                break;
            case POWERUP_PUNTOS:
                this.mostrarMensaje("\u00a1x3 PUNTOS!");
                break;
            case POWERUP_REDUCIR:
                int nuevaLongitud = Math.max(3, this.serpiente.getLongitud() / 2);
                while (this.serpiente.getLongitud() > nuevaLongitud) {
                    this.serpiente.getCuerpo().removeLast();
                }
                this.mostrarMensaje("\u00a1REDUCIDO!");
                break;
        }
    }

    private void verificarNivel() {
        if (this.puntaje >= this.nivel * 50) {
            ++this.nivel;
            this.velocidad = this.calcularVelocidadBase();
        }
    }

    private long calcularVelocidadBase() {
        return Math.max(80L, 400L - (long) (this.nivel - 1) * 40L);
    }

    private int desplazamientoFila() {
        switch (this.serpiente.getDireccion()) {
            case "up": return -1;
            case "down": return 1;
            default: return 0;
        }
    }

    private int desplazamientoColumna() {
        switch (this.serpiente.getDireccion()) {
            case "left": return -1;
            case "right": return 1;
            default: return 0;
        }
    }

    private void mostrarMensaje(String texto) {
        this.mensajeFlotante = texto;
        this.ticksMensaje = TICKS_MENSAJE;
    }

    private void reproducirSonido(String tipo) {
        new Thread(() -> {
            try {
                byte[] datos;
                AudioFormat formato = new AudioFormat(44100.0f, 8, 1, true, false);
                switch (tipo) {
                    case "comer":
                        datos = this.generarTono(880, 80, 44100);
                        break;
                    case "powerup":
                        datos = this.combinarTonos(this.generarTono(660, 80, 44100), this.generarTono(990, 120, 44100));
                        break;
                    case "muerte":
                        datos = this.generarTonoCaida(440, 200, 44100);
                        break;
                    default:
                        return;
                }
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, formato);
                SourceDataLine linea = (SourceDataLine) AudioSystem.getLine(info);
                linea.open(formato);
                linea.start();
                linea.write(datos, 0, datos.length);
                linea.drain();
                linea.close();
            } catch (Exception ignorada) {
            }
        }).start();
    }

    private byte[] generarTono(int frecuencia, int duracionMs, int sampleRate) {
        int muestras = sampleRate * duracionMs / 1000;
        byte[] datos = new byte[muestras];
        for (int i = 0; i < muestras; i++) {
            double angulo = Math.PI * 2 * i * frecuencia / sampleRate;
            datos[i] = (byte) (Math.sin(angulo) * 80.0);
        }
        return datos;
    }

    private byte[] generarTonoCaida(int frecuencia, int duracionMs, int sampleRate) {
        int muestras = sampleRate * duracionMs / 1000;
        byte[] datos = new byte[muestras];
        for (int i = 0; i < muestras; i++) {
            double factor = 1.0 - (double) i / muestras;
            double freqActual = frecuencia * factor;
            double angulo = Math.PI * 2 * i * freqActual / sampleRate;
            datos[i] = (byte) (Math.sin(angulo) * 80.0 * factor);
        }
        return datos;
    }

    private byte[] combinarTonos(byte[] a, byte[] b) {
        byte[] combinado = new byte[a.length + b.length];
        System.arraycopy(a, 0, combinado, 0, a.length);
        System.arraycopy(b, 0, combinado, a.length, b.length);
        return combinado;
    }

    private void cargarRecord() {
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_RECORD))) {
            this.record = Integer.parseInt(br.readLine().trim());
        } catch (Exception e) {
            this.record = 0;
        }
    }

    private void guardarRecord() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_RECORD))) {
            pw.println(this.record);
        } catch (Exception ignorada) {
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        switch (this.estadoPantalla) {
            case "inicio":
                this.dibujarPantallaInicio(g2);
                break;
            case "jugando":
                this.dibujarJuego(g2);
                break;
            case "gameover":
                this.dibujarGameOver(g2);
                break;
        }
    }

    private void dibujarPantallaInicio(Graphics2D g2) {
        g2.setColor(COLOR_SERPIENTE_CABEZA);
        g2.setFont(new Font("Arial", Font.BOLD, 50));
        String titulo = "SNAKE";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(titulo, (this.getWidth() - fm.stringWidth(titulo)) / 2, 90);

        g2.setColor(COLOR_PU_PUNTOS);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        String recordTxt = "R\u00e9cord guardado: " + this.record;
        fm = g2.getFontMetrics();
        g2.drawString(recordTxt, (this.getWidth() - fm.stringWidth(recordTxt)) / 2, 130);

        g2.setColor(COLOR_TEXTO);
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        String[] instrucciones = {
                "Usa W A S D o FLECHAS para moverte",
                "Come la comida roja para crecer y sumar puntos",
                "Evita las paredes y tu propio cuerpo", ""
        };
        int y = 175;
        for (String linea : instrucciones) {
            fm = g2.getFontMetrics();
            g2.drawString(linea, (this.getWidth() - fm.stringWidth(linea)) / 2, y);
            y += 28;
        }

        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.setColor(COLOR_PU_VELOCIDAD);
        g2.drawString("\u25a0  Azul   \u2192 TURBO: mu\u00e9vete m\u00e1s r\u00e1pido", 80, y);
        g2.setColor(COLOR_PU_PUNTOS);
        g2.drawString("\u25a0  Dorado \u2192 x3 PUNTOS por cada comida", 80, y += 26);
        g2.setColor(COLOR_PU_REDUCIR);
        g2.drawString("\u25a0  Morado \u2192 REDUCIR: tu cuerpo se achica", 80, y += 26);
        g2.setColor(new Color(255, 193, 7));
        g2.drawString("\u25c6  Comida dorada \u2192 ComidaEspecial: x3 puntos y modo fantasma", 80, y += 26);
        g2.setColor(new Color(156, 39, 176));
        g2.drawString("\u25c6  Comida morada  \u2192 ComidaVenenosa: resta puntos y encoge", 80, y += 26);
        y += 30;

        if (System.currentTimeMillis() / 500L % 2L == 0L) {
            g2.setColor(COLOR_TEXTO);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            String iniciar = "\u25ba Presiona ENTER para comenzar \u25c4";
            fm = g2.getFontMetrics();
            g2.drawString(iniciar, (this.getWidth() - fm.stringWidth(iniciar)) / 2, y);
        }
    }

    private void dibujarJuego(Graphics2D g2) {
        g2.setColor(COLOR_TEXTO);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("Puntaje: " + this.puntaje + "  |  Nivel: " + this.nivel + "  |  R\u00e9cord: " + this.record, 10, 30);

        if (this.pausa) {
            g2.setColor(COLOR_PU_PUNTOS);
            g2.drawString("[ PAUSA ]", this.getWidth() - 120, 30);
        }

        if (this.serpiente instanceof SerpienteFantasma fantasma && fantasma.isModoFantasmaActivo()) {
            g2.setColor(COLOR_FANTASMA);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString("\u2601 MODO FANTASMA", this.getWidth() / 2 - 70, 30);
        }

        if (this.efectoActivo != 0) {
            String etiqueta = "";
            Color color = COLOR_TEXTO;
            switch (this.efectoActivo) {
                case POWERUP_VELOCIDAD:
                    etiqueta = "\u26a1 TURBO";
                    color = COLOR_PU_VELOCIDAD;
                    break;
                case POWERUP_PUNTOS:
                    etiqueta = "\u2605 x3 PTS";
                    color = COLOR_PU_PUNTOS;
                    break;
                case POWERUP_REDUCIR:
                    etiqueta = "\u2726 REDUCIDO";
                    color = COLOR_PU_REDUCIR;
                    break;
            }
            int restante = DURACION_EFECTO - this.ticksEfecto;
            g2.setColor(color);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString(etiqueta + " (" + restante + ")", this.getWidth() / 2 - 60, 30);
        }

        int x0 = PADDING;
        int y0 = 50;
        g2.setColor(COLOR_BORDE);
        g2.setStroke(new BasicStroke(3.0f));
        g2.drawRect(x0, y0, 1000, 500);

        // La comida se dibuja usando su propio color (POLIMORFISMO: getColor())
        Posicion posComida = this.comida.getPosicion();
        g2.setColor(this.comida.getColor());
        g2.fillOval(x0 + posComida.getColumna() * CELL_SIZE + 2, y0 + posComida.getFila() * CELL_SIZE + 2, 21, 21);

        if (this.powerUpPos != null) {
            Color colorPU;
            switch (this.powerUpTipo) {
                case POWERUP_VELOCIDAD: colorPU = COLOR_PU_VELOCIDAD; break;
                case POWERUP_PUNTOS: colorPU = COLOR_PU_PUNTOS; break;
                case POWERUP_REDUCIR: colorPU = COLOR_PU_REDUCIR; break;
                default: colorPU = COLOR_TEXTO;
            }
            if (this.powerUpDuracion % 2 == 0 || this.powerUpDuracion < 30) {
                int px = x0 + this.powerUpPos.getColumna() * CELL_SIZE;
                int py = y0 + this.powerUpPos.getFila() * CELL_SIZE;
                int mitad = 12;
                int[] xs = {px + mitad, px + CELL_SIZE - 2, px + mitad, px + 2};
                int[] ys = {py + 2, py + mitad, py + CELL_SIZE - 2, py + mitad};
                g2.setColor(colorPU);
                g2.fillPolygon(xs, ys, 4);
                g2.setColor(Color.WHITE);
                g2.drawPolygon(xs, ys, 4);
            }
        }

        LinkedList<Posicion> cuerpo = this.serpiente.getCuerpo();
        boolean esFantasmaActivo = this.serpiente instanceof SerpienteFantasma f && f.isModoFantasmaActivo();
        for (int i = 0; i < cuerpo.size(); i++) {
            Posicion segmento = cuerpo.get(i);
            if (i == 0) {
                g2.setColor(esFantasmaActivo ? COLOR_FANTASMA : COLOR_SERPIENTE_CABEZA);
                g2.fillRect(x0 + segmento.getColumna() * CELL_SIZE, y0 + segmento.getFila() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                this.dibujarOjos(g2, x0 + segmento.getColumna() * CELL_SIZE, y0 + segmento.getFila() * CELL_SIZE);
                continue;
            }
            float alpha = Math.max(0.4f, 1.0f - (float) i / cuerpo.size());
            int base = esFantasmaActivo ? 200 : 56;
            g2.setColor(new Color(base, esFantasmaActivo ? 200 : 142, esFantasmaActivo ? 200 : 60, (int) (alpha * 255.0f)));
            g2.fillRect(x0 + segmento.getColumna() * CELL_SIZE + 1, y0 + segmento.getFila() * CELL_SIZE + 1, CELL_SIZE - 2, CELL_SIZE - 2);
        }

        if (this.ticksMensaje > 0) {
            float alpha = (float) this.ticksMensaje / TICKS_MENSAJE;
            g2.setColor(new Color(1.0f, 1.0f, 0.0f, alpha));
            g2.setFont(new Font("Arial", Font.BOLD, 22));
            FontMetrics fm = g2.getFontMetrics();
            int mx = (this.getWidth() - fm.stringWidth(this.mensajeFlotante)) / 2;
            int my = y0 + 250 - 20;
            g2.drawString(this.mensajeFlotante, mx, my);
        }
    }

    private void dibujarOjos(Graphics2D g2, int x, int y) {
        g2.setColor(Color.WHITE);
        int tam = 4;
        switch (this.serpiente.getDireccion()) {
            case "right":
                g2.fillOval(x + 16, y + 4, tam, tam);
                g2.fillOval(x + 16, y + 16, tam, tam);
                break;
            case "left":
                g2.fillOval(x + 4, y + 4, tam, tam);
                g2.fillOval(x + 4, y + 16, tam, tam);
                break;
            case "up":
                g2.fillOval(x + 4, y + 4, tam, tam);
                g2.fillOval(x + 16, y + 4, tam, tam);
                break;
            case "down":
                g2.fillOval(x + 4, y + 16, tam, tam);
                g2.fillOval(x + 16, y + 16, tam, tam);
                break;
        }
    }

    private void dibujarGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        g2.setColor(new Color(244, 67, 54));
        g2.setFont(new Font("Arial", Font.BOLD, 50));
        String titulo = "GAME OVER";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(titulo, (this.getWidth() - fm.stringWidth(titulo)) / 2, 150);

        g2.setColor(COLOR_TEXTO);
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        String puntajeTxt = "Puntaje: " + this.puntaje;
        String recordTxt = "R\u00e9cord:  " + this.record;
        fm = g2.getFontMetrics();
        g2.drawString(puntajeTxt, (this.getWidth() - fm.stringWidth(puntajeTxt)) / 2, 230);
        g2.drawString(recordTxt, (this.getWidth() - fm.stringWidth(recordTxt)) / 2, 270);

        if (this.puntaje >= this.record && this.puntaje > 0) {
            g2.setColor(COLOR_PU_PUNTOS);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            String nuevoRecord = "\u00a1NUEVO R\u00c9CORD!";
            fm = g2.getFontMetrics();
            g2.drawString(nuevoRecord, (this.getWidth() - fm.stringWidth(nuevoRecord)) / 2, 310);
        }

        g2.setColor(new Color(200, 200, 200));
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        String[] opciones = {"Presiona R para reiniciar", "Presiona Q para salir"};
        int y = 370;
        for (String linea : opciones) {
            fm = g2.getFontMetrics();
            g2.drawString(linea, (this.getWidth() - fm.stringWidth(linea)) / 2, y);
            y += 30;
        }
    }

    public void procesarTecla(int codigo) {
        switch (codigo) {
            case 38:
            case 87:
                this.proximaDireccion = "up";
                break;
            case 40:
            case 83:
                this.proximaDireccion = "down";
                break;
            case 37:
            case 65:
                this.proximaDireccion = "left";
                break;
            case 39:
            case 68:
                this.proximaDireccion = "right";
                break;
            case 80:
                if (this.enCurso) {
                    this.pausa = !this.pausa;
                }
                break;
            case 10:
                if (this.estadoPantalla.equals("inicio")) {
                    this.iniciarJuego();
                }
                break;
            case 82:
                if (this.estadoPantalla.equals("gameover")) {
                    this.iniciarJuego();
                }
                break;
            case 81:
                if (this.estadoPantalla.equals("gameover")) {
                    this.corriendo = false;
                    System.exit(0);
                }
                break;
        }
    }

    private void iniciarJuego() {
        // Se instancia SerpienteFantasma (subclase) en una variable de tipo
        // Serpiente (clase base): esto es POLIMORFISMO en accion.
        this.serpiente = new SerpienteFantasma(10, 20);
        this.comida = new Comida(ALTO, ANCHO, this.serpiente.getCuerpo());
        this.puntaje = 0;
        this.nivel = 1;
        this.velocidad = 400L;
        this.tiempoUltimaActualizacion = System.currentTimeMillis();
        this.pausa = false;
        this.enCurso = true;
        this.estadoPantalla = "jugando";
        this.proximaDireccion = "right";
        this.powerUpPos = null;
        this.powerUpTipo = 0;
        this.ticksSinPowerUp = 0;
        this.efectoActivo = 0;
        this.ticksEfecto = 0;
        this.ticksMensaje = 0;
    }

    public void detener() {
        this.corriendo = false;
    }
}
