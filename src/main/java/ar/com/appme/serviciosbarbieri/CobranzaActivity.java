package ar.com.appme.serviciosbarbieri;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class CobranzaActivity extends Fragment implements View.OnClickListener{

    private TextView tvoficina,tvcobro,tvcerrado;
    public TextView fecha;
    private Calendar c = Calendar.getInstance();
    public int mes = c.get(Calendar.MONTH);
    public int dia = c.get(Calendar.DAY_OF_MONTH);
    public int anio = c.get(Calendar.YEAR);
    private static final int CERO = 0;
    private ProgressDialog dialog;
    private ArrayList<String> oficinas,cobrado,cerrado,rendido;
    private ObtenerServicioWeb hiloconexion;
    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_cobranza, container, false);
        tvoficina = (TextView) view.findViewById(R.id.oficina);
        tvcobro = (TextView)view.findViewById(R.id.cobro);
        tvcerrado = (TextView)view.findViewById(R.id.cerrado);
        fecha = (TextView) view.findViewById(R.id.tvFecha);
        final Button btn = (Button) view.findViewById(R.id.btnfecha);
        dialog=new ProgressDialog(this.getContext());
        btn.setOnClickListener(this);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                obtenerDate();
            }
        });
        oficinas = new ArrayList<String>();
        cobrado = new ArrayList<String>();
        cerrado = new ArrayList<String>();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnfecha:
                obtenerDate();
                break;
        }
    }

    public void obtenerCobranza(){
        fecha.setText(obtenerFecha(c.get(c.DAY_OF_WEEK))+" "+String.valueOf(c.get(c.DAY_OF_MONTH))+", "+obtenerMes(c.get(c.MONTH))+" "+String.valueOf(c.get(c.YEAR)));
        obtenerDatos(c.get(c.YEAR),c.get(c.MONTH),c.get(c.DAY_OF_MONTH));
    }
    public void obtenerCobranza(int year, int month,int day){
        Calendar cal = new GregorianCalendar(year,month,day);
        fecha.setText(obtenerFecha(cal.DAY_OF_WEEK)+" "+String.valueOf(cal.DAY_OF_WEEK_IN_MONTH)+", "+obtenerMes(cal.get(cal.MONTH))+" "+String.valueOf(cal.get(cal.YEAR)));
    }
    public String obtenerFecha(int i){
        switch (i){
            case 1:
                return "Domingo";
            case 2:
                return "Lunes";
            case 3:
                return "Martes";
            case 4:
                return "Miércoles";
            case 5:
                return "Jueves";
            case 6:
                return "Viernes";
            default:
                return "Sabado";
        }
    }
    public String obtenerMes(int i){
        switch (i){
            case 0:
                return "Enero";
            case 1:
                return "Febrero";
            case 2:
                return "Marzo";
            case 3:
                return "Abril";
            case 4:
                return "Mayo";
            case 5:
                return "Junio";
            case 6:
                return "Julio";
            case 7:
                return "Agosto";
            case 8:
                return "Septiembre";
            case 9:
                return "Octubre";
            case 10:
                return "Noviembre";
            default:
                return "Diciembre";
        }
    }
    public void obtenerDate(){
        DatePickerDialog recogerFecha = new DatePickerDialog(this.getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar cal = new GregorianCalendar(year,month,dayOfMonth);
                //fecha.setText(obtenerFecha(cal.DAY_OF_WEEK)+" "+String.valueOf(cal.DAY_OF_WEEK_IN_MONTH)+", "+obtenerMes(cal.get(cal.MONTH))+" "+String.valueOf(cal.get(cal.YEAR)));
                fecha.setText(obtenerFecha(cal.get((cal.DAY_OF_WEEK)))+" "+String.valueOf(cal.get(cal.DAY_OF_MONTH))+", "+obtenerMes(cal.get(cal.MONTH))+" "+String.valueOf(cal.get(cal.YEAR)));
                obtenerDatos(cal.get(cal.YEAR),cal.get(cal.MONTH),cal.get(cal.DAY_OF_MONTH));
            }
            //Estos valores deben ir en ese orden, de lo contrario no mostrara la fecha actual
            /**
             *También puede cargar los valores que usted desee
             */
        },anio, mes, dia);
        //Muestro el widget
        recogerFecha.show();
    }
    private void obtenerDatos(int year, int mes, int dia)
    {
        String ruta = "direccion del php o servicio el cual devuelve el json";
        int mesv = mes+1;
        String parametro = year+"-"+mesv+"-"+dia;
        try {
            tvcerrado.setText("");
            tvcobro.setText("");
            tvoficina.setText("");
            hiloconexion = new ObtenerServicioWeb();
            hiloconexion.execute(ruta,parametro);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public class ObtenerServicioWeb extends AsyncTask<String, Void,String> {

        @Override
        protected void onPreExecute() {
            dialog.setTitle("Obteniendo cobranza");
            dialog.setMessage("Espere unos segundos..");
            dialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            String c = "<strong>Cobrado</strong><br><br>";
            String o = "<strong>Oficinas</strong><br><br>";
            String cerr = "<strong>Cerrado</strong><br><br>";

            for(int i = 0; i<cobrado.size();i++){
                String item = cobrado.get(i);
                if(!item.equals("null")&& i!=cobrado.size()-1)
                    c += "$"+item+"<br><br>";
                else if (i == cobrado.size()-1 && !item.equals("null")){
                    c += "<strong>$"+item+"</strong><br><br>";
                }
                else if(i == cobrado.size()-1){
                    c += "<strong>$ 0 </strong><br><br>";
                }else
                    c += "$ 0 <br><br>";
            }
            for(int i = 0; i<cerrado.size(); i++){
                String item = cerrado.get(i);
                if(!item.equals("null") && i!=cerrado.size()-1)
                    cerr += "$"+item+"<br><br>";
                else if(i == cerrado.size()-1 && !item.equals("null"))
                    cerr += "<strong>$"+item+"</strong><br><br>";
                else if(i == cerrado.size()-1){
                    cerr += "<strong>$ 0 </strong><br><br>";
                }
                else
                    cerr += "$ 0 <br><br>";
            }
            for(String item : oficinas){
                if(!item.equals("null") && !item.equals("Total"))
                    o += item+":<br><br>";
                else if (item.equals("Total")){
                    o += "<strong>"+item+"</strong>";
                }
            }
            tvoficina.setText(Html.fromHtml(o));
            tvcobro.setText(Html.fromHtml(c));
            tvcerrado.setText(Html.fromHtml(cerr));
            dialog.hide();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            String cadena = strings[0]+"?FECHA="+strings[1];
            URL url = null;
            String devuelve = "";
            oficinas.clear();
            cerrado.clear();
            cobrado.clear();
            try{
                url = new URL(cadena);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent","Mozilla/5.0"+" (Linux; Android 1.5; es-ES) Cobranza HTTP");
                //connection.setHeader("content-type","application/json");

                int respuesta = connection.getResponseCode();
                StringBuilder result = new StringBuilder();

                if(respuesta == HttpURLConnection.HTTP_OK){
                    InputStream in = new BufferedInputStream(connection.getInputStream());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    String linea;
                    while((linea = reader.readLine())!= null){
                        result.append(linea);
                    }
                    JSONObject respuestaJson = new JSONObject(result.toString());

                    JSONArray resultJson = respuestaJson.getJSONArray("cobranza");
                    for(int i = 0; i<resultJson.length();i++){
                        oficinas.add(resultJson.getJSONObject(i).getString("oficina"));
                        cobrado.add(resultJson.getJSONObject(i).getString("cobrado"));
                        cerrado.add(resultJson.getJSONObject(i).getString("cerrado"));
                    }
                }
            }catch (MalformedURLException e){
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }catch(JSONException e){
                e.printStackTrace();
            }
            return devuelve;
        }
    }
}
