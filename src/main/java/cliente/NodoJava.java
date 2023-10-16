package cliente;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.Centroide;
import utils.Punto;

public class NodoJava {

    public static void main(String[] args) {

        try {
            Socket s = new Socket("127.0.0.1", 8189);

            InputStream secuenciaDeEntrada = s.getInputStream();
            OutputStream secuenciaDeSalida = s.getOutputStream();

            PrintWriter out = new PrintWriter(secuenciaDeSalida, true);
            Scanner in = new Scanner(secuenciaDeEntrada);

            String line = in.nextLine();
            JSONObject objJSON = new JSONObject(line);

            // Obtener centroides
            JSONArray centroidsJSON = objJSON.getJSONArray("centroids");
            Centroide[] centroides = new Centroide[centroidsJSON.length()];
            obtenerCentroides(centroidsJSON, centroides);

            // Obtener puntos
            JSONArray puntosJSON = objJSON.getJSONArray("data");
            Punto[] puntos = new Punto[puntosJSON.length()];
            obtenerPuntos(puntosJSON, puntos);
            puntosJSON = new JSONArray();

            boolean terminar = false;

            while (!terminar) {
                line = in.nextLine();
                objJSON = new JSONObject(line);

                boolean senddata = objJSON.getBoolean("senddata");

                if (senddata) {
                    puntosJSON = getJSONData(puntos);
                    actualizarObjJSON(objJSON, puntosJSON, centroidsJSON, false, false);
                    out.println(objJSON.toString());
                    terminar = true;
                    continue;
                }

                centroidsJSON = objJSON.getJSONArray("centroids");
                obtenerCentroides(centroidsJSON, centroides);

                boolean cambio = actualizarPuntos(puntos, centroides);
                actualizarCentroides(puntos, centroides);

                centroidsJSON = getJSONCentroids(centroides);

                actualizarObjJSON(objJSON, puntosJSON, centroidsJSON, cambio, false);
                out.println(objJSON.toString());

                out.println(objJSON.toString());

            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Llena el arreglo de centroides usando los datos del arreglo JSON de centroides
     *
     * @param centroidsJSON arreglo JSON de centroides
     * @param centroides arreglo de centroides
     */
    private static void obtenerCentroides(JSONArray centroidsJSON, Centroide[] centroides) {
        for (int i = 0; i < centroides.length; i++) {
            JSONObject centroideJSON = centroidsJSON.getJSONObject(i);
            Centroide centroide = new Centroide();
            centroide.setX(centroideJSON.getInt("x"));
            centroide.setY(centroideJSON.getInt("y"));
            centroide.setN(centroideJSON.getInt("n"));
            centroides[i] = centroide;
            System.out.println("Centroide: " + centroide);
        }
    }

    /**
     * Llena el arreglo de puntos usando el arreglo JSON data
     *
     * @param data arreglo JSON de puntos
     * @param puntos arreglo de puntos
     */
    private static void obtenerPuntos(JSONArray data, Punto[] puntos) {
        for (int i = 0; i < puntos.length; i++) {
            JSONObject puntoJSON = data.getJSONObject(i);
            Punto punto = new Punto();
            punto.setX(puntoJSON.getInt("x"));
            punto.setY(puntoJSON.getInt("y"));
            punto.setCluster(puntoJSON.getInt("cluster"));
            puntos[i] = punto;
            System.out.println("Punto: " + punto);
        }
    }

    private static boolean actualizarPuntos(Punto[] puntos, Centroide[] centroides) {
        boolean cambio = false;

        for (Punto punto : puntos) {
            double dmin = Double.MAX_VALUE;
            int nroCluster = 0;
            for (int i = 0; i < centroides.length; i++) {
                double dist = distancia(punto, centroides[i]);
                if (dist < dmin) {
                    dmin = dist;
                    nroCluster = i;
                }
            }
            int clusterActual = punto.getCluster();

            if (clusterActual != nroCluster) {
                punto.setCluster(nroCluster);
                cambio = true;
            }

        }

        return cambio;
    }

    private static double distancia(Punto punto, Centroide centroide) {
        return Math.sqrt(Math.pow(centroide.getX() - punto.getX(), 2) + Math.pow(centroide.getY() - punto.getY(), 2));
    }

    private static void actualizarCentroides(Punto[] puntos, Centroide[] centroides) {

        for (Centroide centroide : centroides) {
            centroide.setX(0);
            centroide.setY(0);
            centroide.setN(0);
        }

        for (Punto punto : puntos) {
            int cluster = punto.getCluster();
            Centroide centroide = centroides[cluster];
            centroide.setX(centroide.getX() + punto.getX());
            centroide.setY(centroide.getY() + punto.getY());
            centroide.setN(centroide.getN() + 1);
        }
    }

    private static JSONArray getJSONCentroids(Centroide[] centroides) {
        JSONArray centroidsJSON = new JSONArray();
        for (int i = 0; i < centroides.length; i++) {
            JSONObject centroideJSON = new JSONObject();
            centroideJSON.put("x", centroides[i].getX());
            centroideJSON.put("y", centroides[i].getY());
            centroideJSON.put("n", centroides[i].getN());
            centroidsJSON.put(centroideJSON);
        }
        return centroidsJSON;
    }

    private static JSONArray getJSONData(Punto[] puntos) {
        JSONArray data = new JSONArray();
        for (int i = 0; i < puntos.length; i++) {
            JSONObject puntoJSON = new JSONObject();
            puntoJSON.put("x", puntos[i].getX());
            puntoJSON.put("y", puntos[i].getY());
            puntoJSON.put("cluster", puntos[i].getCluster());
            data.put(puntoJSON);
        }
        return data;
    }

    private static void actualizarObjJSON(JSONObject obj, JSONArray puntosJSON, JSONArray centroidsJSON, boolean cambio, boolean senddata) {
        obj.put("data", puntosJSON);
        obj.put("centroids", centroidsJSON);
        obj.put("changed", cambio);
        obj.put("senddata", senddata); // irrelevante al enviar desde el cliente
    }

}
