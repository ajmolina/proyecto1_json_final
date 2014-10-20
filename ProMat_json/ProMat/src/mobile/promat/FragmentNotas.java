package mobile.promat;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class FragmentNotas extends Fragment{
	
	EditText editNotas1;	
	EditText editNotas2;
	EditText editNotas3;
	EditText editNotas4;
	EditText editNotas5;	
	EditText editNotas6;
	EditText editNotas7;
	EditText editNotas8;
	EditText editPorcentaje1;
	EditText editPorcentaje2;
	EditText editPorcentaje3;
	EditText editPorcentaje4;
	EditText editPorcentaje5;
	EditText editPorcentaje6;
	EditText editPorcentaje7;
	EditText editPorcentaje8;
	
	private Spinner spinnerAsignaturas;
	private ArrayList<String> nombreAsignaturas;
	SQLiteDatabase database;
	
	int creditosCursados;
	double promedioAcumulado; //Semestral
	double promedioDeseado = -1.0; //Semestral (-1.0 = asumido como valor inexistente)
	
	public FragmentNotas(int creditosCursados, double promedioAcumulado,
			double promedioDeseado) {
		super();
		this.creditosCursados = creditosCursados;
		this.promedioAcumulado = promedioAcumulado;
		this.promedioDeseado = promedioDeseado;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_notas, container, false);
		
		spinnerAsignaturas = (Spinner)view.findViewById(R.id.spinnerAsignaturas);
		nombreAsignaturas = new ArrayList<>();
		database = getActivity().openOrCreateDatabase("HistorialNotas", Context.MODE_PRIVATE, null);
		Cursor c = database.rawQuery("SELECT * FROM materias", null);
		
		if (c.moveToFirst()){
			do{
				nombreAsignaturas.add(c.getString(1));
			}while(c.moveToNext());
		}
		
		database.close();		
		
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(view.getContext(),android.R.layout.simple_list_item_1,nombreAsignaturas);
		spinnerAsignaturas.setAdapter(dataAdapter);
		addListenerOnSpinnerItemSelection();
		return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		EditText notaDeseada = (EditText) getActivity().findViewById(R.id.editNotaDeseada);
		
		int creditosActuales=0;
		double d = 0;
		
		//NOTA: Debe haber al menos una materia ingresada en el sistema para esta operación. De lo contrario, el resultado de
		//la nota deseada es "infinito" (División por 0 es realísticamente difícil de hacer; Créditos actuales=0).
		//Además, se debió haber pedido la nota de promedio deseado con anterioridad.
		
		
			
		
		
		//Calcular el número de créditos totales en las materias a través de la base de datos
    	SQLiteDatabase database = getActivity().openOrCreateDatabase("HistorialNotas", Context.MODE_PRIVATE, null);

		Cursor c = database.rawQuery("SELECT SUM(creditos) " +
				"FROM materias; ", null);
		
		if (c.moveToFirst()){
			if (!c.isNull(0)){
				
				if (promedioDeseado != -1){
					
					creditosActuales = Integer.parseInt(c.getString(0));
					//Aplicar el algoritmo para calcular la nota deseada
					d = MainActivity.promedioRequeridoSemestreActual(creditosCursados, creditosActuales, promedioAcumulado, promedioDeseado);
					//Sugerir la nota deseada en pantalla
					notaDeseada.setText(""+d);				
					Toast.makeText(getActivity(), 
			                "Recuerde que se debe al menos especificar los porcentajes de las notas antes de " +
			                "calcular lo requerido para obtener una nota deseada.",
			                Toast.LENGTH_LONG).show();
				}
				
			}else{
				Toast.makeText(getActivity(), 
		                "Aún no existen materias en el sistema. Recuerde que las materias se pueden" +
		                " agregarse en la sección anterior.",
		                Toast.LENGTH_LONG).show();
			}
		}
		
		c.close();
		database.close();
		
		
		
		
	}
	
	public void addListenerOnSpinnerItemSelection()
	{		
        spinnerAsignaturas.setOnItemSelectedListener(new OnItemSelectedListener() 
		{   @Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) 
		    {
			
				cleanEditTextFields();
				editNotas1 = (EditText) getActivity().findViewById(R.id.editNotas1);
				editNotas2 = (EditText) getActivity().findViewById(R.id.editNotas2);
				editNotas3 = (EditText) getActivity().findViewById(R.id.editNotas3);
				editNotas4 = (EditText) getActivity().findViewById(R.id.editNotas4);
				editNotas5 = (EditText) getActivity().findViewById(R.id.editNotas5);
				editNotas6 = (EditText) getActivity().findViewById(R.id.editNotas6);
				editNotas7 = (EditText) getActivity().findViewById(R.id.editNotas7);
				editNotas8 = (EditText) getActivity().findViewById(R.id.editNotas8);
				editPorcentaje1 = (EditText) getActivity().findViewById(R.id.editPorcentaje1);
				editPorcentaje2 = (EditText) getActivity().findViewById(R.id.editPorcentaje2);
				editPorcentaje3 = (EditText) getActivity().findViewById(R.id.editPorcentaje3);
				editPorcentaje4 = (EditText) getActivity().findViewById(R.id.editPorcentaje4);
				editPorcentaje5 = (EditText) getActivity().findViewById(R.id.editPorcentaje5);
				editPorcentaje6 = (EditText) getActivity().findViewById(R.id.editPorcentaje6);
				editPorcentaje7 = (EditText) getActivity().findViewById(R.id.editPorcentaje7);
				editPorcentaje8 = (EditText) getActivity().findViewById(R.id.editPorcentaje8);
				
				String nombreMateria = parent.getItemAtPosition(position).toString();
				
				database = getActivity().openOrCreateDatabase("HistorialNotas", Context.MODE_PRIVATE, null);
				Cursor c = database.rawQuery(
						"SELECT notas.valor,notas.porcentaje FROM notas,materias " +
						"WHERE notas.idMateria=materias.id " +
						"AND materias.nombre='"+nombreMateria+"'", null);
				
				
				if (c.moveToFirst()){
					do{
						//La implementación es tal que muestra máximo 4 notas por materia.
						switch(c.getPosition()){
						case 0:								
							editNotas1.setText(c.getString(0));								
							editPorcentaje1.setText(c.getString(1));
							break;
						case 1:								
							editNotas2.setText(c.getString(0));								
							editPorcentaje2.setText(c.getString(1));
							break;
						case 2:								
							editNotas3.setText(c.getString(0));								
							editPorcentaje3.setText(c.getString(1));
							break;
						case 3:								
							editNotas4.setText(c.getString(0));								
							editPorcentaje4.setText(c.getString(1));
							break;
						case 4:								
							editNotas5.setText(c.getString(0));								
							editPorcentaje5.setText(c.getString(1));
							break;
						case 5:								
							editNotas6.setText(c.getString(0));								
							editPorcentaje6.setText(c.getString(1));
							break;
						case 6:								
							editNotas7.setText(c.getString(0));								
							editPorcentaje7.setText(c.getString(1));
							break;
						case 7:								
							editNotas8.setText(c.getString(0));								
							editPorcentaje8.setText(c.getString(1));
							break;
						default:
							break;
						}
						
					}while(c.moveToNext());
				}else{
					Toast.makeText(parent.getContext(), 
			                "No se han ingresado notas para esta materia.",
			                Toast.LENGTH_LONG).show();
				}
				
				database.close();

				
    		}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				 
			}
		});
	}
	
	public void cleanEditTextFields(){
		editNotas1 = (EditText) getActivity().findViewById(R.id.editNotas1);
		editNotas2 = (EditText) getActivity().findViewById(R.id.editNotas2);
		editNotas3 = (EditText) getActivity().findViewById(R.id.editNotas3);
		editNotas4 = (EditText) getActivity().findViewById(R.id.editNotas4);
		editNotas5 = (EditText) getActivity().findViewById(R.id.editNotas5);
		editNotas6 = (EditText) getActivity().findViewById(R.id.editNotas6);
		editNotas7 = (EditText) getActivity().findViewById(R.id.editNotas7);
		editNotas8 = (EditText) getActivity().findViewById(R.id.editNotas8);
		editPorcentaje1 = (EditText) getActivity().findViewById(R.id.editPorcentaje1);
		editPorcentaje2 = (EditText) getActivity().findViewById(R.id.editPorcentaje2);
		editPorcentaje3 = (EditText) getActivity().findViewById(R.id.editPorcentaje3);
		editPorcentaje4 = (EditText) getActivity().findViewById(R.id.editPorcentaje4);
		editPorcentaje5 = (EditText) getActivity().findViewById(R.id.editPorcentaje5);
		editPorcentaje6 = (EditText) getActivity().findViewById(R.id.editPorcentaje6);
		editPorcentaje7 = (EditText) getActivity().findViewById(R.id.editPorcentaje7);
		editPorcentaje8 = (EditText) getActivity().findViewById(R.id.editPorcentaje8);
		editNotas1.setText("");
		editNotas2.setText("");
		editNotas3.setText("");
		editNotas4.setText("");
		editNotas5.setText("");
		editNotas6.setText("");
		editNotas7.setText("");
		editNotas8.setText("");
		editPorcentaje1.setText("");
		editPorcentaje2.setText("");
		editPorcentaje3.setText("");
		editPorcentaje4.setText("");
		editPorcentaje5.setText("");
		editPorcentaje6.setText("");
		editPorcentaje7.setText("");
		editPorcentaje8.setText("");
	}
}
