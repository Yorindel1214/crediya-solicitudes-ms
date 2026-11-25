// Endpoint de la historia de usuario 2 (API_BASE viene de comun.js)
const API_URL = `${API_BASE}/api/v1/solicitud`;

// HU3: solo un CLIENTE con sesión puede pedir préstamos, y solo a su nombre:
// se precarga el documento que viene en su token (sesion.js)
requerirSesion(["CLIENTE"]);
const documentoSesion = datosDelToken().documento;
if (documentoSesion) {
  document.getElementById("documento_identidad").value = documentoSesion;
}

const formulario = document.getElementById("formulario");
const boton = document.getElementById("botonEnviar");
const mensaje = document.getElementById("mensaje");
const resultado = document.getElementById("resultado");
const resumen = document.getElementById("resumen");
const monto = document.getElementById("monto");
const montoAyuda = document.getElementById("montoAyuda");
const plazo = document.getElementById("plazo_meses");
const plazoAyuda = document.getElementById("plazoAyuda");

const ICONO_EXITO = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="m8 12 3 3 5-6"/></svg>';
const ICONO_ERROR = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/></svg>';

const pesos = (valor) => `$ ${Number(valor).toLocaleString("es-CO")}`;

// Ayudas mientras se escribe: monto con puntos de miles y plazo también en años
monto.addEventListener("input", () => {
  montoAyuda.textContent = monto.value === "" ? "Mayor que $0" : `${pesos(monto.value)} · Mayor que $0`;
});

plazo.addEventListener("input", () => {
  const meses = Number(plazo.value);
  if (plazo.value === "" || meses < 12) {
    plazoAyuda.textContent = "Entre 1 y 360 meses";
    return;
  }
  const anios = (meses / 12).toLocaleString("es-CO", { maximumFractionDigits: 1 });
  plazoAyuda.textContent = `${anios} ${anios === "1" ? "año" : "años"} · Entre 1 y 360 meses`;
});

// Arma el JSON con los mismos nombres que espera la API (snake_case)
function leerFormulario() {
  const tipo = document.querySelector('input[name="id_tipo_prestamo"]:checked');
  const documento = document.getElementById("documento_identidad").value.trim();
  return {
    documento_identidad: documento === "" ? null : documento,
    monto: monto.value === "" ? null : Number(monto.value),
    plazo_meses: plazo.value === "" ? null : Number(plazo.value),
    id_tipo_prestamo: tipo ? Number(tipo.value) : null,
  };
}

function mostrarMensaje(tipo, titulo, texto) {
  mensaje.innerHTML = "";
  const caja = document.createElement("div");
  caja.className = `mensaje mensaje--${tipo}`;
  caja.innerHTML = tipo === "exito" ? ICONO_EXITO : ICONO_ERROR;
  const contenido = document.createElement("div");
  const fuerte = document.createElement("strong");
  fuerte.textContent = titulo;
  contenido.append(fuerte, document.createTextNode(texto));
  caja.appendChild(contenido);
  mensaje.appendChild(caja);
}

function limpiarErrores() {
  document.querySelectorAll(".con-error").forEach((elemento) => elemento.classList.remove("con-error"));
}

// Marca en rojo el campo que menciona el mensaje de error de la API
function resaltarCampo(textoError) {
  limpiarErrores();
  const texto = textoError.toLowerCase();
  const reglas = [
    ["documento", "documento_identidad"],
    ["monto", "monto"],
    ["plazo", "plazo_meses"],
    ["tipo", "tipos"],
  ];
  for (const [palabra, id] of reglas) {
    if (texto.includes(palabra)) {
      const elemento = document.getElementById(id);
      elemento.classList.add("con-error");
      (elemento.querySelector("input") || elemento).focus();
      return;
    }
  }
}

function formatearFecha(fechaIso) {
  if (!fechaIso) return null;
  const fecha = new Date(fechaIso);
  return fecha.toLocaleString("es-CO", { dateStyle: "medium", timeStyle: "short" });
}

function mostrarSolicitud(s) {
  document.getElementById("numeroSolicitud").textContent = `#${s.id}`;
  document.getElementById("tituloResultado").textContent = `${s.tipo_prestamo} · ${pesos(s.monto)}`;
  document.getElementById("estadoResultado").textContent = s.estado;

  const filas = [
    ["Número de solicitud", s.id],
    ["Documento", s.documento_identidad],
    ["Monto", pesos(s.monto)],
    ["Plazo", `${s.plazo_meses} meses`],
    ["Tipo de préstamo", s.tipo_prestamo],
    ["Fecha de registro", formatearFecha(s.fecha_creacion)],
  ];
  resumen.innerHTML = "";
  for (const [etiqueta, valor] of filas) {
    const bloque = document.createElement("div");
    const dt = document.createElement("dt");
    dt.textContent = etiqueta;
    const dd = document.createElement("dd");
    dd.textContent = valor ?? "—";
    bloque.append(dt, dd);
    resumen.appendChild(bloque);
  }
  formulario.hidden = true;
  resultado.hidden = false;
}

formulario.addEventListener("submit", async (evento) => {
  evento.preventDefault();
  limpiarErrores();
  boton.disabled = true;
  boton.textContent = "Enviando…";

  try {
    const respuesta = await fetch(API_URL, {
      method: "POST",
      headers: cabecerasConToken(),
      body: JSON.stringify(leerFormulario()),
    });
    const datos = await respuesta.json().catch(() => ({}));

    if (respuesta.status === 401) {
      cerrarSesion(); // token vencido o inválido: volver a iniciar sesión
      return;
    }

    if (respuesta.status === 201) {
      mostrarMensaje("exito", "¡Solicitud enviada!", ` Quedó registrada con el número ${datos.id} y pasa a revisión.`);
      mostrarSolicitud(datos);
    } else {
      const textoError = datos.error || "No se pudo registrar la solicitud.";
      mostrarMensaje("error", `Error ${respuesta.status}`, ` ${textoError}`);
      resaltarCampo(textoError);
    }
  } catch (error) {
    // fetch falla así cuando la API está apagada o el navegador bloquea la petición (CORS)
    mostrarMensaje(
      "error",
      "No se pudo conectar con la API",
      " Verifica que el microservicio de Solicitudes esté corriendo en http://localhost:8081 (./gradlew bootRun)."
    );
  } finally {
    boton.disabled = false;
    boton.textContent = "Enviar solicitud";
  }
});

document.getElementById("otraSolicitud").addEventListener("click", () => {
  formulario.reset();
  if (documentoSesion) document.getElementById("documento_identidad").value = documentoSesion;
  montoAyuda.textContent = "Mayor que $0";
  plazoAyuda.textContent = "Entre 1 y 360 meses";
  mensaje.innerHTML = "";
  resultado.hidden = true;
  formulario.hidden = false;
  document.getElementById("documento_identidad").focus();
});
