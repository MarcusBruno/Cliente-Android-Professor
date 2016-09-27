package com.ifms.tcc.marcus_bruno.tcc.Utils;

//http://blog.romarconsultoria.com.br/2013/12/verificar-existencia-de-conexao-com.html
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class DetectaConexao {
    private Context _context;

    public DetectaConexao(Context context){
        this._context = context;
    }

    public boolean existeConexao(){
        ConnectivityManager connectivity = (ConnectivityManager)
                _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo netInfo = connectivity.getActiveNetworkInfo();
            // Se não existe nenhum tipo de conexão retorna false
            if (netInfo == null) {
                return false;
            }

            int netType = netInfo.getType();
            // Verifica se a conexão é do tipo WiFi ou Mobile e
            // retorna true se estiver conectado ou false em
            // caso contrário
            if (netType == ConnectivityManager.TYPE_WIFI ||
                    netType == ConnectivityManager.TYPE_MOBILE) {
                return netInfo.isConnected();

            } else {
                return false;
            }
        }else{
            return false;
        }
    }
    //Customização para que também seja possivel verificar o GPS.
    public boolean localizacaoAtiva(){
        //http://pt.stackoverflow.com/questions/48787/c%C3%B3digo-para-checar-se-o-gps-est%C3%A1-ativo
        LocationManager manager = (LocationManager) _context.getSystemService(Context.LOCATION_SERVICE);
        boolean isOn = manager.isProviderEnabled( LocationManager.GPS_PROVIDER);
        return isOn;
    }
}
