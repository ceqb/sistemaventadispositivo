package com.ceqb.SistemaVentaDispositivos2025.service.serviceImpl;

import com.ceqb.SistemaVentaDispositivos2025.dto.CarritoDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import com.ceqb.SistemaVentaDispositivos2025.mapper.CarritoMapper;
import com.ceqb.SistemaVentaDispositivos2025.model.Carrito;
import com.ceqb.SistemaVentaDispositivos2025.model.Producto;
import com.ceqb.SistemaVentaDispositivos2025.model.Usuario;
import com.ceqb.SistemaVentaDispositivos2025.repository.CarritoRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.ProductoRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.UsuarioRepository;
import com.ceqb.SistemaVentaDispositivos2025.service.CarritoService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CarritoServiceImpl implements CarritoService {
    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarritoMapper carritoMapper;

    @Override
    public void asignarPrecioUnitario(CarritoDTO item) {
        Producto producto = productoRepository.findById(item.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        item.setPrecioUnitario(BigDecimal.valueOf(producto.getPrecio_dpc()));
        item.setModeloProducto(producto.getModelo_dpc());
        item.setImagenProducto(producto.getRutaFoto_dpc());
    }

    @Override
    public void agregarProductoAnonimo(Long productoId, int cantidad, HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CarritoDTO> carritoAnonimo = (List<CarritoDTO>) session.getAttribute("carritoAnonimo");
        if (carritoAnonimo == null) {
            carritoAnonimo = new ArrayList<>();
            session.setAttribute("carritoAnonimo", carritoAnonimo);
        }

        boolean existe = false;
        for (CarritoDTO item : carritoAnonimo) {
            if (item.getProductoId().equals(productoId)) {
                item.setCantidad(item.getCantidad() + cantidad);
                existe = true;
                break;
            }
        }
        if (!existe) {
            CarritoDTO nuevo = new CarritoDTO();
            nuevo.setProductoId(productoId);
            nuevo.setCantidad(cantidad);
            carritoAnonimo.add(nuevo);
        }
    }

    @Override
    public void agregarProducto(UsuarioDTO usuarioDTO, Long productoId, int cantidad) {
        Usuario usuario = usuarioRepository.findById(usuarioDTO.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        int stockDisponible = stockDisponible(producto);
        if (stockDisponible < cantidad) {
            throw new RuntimeException("Stock insuficiente para el producto: " + producto.getModelo_dpc());
        }

        Optional<Carrito> carritoExistente = carritoRepository.findByUsuarioAndProducto_Id(usuario, producto.getId());

        if (carritoExistente.isPresent()) {
            Carrito item = carritoExistente.get();
            if (stockDisponible < item.getCantidad() + cantidad) {
                throw new RuntimeException("No hay suficiente stock para aumentar la cantidad en el carrito");
            }
            item.setCantidad(item.getCantidad() + cantidad);
            item.setPrecioUnitario(BigDecimal.valueOf(producto.getPrecio_dpc()));
            item.calcularSubtotal();
            carritoRepository.save(item);
        } else {
            Carrito nuevoItem = new Carrito();
            nuevoItem.setUsuario(usuario);
            nuevoItem.setProducto(producto);
            nuevoItem.setCantidad(cantidad);
            nuevoItem.setPrecioUnitario(BigDecimal.valueOf(producto.getPrecio_dpc()));
            nuevoItem.calcularSubtotal();
            carritoRepository.save(nuevoItem);
        }
    }

    @Override
    public List<CarritoDTO> obtenerCarrito(Long usuarioId) {
        List<Carrito> carritoEntities = carritoRepository.findByUsuario_Id(usuarioId);
        return CarritoMapper.toDtoList(carritoEntities);
    }

    @Override
    public void fusionarCarrito(List<CarritoDTO> carritoAnonimo, UsuarioDTO usuario) {
        if (carritoAnonimo != null && !carritoAnonimo.isEmpty()) {
            Usuario usuarioEntity = usuarioRepository.findById(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            for (CarritoDTO itemAnonimo : carritoAnonimo) {
                Producto producto = productoRepository.findById(itemAnonimo.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                int stockDisponible = stockDisponible(producto);

                Optional<Carrito> carritoExistente = carritoRepository.findByUsuarioAndProducto_Id(usuarioEntity, producto.getId());

                if (carritoExistente.isPresent()) {
                    Carrito item = carritoExistente.get();
                    int nuevaCantidad = item.getCantidad() + itemAnonimo.getCantidad();

                    if (nuevaCantidad > stockDisponible) {
                        nuevaCantidad = stockDisponible; // límite por stock
                    }

                    item.setCantidad(nuevaCantidad);
                    item.calcularSubtotal();
                    carritoRepository.save(item);
                } else {
                    int cantidadFinal = Math.min(itemAnonimo.getCantidad(), stockDisponible);
                    if (cantidadFinal > 0) {
                        Carrito nuevoItem = new Carrito();
                        nuevoItem.setUsuario(usuarioEntity);
                        nuevoItem.setProducto(producto);
                        nuevoItem.setCantidad(cantidadFinal);
                        nuevoItem.setPrecioUnitario(BigDecimal.valueOf(producto.getPrecio_dpc()));
                        nuevoItem.calcularSubtotal();
                        carritoRepository.save(nuevoItem);
                    }
                }
            }
        }
    }

    @Override
    public List<CarritoDTO> obtenerCarritoPorUsuario(Long usuarioId) {
        List<Carrito> carritoEntities = carritoRepository.findByUsuario_Id(usuarioId);
        return CarritoMapper.toDtoList(carritoEntities);
    }

    @Override
    public void disminuirCantidad(Long productoId, HttpSession session) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");
        if (usuario != null) {
            Usuario usuarioEntity = usuarioRepository.findById(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            carritoRepository.findByUsuarioAndProducto_Id(usuarioEntity, productoId).ifPresent(item -> {
                if (item.getCantidad() > 1) {
                    item.setCantidad(item.getCantidad() - 1);
                    item.calcularSubtotal();
                    carritoRepository.save(item);
                } else {
                    carritoRepository.delete(item);
                }
            });
        } else {
            @SuppressWarnings("unchecked")
            List<CarritoDTO> carritoAnonimo = (List<CarritoDTO>) session.getAttribute("carritoAnonimo");
            if (carritoAnonimo != null) {
                carritoAnonimo.removeIf(item -> {
                    if (item.getProductoId().equals(productoId)) {
                        if (item.getCantidad() > 1) {
                            item.setCantidad(item.getCantidad() - 1);
                            return false;
                        } else {
                            return true;
                        }
                    }
                    return false;
                });
            }
        }
    }
    private int stockDisponible(Producto producto) {
        return producto.getInventarioDisponible();
    }
    @Override
    public void aumentarCantidad(Long productoId, HttpSession session) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        int stockDisponible = stockDisponible(producto);

        if (usuario != null) {
            Usuario usuarioEntity = usuarioRepository.findById(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            carritoRepository.findByUsuarioAndProducto_Id(usuarioEntity, productoId).ifPresent(item -> {
                if (item.getCantidad() + 1 > stockDisponible) {
                    throw new RuntimeException("Stock insuficiente para el producto: " + producto.getModelo_dpc());
                }
                item.setCantidad(item.getCantidad() + 1);
                item.calcularSubtotal();
                carritoRepository.save(item);
            });
        } else {
            @SuppressWarnings("unchecked")
            List<CarritoDTO> carritoAnonimo = (List<CarritoDTO>) session.getAttribute("carritoAnonimo");
            if (carritoAnonimo != null) {
                carritoAnonimo.forEach(item -> {
                    if (item.getProductoId().equals(productoId)) {
                        if (item.getCantidad() + 1 > stockDisponible) {
                            throw new RuntimeException("Stock insuficiente para el producto: " + producto.getModelo_dpc());
                        }
                        item.setCantidad(item.getCantidad() + 1);
                    }
                });
            }
        }
    }

    @Override
    public void eliminarProducto(Long usuarioId, Long productoId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        carritoRepository.deleteByUsuarioAndProducto_Id(usuario, productoId);
    }

    @Override
    public void vaciarCarrito(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        carritoRepository.deleteByUsuario(usuario);
    }

    @Override
    public void vaciarCarritoAnonimo(HttpSession session) {
        session.removeAttribute("carritoAnonimo");
    }
}
