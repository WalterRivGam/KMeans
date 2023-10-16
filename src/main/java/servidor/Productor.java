package servidor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.Centroide;
import utils.Punto;

public class Productor {

    private static int nroNodos = 3;
    private static int nroDatos = 30;
    private static int nroCentroides = 4;

    public static void main(String[] args) {
        try {
            ServerSocket s = new ServerSocket(8189);

            int nroNodosConectados = 0;
            Socket[] conexiones = new Socket[nroNodos];

            // acepta conexiones de nodos
            while (nroNodosConectados < nroNodos) {
                conexiones[nroNodosConectados] = s.accept();
                System.out.println("Se conectó el nodo: " + nroNodosConectados);
                nroNodosConectados++;
            }

            // Generar centroides aleatorios
            Random rand = new Random();
            JSONArray centroidesJSON = new JSONArray();
            for (int i = 0; i < nroCentroides; i++) {
                JSONObject centroide = new JSONObject();
                centroide.put("x", rand.nextInt(-1000, 1000));
                centroide.put("y", rand.nextInt(-1000, 1000));
                centroide.put("n", -1);
                centroidesJSON.put(centroide);
            }

            // Generar Datos aleatorios
            JSONArray[] dataNodo = new JSONArray[nroNodos];

            for (int i = 0; i < nroNodos; i++) {

                dataNodo[i] = new JSONArray();

                for (int j = i * (nroDatos / nroNodos); j < (i + 1) * (nroDatos / nroNodos); j++) {
                    JSONObject punto = new JSONObject();
                    int x = rand.nextInt(-1000, 1000);
                    int y = rand.nextInt(-1000, 1000);
                    punto.put("x", x);
                    punto.put("y", y);
                    punto.put("cluster", -1);
                    dataNodo[i].put(punto);
                }
            }

            // Crear objetos JSON a enviar a los nodos
            JSONObject[] objs = new JSONObject[nroNodos];
            for (int i = 0; i < nroNodos; i++) {
                objs[i] = new JSONObject();
                objs[i].put("data", dataNodo[i]);
                objs[i].put("centroids", centroidesJSON);
                objs[i].put("senddata", false);
                objs[i].put("changed", true);
            }

            FutureTask<JSONObject>[] tareas = new FutureTask[nroNodos];

            int nroIteraciones = 100;

            Centroide[] cents = new Centroide[nroCentroides];

            for (int i = 0; i < nroIteraciones; i++) {

                // crear manejadores de nodos
                for (int j = 0; j < nroNodos; j++) {
                    tareas[j] = new FutureTask<JSONObject>(new ManejadorDeNodo(conexiones[j]));
                    new Thread(tareas[j]).start();
                }

                enviarDatos(conexiones, objs);

                // Obtener resultados
                for (int j = 0; j < nroNodos; j++) {
                    objs[j] = tareas[j].get();
                }

                // Verificar si no hubo cambios en los centroides
                boolean terminar = true;
                for (int j = 0; j < nroNodos; j++) {
                    boolean cambio = objs[j].getBoolean("changed");
                    if (cambio) {
                        terminar = false;
                        break;
                    }
                }

                // Manejar finalización de algoritmo
                if (terminar || (i == nroIteraciones - 1)) {
                    // crear manejadores de nodos
                    for (int j = 0; j < nroNodos; j++) {
                        tareas[j] = new FutureTask<JSONObject>(new ManejadorDeNodo(conexiones[j]));
                        new Thread(tareas[j]).start();
                    }

                    // modificar objetos para que devuelvan datos
                    for (int j = 0; j < nroNodos; j++) {
                        objs[j].put("senddata", true);
                    }

                    enviarDatos(conexiones, objs);

                    // Obtener resultados
                    for (int j = 0; j < nroNodos; j++) {
                        objs[j] = tareas[j].get();
                    }

                    // obtener puntos
                    Punto[] puntos = obtenerPuntos(objs);

                    // mostrar resultados
                    mostrarResultados(cents, puntos);

                    break;
                }

                cents = obtenerNuevosCentroides(objs);

                actualizarCentroidesJSON(cents, centroidesJSON);

                for (int j = 0; j < nroNodos; j++) {
                    objs[j].put("centroids", centroidesJSON);
                }

            }

        } catch (IOException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
    }

    private static void enviarDatos(Socket[] conexiones, JSONObject[] objs) {
        for (int j = 0; j < conexiones.length; j++) {
            OutputStream secuenciaDeSalida;
            try {
                secuenciaDeSalida = conexiones[j].getOutputStream();
                PrintWriter out = new PrintWriter(secuenciaDeSalida, true);
                out.println(objs[j].toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }

    private static Punto[] obtenerPuntos(JSONObject[] objs) {
        Punto[] puntos = new Punto[nroDatos];
        int c = 0; // indice de puntos
        for (int j = 0; j < nroNodos; j++) {
            JSONArray puntosNodo = objs[j].getJSONArray("data");
            for (int k = 0; k < puntosNodo.length(); k++) {
                JSONObject puntoJSON = puntosNodo.getJSONObject(k);
                puntos[c] = new Punto();
                puntos[c].setX(puntoJSON.getInt("x"));
                puntos[c].setY(puntoJSON.getInt("y"));
                puntos[c].setCluster(puntoJSON.getInt("cluster"));
                c++;
            }
        }
        return puntos;
    }

    private static Centroide[] obtenerCentroides(JSONObject obj) {
        JSONArray centroides = obj.getJSONArray("centroids");
        Centroide[] cents = new Centroide[nroCentroides];
        for (int j = 0; j < centroides.length(); j++) {
            JSONObject centroideJSON = centroides.getJSONObject(j);
            cents[j] = new Centroide();
            cents[j].setX(centroideJSON.getDouble("x"));
            cents[j].setY(centroideJSON.getDouble("y"));
            cents[j].setN(centroideJSON.getInt("n"));
        }
        return cents;
    }

    private static Centroide[] obtenerNuevosCentroides(JSONObject[] objs) {

        // crea arreglo de centroides y lo inicializa con ceros
        Centroide[] nuevosCentroides = new Centroide[nroCentroides];
        for (int i = 0; i < nuevosCentroides.length; i++) {
            nuevosCentroides[i] = new Centroide(0, 0, 0);
        }

        // Obtener sumas parciales de centroides de cada nodo y sumar sus 'x', 'y' y 'n'
        for (int i = 0; i < nroNodos; i++) {
            Centroide[] cents = obtenerCentroides(objs[i]);

            for (int j = 0; j < nuevosCentroides.length; j++) {
                nuevosCentroides[j].setX(nuevosCentroides[j].getX() + cents[j].getX());
                nuevosCentroides[j].setY(nuevosCentroides[j].getY() + cents[j].getY());
                nuevosCentroides[j].setN(nuevosCentroides[j].getN() + cents[j].getN());
            }
        }

        // actualizar centroides usando las sumas parciales
        for (int i = 0; i < nuevosCentroides.length; i++) {

            if (nuevosCentroides[i].getN() != 0) {
                nuevosCentroides[i].setX(nuevosCentroides[i].getX() / nuevosCentroides[i].getN());
                nuevosCentroides[i].setY(nuevosCentroides[i].getY() / nuevosCentroides[i].getN());
            }

        }

        return nuevosCentroides;
    }

    // actualiza el JSON de centroides usando el arreglo de centroides
    private static void actualizarCentroidesJSON(Centroide[] cents, JSONArray centroidesJSON) {
        for (int i = 0; i < nroCentroides; i++) {
            JSONObject centroide = centroidesJSON.getJSONObject(i);
            centroide.put("x", cents[i].getX());
            centroide.put("y", cents[i].getY());
            centroide.put("n", cents[i].getN());
            centroidesJSON.put(centroide);
        }
    }

    private static void mostrarResultados(Centroide[] centroides, Punto[] puntos) {
        System.out.println("  _____                    _  _              _             \n"
                + " |  __ \\                  | || |            | |            \n"
                + " | |__) | ___  ___  _   _ | || |_  __ _   __| |  ___   ___ \n"
                + " |  _  / / _ \\/ __|| | | || || __|/ _` | / _` | / _ \\ / __|\n"
                + " | | \\ \\|  __/\\__ \\| |_| || || |_| (_| || (_| || (_) |\\__ \\\n"
                + " |_|  \\_\\\\___||___/ \\__,_||_| \\__|\\__,_| \\__,_| \\___/ |___/\n"
                + "                                                           \n"
                + "                                                           ");

        System.out.println("Centroides:");
        for (Centroide centroide : centroides) {
            System.out.println(centroide);
        }
        System.out.println("\nPuntos:");
        for (Punto punto : puntos) {
            System.out.println(punto);
        }
    }
}
