package mobile.promat;

public class Datos {

	private Double valor;
	private Double porcentaje;
	private Double valorReal;
	private Double valorMax;
	private Double valorNuevo;
	boolean valorNull;		
	
	public Datos(Double valor, Double porcentaje, Double valorReal,
			Double valorMax, boolean asValorNuevo, boolean isNullValue) {
		this.valor = valor;
		this.porcentaje = porcentaje;
		this.valorReal = valorReal;
		this.valorMax = valorMax;
		this.valorNull = isNullValue;
		
		if (asValorNuevo==true){
			this.valorNuevo = this.valor;
		}else{
			this.valorNuevo = 0.0;
		}
	}
	
	public Double getValor() {
		return valor;
	}
	public void setValor(Double valor) {
		this.valor = valor;
	}
	public Double getPorcentaje() {
		return porcentaje;
	}
	public void setPorcentaje(Double porcentaje) {
		this.porcentaje = porcentaje;
	}
	public Double getValorReal() {
		return valorReal;
	}
	public void setValorReal(Double valorReal) {
		this.valorReal = valorReal;
	}
	public Double getValorMax() {
		return valorMax;
	}
	public void setValorMax(Double valorMax) {
		this.valorMax = valorMax;
	}
	public boolean isValorNull() {
		return valorNull;
	}
	public void setValorNull(boolean valorNull) {
		this.valorNull = valorNull;
	}
	public Double getValorNuevo() {
		return valorNuevo;
	}
	public void setValorNuevo(Double valorNuevo) {
		this.valorNuevo = valorNuevo;
	}
}
