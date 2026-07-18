/**
 * Clase Posicion.
 * Representa una coordenada (fila, columna) dentro del tablero del juego.
 * Es utilizada tanto por la Serpiente (cada segmento de su cuerpo)
 * como por la Comida (el lugar donde aparece).
 */
public class Posicion {
    private int fila;
    private int columna;

    public Posicion(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }

    public int getFila() {
        return this.fila;
    }

    public int getColumna() {
        return this.columna;
    }

    public void setFila(int fila) {
        this.fila = fila;
    }

    public void setColumna(int columna) {
        this.columna = columna;
    }

    public boolean equals(Posicion otra) {
        return this.fila == otra.fila && this.columna == otra.columna;
    }

    public String toString() {
        return "(" + this.fila + ", " + this.columna + ")";
    }
}
