package servidor;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Callable;
import org.json.JSONObject;

public class ManejadorDeNodo implements Callable<JSONObject> {

    private Socket conexion;

    public ManejadorDeNodo(Socket conexion) {
        this.conexion = conexion;
    }

    @Override
    public JSONObject call() {
        String linea = "";
        try {

            InputStream secuenciaDeEntrada = conexion.getInputStream();

            Scanner in = new Scanner(secuenciaDeEntrada);

            linea = in.nextLine();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new JSONObject(linea);
    }

}
