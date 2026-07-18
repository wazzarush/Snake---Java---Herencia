import java.util.LinkedList;

/**
 * ComidaEspecial (CLASE DERIVADA de Comida).
 *
 * Representa una comida dorada que otorga el TRIPLE de puntos.
 * - REUTILIZA de la clase base: el atributo "valorPuntos", el atributo
 *   "random" y el metodo generar(...) (no se vuelve a escribir esa logica).
 * - SOBRESCRIBE (override): getPuntos(), getColor() y getNombre().
 * - AGREGA un atributo y un metodo exclusivos de esta subclase:
 *   "tiempoVidaTicks" y estaPorExpirar(), que controlan que esta comida
 *   especial desaparezca despues de un tiempo si no se come.
 */
public class ComidaEspecial extends Comida {

    // Atributo EXCLUSIVO de esta subclase (no existe en la clase base)
    private int tiempoVidaTicks;
    private static final int DURACION_TOTAL = 25;

    public ComidaEspecial(int filas, int columnas, LinkedList<Posicion> cuerpoSerpiente) {
        // super(...) llama al constructor de la clase base: reutiliza toda
        // la logica de generacion de posicion (herencia de comportamiento).
        super(filas, columnas, cuerpoSerpiente);
        this.tiempoVidaTicks = DURACION_TOTAL;
    }

    @Override
    public int getPuntos() {
        // Reutiliza el atributo heredado "valorPuntos" y lo multiplica por 3
        return this.valorPuntos * 3;
    }

    @Override
    public java.awt.Color getColor() {
        return new java.awt.Color(255, 193, 7); // dorado
    }

    @Override
    public String getNombre() {
        return "Comida Especial (x3 puntos)";
    }

    /** Metodo EXCLUSIVO de ComidaEspecial: hace avanzar su temporizador de vida. */
    public void avanzarTick() {
        if (this.tiempoVidaTicks > 0) {
            this.tiempoVidaTicks--;
        }
    }

    /** Metodo EXCLUSIVO de ComidaEspecial: indica si ya casi desaparece (para parpadear). */
    public boolean estaPorExpirar() {
        return this.tiempoVidaTicks <= 6;
    }

    /** Metodo EXCLUSIVO de ComidaEspecial: indica si el tiempo de vida ya termino. */
    public boolean expiro() {
        return this.tiempoVidaTicks <= 0;
    }
}
