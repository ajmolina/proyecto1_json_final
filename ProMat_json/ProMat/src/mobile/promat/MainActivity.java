package mobile.promat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import mobile.promat.FragmentSeleccionAsignaturas.GetDataTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.array;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	int creditosCursados;
	double promedioAcumulado; //Semestral
	double promedioDeseado = -1.0; //Semestral (-1.0 = asumido como valor inexistente)
	
	protected JSONObject mData;
	
	public static final String TAG_FRAGMENT = "TAG_FRAGMENT";
	SQLiteDatabase database;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		FragmentManager fm = getFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.container);

		if (fragment == null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		
		database = openOrCreateDatabase("HistorialNotas", Context.MODE_PRIVATE, null);
		//database.execSQL("DROP TABLE IF EXISTS materias");
		//database.execSQL("DROP TABLE IF EXISTS notas");
		database.execSQL("CREATE TABLE IF NOT EXISTS materias(id INTEGER PRIMARY KEY AUTOINCREMENT,nombre TEXT NOT NULL,creditos INTEGER NOT NULL,nota REAL);");
		database.execSQL("CREATE TABLE IF NOT EXISTS notas(id INTEGER PRIMARY KEY AUTOINCREMENT,valor REAL,porcentaje REAL,idMateria INTEGER NOT NULL,FOREIGN KEY(idMateria) REFERENCES materias(id));");
		database.close();
		
		//JSON
//		if (isNetworkAvailable()) {
//			GetDataTask getDataTask = new GetDataTask();
//			getDataTask.execute();
//		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void ingresarDatosIniciales(View v){
		
		EditText editCreditosCursados = (EditText)findViewById(R.id.editCreditosCursados);
		EditText editPromedioAcumulado = (EditText)findViewById(R.id.editPromedioAcumulado);
		
		if (editCreditosCursados.getText().toString().trim().length()!=0 &&
				editPromedioAcumulado.getText().toString().trim().length()!=0){
			this.creditosCursados = Integer.parseInt(editCreditosCursados.getText().toString());
			this.promedioAcumulado = Double.parseDouble(editPromedioAcumulado.getText().toString());
			
			// Begin the transaction
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			// Replace the container with the new fragment
			ft.replace(R.id.container, new FragmentMenu()).addToBackStack(TAG_FRAGMENT);
			// or ft.add(R.id.your_placeholder, new FooFragment());
			// Execute the changes specified
			ft.commit();
			
			Toast.makeText(this, 
					"Creditos cursados: "+this.creditosCursados+"\n"+
					"Promedio acumulado: "+this.promedioAcumulado,
					Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(this, 
					"Por favor, ingrese todos los datos requeridos antes de continuar.",
					Toast.LENGTH_SHORT).show();
		}
		
		
		
	}
	
	public void verVistaSeleccionAsignaturas(View v){
		// Begin the transaction
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		// Replace the container with the new fragment
		//ft.replace(R.id.container, new FragmentAsignaturas()).addToBackStack(TAG_FRAGMENT);
		ft.replace(R.id.container, new FragmentSeleccionAsignaturas()).addToBackStack(TAG_FRAGMENT);
		// or ft.add(R.id.your_placeholder, new FooFragment());
		// Execute the changes specified
		ft.commit();
	}
	
	public void verVistaAsignaturas(View v){
		// Begin the transaction
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		// Replace the container with the new fragment
		//ft.replace(R.id.container, new FragmentAsignaturas()).addToBackStack(TAG_FRAGMENT);
		ft.replace(R.id.container, new FragmentNotas(creditosCursados, promedioAcumulado, promedioDeseado)).addToBackStack(TAG_FRAGMENT);
		// or ft.add(R.id.your_placeholder, new FooFragment());
		// Execute the changes specified
		ft.commit();
	}
	
	public void verVistaPromedio(View v){
		
		//Preguntar si hay alguna materia en el sistema o no
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		
		database = openOrCreateDatabase("HistorialNotas", Context.MODE_PRIVATE, null);
				
		Cursor c = database.rawQuery("SELECT * FROM materias; ", null);
		
		if (c.moveToFirst()){
			//Existen materias en el sistema
			ft.replace(R.id.container, new FragmentPromedio(this.creditosCursados,this.promedioAcumulado)).addToBackStack(TAG_FRAGMENT);
		}else{
			//No existen materias en el sistema
			ft.replace(R.id.container, new FragmentPromedioDefault(this.creditosCursados,this.promedioAcumulado)).addToBackStack(TAG_FRAGMENT);
		}
		
		database.close();
		ft.commit();
		
	}
	
	public void verVistaNotas(View v){
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.container, new FragmentNotas(creditosCursados, promedioAcumulado, promedioDeseado)).addToBackStack(TAG_FRAGMENT);
		ft.commit();
	}
	
	public void ingresarAsignaturaDelServidor(View v){
		
		EditText editTextCreditos = (EditText) findViewById(R.id.editCreditosSeleccion);
		
		if (editTextCreditos.getText().toString().trim().length()!=0){
			//AGREGAR MATERIA Y NOTAS CORRESPONDIENTES A LA BASE DE DATOS
			if (isNetworkAvailable()) {
				GetDataTask getDataTask = new GetDataTask();
				getDataTask.execute();
			}
		}else{
			Toast.makeText(this, "Por favor ingrese el número de creditos de la asignatura", Toast.LENGTH_SHORT).show();
		}
		
		
	}
	
	public void ingresarAsignatura(View v){
		
		//AGREGAR A LA BASE DE DATOS
		EditText editAsignatura1 = (EditText) findViewById(R.id.editAsignatura1);
		EditText editAsignatura2 = (EditText) findViewById(R.id.editAsignatura2);
		EditText editAsignatura3 = (EditText) findViewById(R.id.editAsignatura3);
		EditText editCreditos1 = (EditText) findViewById(R.id.editCreditos1);
		EditText editCreditos2 = (EditText) findViewById(R.id.editCreditos2);
		EditText editCreditos3 = (EditText) findViewById(R.id.editCreditos3);
		boolean nuevaMateria = false;
		boolean campoUsado1 = false;
		boolean campoUsado2 = false;
		boolean campoUsado3 = false;

		database = openOrCreateDatabase("HistorialNotas", Context.MODE_PRIVATE, null);
		if (editAsignatura1.getText().toString().trim().length() != 0 &&
				editCreditos1.getText().toString().trim().length() != 0){			
			database.execSQL("INSERT INTO materias (nombre,creditos,nota) VALUES('"
					+ editAsignatura1.getText() + "',"
					+ editCreditos1.getText() + ",0);");
			nuevaMateria = true;
			campoUsado1 = true;
		}
		
		if (editAsignatura2.getText().toString().trim().length() != 0 &&
				editCreditos2.getText().toString().trim().length() != 0){
			database.execSQL("INSERT INTO materias (nombre,creditos,nota) VALUES('"
					+ editAsignatura2.getText() + "',"
					+ editCreditos2.getText() + ",0);");
			nuevaMateria = true;
			campoUsado2 = true;
		}		
		
		if (editAsignatura3.getText().toString().trim().length() != 0 &&
				editCreditos3.getText().toString().trim().length() != 0){
			database.execSQL("INSERT INTO materias (nombre,creditos,nota) VALUES('"
					+ editAsignatura3.getText() + "',"
					+ editCreditos3.getText() + ",0);");
			nuevaMateria = true;
			campoUsado3 = true;
		}
		
		database.close();
		
		if (nuevaMateria == false){
			Toast.makeText(this, "No se agregaron nuevas materias a la base de datos.",
					Toast.LENGTH_SHORT).show();
		}else{			
			if (campoUsado1 == true){
				Toast.makeText(this, 
						editAsignatura1.getText()+" , con "+editCreditos1.getText()+" creditos, fue agregado " +
								"satisfactoriamente.",
						Toast.LENGTH_SHORT).show();
			}
			if (campoUsado2 == true){
				Toast.makeText(this, 
						editAsignatura2.getText()+" , con "+editCreditos2.getText()+" creditos, fue agregado " +
								"satisfactoriamente.",
						Toast.LENGTH_SHORT).show();
			}
			if (campoUsado3 == true){
				Toast.makeText(this, 
						editAsignatura3.getText()+" , con "+editCreditos3.getText()+" creditos, fue agregado " +
								"satisfactoriamente.",
						Toast.LENGTH_SHORT).show();
			}
			
		}
		
		editAsignatura1.setText("");
		editAsignatura2.setText("");
		editAsignatura3.setText("");
		editCreditos1.setText("");
		editCreditos2.setText("");
		editCreditos3.setText("");
		
		
		
	}

	public void ingresarNotas(View v){
		Spinner spinnerAsignaturas = (Spinner) findViewById(R.id.spinnerAsignaturas);
		EditText editNotas1 = (EditText) findViewById(R.id.editNotas1);
		EditText editNotas2 = (EditText) findViewById(R.id.editNotas2);
		EditText editNotas3 = (EditText) findViewById(R.id.editNotas3);
		EditText editNotas4 = (EditText) findViewById(R.id.editNotas4);
		EditText editNotas5 = (EditText) findViewById(R.id.editNotas5);
		EditText editNotas6 = (EditText) findViewById(R.id.editNotas6);
		EditText editNotas7 = (EditText) findViewById(R.id.editNotas7);
		EditText editNotas8 = (EditText) findViewById(R.id.editNotas8);
		EditText editPorcentaje1 = (EditText) findViewById(R.id.editPorcentaje1);
		EditText editPorcentaje2 = (EditText) findViewById(R.id.editPorcentaje2);
		EditText editPorcentaje3 = (EditText) findViewById(R.id.editPorcentaje3);
		EditText editPorcentaje4 = (EditText) findViewById(R.id.editPorcentaje4);
		EditText editPorcentaje5 = (EditText) findViewById(R.id.editPorcentaje5);
		EditText editPorcentaje6 = (EditText) findViewById(R.id.editPorcentaje6);
		EditText editPorcentaje7 = (EditText) findViewById(R.id.editPorcentaje7);
		EditText editPorcentaje8 = (EditText) findViewById(R.id.editPorcentaje8);
		boolean nuevaNota = false;
		
		database = openOrCreateDatabase("HistorialNotas", Context.MODE_PRIVATE, null);
		String nombreAsignatura = spinnerAsignaturas.getSelectedItem().toString();
		Cursor c = database.rawQuery(
				"SELECT id FROM materias " +
				"WHERE nombre ='"+ nombreAsignatura + "'", null);
		
		
		if (c.moveToFirst()){
			String id = c.getString(0);

			database.execSQL(
					"DELETE FROM notas " +
					"WHERE idMateria="+id+";"
					);
			
			
			if (editPorcentaje1.getText().toString().trim().length() != 0){
				nuevaNota=true;
				if (editNotas1.getText().toString().trim().length() != 0){
					database.execSQL("INSERT INTO notas (valor,porcentaje,idMateria) VALUES(" +
							+ Double.parseDouble(editNotas1.getText().toString()) + "," 
							+ Double.parseDouble(editPorcentaje1.getText().toString()) + ","
							+"'"+ id +"');");
				}else{
					database.execSQL("INSERT INTO notas (valor,porcentaje,idMateria) VALUES("
							+ "null" + "," 
							+ Double.parseDouble(editPorcentaje1.getText().toString()) + ","
							+"'"+ id +"');");
				}			

			}
			
			if (editPorcentaje2.getText().toString().trim().length() != 0){
				nuevaNota=true;
				if (editNotas2.getText().toString().trim().length() != 0){
					database.execSQL("INSERT INTO notas (valor,porcentaje,idMateria) VALUES(" +
							+ Double.parseDouble(editNotas2.getText().toString()) + "," 
							+ Double.parseDouble(editPorcentaje2.getText().toString()) + ","
							+"'"+ id +"');");
				}else{
					database.execSQL("INSERT INTO notas (valor,porcentaje,idMateria) VALUES("
							+ "null" + "," 
							+ Double.parseDouble(editPorcentaje2.getText().toString()) + ","
							+"'"+ id +"');");
				}			

			}
			
			if (editPorcentaje3.getText().toString().trim().length() != 0){
				nuevaNota=true;
				if (editNotas3.getText().toString().trim().length() != 0){
					database.execSQL("INSERT INTO notas (valor,porcentaje,idMateria) VALUES(" +
							+ Double.parseDouble(editNotas3.getText().toString()) + "," 
							+ Double.parseDouble(editPorcentaje3.getText().toString()) + ","
							+"'"+ id +"');");
				}else{
					database.execSQL("INSERT INTO notas (valor,porcentaje,idMateria) VALUES("
							+ "null" + "," 
							+ Double.parseDouble(editPorcentaje3.getText().toString()) + ","
							+"'"+ id +"');");
				}			

			}
			
			if (editPorcentaje4.getText().toString().trim().length() != 0){
				nuevaNota=true;
				if (editNotas4.getText().toString().trim().length() != 0){
					database.execSQL("INSERT INTO notas (valor,porcentaje,idMateria) VALUES(" +
							+ Double.parseDouble(editNotas4.getText().toString()) + "," 
							+ Double.parseDouble(editPorcentaje4.getText().toString()) + ","
							+"'"+ id +"');");
				}else{
					database.execSQL("INSERT INTO notas (valor,porcentaje,idMateria) VALUES("
							+ "null" + "," 
							+ Double.parseDouble(editPorcentaje4.getText().toString()) + ","
							+"'"+ id +"');");
				}			

			}
			
			if (editPorcentaje5.getText().toString().trim().length() != 0){
				nuevaNota=true;
				if (editNotas5.getText().toString().trim().length() != 0){
					database.execSQL("INSERT INTO notas (valor,porcentaje,idMateria) VALUES(" +
							+ Double.parseDouble(editNotas5.getText().toString()) + "," 
							+ Double.parseDouble(editPorcentaje5.getText().toString()) + ","
							+"'"+ id +"');");
				}else{
					database.execSQL("INSERT INTO notas (valor,porcentaje,idMateria) VALUES("
							+ "null" + "," 
							+ Double.parseDouble(editPorcentaje5.getText().toString()) + ","
							+"'"+ id +"');");
				}			

			}
			
			if (editPorcentaje6.getText().toString().trim().length() != 0){
				nuevaNota=true;
				if (editNotas6.getText().toString().trim().length() != 0){
					database.execSQL("INSERT INTO notas (valor,porcentaje,idMateria) VALUES(" +
							+ Double.parseDouble(editNotas6.getText().toString()) + "," 
							+ Double.parseDouble(editPorcentaje6.getText().toString()) + ","
							+"'"+ id +"');");
				}else{
					database.execSQL("INSERT INTO notas (valor,porcentaje,idMateria) VALUES("
							+ "null" + "," 
							+ Double.parseDouble(editPorcentaje6.getText().toString()) + ","
							+"'"+ id +"');");
				}			

			}
			
			if (editPorcentaje7.getText().toString().trim().length() != 0){
				nuevaNota=true;
				if (editNotas7.getText().toString().trim().length() != 0){
					database.execSQL("INSERT INTO notas (valor,porcentaje,idMateria) VALUES(" +
							+ Double.parseDouble(editNotas7.getText().toString()) + "," 
							+ Double.parseDouble(editPorcentaje7.getText().toString()) + ","
							+"'"+ id +"');");
				}else{
					database.execSQL("INSERT INTO notas (valor,porcentaje,idMateria) VALUES("
							+ "null" + "," 
							+ Double.parseDouble(editPorcentaje7.getText().toString()) + ","
							+"'"+ id +"');");
				}			

			}
			
			if (editPorcentaje8.getText().toString().trim().length() != 0){
				nuevaNota=true;
				if (editNotas8.getText().toString().trim().length() != 0){
					database.execSQL("INSERT INTO notas (valor,porcentaje,idMateria) VALUES(" +
							+ Double.parseDouble(editNotas8.getText().toString()) + "," 
							+ Double.parseDouble(editPorcentaje8.getText().toString()) + ","
							+"'"+ id +"');");
				}else{
					database.execSQL("INSERT INTO notas (valor,porcentaje,idMateria) VALUES("
							+ "null" + "," 
							+ Double.parseDouble(editPorcentaje8.getText().toString()) + ","
							+"'"+ id +"');");
				}			

			}
		}
		
		if (nuevaNota==false){
			Toast.makeText(this, "No se actualizaron notas.",
					Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(this, "Se han actualizado notas satisfactoriamente.",
					Toast.LENGTH_SHORT).show();
		}
		
		database.close();
	}
	
	public void calcularNotas(View v){
		
		Spinner spinnerAsignaturas = (Spinner) findViewById(R.id.spinnerAsignaturas);
		EditText editNotaDeseada = (EditText) findViewById(R.id.editNotaDeseada);
		EditText editNotas1 = (EditText) findViewById(R.id.editNotas1);
		EditText editNotas2 = (EditText) findViewById(R.id.editNotas2);
		EditText editNotas3 = (EditText) findViewById(R.id.editNotas3);
		EditText editNotas4 = (EditText) findViewById(R.id.editNotas4);
		EditText editNotas5 = (EditText) findViewById(R.id.editNotas5);
		EditText editNotas6 = (EditText) findViewById(R.id.editNotas6);
		EditText editNotas7 = (EditText) findViewById(R.id.editNotas7);
		EditText editNotas8 = (EditText) findViewById(R.id.editNotas8);
		
		if (editNotaDeseada.getText().toString().trim().length()!=0){
		
			database = openOrCreateDatabase("HistorialNotas", Context.MODE_PRIVATE, null);
			String nombreAsignatura = spinnerAsignaturas.getSelectedItem().toString();
			
			Double notaDeseada = Double.parseDouble(editNotaDeseada.getText().toString());
			Double notaActual = 0.0;
			Double notaFaltante;
			Double notaLibre = 0.0;
			Double sobrante;
			
			ArrayList<Datos> datos = new ArrayList<>();
			
			Cursor c = database.rawQuery(
					"SELECT notas.valor,notas.porcentaje,(notas.valor*notas.porcentaje)/100,(5*notas.porcentaje)/100 " +
					"FROM notas,materias " +
					"WHERE notas.idMateria=materias.id " +
					"AND materias.nombre ='"+ nombreAsignatura + "'", null);
			
			if (c.moveToFirst()){
				do{
												
					if (c.isNull(0)){
						datos.add(new Datos(
								0.0,
								Double.parseDouble(c.getString(1)),
								0.0,
								Double.parseDouble(c.getString(3)),
								false,
								true));
						datos.get(c.getPosition()).setValorNull(true);
						notaLibre = notaLibre + datos.get(c.getPosition()).getValorMax();
					}else{
						datos.add(new Datos(
								Double.parseDouble(c.getString(0)),
								Double.parseDouble(c.getString(1)),
								Double.parseDouble(c.getString(2)),
								Double.parseDouble(c.getString(3)),
								true,
								false));
					}				
					notaActual = notaActual + datos.get(c.getPosition()).getValorReal();
				
				}while(c.moveToNext());
				
			}		
			
			database.close();
			notaFaltante = notaDeseada - notaActual;
			if (notaFaltante<=0.0){
				Toast.makeText(this, "Ya has alcanzado la nota deseada.",
						Toast.LENGTH_SHORT).show();
				//MOSTRAR NOTAS ORIGINALES:
				for (int i=0;i<datos.size();i++){
					if (datos.get(i).isValorNull()==true){
						switch(i){
						case 0:
							editNotas1.setText(""+datos.get(i).getValorNuevo());
							break;
						case 1:
							editNotas2.setText(""+datos.get(i).getValorNuevo());
							break;
						case 2:
							editNotas3.setText(""+datos.get(i).getValorNuevo());
							break;
						case 3:
							editNotas4.setText(""+datos.get(i).getValorNuevo());
							break;
						case 4:
							editNotas5.setText(""+datos.get(i).getValorNuevo());
							break;
						case 5:
							editNotas6.setText(""+datos.get(i).getValorNuevo());
							break;
						case 6:
							editNotas7.setText(""+datos.get(i).getValorNuevo());
							break;
						case 7:
							editNotas8.setText(""+datos.get(i).getValorNuevo());
							break;
						default: break;
						}
					}
					
				}
				
			}else{
				sobrante = (notaActual+notaLibre)-notaDeseada;
				if (sobrante>=0){
					//Se puede alcanzar la nota deseada
					while (notaFaltante>0){
						
						for (int i=0;i<datos.size();i++){
							if (datos.get(i).isValorNull()){
								if (notaFaltante-datos.get(i).getValorMax()<=0){
									//Si el estudiante sacara 5.0 en esta nota.
									//No dependerﾃｭa de otra nota para pasar.
									//Hay que adicionar lo que falte en la nota correspondiente.
									//Parar el proceso con notaFaltante=0
									datos.get(i).setValorNuevo((notaFaltante/(datos.get(i).getPorcentaje()/100)));
									notaFaltante = 0.0;
								}else{
									//No es suficiente.
									//El estudiante requiere de otra nota para pasar.
									//Repetir el proceso
									datos.get(i).setValorNuevo(5.0);
									notaFaltante = notaFaltante - datos.get(i).getValorMax();
								}
							}
						}
					}
					
					//MOSTRAR NOTAS FALTANTES:
					for (int i=0;i<datos.size();i++){
						if (datos.get(i).isValorNull()==true){
							switch(i){
							case 0:
								editNotas1.setText(""+datos.get(i).getValorNuevo());
								break;
							case 1:
								editNotas2.setText(""+datos.get(i).getValorNuevo());
								break;
							case 2:
								editNotas3.setText(""+datos.get(i).getValorNuevo());
								break;
							case 3:
								editNotas4.setText(""+datos.get(i).getValorNuevo());
								break;
							case 4:
								editNotas5.setText(""+datos.get(i).getValorNuevo());
								break;
							case 5:
								editNotas6.setText(""+datos.get(i).getValorNuevo());
								break;
							case 6:
								editNotas7.setText(""+datos.get(i).getValorNuevo());
								break;
							case 7:
								editNotas8.setText(""+datos.get(i).getValorNuevo());
								break;
							default: break;
							}
						}
						
					}
					
				}else{
					//Es imposible alcanzar la nota deseada
					Toast.makeText(this, "No es posible alcanzar la nota deseada.",
							Toast.LENGTH_SHORT).show();
					//MOSTRAR NOTAS ORIGINALES:
					for (int i=0;i<datos.size();i++){
						if (datos.get(i).isValorNull()==true){
							switch(i){
							case 0:
								editNotas1.setText(""+datos.get(i).getValorNuevo());
								break;
							case 1:
								editNotas2.setText(""+datos.get(i).getValorNuevo());
								break;
							case 2:
								editNotas3.setText(""+datos.get(i).getValorNuevo());
								break;
							case 3:
								editNotas4.setText(""+datos.get(i).getValorNuevo());
								break;
							case 4:
								editNotas5.setText(""+datos.get(i).getValorNuevo());
								break;
							case 5:
								editNotas6.setText(""+datos.get(i).getValorNuevo());
								break;
							case 6:
								editNotas7.setText(""+datos.get(i).getValorNuevo());
								break;
							case 7:
								editNotas8.setText(""+datos.get(i).getValorNuevo());
								break;
							default: break;
							}
						}
						
					}
				}
			}
		
		}else{
			Toast.makeText(this, "Por favor digite la nota deseada.",
					Toast.LENGTH_SHORT).show();
		}
		
	}
	
	public void borrarBaseDatos(View v){
		database = openOrCreateDatabase("HistorialNotas", Context.MODE_PRIVATE, null);
		database.execSQL("DROP TABLE IF EXISTS materias");
		database.execSQL("DROP TABLE IF EXISTS notas");
		database.execSQL("CREATE TABLE IF NOT EXISTS materias(id INTEGER PRIMARY KEY AUTOINCREMENT,nombre TEXT NOT NULL,creditos INTEGER NOT NULL,nota REAL);");
		database.execSQL("CREATE TABLE IF NOT EXISTS notas(id INTEGER PRIMARY KEY AUTOINCREMENT,valor REAL,porcentaje REAL,idMateria INTEGER NOT NULL,FOREIGN KEY(idMateria) REFERENCES materias(id));");
		database.close();
		Toast.makeText(this, "La base de datos de materias y notas fue borrado exitosamente.",
				Toast.LENGTH_SHORT).show();
	}
	
	public void calcularPromedioAcumuladoPorDefecto(View v){
		//CALCULAR PROMEDIO ACUMULADO POR DEFECTO
    	
    	EditText editPromedioDeseado = (EditText) findViewById(R.id.editPromedioDeseadoDefault);
    	TextView txtCreditosCursados = (TextView) findViewById(R.id.txtValorCreditosCursadosDefault);
    	TextView txtPromedioAcumulado = (TextView) findViewById(R.id.txtValorPromedioAcumuladoDefault);
    	TextView txtResultado12 = (TextView) findViewById(R.id.txtResult12);
    	TextView txtResultado13 = (TextView) findViewById(R.id.txtResult13);
    	TextView txtResultado14 = (TextView) findViewById(R.id.txtResult14);
    	TextView txtResultado15 = (TextView) findViewById(R.id.txtResult15);
    	TextView txtResultado16 = (TextView) findViewById(R.id.txtResult16);
    	TextView txtResultado17 = (TextView) findViewById(R.id.txtResult17);
    	TextView txtResultado18 = (TextView) findViewById(R.id.txtResult18);
    	
    	if (editPromedioDeseado.getText().toString().trim().length()!=0){
    	
		
			for (int creditosSemestreActual=12; 
					creditosSemestreActual<=18;
					creditosSemestreActual++){
				
				double promedioRequeridoSemestreActual = promedioRequeridoSemestreActual(
						Integer.parseInt(txtCreditosCursados.getText().toString()),
						creditosSemestreActual,
						Double.parseDouble(txtPromedioAcumulado.getText().toString()),
						Double.parseDouble(editPromedioDeseado.getText().toString()));
				
				switch(creditosSemestreActual){
				case 12:txtResultado12.setText(""+promedioRequeridoSemestreActual); break;
				case 13:txtResultado13.setText(""+promedioRequeridoSemestreActual); break;
				case 14:txtResultado14.setText(""+promedioRequeridoSemestreActual); break;
				case 15:txtResultado15.setText(""+promedioRequeridoSemestreActual); break;
				case 16:txtResultado16.setText(""+promedioRequeridoSemestreActual); break;
				case 17:txtResultado17.setText(""+promedioRequeridoSemestreActual); break;
				case 18:txtResultado18.setText(""+promedioRequeridoSemestreActual); break;
				}			
			}
			
			this.promedioDeseado = Double.parseDouble(editPromedioDeseado.getText().toString());
		
    	}else{
			Toast.makeText(this, 
					"Por favor, ingrese el promedio semestral deseado antes de continuar.",
					Toast.LENGTH_SHORT).show();   		
    		
    	} //Fin de la validaciﾃｳn

	}
	
	static double promedioRequeridoSemestreActual(int creditosCursados, 
												int creditosActuales,
												double promedioAcumulado, 
												double promedioDeseado){
		return ((promedioDeseado * (creditosCursados + creditosActuales)) 
				- (promedioAcumulado * creditosCursados)) 
				/ creditosActuales;
		
		/*
		double promedioRequeridoSemestreActual = (
				(Double.parseDouble(editPromedioDeseado.getText().toString()) * 
				(Integer.parseInt(txtCreditosCursados.getText().toString()) +
						creditosSemestreActual)) - 
				(Double.parseDouble(txtPromedioAcumulado.getText().toString()) *
				Integer.parseInt(txtCreditosCursados.getText().toString())))
				/ creditosSemestreActual;
		*/
	}

	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
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
						
						runOnUiThread(new Runnable() {
							  public void run() {
							    Toast.makeText(getApplicationContext(), "Iniciando carga JSON", Toast.LENGTH_SHORT).show();
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
				Toast.makeText(getApplicationContext(), "Hubo un problema al realizar la transacción. Por favor inténtalo de nuevo.", Toast.LENGTH_SHORT).show();
			}else{
				mData = result;
				cargar();
			}
		}

	}
	
	public void cargar(){
		
		Spinner spinnerAsignaturasDisponibles = (Spinner) findViewById(R.id.spinnerAsignaturasServidor);
		EditText editTextCreditos = (EditText) findViewById(R.id.editCreditosSeleccion);
		
		Toast.makeText(this, "Ejecutando transacción ", Toast.LENGTH_LONG).show();
	
		try {
			
			JSONArray jsonPosts = mData.getJSONArray("materias");
			boolean estado=false;
			
			for (int i = 0;i<jsonPosts.length() && estado==false;i++){
				
				JSONObject post = jsonPosts.getJSONObject(i);
				String nombreMateria = post.getString("nombre_materia");
				JSONArray jsonComponentes = post.getJSONArray("componetes");
				
				if (spinnerAsignaturasDisponibles.getSelectedItem().toString().compareTo(nombreMateria)==0){
					//SON IGUALES: BUSCAR LAS NOTAS AQUÍ
					//AGREGAR INFORMACIÓN A LA BASE DE DATOS
					database = openOrCreateDatabase("HistorialNotas", Context.MODE_PRIVATE,null);
					
					database.execSQL("INSERT INTO materias (nombre,creditos) VALUES('"
							+ nombreMateria + "'," 
							+ Double.parseDouble(editTextCreditos.getText().toString()) 
							+ ");");
					
					Cursor c = database.rawQuery("SELECT id FROM materias "
									+ "WHERE nombre ='"
									+ nombreMateria + "';", null);
					
					if (c.moveToFirst()){
						
						for (int j = 0; j < jsonComponentes.length(); j++) {
							JSONObject componente = jsonComponentes.getJSONObject(j);
							String porcentaje = componente.getString("peso");						
							database.execSQL("INSERT INTO notas (valor,porcentaje,idMateria) VALUES("
									+ "null" + "," 
									+ Double.parseDouble(porcentaje) + ","
									+"'"+ c.getString(0) +"');");
							
							
							
						}
						
					}					
					
					
					estado=true;					
					database.close();
					
				}else{
					//NO SON IGUALES: SEGUIR BUSCANDO
					
				}				
					
				
			}
			
		} catch (JSONException e) {
			Toast.makeText(this, "Transacción cancelada ", Toast.LENGTH_LONG)
			.show();
		}
		
		Toast.makeText(this, "Transacción finalizada ", Toast.LENGTH_LONG)
		.show();
		
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(MainActivity.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		boolean isNetworkAvaible = false;
		if (networkInfo != null && networkInfo.isConnected()) {
			isNetworkAvaible = true;
			Toast.makeText(this, "Network is available ", Toast.LENGTH_LONG)
					.show();
		} else {
			Toast.makeText(this, "Network is not available ", Toast.LENGTH_LONG)
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
