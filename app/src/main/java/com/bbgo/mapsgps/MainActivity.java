package com.bbgo.mapsgps;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

/**
 * Exemplo com GPS
 * Fused location Provider:
 *    - Provedor de GPS criado pela Google que faz parte do Google Play Services.
 *    - Google Play Services é um aplicativo distribuído pelo Google Play que apresenta funcionalidades
 *      essenciais para se comunicar com os serviços do Google, além de várias APIs.
 * Encapsula o acesso aos provedores:
 *    - GPS_PROVIDER e NETWORK_PROVIDER aplicando todas as otimizações e boas práticas necessárias
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "livroandroid";
    protected GoogleMap map;
    private SupportMapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_main);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Configura o objeto GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Solicita as permissões
        String[] permissoes = new String[]{
                //Leitura das
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
        };
        if(PermissionUtils.validate(this, 0, permissoes)){
            Toast.makeText(this,"Permissão ok!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,"Falha permissões!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                // Alguma permissão foi negada, agora é com você :-)
                alertAndFinish();
                return;
            }
        }
        // Se chegou aqui está OK :-)
    }

    private void alertAndFinish() {
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name).setMessage("Para utilizar este aplicativo, você precisa aceitar as permissões.");
            // Add the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            android.support.v7.app.AlertDialog dialog = builder.create();
            dialog.show();

        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.d(TAG, "onMapReady: " + map);
        this.map = map;

        // Configura o tipo do mapa
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Bolinha azul: Exibe a localização do usuário no mapa mas não fornece a localização ao aplicativo
        map.setMyLocationEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Conecta no Google Play Services
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        // Desconecta quando o app é parado
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {toast("Conectado no Google Play Services!");}

    @Override
    public void onConnectionSuspended(int cause) {toast("Conexão interrompida.");}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {toast("Erro ao conectar: " + connectionResult);}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_my_location:
                // Última localização
                Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                Log.d(TAG, "lastLocation: " + l);

                // Atualiza a localização do mapa
                setMapLocation(l);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Atualiza a coordenada do mapa
    private void setMapLocation(Location l) {
        //Se mapa e localidade foram definidos
        if (map != null && l != null) {
            LatLng latLng = new LatLng(l.getLatitude(), l.getLongitude());

            //Define posição final da câmera
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);

            //Anima a câmera
            map.animateCamera(update);

            //Escreve posição na tela
            TextView text = (TextView) findViewById(R.id.text);
            text.setText(String.format("Lat/Lnt %f/%f, provider: %s", l.getLatitude(), l.getLongitude(), l.getProvider()));

            // Desenha uma bolinha vermelha
            CircleOptions circle = new CircleOptions().center(latLng);
            circle.fillColor(Color.RED);
            circle.radius(25); // Em metros
            map.clear();
            map.addCircle(circle);
        }
    }

    //Facilita toast
    private void toast(String s) {
        Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT).show();
    }
}