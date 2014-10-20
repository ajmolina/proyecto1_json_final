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

public class FragmentPromedioDefault extends Fragment{
	
	int creditosCursados;
	double promedioAcumulado;
	
	public FragmentPromedioDefault(int creditosCursados, double promedioAcumulado) {
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
		View view = inflater.inflate(R.layout.fragment_promedio_default, container, false);
		return view;
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		TextView txtValorCreditosCursados = (TextView)getActivity().findViewById(R.id.txtValorCreditosCursadosDefault);
		TextView txtValorPromedioAcumulado = (TextView)getActivity().findViewById(R.id.txtValorPromedioAcumuladoDefault);
		
		txtValorCreditosCursados.setText(""+this.creditosCursados);
		txtValorPromedioAcumulado.setText(""+this.promedioAcumulado);

		
	}
	
	
	    
	    

	



}
