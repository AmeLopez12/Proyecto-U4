package Modelo;

public class Detalle {

    private int id;
    private int codigo_pro;
    private int cantidad;
    private double precio;
    private int id_venta;

    public Detalle() {
       
    }

    public Detalle(int id, int codigo_pro, int cantidad, double precio, int id_venta) {
        this.id = id;
        this.codigo_pro = codigo_pro;
        this.cantidad = cantidad;
        this.precio = precio;
        this.id_venta = id_venta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCodigoPro() {
        return codigo_pro;
    }

    public void setCodigoPro(int codigo_pro) {
        this.codigo_pro = codigo_pro;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getIdVenta() {
        return id_venta;
    }

    public void setIdVenta(int id_venta) {
        this.id_venta = id_venta;
    }
}
