import java.util.LinkedList;

/**
 * Clase Serpiente (CLASE BASE de la jerarquia de herencia #2).
 *
 * Contiene toda la logica de movimiento y colision del jugador.
 * Los atributos "cuerpo" y "direccion" se declararon como "protected"
 * para que las clases derivadas puedan acceder y reutilizar
 * directamente esa informacion (herencia de atributos), y los metodos
 * de colision se dejaron disponibles para ser sobrescritos.
 *
 * Clase derivada: SerpienteFantasma (ver ese archivo), que reutiliza
 * todo el movimiento de esta clase pero sobrescribe el comportamiento
 * de colision consigo misma.
 */
public class Serpiente {

    protected LinkedList<Posicion> cuerpo = new LinkedList<>();
    protected String direccion = "right";

    public Serpiente(int filaInicial, int columnaInicial) {
        this.cuerpo.add(new Posicion(filaInicial, columnaInicial));
        this.cuerpo.add(new Posicion(filaInicial, columnaInicial - 1));
        this.cuerpo.add(new Posicion(filaInicial, columnaInicial - 2));
    }

    public LinkedList<Posicion> getCuerpo() {
        return this.cuerpo;
    }

    public Posicion getCabeza() {
        return this.cuerpo.getFirst();
    }

    public String getDireccion() {
        return this.direccion;
    }

    public int getLongitud() {
        return this.cuerpo.size();
    }

    public void cambiarDireccion(String nueva) {
        boolean esOpuesta =
                (nueva.equals("up") && this.direccion.equals("down")) ||
                (nueva.equals("down") && this.direccion.equals("up")) ||
                (nueva.equals("left") && this.direccion.equals("right")) ||
                (nueva.equals("right") && this.direccion.equals("left"));
        if (!esOpuesta) {
            this.direccion = nueva;
        }
    }

    public void mover(boolean creceio) {
        Posicion cabeza = this.getCabeza();
        int fila = cabeza.getFila();
        int columna = cabeza.getColumna();
        switch (this.direccion) {
            case "up":
                fila--;
                break;
            case "down":
                fila++;
                break;
            case "left":
                columna--;
                break;
            case "right":
                columna++;
                break;
        }
        this.cuerpo.addFirst(new Posicion(fila, columna));
        if (!creceio) {
            this.cuerpo.removeLast();
        }
    }

    public boolean colisionaCon(Posicion objetivo) {
        return this.getCabeza().equals(objetivo);
    }

    /**
     * Comportamiento base: la serpiente muere si su cabeza toca cualquier
     * segmento de su propio cuerpo. Sobrescrito en SerpienteFantasma.
     */
    public boolean colisionaConsigoMisma() {
        Posicion cabeza = this.getCabeza();
        for (int i = 1; i < this.cuerpo.size(); i++) {
            if (cabeza.equals(this.cuerpo.get(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean colisionaConBorde(int filas, int columnas) {
        Posicion cabeza = this.getCabeza();
        return cabeza.getFila() <= 0 || cabeza.getFila() >= filas - 1 ||
               cabeza.getColumna() <= 0 || cabeza.getColumna() >= columnas - 1;
    }
}
