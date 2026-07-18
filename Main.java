import java.util.LinkedList;
import javax.swing.SwingUtilities;

/**
 * Clase Main.
 *
 * Primero ejecuta una PRUEBA POR CONSOLA que demuestra el uso de herencia
 * (instancia objetos de las subclases, llama metodos heredados y
 * sobrescritos, y muestra que el mismo codigo se comporta distinto segun
 * el tipo real del objeto -> polimorfismo). Despues abre la ventana del
 * juego, donde esa misma herencia ya esta integrada (ver SnakePanel).
 */
public class Main {

    public static void main(String[] args) {
        probarHerencia();
        SwingUtilities.invokeLater(SnakeFrame::new);
    }

    private static void probarHerencia() {
        System.out.println("=== DEMOSTRACION DE HERENCIA ===\n");

        // ----- Jerarquia 1: Comida -----
        LinkedList<Posicion> cuerpoDemo = new LinkedList<>();
        cuerpoDemo.add(new Posicion(5, 5));

        Comida[] comidas = new Comida[]{
                new Comida(20, 40, cuerpoDemo),
                new ComidaEspecial(20, 40, cuerpoDemo),
                new ComidaVenenosa(20, 40, cuerpoDemo)
        };

        System.out.println("-- Jerarquia Comida (clase base) --");
        for (Comida c : comidas) {
            // Se llama a los MISMOS metodos sobre el tipo base "Comida",
            // pero cada subclase responde distinto (polimorfismo):
            System.out.printf("%-30s | Puntos: %-4d | Posicion: %s | Color: %s%n",
                    c.getNombre(), c.getPuntos(), c.getPosicion(), c.getColor());
        }

        ComidaEspecial especial = (ComidaEspecial) comidas[1];
        System.out.println("Metodo exclusivo de ComidaEspecial -> estaPorExpirar(): " + especial.estaPorExpirar());

        ComidaVenenosa venenosa = (ComidaVenenosa) comidas[2];
        System.out.println("Metodo exclusivo de ComidaVenenosa -> getSegmentosAQuitar(): " + venenosa.getSegmentosAQuitar());

        // ----- Jerarquia 2: Serpiente -----
        System.out.println("\n-- Jerarquia Serpiente (clase base) --");
        Serpiente serpienteNormal = new Serpiente(10, 10);
        Serpiente serpienteFantasma = new SerpienteFantasma(10, 10);

        // Forzamos una colision consigo misma en ambas serpientes para
        // comparar el comportamiento HEREDADO vs el SOBRESCRITO:
        forzarAutocolision(serpienteNormal);
        forzarAutocolision(serpienteFantasma);

        System.out.println("Serpiente normal   colisionaConsigoMisma(): " + serpienteNormal.colisionaConsigoMisma());
        System.out.println("SerpienteFantasma  colisionaConsigoMisma() (sin activar): " + serpienteFantasma.colisionaConsigoMisma());

        ((SerpienteFantasma) serpienteFantasma).activarModoFantasma();
        System.out.println("SerpienteFantasma  colisionaConsigoMisma() (modo activo): " + serpienteFantasma.colisionaConsigoMisma());

        System.out.println("\n=== FIN DE LA PRUEBA. Abriendo el juego... ===\n");
    }

    /** Utilidad para el test: hace que la cabeza de la serpiente coincida con otro segmento. */
    private static void forzarAutocolision(Serpiente s) {
        Posicion cabeza = s.getCabeza();
        s.getCuerpo().add(new Posicion(cabeza.getFila(), cabeza.getColumna()));
    }
}
