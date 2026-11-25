// HU3: inicio de sesión contra el microservicio de Autenticación (API_AUTENTICACION viene de sesion.js)
const formulario = document.getElementById("formularioLogin");
const boton = document.getElementById("botonLogin");
const mensaje = document.getElementById("mensaje");

// Página a la que va cada rol después de iniciar sesión
const INICIO_POR_ROL = { CLIENTE: "solicitar.html", ASESOR: "revision.html" };

function mostrarError(texto) {
  mensaje.innerHTML = "";
  const caja = document.createElement("div");
  caja.className = "mensaje mensaje--error";
  const contenido = document.createElement("div");
  const fuerte = document.createElement("strong");
  fuerte.textContent = "No se pudo iniciar sesión. ";
  contenido.append(fuerte, document.createTextNode(texto));
  caja.appendChild(contenido);
  mensaje.appendChild(caja);
}

formulario.addEventListener("submit", async (evento) => {
  evento.preventDefault();
  boton.disabled = true;
  boton.textContent = "Ingresando…";
  try {
    const respuesta = await fetch(`${API_AUTENTICACION}/api/v1/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        correo_electronico: document.getElementById("correo").value.trim(),
        clave: document.getElementById("clave").value,
      }),
    });
    const datos = await respuesta.json().catch(() => ({}));
    if (respuesta.ok) {
      guardarSesion(datos);
      const volver = new URLSearchParams(location.search).get("volver");
      window.location.href = volver && /^[\w-]+\.html$/.test(volver)
        ? volver
        : (INICIO_POR_ROL[datos.rol] || "index.html");
    } else {
      mostrarError(datos.error || `Error ${respuesta.status}`);
    }
  } catch (error) {
    mostrarError("Verifica que el microservicio de Autenticación esté corriendo en http://localhost:8080.");
  } finally {
    boton.disabled = false;
    boton.textContent = "Iniciar sesión";
  }
});
