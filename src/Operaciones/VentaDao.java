package Operaciones;
import Modelo.Detalle;
import Modelo.Venta;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.filechooser.FileSystemView;

public class VentaDao {

    Connection con;
    Conexion cn = new Conexion();
    PreparedStatement ps;
    ResultSet rs;
    int r;

    public int IdVenta() {
        int id = 0;
        String sql = "SELECT MAX(id) FROM ventas";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return id;
    }

    

public int RegistrarVentaConDetalle(Venta v, List<Detalle> detalles) {
    String sqlVenta = "INSERT INTO ventas (cliente, vendedor, total, fecha) VALUES (?,?,?,?)";
    String sqlDetalle = "INSERT INTO detalle (codigo_pro, cantidad, precio, id_venta) VALUES (?,?,?,?)";
    Connection con = null;
    int resultado = 0;
    int idVenta = 0;

    try {
        con = cn.getConnection();
        con.setAutoCommit(false); //Iniciar transaccion

        //Registrar la venta
        PreparedStatement psVenta = con.prepareStatement(sqlVenta, PreparedStatement.RETURN_GENERATED_KEYS);
        psVenta.setInt(1, v.getCliente());
        psVenta.setString(2, v.getVendedor());
        psVenta.setDouble(3, v.getTotal());

        //Convertir la fecha al formato adecuado
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = inputFormat.parse(v.getFecha());
        String formattedDate = outputFormat.format(date);

        psVenta.setString(4, formattedDate);
        int filasAfectadas = psVenta.executeUpdate();

        //Obtener el ID de la venta generada
        ResultSet rsVenta = psVenta.getGeneratedKeys();
        if (rsVenta.next()) {
            idVenta = rsVenta.getInt(1);
            v.setId(idVenta);
        }

        //Verificar que idVenta es valido antes de registrar los detalles
        if (idVenta > 0) {
            PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);
            for (Detalle detalle : detalles) {
                psDetalle.setInt(1, detalle.getCodigoPro());
                psDetalle.setInt(2, detalle.getCantidad());
                psDetalle.setDouble(3, detalle.getPrecio());
                psDetalle.setInt(4, idVenta);
                psDetalle.executeUpdate();

                //Actualizar el stock del producto
                actualizarStockProducto(detalle.getCodigoPro(), detalle.getCantidad());
            }
            con.commit();
            resultado = 1; //Operación exitosa
        } else {
            System.out.println("Error: No se generó un ID de venta válido.");
            con.rollback();
        }
    } catch (Exception e) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ex) {
                System.out.println("Error al hacer rollback: " + ex.getMessage());
            }
        }
        System.out.println("Error al registrar la venta con detalle: " + e.getMessage());
        resultado = 0;
    } finally {
        try {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        } catch (SQLException e) {
            System.out.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
    return resultado;
}
///////////////////////////////
public void actualizarStockProducto(int codigoPro, int cantidadVendida) {
    String sql = "UPDATE productos SET stock = stock - ? WHERE id = ?";
    try {
        con = cn.getConnection();
        ps = con.prepareStatement(sql);
        ps.setInt(1, cantidadVendida);
        ps.setInt(2, codigoPro);
        ps.executeUpdate();
    } catch (SQLException e) {
        System.out.println("Error al actualizar el stock: " + e.toString());
    } finally {
        try {
            con.close();
        } catch (SQLException e) {
            System.out.println("Error al cerrar la conexión después de actualizar stock: " + e.toString());
        }
    }
}
////////////////////////////////////////////////////

    public boolean ActualizarStock(int cant, int id) {
        String sql = "UPDATE productos SET stock = ? WHERE id = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, cant);
            ps.setInt(2, id);
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        }
    }

    public List Listarventas() {
        List<Venta> ListaVenta = new ArrayList();
        String sql = "SELECT c.id AS id_cli, c.nombre, v.* FROM clientes c INNER JOIN ventas v ON c.id = v.cliente";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Venta vent = new Venta();
                vent.setId(rs.getInt("id"));
                vent.setNombre_cli(rs.getString("nombre"));
                vent.setVendedor(rs.getString("vendedor"));
                vent.setTotal(rs.getDouble("total"));
                ListaVenta.add(vent);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return ListaVenta;
    }

    public Venta BuscarVenta(int id) {
        Venta cl = new Venta();
        String sql = "SELECT * FROM ventas WHERE id = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                cl.setId(rs.getInt("id"));
                cl.setCliente(rs.getInt("cliente"));
                cl.setTotal(rs.getDouble("total"));
                cl.setVendedor(rs.getString("vendedor"));
                cl.setFecha(rs.getString("fecha"));
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return cl;
    }
    
    public void abrirpdf(int idventa, int Cliente, double total, String usuario){
        try {
            String url = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
            File salida = new File(url + "/venta"+idventa+".pdf");
            if (salida.exists()){
                Desktop desktop = Desktop.getDesktop();
                desktop.open(salida);
            }
            else{
                pdfV(idventa, Cliente, total, usuario);
            }
        } catch (Exception e) {
        }
    }

    public void pdfV(int idventa, int Cliente, double total, String usuario) {
        try {
            //Definir ruta
            String url = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
            File salida = new File(url + "/venta"+idventa+".pdf");

            //Crear el PDF
            FileOutputStream archivo = new FileOutputStream(salida);
            Document doc = new Document();
            PdfWriter.getInstance(doc, archivo);
            doc.open();

            //Fecha
            Date date = new Date();
            Font negrita = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLUE);
            Paragraph fecha = new Paragraph(new SimpleDateFormat("dd/MM/yyyy").format(date), negrita);
            fecha.setAlignment(Element.ALIGN_RIGHT);
            doc.add(fecha);
            doc.add(Chunk.NEWLINE);
            PdfPTable Encabezado = new PdfPTable(4);
            Encabezado.setWidthPercentage(100);
            Encabezado.getDefaultCell().setBorder(0);
            float[] columnWidthsEncabezado = new float[]{20f, 30f, 70f, 40f};
            Encabezado.setWidths(columnWidthsEncabezado);
            Encabezado.setHorizontalAlignment(Element.ALIGN_LEFT);
            Encabezado.addCell("");
                      
            //info empresa
            String config = "SELECT * FROM config";
            String mensaje = "";
            try {
                con = cn.getConnection();
                ps = con.prepareStatement(config);
                rs = ps.executeQuery();
                if (rs.next()) {
                    mensaje = rs.getString("mensaje");
                    Encabezado.addCell("Ruc:    " + rs.getString("ruc") + "\nNombre: " + rs.getString("nombre") + "\nTeléfono: " + rs.getString("telefono") + "\nDirección: " + rs.getString("direccion") + "\n\n");
                }
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
            Encabezado.addCell(fecha);
            doc.add(Encabezado);
            
            //cliente
            Paragraph cli = new Paragraph();
            cli.add(Chunk.NEWLINE);
            cli.add("DATOS DEL CLIENTE" + "\n\n");
            doc.add(cli);

            PdfPTable proveedor = new PdfPTable(3);
            proveedor.setWidthPercentage(100);
            proveedor.getDefaultCell().setBorder(0);
            float[] columnWidthsCliente = new float[]{50f, 25f, 25f};
            proveedor.setWidths(columnWidthsCliente);
            proveedor.setHorizontalAlignment(Element.ALIGN_LEFT);
            PdfPCell cliNom = new PdfPCell(new Phrase("Nombre", negrita));
            PdfPCell cliTel = new PdfPCell(new Phrase("Télefono", negrita));
            PdfPCell cliDir = new PdfPCell(new Phrase("Dirección", negrita));
            cliNom.setBorder(Rectangle.NO_BORDER);
            cliTel.setBorder(Rectangle.NO_BORDER);
            cliDir.setBorder(Rectangle.NO_BORDER);
            proveedor.addCell(cliNom);
            proveedor.addCell(cliTel);
            proveedor.addCell(cliDir);
            String prove = "SELECT * FROM clientes WHERE id = ?";
            try {
                ps = con.prepareStatement(prove);
                ps.setInt(1, Cliente);
                rs = ps.executeQuery();
                if (rs.next()) {
                    proveedor.addCell(rs.getString("nombre"));
                    proveedor.addCell(rs.getString("telefono"));
                    proveedor.addCell(rs.getString("direccion") + "\n\n");
                } else {
                    proveedor.addCell("Publico en General");
                    proveedor.addCell("S/N");
                    proveedor.addCell("S/N" + "\n\n");
                }

            } catch (SQLException e) {
                System.out.println(e.toString());
            }
            doc.add(proveedor);

            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.getDefaultCell().setBorder(0);
            float[] columnWidths = new float[]{10f, 50f, 15f, 15f};
            tabla.setWidths(columnWidths);
            tabla.setHorizontalAlignment(Element.ALIGN_LEFT);
            PdfPCell c1 = new PdfPCell(new Phrase("Cant.", negrita));
            PdfPCell c2 = new PdfPCell(new Phrase("Descripción.", negrita));
            PdfPCell c3 = new PdfPCell(new Phrase("P. unt.", negrita));
            PdfPCell c4 = new PdfPCell(new Phrase("P. Total", negrita));
            c1.setBorder(Rectangle.NO_BORDER);
            c2.setBorder(Rectangle.NO_BORDER);
            c3.setBorder(Rectangle.NO_BORDER);
            c4.setBorder(Rectangle.NO_BORDER);
            c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            c2.setBackgroundColor(BaseColor.LIGHT_GRAY);
            c3.setBackgroundColor(BaseColor.LIGHT_GRAY);
            c4.setBackgroundColor(BaseColor.LIGHT_GRAY);
            tabla.addCell(c1);
            tabla.addCell(c2);
            tabla.addCell(c3);
            tabla.addCell(c4);
            String product = "SELECT d.id, d.codigo_pro, d.id_venta, d.precio, d.cantidad, p.id, p.nombre FROM detalle d INNER JOIN productos p ON d.codigo_pro = p.id WHERE d.id_venta = ?";
            try {
                ps = con.prepareStatement(product);
                ps.setInt(1, idventa);
                rs = ps.executeQuery();
                while (rs.next()) {
                    double subTotal = rs.getInt("cantidad") * rs.getDouble("precio");
                    tabla.addCell(rs.getString("cantidad"));
                    tabla.addCell(rs.getString("nombre"));
                    tabla.addCell(rs.getString("precio"));
                    tabla.addCell(String.valueOf(subTotal));
                }

            } catch (SQLException e) {
                System.out.println(e.toString());
            }
            doc.add(tabla);
            Paragraph info = new Paragraph();
            info.add(Chunk.NEWLINE);
            info.add("Total S/: " + total);
            info.setAlignment(Element.ALIGN_RIGHT);
            doc.add(info);
            Paragraph gr = new Paragraph();
            gr.add(Chunk.NEWLINE);
            gr.add(mensaje);
            gr.setAlignment(Element.ALIGN_CENTER);
            doc.add(gr);

            doc.close();
            archivo.close();
            Desktop.getDesktop().open(salida);
        } catch (DocumentException | IOException e) {
            System.out.println(e.toString());
        }
    }

    public void pdfTotal(){
        Connection conn = null;
        Document document = new Document();
        double gananciasTotales = 0.0;

        try {
            //Obtener la ruta predeterminada de la carpeta de documentos del usuario
            String url = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
            File salida = new File(url + "/ventaTotal.pdf");
            FileOutputStream archivo = new FileOutputStream(salida);

            //Crear el PDF
            PdfWriter.getInstance(document, archivo);
            document.open();

            //Título del PDF
            Font fontTitulo = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph titulo = new Paragraph("Reporte de Ventas", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(new Paragraph("\n"));

            //Crear tabla
            PdfPTable table = new PdfPTable(6); // 6 columnas
            table.setWidthPercentage(100);

            //Encabezados de la tabla
            String[] encabezados = {"Código", "Nombre", "ID Venta", "Cantidad", "Precio", "Total"};
            for (String encabezado : encabezados) {
                PdfPCell header = new PdfPCell(new Phrase(encabezado));
                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(header);
            }

            //Conectar a la base de datos y ejecutar la consulta
            conn = new Conexion().getConnection();
            String query = "SELECT pro.codigo, pro.nombre, v.id, d.cantidad, d.precio, (d.cantidad*d.precio) as total FROM detalle d JOIN ventas v ON d.id_venta = v.id JOIN productos pro ON pro.id = d.codigo_pro ORDER BY d.codigo_pro;";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            //Agregar los datos de la consulta a la tabla
            while (resultSet.next()) {
                table.addCell(resultSet.getString("codigo"));
                table.addCell(resultSet.getString("nombre"));
                table.addCell(resultSet.getString("id"));
                table.addCell(resultSet.getString("cantidad"));
                table.addCell(resultSet.getString("precio"));
                table.addCell(resultSet.getString("total"));
                gananciasTotales += resultSet.getDouble("total");
            }

            // Añadir la tabla al documento
            document.add(table);

            // Agregar total de ganancias al final
            document.add(new Paragraph("\n"));
            Font fontGanancias = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Paragraph ganancias = new Paragraph("Ganancias Totales: $" + String.format("%.2f", gananciasTotales), fontGanancias);
            ganancias.setAlignment(Element.ALIGN_RIGHT);
            document.add(ganancias);

            //Añadir la tabla al documento
            System.out.println("PDF generado en: " + salida.getAbsolutePath());
            Desktop desktop = Desktop.getDesktop();
            desktop.open(salida);

        } catch (SQLException e) {
            System.out.println("Error de SQL: " + e.getMessage());
        } catch (DocumentException e) {
            System.out.println("Error al crear el documento PDF: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error de IO: " + e.getMessage());
        }finally {
            document.close();
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
    
    private void limpiarDetalle(int idVenta) {
        String sql = "DELETE FROM detalle WHERE id_venta = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idVenta);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error al limpiar los detalles de la venta: " + e.toString());
        }
    }

}

