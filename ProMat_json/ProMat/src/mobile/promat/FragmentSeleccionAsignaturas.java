package mobile.promat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class FragmentSeleccionAsignaturas extends Fragment{
	
	protected JSONObject mData;
	SQLiteDatabase database;
	ArrayList<String> informacionMateriasServidor;
	
	//NOTA: EN ESTA CLASE SE IMPLEMENTA EL ENVÍO DE MATERIAS DEL SERVIDOR AL SPINNER.
	
	private Spinner spinnerAsignaturasDisponibles;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {	
		View v = inflater.inflate(R.layout.fragment_seleccion_asignaturas, container, false);	

		
		return v;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		spinnerAsignaturasDisponibles = (Spinner)getActivity().findViewById(R.id.spinnerAsignaturasServidor);
		informacionMateriasServidor = new ArrayList<String>();
		
		//AGREGUE MATERIAS EN EL ARRAYLIST
		if (isNetworkAvailable()) {
			GetDataTask getDataTask = new GetDataTask();
			getDataTask.execute();
		}
		
	}
	
	public class GetDataTask extends AsyncTask<Object, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(Object... params) {
			int responseCode = -1;
			JSONObject jsonResponse = null;
			try {
				URL blogFeedUsr = new URL(
						"http://augustodesarrollador.com/promedio_app/read.php");
				HttpURLConnection connection = (HttpURLConnection) blogFeedUsr
						.openConnection();
				connection.connect();

				responseCode = connection.getResponseCode();

				if (responseCode == HttpURLConnection.HTTP_OK) {

					try {
						getActivity().runOnUiThread(new Runnable() {
							  public void run() {
							    Toast.makeText(getActivity(), "Iniciando carga JSON", Toast.LENGTH_SHORT).show();
							  }
							});
						
						jsonResponse = new JSONObject(
								readUrl("http://augustodesarrollador.com/promedio_app/read.php"));

					} catch (JSONException e) {
						e.printStackTrace();
					}

				} else {

				}
				
			} catch (MalformedURLException e) {

			} catch (IOException e) {

			} catch (Exception e) {

			}
			return jsonResponse;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			if (result==null){
				Toast.makeText(getActivity(), "Hubo un problema al realizar la transacción. Por favor inténtalo de nuevo.", Toast.LENGTH_SHORT).show();
			}else{
				mData = result;
				cargar();
			}
			
		}

	}
	
	public void cargar(){
		
		Toast.makeText(getActivity(), "Ejecutando transacción ", Toast.LENGTH_LONG)
		.show();
	
		try {
			
			JSONArray jsonPosts = mData.getJSONArray("materias");
			for (int i = 0;i< jsonPosts.length();i++){
				
				JSONObject post = jsonPosts.getJSONObject(i);
				informacionMateriasServidor.add(post.getString("nombre_materia"));		
				
			}
			
			ArrayAdapter<String> Adapter = new ArrayAdapter<String>(getActivity().getBaseContext(), android.R.layout.simple_list_item_1, informacionMateriasServidor);
			spinnerAsignaturasDisponibles.setAdapter(Adapter);
			
		} catch (JSONException e) {
			Toast.makeText(getActivity(), "Transacción cancelada ", Toast.LENGTH_LONG)
			.show();
		}
		
		Toast.makeText(getActivity(), "Transacción finalizada ", Toast.LENGTH_LONG)
		.show();
		
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(MainActivity.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		boolean isNetworkAvaible = false;
		if (networkInfo != null && networkInfo.isConnected()) {
			isNetworkAvaible = true;
			Toast.makeText(getActivity(), "Network is available ", Toast.LENGTH_LONG)
					.show();
		} else {
			Toast.makeText(getActivity(), "Network is not available ", Toast.LENGTH_LONG)
					.show();
		}
		return isNetworkAvaible;
	}
	
	private static String readUrl(String urlString) throws Exception {
		BufferedReader reader = null;
		try {
			URL url = new URL(urlString);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer buffer = new StringBuffer();
			int read;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1)
				buffer.append(chars, 0, read);

			return buffer.toString();
		} finally {
			if (reader != null)
				reader.close();
		}
	}
	
	
	
	

	
}
