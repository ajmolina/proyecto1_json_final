package mobile.promat;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentPromedio extends Fragment{
	
	int creditosCursados;
	double promedioAcumulado;	
	
	private ListView listaAsignaturas;
	private Button getAllValue;
	private EditorAdapter adapter;
	
	//Para el calculo de promedio acumulado:
	private ArrayList<Double> puntajes = new ArrayList<Double>();
	private ArrayList<Boolean> estados = new ArrayList<Boolean>();
	//false: fijo( no modificable ), true:modificable
	private ArrayList<Integer> creditos = new ArrayList<Integer>();
	
	public FragmentPromedio(int creditosCursados, double promedioAcumulado) {
		super();
		this.creditosCursados = creditosCursados;
		this.promedioAcumulado = promedioAcumulado;
	}

	public int getCreditosCursados() {
		return creditosCursados;
	}

	public void setCreditosCursados(int creditosCursados) {
		this.creditosCursados = creditosCursados;
	}

	public double getPromedioAcumulado() {
		return promedioAcumulado;
	}

	public void setPromedioAcumulado(double promedioAcumulado) {
		this.promedioAcumulado = promedioAcumulado;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_promedio, container, false);
		return view;
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		TextView txtValorCreditosCursados = (TextView)getActivity().findViewById(R.id.txtValorCreditosCursados);
		TextView txtValorPromedioAcumulado = (TextView)getActivity().findViewById(R.id.txtValorPromedioAcumulado);
		
		txtValorCreditosCursados.setText(""+this.creditosCursados);
		txtValorPromedioAcumulado.setText(""+this.promedioAcumulado);
		
		//Generar listado de asignaturas
		listaAsignaturas = (ListView)getActivity().findViewById(R.id.listViewAsignaturas);
		getAllValue=(Button)getActivity().findViewById(R.id.btnCalcularPromAcum);
		
		ArrayList<HashMap<String,String>> data = new ArrayList<HashMap<String, String>>();
        
		//Llamado a la base de datos: Obtención de todas las asignaturas del semestre.
		
		SQLiteDatabase database = getActivity().openOrCreateDatabase("HistorialNotas", Context.MODE_PRIVATE, null);
		
		Cursor c = database.rawQuery(
				"SELECT materias.nombre, TOTAL((notas.valor*notas.porcentaje)/100) " +
				"FROM materias,notas " +
				"WHERE materias.id=notas.idMateria " +
				"GROUP BY materias.nombre;", null);
		
		if(c.moveToFirst()){
			do{
				HashMap<String,String> temp = new HashMap<String, String>();
				temp.put("name",c.getString(0));
				
				boolean allValuesFilled=true;
				Cursor d = database.rawQuery(
						"SELECT notas.valor " +
						"FROM materias,notas " +
						"WHERE materias.id=notas.idMateria " +
						"AND materias.nombre = '"+c.getString(0)+"';", null);
				
				if (d.moveToFirst()){
					
					do{
						if (d.isNull(0)){
							allValuesFilled=false;
						}
						
					}while(d.moveToNext()&&allValuesFilled);
					
				}
				
				if (allValuesFilled==true){
					temp.put("value",c.getString(1));
				}else{
					temp.put("value","");
				}
				   
				data.add(temp);
				
			}while(c.moveToNext());
		}
		
		database.close();
		
		getAllValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	            	
            	//CALCULAR PROMEDIO ACUMULADO
            	
            	EditText editPromedioDeseado = (EditText) getActivity().findViewById(R.id.editPromedioDeseado);
            	TextView txtCreditosCursados = (TextView) getActivity().findViewById(R.id.txtValorCreditosCursados);
            	TextView txtPromedioAcumulado = (TextView) getActivity().findViewById(R.id.txtValorPromedioAcumulado);
            	
            	if (editPromedioDeseado.getText().toString().trim().length()!=0){
            		
            	
            	
            	SQLiteDatabase database = getActivity().openOrCreateDatabase("HistorialNotas", Context.MODE_PRIVATE, null);

        		Cursor c = database.rawQuery("SELECT SUM(creditos*5) " +
        				"FROM materias; ", null);
        		Cursor d = database.rawQuery("SELECT materias.nombre, TOTAL((notas.valor*notas.porcentaje)/100), materias.creditos " +
        				"FROM materias,notas " +
        				"WHERE notas.idMateria = materias.id " +
        				"GROUP BY materias.nombre;" , null);

        		double creditosSemestreActual = 0;
        		double promedioRequeridoSemestreActual = 0;
        		double puntajeRequeridoSemestreActual = 0;        		
        		double puntajeMaximoSemestreActual = 0;        		
        		double totalPuntajeAlcanzado = 0;
        		
        		puntajes = new ArrayList<Double>();
        		estados = new ArrayList<Boolean>();
        		creditos = new ArrayList<Integer>();
        		
        		if (c.moveToFirst()) {
        			puntajeMaximoSemestreActual = puntajeMaximoSemestreActual +
        					Double.parseDouble(c.getString(0).toString()); 
        		}		
        		
        		c.close();
        		
        		if (d.moveToFirst()){
        			do {
        				
        				boolean allValuesFilled=true;
        				Cursor e = database.rawQuery(
        						"SELECT notas.valor " +
        						"FROM materias,notas " +
        						"WHERE materias.id=notas.idMateria " +
        						"AND materias.nombre = '"+d.getString(0)+"';", null);
        				
        				if (e.moveToFirst()){
        					
        					do{
        						if (e.isNull(0)){
        							allValuesFilled=false;
        						}
        						
        					}while(e.moveToNext()&&allValuesFilled);
        					
        				}
        				
        				if (allValuesFilled==true){
        					estados.add(false);
        					puntajes.add(Double.parseDouble(d.getString(1)));
        					creditos.add(Integer.parseInt(d.getString(2)));
        					creditosSemestreActual = creditosSemestreActual + Integer.parseInt((d.getString(2)));

        				}else{
        					estados.add(true);
        					puntajes.add(Double.parseDouble(d.getString(1)));
        					creditos.add(Integer.parseInt(d.getString(2)));
        					creditosSemestreActual = creditosSemestreActual + Integer.parseInt((d.getString(2)));

        				}
        				
	        			
        			
        			} while (d.moveToNext());
        			
        		}

        		promedioRequeridoSemestreActual = (
        				(Double.parseDouble(editPromedioDeseado.getText().toString()) * 
						(Integer.parseInt(txtCreditosCursados.getText().toString()) +
								creditosSemestreActual)) - 
						(Double.parseDouble(txtPromedioAcumulado.getText().toString()) *
						Integer.parseInt(txtCreditosCursados.getText().toString())))
        				/ creditosSemestreActual;
        		
        		puntajeRequeridoSemestreActual = (promedioRequeridoSemestreActual*
        				puntajeMaximoSemestreActual)/5;
        		
        		Toast.makeText(getActivity(), "Promedio requerido en el semestre actual: "+promedioRequeridoSemestreActual, Toast.LENGTH_SHORT).show();
        		//Toast.makeText(getActivity(), "puntaje requerido: "+puntajeRequeridoSemestreActual, Toast.LENGTH_SHORT).show();
        		
        		
        		totalPuntajeAlcanzado=0;
        		for (int i=0;i<puntajes.size();i++){
        			totalPuntajeAlcanzado= totalPuntajeAlcanzado+(puntajes.get(i)*creditos.get(i));
        			
        		}
        		
        		if(totalPuntajeAlcanzado>=puntajeRequeridoSemestreActual){
        			Toast.makeText(getActivity(), "No necesitas más nota para alcanzar el promedio deseado.", Toast.LENGTH_SHORT).show();
        		}else{  			
        			
        			boolean abortar=false;
        			        			
        			int indice=0;
        			for (int i=0;i<puntajes.size();i++){     
        				
        				if (abortar==false){
        					if (estados.get(i)==true){       					
            					puntajes.set(i, 5.0);
            					indice=i;
            					totalPuntajeAlcanzado=0;
            					for (int j=0;j<puntajes.size();j++){
            						totalPuntajeAlcanzado= totalPuntajeAlcanzado+(puntajes.get(j)*creditos.get(j));
            					}       					
            					if(totalPuntajeAlcanzado>=puntajeRequeridoSemestreActual){
            						abortar=true;
            						//No necesito más. Ahora sólo debo bajar la nota de esta materia hasta que
            						//se ajuste a lo necesario.
            					}else{
            						//Necesito la nota de otra materia más para completar nota.
            						//Si llego aquí... y no hay más materias... entonces no es posible
            						//alcanzar el promedio deseado.
            					}
            				}
        				}
        				
        				
            		}
        			
        			if(abortar==true){
        				double p=5.0;
        				abortar=false;
        				while (abortar==false){
        					puntajes.set(indice, p);
        					totalPuntajeAlcanzado=0;
        	        		for (int i=0;i<puntajes.size();i++){
        	        			totalPuntajeAlcanzado= totalPuntajeAlcanzado+(puntajes.get(i)*creditos.get(i));        	        			
        	        		}
        	        		if(totalPuntajeAlcanzado<puntajeRequeridoSemestreActual){
        	        			puntajes.set(indice, p+0.1);
        	        			abortar=true;
        	        			//Toast.makeText(getActivity(), "He logrado un resultado.", Toast.LENGTH_SHORT).show();
        	        			
        	        			//MOSTRAR RESULTADOS AQUÍ
        	        			ArrayList<HashMap<String,String>> data = new ArrayList<HashMap<String, String>>();
        	        			
        	        			for (int i=0;i<puntajes.size();i++){
        	        				HashMap<String,String> temp = new HashMap<String, String>();
        	        				HashMap<String,String> aux = (HashMap<String, String>) adapter.getItem(i);
        	        				temp.put("name", aux.get("name"));
        	        				temp.put("value", ""+puntajes.get(i));
        	        				data.add(temp);
        	        			}
        	        			adapter = new EditorAdapter(getActivity(),data);
        	        	        listaAsignaturas.setAdapter(adapter);
        	        			//adapter.setData(data);
        	        			
        	        			
        	        		}
        	        		p=p-0.1;
        	        		
        				}
        			}else{
        				Toast.makeText(getActivity(), "Es imposible alcanzar el promedio deseado.", Toast.LENGTH_SHORT).show();
        			}
        			
        			
        			
        			
        			
        		}
        			
        			
        
   
            	/*
                String allValues="";
                ArrayList<String> valueList = new ArrayList<String>();
                for (int i=0;i<adapter.getCount();i++){
                    allValues +=((HashMap<String,String>)adapter.getItem(i)).get("value")+ ",";
                    valueList.add(((HashMap<String,String>)adapter.getItem(i)).get("value"));
                }
                // use this valueList as per ur requirement
                allValues = allValues.substring(0,allValues.length()-1);
                Toast.makeText(getActivity(),allValues,Toast.LENGTH_LONG).show();
        	 	*/
        		
        		
            	}else{
            		Toast.makeText(getActivity(),"Por favor, ingrese el promedio semestral deseado antes de continuar.",Toast.LENGTH_LONG).show();
            	}
        		
            } //Fin onClick()
        });
		
		
		adapter = new EditorAdapter(getActivity(),data);
        listaAsignaturas.setAdapter(adapter);
		
	}
	
	class EditorAdapter extends BaseAdapter {

	    Context context;
	    ArrayList<HashMap<String,String>> data;

	    public EditorAdapter(Context context, ArrayList<HashMap<String,String>> data){

	        this.context = context;
	        this.data = data;
	    }

	    @Override
	    public int getCount() {
	        return data.size();
	    }

	    @Override
	    public Object getItem(int pos) {
	        return data.get(pos);
	    }

	    @Override
	    public long getItemId(int pos) {
	        return pos;
	    }
	    
	    public void setData(ArrayList<HashMap<String, String>> data) {
			this.data = data;
		}

	    @Override
	    public View getView(final int position, View convertView, ViewGroup parent) {
	        final ViewHolder holder;
	        if(convertView==null){
	            holder = new ViewHolder();
	            convertView = LayoutInflater.from(context).inflate(R.layout.row_asignaturas,
	            		(ViewGroup) getActivity().findViewById(R.id.listViewAsignaturas),false);
	            //null en vez de ViewGroup
	            holder.editNombreAsignatura = (EditText) convertView.findViewById(R.id.editListNombreAsignatura);
	            holder.editNotaAsignatura = (EditText) convertView.findViewById(R.id.editListNotaAsignatura);
	            convertView.setTag(holder);
	        }else{
	            holder = (ViewHolder) convertView.getTag();
	        }
	        
	        holder.editNombreAsignatura.setText(data.get(position).get("name"));
	        holder.editNombreAsignatura.setKeyListener(null);
	        holder.editNotaAsignatura.setText(data.get(position).get("value"));
	        if (data.get(position).get("value")!=""){
	        	holder.editNotaAsignatura.setKeyListener(null);
	        }
	        
	        holder.editNotaAsignatura.addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					// TODO Auto-generated method stub
					final EditText editValue = (EditText) holder.editNotaAsignatura;
					if (editValue.getText().toString().trim().length()!=0){
						data.get(position).put("value", s.toString());
					}
					
					
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
					
				}
			});

	        convertView.setTag(holder);
	        return convertView;
	    }

	    class ViewHolder {
	        EditText editNombreAsignatura;
	        EditText editNotaAsignatura;
	    }
	    
	    

	}



}
