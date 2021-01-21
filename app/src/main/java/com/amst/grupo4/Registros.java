package com.amst.grupo4;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;

public class Registros extends AppCompatActivity {
    public BarChart graficosBarras;
    private RequestQueue ListaRequest = null;
    private String token = "1";
    private LinearLayout contentedorTemperaturas;
    private Map<String, TextView> temperaturasTVs;
    private Map<String,TextView> fechasTVs;
    private Registros contexto;

    public Registros() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registros);

        setTitle("Grafico de barras");
        temperaturasTVs = new HashMap<String,TextView>();
        fechasTVs = new HashMap<String,TextView>();
        ListaRequest = Volley.newRequestQueue(this);
        contexto = this;

        this.iniciarGrafico();
        this.solicitarTemperaturas();
    }

    public void iniciarGrafico(){
        //graficosBarras = findViewById(R.id.barChart);
        graficosBarras.getDescription().setEnabled(false);
        graficosBarras.setMaxVisibleValueCount(60);
        graficosBarras.setPinchZoom(false);
        graficosBarras.setDrawBarShadow(false);
        graficosBarras.setDrawGridBackground(false);

        XAxis xAxis = graficosBarras.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        graficosBarras.getAxisLeft().setDrawGridLines(false);
        graficosBarras.animateY(1500);
        graficosBarras.getLegend().setEnabled(false);
    }

    public void solicitarTemperaturas(){
        String url_registros = "https:amstdb.herokuapp.com/db/logTres";
        JsonArrayRequest requestRegistros = new JsonArrayRequest(
                Request.Method.GET, url_registros,null,
        new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                mostrarTemperaturas(response);
                actualizarGrafico(response);
            }
        }
        , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });

        ListaRequest.add(requestRegistros);
    }
    /*@Override
    public Map<String, String> getHeaders(){
        Map<String,String> params = new HashMap<>();
        params.put("Authorization", "JWT   " + token);
        return params;
    }*/

    private void mostrarTemperaturas(JSONArray temperaturas){
        String registroId;
        JSONObject registroTemp;
        LinearLayout nuevoRegistro;
        TextView fechaRegistro;
        TextView valorRegistro;

        //contentedorTemperaturas = findViewById(R.id.cont_temperaturas);
        LinearLayout.LayoutParams parametrosLayout = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1);
        try{
            for(int i =0; i<temperaturas.length();i++){
                registroTemp = (JSONObject) temperaturas.get(i);
                registroId = registroTemp.getString("id");
                if(registroTemp.getString("key").equals("temperatura")){
                    if(temperaturasTVs.containsKey(registroId) && fechasTVs.containsKey(registroId)){
                        fechaRegistro=fechasTVs.get(registroId);
                        valorRegistro=temperaturasTVs.get(registroId);

                        fechaRegistro.setText(registroTemp.getString("date_created"));
                        valorRegistro.setText(registroTemp.getString("value")+" C");
                    } else{
                        nuevoRegistro = new LinearLayout(this);
                        nuevoRegistro.setOrientation(LinearLayout.HORIZONTAL);

                        fechaRegistro = new TextView(this);
                        fechaRegistro.setLayoutParams(parametrosLayout);
                        fechaRegistro.setText(registroTemp.getString("date_created"));
                        nuevoRegistro.addView(fechaRegistro);

                        valorRegistro = new TextView(this);
                        valorRegistro.setLayoutParams(parametrosLayout);
                        valorRegistro.setText(registroTemp.getString("value")+ "C");
                        nuevoRegistro.addView(valorRegistro);

                        contentedorTemperaturas.addView(nuevoRegistro);
                        fechasTVs.put(registroId,fechaRegistro);
                        temperaturasTVs.put(registroId,valorRegistro);
                    }
                }
            }
        } catch(JSONException e){
            e.printStackTrace();
            System.out.println("error");
        }
    }

    private void actualizarGrafico(JSONArray temperaturas){
        JSONObject registro_temp;
        String temp;
        String date;
        int count = 0;
        float temp_val;
        ArrayList<BarEntry> dato_temp = new ArrayList<>();

        try{
            for(int i=0;i<temperaturas.length();i++){
                registro_temp = (JSONObject) temperaturas.get(i);
                if(registro_temp.getString("key").equals("temperatura")){
                    temp = registro_temp.getString("value");
                    date = registro_temp.getString("date_created");
                    temp_val = Float.parseFloat(temp);
                    dato_temp.add(new BarEntry(count,temp_val));
                    count++;
                }
            }
        } catch (JSONException e){
            e.printStackTrace();
            System.out.println("error");
        }
        System.out.println(dato_temp);
        llenarGrafico(dato_temp);
    }

    private void llenarGrafico(ArrayList<BarEntry> dato_temp){
        BarDataSet temperaturasDataSet;
        if(graficosBarras.getData() != null && graficosBarras.getData().getDataSetCount()>0){
            temperaturasDataSet = (BarDataSet) graficosBarras.getData().getDataSetByIndex(0);
            temperaturasDataSet.setValues(dato_temp);
            graficosBarras.getData().notifyDataChanged();
            graficosBarras.notifyDataSetChanged();
        } else {
            temperaturasDataSet = new BarDataSet (dato_temp,"Data Set");
            temperaturasDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
            temperaturasDataSet.setDrawValues(true);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(temperaturasDataSet);

            BarData data = new BarData(dataSets);
            graficosBarras.setData(data);
            graficosBarras.setFitBars(true);
        }
        graficosBarras.invalidate();
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                solicitarTemperaturas();
            }
        };
        handler.postDelayed(runnable,3000);
    }
}