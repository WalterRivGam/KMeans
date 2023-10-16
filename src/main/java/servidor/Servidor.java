package servidor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class Servidor {

    public static void main(String[] args) {
        try {
            ServerSocket s = new ServerSocket(8189);
            Socket entrante = s.accept();
            try {
                InputStream secuenciaDeEntrada = entrante.getInputStream();
                OutputStream secuenciaDeSalida = entrante.getOutputStream();

                Scanner in = new Scanner(secuenciaDeEntrada);
                PrintWriter out = new PrintWriter(secuenciaDeSalida, true);


                // Generar centroides
                JSONObject centroide1 = new JSONObject();
                centroide1.put("x", 100);
                centroide1.put("y", 120);
                centroide1.put("n", -1);

                JSONObject centroide2 = new JSONObject();
                centroide2.put("x", 600);
                centroide2.put("y", 700);
                centroide2.put("n", -1);

                JSONObject centroide3 = new JSONObject();
                centroide3.put("x", 600);
                centroide3.put("y", 150);
                centroide3.put("n", -1);

                JSONArray centroids = new JSONArray();
                centroids.put(centroide1);
                centroids.put(centroide2);
                centroids.put(centroide3);

                // Generar datos
                JSONArray data = new JSONArray();
                
                Random random = new Random();
                
                int nroPuntos = 3;

                for (int i = 0; i < nroPuntos/3; i++) {
                    JSONObject punto = new JSONObject();
                    punto.put("x", random.nextInt(100, 350));
                    punto.put("y", random.nextInt(100, 350));
                    punto.put("cluster", -1);
                    data.put(punto);
                }
                
                for (int i = 0; i < nroPuntos/3; i++) {
                    JSONObject punto = new JSONObject();
                    punto.put("x", random.nextInt(650, 850));
                    punto.put("y", random.nextInt(650, 850));
                    punto.put("cluster", -1);
                    data.put(punto);
                }
                
                for (int i = 0; i < nroPuntos/3; i++) {
                    JSONObject punto = new JSONObject();
                    punto.put("x", random.nextInt(650, 850));
                    punto.put("y", random.nextInt(100, 350));
                    punto.put("cluster", -1);
                    data.put(punto);
                }

                // Objeto a enviar
                JSONObject obj = new JSONObject();
                obj.put("data", data);
                obj.put("centroids", centroids);
                obj.put("senddata", false);
                obj.put("changed", true);
                
                out.println(obj.toString());
                
                // recibir sumas parciales centroides
                
                // calcular y enviar nuevos centroides
                

            } finally {
                entrante.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
