import java.util.LinkedList;

/**
 * ComidaVenenosa (CLASE DERIVADA de Comida).
 *
 * Representa una comida morada que PERJUDICA al jugador: resta puntos
 * y reduce el tamano de la serpiente.
 * - REUTILIZA de la clase base: el atributo "valorPuntos" y el metodo
 *   generar(...) heredado tal cual (misma logica de posicionamiento).
 * - SOBRESCRIBE: getPuntos() (retorna puntos negativos), getColor()
 *   y getNombre().
 * - AGREGA un metodo y un atributo exclusivos de esta subclase:
 *   "segmentosAQuitar" y getSegmentosAQuitar(), usados por el panel
 *   del juego para encoger a la serpiente al comerla.
 */
public class ComidaVenenosa extends Comida {

    // Atributo EXCLUSIVO de esta subclase
    private int segmentosAQuitar = 2;

    public ComidaVenenosa(int filas, int columnas, LinkedList<Posicion> cuerpoSerpiente) {
        super(filas, columnas, cuerpoSerpiente); // reutiliza el constructor/logica base
    }

    @Override
    public int getPuntos() {
        // Reutiliza "valorPuntos" heredado, pero lo devuelve en negativo
        return -this.valorPuntos;
    }

    @Override
    public java.awt.Color getColor() {
        return new java.awt.Color(156, 39, 176); // morado
    }

    @Override
    public String getNombre() {
        return "Comida Venenosa (-puntos)";
    }

    /** Metodo EXCLUSIVO de ComidaVenenosa: cuantos segmentos debe perder la serpiente. */
    public int getSegmentosAQuitar() {
        return this.segmentosAQuitar;
    }
}
