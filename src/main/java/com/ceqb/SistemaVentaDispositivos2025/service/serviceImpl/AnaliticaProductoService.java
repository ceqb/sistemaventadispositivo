package com.ceqb.SistemaVentaDispositivos2025.service.serviceImpl;

import com.ceqb.SistemaVentaDispositivos2025.model.Producto;
import com.ceqb.SistemaVentaDispositivos2025.repository.DetallePedidoRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.PedidoRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.ProductoRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnaliticaProductoService {
    private final ProductoRepository productoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    public List<Producto> obtener10Clickeados() {
        return productoRepository.findTop10ByOrderByClicsDesc();
    }


    public Producto obtenerTop1Clickeado(){
        Producto top = productoRepository.findTopByOrderByClicsDesc();

        return top;
    }

    public List<Producto> obtener10MasVendidos() {
        return productoRepository.findTop10ByOrderByVentasRecientesDesc();
    }

    public List<Producto> obtenerTop5BajaConversion() {
        return productoRepository.findProductosConBajaConversion(PageRequest.of(0, 5));

    }


    public Producto obtenerProductoMasVendido() {
        return productoRepository.findTop1ByOrderByVentasRecientesDesc();
    }

    public Object[] obtenerClienteTop() {
        List<Object[]> resultados = detallePedidoRepository.findClienteConMasComprasAprobadas();
        return resultados.isEmpty() ? null : resultados.get(0);
    }


    public Long obtenerTotalProductos() {
        return productoRepository.count();
    }

    public Long obtenerTotalClientes() {
        return usuarioRepository.count();
    }

    public Long obtenerPedidosAprobados() {
        return pedidoRepository.countByEstadoPago("APROBADO");
    }


}
