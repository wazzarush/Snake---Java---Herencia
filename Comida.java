import java.util.LinkedList;
import java.util.Random;

/**
 * Clase Comida (CLASE BASE de la jerarquia de herencia #1).
 *
 * Antes esta clase solo generaba una posicion aleatoria en el tablero.
 * Para la Actividad 3 se modifico para que sus atributos principales sean
 * "protected" (en lugar de "private") y para que exponga metodos que
 * las clases derivadas puedan REUTILIZAR tal cual o SOBRESCRIBIR
 * (getPuntos, getColor, getNombre), logrando asi comportamiento
 * distinto para cada tipo de comida sin duplicar codigo.
 *
 * Clases derivadas: ComidaEspecial y ComidaVenenosa (ver esos archivos).
 */
public class Comida {

    // protected: visibles para esta clase y para sus subclases (reutilizacion de atributos)
    protected Posicion posicion;
    protected Random random = new Random();
    protected int valorPuntos = 10; // puntos base que otorga la comida al ser comida

    public Comida(int filas, int columnas, LinkedList<Posicion> cuerpoSerpiente) {
        generar(filas, columnas, cuerpoSerpiente);
    }

    public Posicion getPosicion() {
        return this.posicion;
    }

    /**
     * Puntos que otorga esta comida. Las subclases pueden sobrescribir
     * este metodo para dar mas, menos o incluso puntos negativos.
     */
    public int getPuntos() {
        return this.valorPuntos;
    }

    /**
     * Color con el que se dibuja esta comida en el panel.
     * Metodo pensado para ser sobrescrito por cada subclase.
     */
    public java.awt.Color getColor() {
        return new java.awt.Color(244, 67, 54); // rojo (comida normal)
    }

    /**
     * Nombre / etiqueta de este tipo de comida, usado para mensajes en pantalla.
     */
    public String getNombre() {
        return "Comida";
    }

    /**
     * Genera una posicion aleatoria para la comida que no choque con el
     * cuerpo de la serpiente. Metodo heredado y reutilizado sin cambios
     * por todas las subclases.
     */
    public void generar(int filas, int columnas, LinkedList<Posicion> cuerpoSerpiente) {
        Posicion nueva;
        boolean ocupada;
        do {
            int f = this.random.nextInt(filas - 2) + 1;
            int c = this.random.nextInt(columnas - 2) + 1;
            nueva = new Posicion(f, c);
            ocupada = false;
            for (Posicion segmento : cuerpoSerpiente) {
                if (nueva.equals(segmento)) {
                    ocupada = true;
                    break;
                }
            }
        } while (ocupada);
        this.posicion = nueva;
    }
}
