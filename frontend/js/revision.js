// HU4: listado paginado y filtrable de solicitudes para el asesor (API_BASE viene de comun.js)
const API_URL = `${API_BASE}/api/v1/solicitud`;

requerirSesion(["ASESOR"]);

const filas = document.getElementById("filas");
const mensaje = document.getElementById("mensaje");
const infoPagina = document.getElementById("infoPagina");
const anterior = document.getElementById("anterior");
const siguiente = document.getElementById("siguiente");
let paginaActual = 0;

const pesos = (valor) => (valor == null ? "—" : `$ ${Number(valor).toLocaleString("es-CO", { maximumFractionDigits: 2 })}`);

function mostrarError(texto) {
  mensaje.innerHTML = "";
  const caja = document.createElement("div");
  caja.className = "mensaje mensaje--error";
  const contenido = document.createElement("div");
  const fuerte = document.createElement("strong");
  fuerte.textContent = "No se pudo cargar el listado. ";
  contenido.append(fuerte, document.createTextNode(texto));
  caja.appendChild(contenido);
  mensaje.appendChild(caja);
}

function celda(texto, clase) {
  const td = document.createElement("td");
  td.textContent = texto ?? "—";
  if (clase) td.className = clase;
  return td;
}

function pintar(resultado) {
  filas.innerHTML = "";
  if (resultado.contenido.length === 0) {
    const tr = document.createElement("tr");
    const td = celda("No hay solicitudes con esos filtros.", "vacio");
    td.colSpan = 9;
    tr.appendChild(td);
    filas.appendChild(tr);
  }
  for (const s of resultado.contenido) {
    const tr = document.createElement("tr");
    const cliente = document.createElement("td");
    const nombre = document.createElement("strong");
    nombre.textContent = s.nombre ?? "Sin datos del cliente";
    const correo = document.createElement("small");
    correo.textContent = s.email ?? "solicitud anterior a la HU3";
    cliente.append(nombre, document.createElement("br"), correo);
    tr.append(
      celda(s.id),
      cliente,
      celda(s.tipo_prestamo),
      celda(pesos(s.monto), "num"),
      celda(`${s.plazo} meses`, "num"),
      celda(s.tasa_interes == null ? "—" : `${s.tasa_interes} %`, "num"),
      celda(s.estado_solicitud),
      celda(pesos(s.salario_base), "num"),
      celda(pesos(s.deuda_total_mensual_solicitudes_aprobadas), "num"),
    );
    filas.appendChild(tr);
  }
  const total = resultado.total_paginas || 1;
  infoPagina.textContent = `Página ${resultado.pagina + 1} de ${total} · ${resultado.total_elementos} solicitudes`;
  anterior.disabled = resultado.pagina === 0;
  siguiente.disabled = resultado.pagina + 1 >= total;
}

async function cargar(pagina) {
  mensaje.innerHTML = "";
  const parametros = new URLSearchParams({ pagina, tamano: document.getElementById("tamano").value });
  const estado = document.getElementById("estado").value;
  const email = document.getElementById("email").value.trim();
  const tipo = document.getElementById("tipo").value;
  if (estado) parametros.set("estado", estado);
  if (email) parametros.set("email", email);
  if (tipo) parametros.set("id_tipo_prestamo", tipo);

  try {
    const respuesta = await fetch(`${API_URL}?${parametros}`, { headers: cabecerasConToken() });
    const datos = await respuesta.json().catch(() => ({}));
    if (respuesta.status === 401) {
      cerrarSesion();
      return;
    }
    if (!respuesta.ok) {
      mostrarError(datos.error || `Error ${respuesta.status}`);
      return;
    }
    paginaActual = datos.pagina;
    pintar(datos);
  } catch (error) {
    mostrarError("Verifica que el microservicio de Solicitudes esté corriendo en http://localhost:8081.");
  }
}

document.getElementById("filtros").addEventListener("submit", (evento) => {
  evento.preventDefault();
  cargar(0);
});
anterior.addEventListener("click", () => cargar(paginaActual - 1));
siguiente.addEventListener("click", () => cargar(paginaActual + 1));

cargar(0);
