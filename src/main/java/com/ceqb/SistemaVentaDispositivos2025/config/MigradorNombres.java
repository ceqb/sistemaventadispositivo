package com.ceqb.SistemaVentaDispositivos2025.config;

import java.io.File;
import java.sql.*;
import java.text.Normalizer;

public class MigradorNombres {

    public static String normalizarNombre(String nombre) {
        String normalizado = Normalizer.normalize(nombre, Normalizer.Form.NFD);
        normalizado = normalizado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return normalizado.replaceAll("[^a-zA-Z0-9.-]", "_").toLowerCase();
    }

    // Método recursivo para encontrar un archivo en subcarpetas
    public static File buscarArchivo(File directorio, String nombreArchivo) {
        File[] archivos = directorio.listFiles();
        if (archivos != null) {
            for (File file : archivos) {
                if (file.isDirectory()) {
                    File encontrado = buscarArchivo(file, nombreArchivo);
                    if (encontrado != null) return encontrado;
                } else if (file.getName().equals(nombreArchivo)) {
                    return file;
                }
            }
        }
        return null;
    }

    public static void ejecutarMigracion(String carpetaRaiz, String dbUrl, String user, String pass) {
        File carpetaBase = new File(carpetaRaiz);

        try (Connection conn = DriverManager.getConnection(dbUrl, user, pass)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id_dpc, rutaFoto_dpc FROM productos");

            while (rs.next()) {
                int id = rs.getInt("id_dpc");
                String nombreOriginal = rs.getString("rutaFoto_dpc");
                if (nombreOriginal == null || nombreOriginal.isEmpty()) continue;

                String nuevoNombre = normalizarNombre(nombreOriginal);

                // BUSCAR el archivo en cualquier subcarpeta
                File archivoOriginal = buscarArchivo(carpetaBase, nombreOriginal);

                if (archivoOriginal != null && archivoOriginal.exists()) {
                    File archivoNuevo = new File(archivoOriginal.getParent(), nuevoNombre);
                    if (archivoOriginal.renameTo(archivoNuevo)) {
                        System.out.println("Renombrado: " + nombreOriginal + " -> " + nuevoNombre);

                        // Solo si el archivo se renombró, actualizamos la base de datos
                        PreparedStatement ps1 = conn.prepareStatement("UPDATE productos SET rutaFoto_dpc = ? WHERE id_dpc = ?");
                        ps1.setString(1, nuevoNombre);
                        ps1.setInt(2, id);
                        ps1.executeUpdate();
                    }
                } else {
                    System.out.println("Archivo no encontrado: " + nombreOriginal);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}