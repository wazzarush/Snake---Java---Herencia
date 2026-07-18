/**
 * SerpienteFantasma (CLASE DERIVADA de Serpiente).
 *
 * Variante especial de la serpiente que puede activar un "modo fantasma":
 * mientras esta activo, puede atravesar su propio cuerpo sin morir.
 *
 * - REUTILIZA de la clase base: los atributos "cuerpo" y "direccion"
 *   (protected) y TODOS los metodos de movimiento (mover, cambiarDireccion,
 *   colisionaCon, colisionaConBorde) sin volver a escribirlos.
 * - SOBRESCRIBE: colisionaConsigoMisma(), para ignorar colisiones propias
 *   cuando el modo fantasma esta activo (usa super.colisionaConsigoMisma()
 *   cuando el modo esta apagado, es decir, reutiliza la logica original).
 * - AGREGA miembros exclusivos: "modoFantasmaActivo", "ticksRestantes",
 *   activarModoFantasma() y actualizarModoFantasma().
 */
public class SerpienteFantasma extends Serpiente {

    // Atributos EXCLUSIVOS de esta subclase
    private boolean modoFantasmaActivo = false;
    private int ticksRestantes = 0;
    private static final int DURACION_FANTASMA = 25;

    public SerpienteFantasma(int filaInicial, int columnaInicial) {
        super(filaInicial, columnaInicial); // reutiliza el constructor de Serpiente
    }

    /** Metodo EXCLUSIVO: activa el modo fantasma por un numero fijo de "ticks". */
    public void activarModoFantasma() {
        this.modoFantasmaActivo = true;
        this.ticksRestantes = DURACION_FANTASMA;
    }

    /** Metodo EXCLUSIVO: hace avanzar el temporizador del modo fantasma. */
    public void actualizarModoFantasma() {
        if (this.modoFantasmaActivo) {
            this.ticksRestantes--;
            if (this.ticksRestantes <= 0) {
                this.modoFantasmaActivo = false;
            }
        }
    }

    public boolean isModoFantasmaActivo() {
        return this.modoFantasmaActivo;
    }

    @Override
    public boolean colisionaConsigoMisma() {
        if (this.modoFantasmaActivo) {
            // Mientras el modo fantasma esta activo, nunca choca consigo misma
            return false;
        }
        // Si no esta activo, reutiliza (no reescribe) el comportamiento original
        return super.colisionaConsigoMisma();
    }
}
